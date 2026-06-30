package pe.cmacica.clientes.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.cmacica.clientes.data.model.CuentaAhorro
import pe.cmacica.clientes.data.model.Movimiento
import pe.cmacica.clientes.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CuentaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun clienteRef(uid: String) = firestore.collection(COL_CLIENTES).document(uid)

    suspend fun crearPerfilInicial(
        uid: String,
        documento: String,
        nombre: String,
        apellidos: String,
        fechaNacimiento: String,
        email: String
    ) {
        val perfil = mapOf(
            "documento" to documento,
            "nombre" to nombre,
            "apellidos" to apellidos,
            "fechaNacimiento" to fechaNacimiento,
            "nombreCompleto" to "$nombre $apellidos".trim(),
            "email" to email,
            "saldo" to 12845.32
        )
        clienteRef(uid).set(perfil).await()

        val movimientosDemo = listOf(
            movimientoMap("Depósito ventanilla — Ahorros", "2026-05-12", 350.0, true),
            movimientoMap("Pago tarjeta — establecimiento", "2026-05-11", -42.90, false),
            movimientoMap("Transferencia recibida", "2026-05-10", 120.0, true),
            movimientoMap("Cuota crédito consumo", "2026-05-09", -215.40, false),
            movimientoMap("Yape — envío", "2026-05-08", -30.0, false),
            movimientoMap("Intereses ganados", "2026-05-07", 8.52, true)
        )
        val batch = firestore.batch()
        movimientosDemo.forEach { data ->
            val doc = clienteRef(uid).collection(COL_MOVIMIENTOS).document()
            batch.set(doc, data)
        }
        batch.commit().await()
        seedCuentasAhorro(uid)
    }

    private suspend fun seedCuentasAhorro(uid: String) {
        val cuentas = listOf(
            mapOf(
                "nombre" to "Ahorro Simple",
                "numero" to "001-0458921",
                "saldo" to 8540.00,
                "tea" to 2.5
            ),
            mapOf(
                "nombre" to "Ahorro Programado",
                "numero" to "001-0458922",
                "saldo" to 3205.32,
                "tea" to 3.8
            ),
            mapOf(
                "nombre" to "CTS",
                "numero" to "001-0458930",
                "saldo" to 1100.00,
                "tea" to 1.5
            )
        )
        val batch = firestore.batch()
        cuentas.forEach { data ->
            val doc = clienteRef(uid).collection(COL_CUENTAS_AHORRO).document()
            batch.set(doc, data)
        }
        batch.commit().await()
    }

    fun observePerfil(uid: String): Flow<UserProfile?> = callbackFlow {
        val listener = clienteRef(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot == null || !snapshot.exists()) {
                trySend(null)
                return@addSnapshotListener
            }
            trySend(
                UserProfile(
                    uid = uid,
                    documento = snapshot.getString("documento").orEmpty(),
                    nombre = snapshot.getString("nombre").orEmpty(),
                    apellidos = snapshot.getString("apellidos").orEmpty(),
                    fechaNacimiento = snapshot.getString("fechaNacimiento").orEmpty(),
                    email = snapshot.getString("email").orEmpty(),
                    saldo = snapshot.getDouble("saldo") ?: 0.0
                )
            )
        }
        awaitClose { listener.remove() }
    }

    fun observeMovimientos(uid: String): Flow<List<Movimiento>> = callbackFlow {
        val listener = clienteRef(uid)
            .collection(COL_MOVIMIENTOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.map { doc ->
                    Movimiento(
                        id = doc.id,
                        titulo = doc.getString("titulo").orEmpty(),
                        fecha = doc.getString("fecha").orEmpty(),
                        monto = doc.getDouble("monto") ?: 0.0,
                        esIngreso = doc.getBoolean("esIngreso") ?: false,
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }.orEmpty()
                    .sortedByDescending { it.timestamp.takeIf { ts -> ts > 0 } ?: it.fecha.hashCode().toLong() }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun observeCuentasAhorro(uid: String): Flow<List<CuentaAhorro>> = callbackFlow {
        val listener = clienteRef(uid)
            .collection(COL_CUENTAS_AHORRO)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.map { doc ->
                    CuentaAhorro(
                        id = doc.id,
                        nombre = doc.getString("nombre").orEmpty(),
                        numero = doc.getString("numero").orEmpty(),
                        saldo = doc.getDouble("saldo") ?: 0.0,
                        tea = doc.getDouble("tea") ?: 0.0
                    )
                }.orEmpty()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun ensureCuentasAhorro(uid: String) {
        val snapshot = clienteRef(uid).collection(COL_CUENTAS_AHORRO).limit(1).get().await()
        if (snapshot.isEmpty) seedCuentasAhorro(uid)
    }

    suspend fun registrarOperacion(
        uid: String,
        titulo: String,
        monto: Double,
        esIngreso: Boolean
    ): Result<Double> = runCatching {
        val perfilRef = clienteRef(uid)
        val nuevoSaldo = firestore.runTransaction { transaction ->
            val perfil = transaction.get(perfilRef)
            val saldoActual = perfil.getDouble("saldo") ?: 0.0
            if (!esIngreso && saldoActual + monto < 0) {
                error("Saldo insuficiente para realizar la operación.")
            }
            val saldoFinal = saldoActual + monto
            transaction.update(perfilRef, "saldo", saldoFinal)
            val movRef = perfilRef.collection(COL_MOVIMIENTOS).document()
            transaction.set(
                movRef,
                movimientoMap(
                    titulo = titulo,
                    fecha = fechaHoy(),
                    monto = monto,
                    esIngreso = esIngreso,
                    timestamp = System.currentTimeMillis()
                )
            )
            saldoFinal
        }.await()
        nuevoSaldo
    }

    suspend fun getPerfil(uid: String): UserProfile? {
        val snapshot = clienteRef(uid).get().await()
        if (!snapshot.exists()) return null
        return UserProfile(
            uid = uid,
            documento = snapshot.getString("documento").orEmpty(),
            nombre = snapshot.getString("nombre").orEmpty(),
            apellidos = snapshot.getString("apellidos").orEmpty(),
            fechaNacimiento = snapshot.getString("fechaNacimiento").orEmpty(),
            email = snapshot.getString("email").orEmpty(),
            saldo = snapshot.getDouble("saldo") ?: 0.0
        )
    }

    private fun movimientoMap(
        titulo: String,
        fecha: String,
        monto: Double,
        esIngreso: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ) = mapOf(
        "titulo" to titulo,
        "fecha" to fecha,
        "monto" to monto,
        "esIngreso" to esIngreso,
        "timestamp" to timestamp
    )

    private fun fechaHoy(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    companion object {
        private const val COL_CLIENTES = "clientes"
        private const val COL_MOVIMIENTOS = "movimientos"
        private const val COL_CUENTAS_AHORRO = "cuentas_ahorro"
    }
}
