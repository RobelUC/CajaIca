package pe.cmacica.clientes.data.model

/** Canal por el que ingresa la solicitud (paso 1). */
enum class CanalSolicitud(val value: String) {
    CLIENTE("cliente"),
    AGENCIA("agencia"),
    FUERZA_VENTAS("fuerza_ventas")
}

/** Estados del expediente a lo largo del flujo móvil (pasos 1–8). */
enum class EstadoSolicitud(val value: String, val orden: Int) {
    ENVIADO("enviado", 1),
    RECIBIDO_CORE("recibido_core", 2),
    EN_CARTERA_ASESOR("en_cartera_asesor", 3),
    VISITADO("visitado", 4),
    PRE_EVALUACION_OK("pre_evaluacion_ok", 5),
    DOCUMENTOS_FIRMADOS("documentos_firmados", 6),
    PROMOVIDO_NUCLEO("promovido_nucleo", 7),
    RECIBIDO_COMITE("recibido_comite", 8),
    EN_EVALUACION("en_evaluacion", 9),
    APROBADO("aprobado", 10),
    CONDICIONADO("condicionado", 11),
    RECHAZADO("rechazado", 12),
    DESEMBOLSADO("desembolsado", 13),
    CERRADO("cerrado", 14);

    companion object {
        fun fromValue(value: String?): EstadoSolicitud =
            entries.find { it.value == value } ?: ENVIADO
    }
}

/** Tipo de gestión en cartera del asesor (paso 3). */
enum class TipoGestion(val value: String) {
    NUEVA_SOLICITUD("NUEVA_SOLICITUD"),
    SEGUIMIENTO("SEGUIMIENTO"),
    RENOVACION("RENOVACION")
}

data class VisitaCampo(
    val visitado: Boolean = false,
    val observacion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fecha: String = ""
)

data class PreEvaluacion(
    val capacidadPagoOk: Boolean = false,
    val buroOk: Boolean = false,
    val listasOk: Boolean = false,
    val resultadoEsperado: String = "",
    val resultadoObtenido: String = "",
    val fecha: String = ""
)

data class DocumentoAdjunto(
    val tipo: String = "",
    val url: String = "",
    val nombre: String = ""
)

data class DecisionComite(
    val tipo: String = "",
    val motivo: String = "",
    val fecha: String = ""
)

data class CuotaCronograma(
    val numero: Int = 0,
    val fechaVencimiento: String = "",
    val capital: Double = 0.0,
    val interes: Double = 0.0,
    val cuota: Double = 0.0,
    val saldo: Double = 0.0
)

data class Desembolso(
    val monto: Double = 0.0,
    val fecha: String = "",
    val cuentaDestino: String = "",
    val cronograma: List<CuotaCronograma> = emptyList()
)

data class SolicitudCredito(
    val id: String = "",
    val expediente: String = "",
    val clienteUid: String = "",
    val documentoCliente: String = "",
    val nombreCliente: String = "",
    val monto: Double = 0.0,
    val plazoMeses: Int = 0,
    val destino: String = "",
    val garantia: String = "",
    val canal: String = CanalSolicitud.CLIENTE.value,
    val estado: String = EstadoSolicitud.ENVIADO.value,
    val agenciaId: String = "",
    val asesorCodigo: String = "",
    val tipoGestion: String = TipoGestion.NUEVA_SOLICITUD.value,
    val visita: VisitaCampo = VisitaCampo(),
    val preEvaluacion: PreEvaluacion = PreEvaluacion(),
    val documentos: List<DocumentoAdjunto> = emptyList(),
    val firmaCapturada: Boolean = false,
    val decision: DecisionComite = DecisionComite(),
    val desembolso: Desembolso? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
