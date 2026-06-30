package pe.cmacica.clientes.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.cmacica.clientes.data.model.CanalSolicitud
import pe.cmacica.clientes.data.model.CuotaCronograma
import pe.cmacica.clientes.data.model.DecisionComite
import pe.cmacica.clientes.data.model.Desembolso
import pe.cmacica.clientes.data.model.DocumentoAdjunto
import pe.cmacica.clientes.data.model.EstadoSolicitud
import pe.cmacica.clientes.data.model.PreEvaluacion
import pe.cmacica.clientes.data.model.SolicitudCredito
import pe.cmacica.clientes.data.model.TipoGestion
import pe.cmacica.clientes.data.model.VisitaCampo
import java.util.Calendar

class SolicitudCreditoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun registrarSolicitudCliente(
        clienteUid: String,
        documento: String,
        nombre: String,
        monto: Double,
        plazoMeses: Int,
        destino: String,
        garantia: String
    ): Result<SolicitudCredito> = runCatching {
        val expediente = generarExpediente()
        val now = System.currentTimeMillis()
        val solicitudRef = firestore.collection(COL_SOLICITUDES).document()

        val solicitudData = mapOf(
            "expediente" to expediente,
            "clienteUid" to clienteUid,
            "documentoCliente" to documento,
            "nombreCliente" to nombre,
            "monto" to monto,
            "plazoMeses" to plazoMeses,
            "destino" to destino,
            "garantia" to garantia,
            "canal" to CanalSolicitud.CLIENTE.value,
            "estado" to EstadoSolicitud.ENVIADO.value,
            "tipoGestion" to TipoGestion.NUEVA_SOLICITUD.value,
            "createdAt" to now,
            "updatedAt" to now
        )

        firestore.runTransaction { transaction ->
            transaction.set(solicitudRef, solicitudData)
        }.await()

        val solicitud = mapToSolicitud(solicitudRef.id, solicitudData)
        procesarRecepcionCore(solicitudRef.id, solicitud)
        solicitud.copy(
            id = solicitudRef.id,
            estado = EstadoSolicitud.RECIBIDO_CORE.value,
            agenciaId = AGENCIA_DEFAULT,
            asesorCodigo = ASESOR_DEFAULT
        )
    }

    /** Paso 2 — Core: encola y asigna a agencia/asesor (simulado en Firestore). */
    private suspend fun procesarRecepcionCore(solicitudId: String, solicitud: SolicitudCredito) {
        val now = System.currentTimeMillis()
        val batch = firestore.batch()
        val solicitudRef = firestore.collection(COL_SOLICITUDES).document(solicitudId)

        batch.update(
            solicitudRef,
            mapOf(
                "estado" to EstadoSolicitud.RECIBIDO_CORE.value,
                "agenciaId" to AGENCIA_DEFAULT,
                "asesorCodigo" to ASESOR_DEFAULT,
                "updatedAt" to now
            )
        )

        val colaRef = firestore.collection(COL_CORE_COLA).document(solicitudId)
        batch.set(
            colaRef,
            mapOf(
                "solicitudId" to solicitudId,
                "expediente" to solicitud.expediente,
                "estado" to "pendiente_promocion",
                "agenciaId" to AGENCIA_DEFAULT,
                "asesorCodigo" to ASESOR_DEFAULT,
                "createdAt" to now
            )
        )

        val carteraRef = firestore.collection(COL_CARTERA_DIA)
            .document(ASESOR_DEFAULT)
            .collection(COL_ITEMS)
            .document(solicitudId)

        batch.set(
            carteraRef,
            mapOf(
                "solicitudId" to solicitudId,
                "expediente" to solicitud.expediente,
                "documentoCliente" to solicitud.documentoCliente,
                "nombreCliente" to solicitud.nombreCliente,
                "tipoGestion" to TipoGestion.NUEVA_SOLICITUD.value,
                "estado" to EstadoSolicitud.RECIBIDO_CORE.value,
                "monto" to solicitud.monto,
                "createdAt" to now
            )
        )

        batch.commit().await()
    }

    fun observeSolicitudesCliente(clienteUid: String): Flow<List<SolicitudCredito>> = callbackFlow {
        val listener = firestore.collection(COL_SOLICITUDES)
            .whereEqualTo("clienteUid", clienteUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.map { doc ->
                    mapToSolicitud(doc.id, doc.data.orEmpty())
                }.orEmpty()
                    .sortedByDescending { it.createdAt }
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun observeSolicitud(solicitudId: String): Flow<SolicitudCredito?> = callbackFlow {
        val listener = firestore.collection(COL_SOLICITUDES).document(solicitudId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(mapToSolicitud(snapshot.id, snapshot.data.orEmpty()))
            }
        awaitClose { listener.remove() }
    }

    private suspend fun generarExpediente(): String {
        val anio = Calendar.getInstance().get(Calendar.YEAR)
        val counterRef = firestore.collection(COL_CONTADORES).document("expedientes")

        val numero = firestore.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val ultimoAnio = snapshot.getLong("anio")?.toInt() ?: anio
            val ultimo = if (ultimoAnio == anio) {
                (snapshot.getLong("ultimo") ?: 0L) + 1
            } else {
                1L
            }
            transaction.set(
                counterRef,
                mapOf("anio" to anio, "ultimo" to ultimo)
            )
            ultimo
        }.await()

        return "EXP-$anio-${numero.toString().padStart(5, '0')}"
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapToSolicitud(id: String, data: Map<String, Any?>): SolicitudCredito {
        val visitaMap = data["visita"] as? Map<String, Any?>
        val preMap = data["preEvaluacion"] as? Map<String, Any?>
        val decisionMap = data["decision"] as? Map<String, Any?>
        val desembolsoMap = data["desembolso"] as? Map<String, Any?>
        val docsList = data["documentos"] as? List<Map<String, Any?>>

        return SolicitudCredito(
            id = id,
            expediente = data["expediente"] as? String ?: "",
            clienteUid = data["clienteUid"] as? String ?: "",
            documentoCliente = data["documentoCliente"] as? String ?: "",
            nombreCliente = data["nombreCliente"] as? String ?: "",
            monto = (data["monto"] as? Number)?.toDouble() ?: 0.0,
            plazoMeses = (data["plazoMeses"] as? Number)?.toInt() ?: 0,
            destino = data["destino"] as? String ?: "",
            garantia = data["garantia"] as? String ?: "",
            canal = data["canal"] as? String ?: CanalSolicitud.CLIENTE.value,
            estado = data["estado"] as? String ?: EstadoSolicitud.ENVIADO.value,
            agenciaId = data["agenciaId"] as? String ?: "",
            asesorCodigo = data["asesorCodigo"] as? String ?: "",
            tipoGestion = data["tipoGestion"] as? String ?: TipoGestion.NUEVA_SOLICITUD.value,
            visita = VisitaCampo(
                visitado = visitaMap?.get("visitado") as? Boolean ?: false,
                observacion = visitaMap?.get("observacion") as? String ?: "",
                latitud = (visitaMap?.get("latitud") as? Number)?.toDouble() ?: 0.0,
                longitud = (visitaMap?.get("longitud") as? Number)?.toDouble() ?: 0.0,
                fecha = visitaMap?.get("fecha") as? String ?: ""
            ),
            preEvaluacion = PreEvaluacion(
                capacidadPagoOk = preMap?.get("capacidadPagoOk") as? Boolean ?: false,
                buroOk = preMap?.get("buroOk") as? Boolean ?: false,
                listasOk = preMap?.get("listasOk") as? Boolean ?: false,
                resultadoEsperado = preMap?.get("resultadoEsperado") as? String ?: "",
                resultadoObtenido = preMap?.get("resultadoObtenido") as? String ?: "",
                fecha = preMap?.get("fecha") as? String ?: ""
            ),
            documentos = docsList?.map { doc ->
                DocumentoAdjunto(
                    tipo = doc["tipo"] as? String ?: "",
                    url = doc["url"] as? String ?: "",
                    nombre = doc["nombre"] as? String ?: ""
                )
            }.orEmpty(),
            firmaCapturada = data["firmaCapturada"] as? Boolean ?: false,
            decision = DecisionComite(
                tipo = decisionMap?.get("tipo") as? String ?: "",
                motivo = decisionMap?.get("motivo") as? String ?: "",
                fecha = decisionMap?.get("fecha") as? String ?: ""
            ),
            desembolso = desembolsoMap?.let { d ->
                val cronograma = d["cronograma"] as? List<Map<String, Any?>>
                Desembolso(
                    monto = (d["monto"] as? Number)?.toDouble() ?: 0.0,
                    fecha = d["fecha"] as? String ?: "",
                    cuentaDestino = d["cuentaDestino"] as? String ?: "",
                    cronograma = cronograma?.map { c ->
                        CuotaCronograma(
                            numero = (c["numero"] as? Number)?.toInt() ?: 0,
                            fechaVencimiento = c["fechaVencimiento"] as? String ?: "",
                            capital = (c["capital"] as? Number)?.toDouble() ?: 0.0,
                            interes = (c["interes"] as? Number)?.toDouble() ?: 0.0,
                            cuota = (c["cuota"] as? Number)?.toDouble() ?: 0.0,
                            saldo = (c["saldo"] as? Number)?.toDouble() ?: 0.0
                        )
                    }.orEmpty()
                )
            },
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
        )
    }

    companion object {
        private const val COL_SOLICITUDES = "solicitudes_credito"
        private const val COL_CORE_COLA = "core_cola"
        private const val COL_CARTERA_DIA = "cartera_dia"
        private const val COL_ITEMS = "items"
        private const val COL_CONTADORES = "contadores"
        const val AGENCIA_DEFAULT = "AG-Ica-01"
        const val ASESOR_DEFAULT = "EMP-45821"
    }
}
