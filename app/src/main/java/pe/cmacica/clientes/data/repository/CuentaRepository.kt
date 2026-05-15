package pe.cmacica.clientes.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.cmacica.clientes.data.model.Movimiento
import pe.cmacica.clientes.data.model.UserProfile

class CuentaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun clienteRef(uid: String) = firestore.collection(COL_CLIENTES).document(uid)

    suspend fun crearPerfilInicial(uid: String, nombre: String, email: String) {
        val perfil = mapOf(
            "nombre" to nombre,
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
                    nombre = snapshot.getString("nombre").orEmpty(),
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
            .orderBy("fecha", Query.Direction.DESCENDING)
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
                        esIngreso = doc.getBoolean("esIngreso") ?: false
                    )
                }.orEmpty()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    private fun movimientoMap(
        titulo: String,
        fecha: String,
        monto: Double,
        esIngreso: Boolean
    ) = mapOf(
        "titulo" to titulo,
        "fecha" to fecha,
        "monto" to monto,
        "esIngreso" to esIngreso
    )

    companion object {
        private const val COL_CLIENTES = "clientes"
        private const val COL_MOVIMIENTOS = "movimientos"
    }
}
