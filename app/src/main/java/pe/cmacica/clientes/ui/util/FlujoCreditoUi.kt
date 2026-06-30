package pe.cmacica.clientes.ui.util

import pe.cmacica.clientes.data.model.EstadoSolicitud

data class PasoFlujo(
    val titulo: String,
    val descripcion: String,
    val estados: Set<EstadoSolicitud>,
    val orden: Int
)

object FlujoCreditoUi {
    val pasos = listOf(
        PasoFlujo(
            titulo = "Solicitud enviada",
            descripcion = "Registraste la solicitud desde App Clientes (canal cliente, estado enviado).",
            estados = setOf(EstadoSolicitud.ENVIADO),
            orden = 1
        ),
        PasoFlujo(
            titulo = "Recepción en core",
            descripcion = "La solicitud llegó al core y quedó visible para la agencia.",
            estados = setOf(EstadoSolicitud.RECIBIDO_CORE),
            orden = 2
        ),
        PasoFlujo(
            titulo = "Cartera del asesor",
            descripcion = "Asignada al asesor con gestión NUEVA_SOLICITUD.",
            estados = setOf(EstadoSolicitud.EN_CARTERA_ASESOR),
            orden = 3
        ),
        PasoFlujo(
            titulo = "Visita en campo",
            descripcion = "El asesor registró la visita con observación y coordenadas.",
            estados = setOf(EstadoSolicitud.VISITADO),
            orden = 4
        ),
        PasoFlujo(
            titulo = "Pre-evaluación y buró",
            descripcion = "Capacidad de pago, buró y listas verificados.",
            estados = setOf(EstadoSolicitud.PRE_EVALUACION_OK),
            orden = 5
        ),
        PasoFlujo(
            titulo = "Documentos y firma",
            descripcion = "Documentos adjuntos y firma del cliente capturados.",
            estados = setOf(EstadoSolicitud.DOCUMENTOS_FIRMADOS),
            orden = 6
        ),
        PasoFlujo(
            titulo = "Comité de crédito",
            descripcion = "Expediente en comité: recibido → en evaluación → decisión.",
            estados = setOf(
                EstadoSolicitud.PROMOVIDO_NUCLEO,
                EstadoSolicitud.RECIBIDO_COMITE,
                EstadoSolicitud.EN_EVALUACION
            ),
            orden = 7
        ),
        PasoFlujo(
            titulo = "Decisión y desembolso",
            descripcion = "Aprobado, condicionado, rechazado o desembolsado.",
            estados = setOf(
                EstadoSolicitud.APROBADO,
                EstadoSolicitud.CONDICIONADO,
                EstadoSolicitud.RECHAZADO,
                EstadoSolicitud.DESEMBOLSADO,
                EstadoSolicitud.CERRADO
            ),
            orden = 8
        )
    )

    fun estadoActual(estadoValue: String): EstadoSolicitud =
        EstadoSolicitud.fromValue(estadoValue)

    fun pasoCompletado(paso: PasoFlujo, estadoActual: EstadoSolicitud): Boolean =
        estadoActual.orden >= paso.estados.minOf { it.orden }

    fun pasoEnCurso(paso: PasoFlujo, estadoActual: EstadoSolicitud): Boolean =
        paso.estados.contains(estadoActual)

    fun etiquetaEstado(estado: String): String = when (EstadoSolicitud.fromValue(estado)) {
        EstadoSolicitud.ENVIADO -> "Enviado"
        EstadoSolicitud.RECIBIDO_CORE -> "Recibido en core"
        EstadoSolicitud.EN_CARTERA_ASESOR -> "En cartera del asesor"
        EstadoSolicitud.VISITADO -> "Visitado"
        EstadoSolicitud.PRE_EVALUACION_OK -> "Pre-evaluación OK"
        EstadoSolicitud.DOCUMENTOS_FIRMADOS -> "Documentos y firma"
        EstadoSolicitud.PROMOVIDO_NUCLEO -> "Promovido al núcleo"
        EstadoSolicitud.RECIBIDO_COMITE -> "Recibido en comité"
        EstadoSolicitud.EN_EVALUACION -> "En evaluación"
        EstadoSolicitud.APROBADO -> "Aprobado"
        EstadoSolicitud.CONDICIONADO -> "Condicionado"
        EstadoSolicitud.RECHAZADO -> "Rechazado"
        EstadoSolicitud.DESEMBOLSADO -> "Desembolsado"
        EstadoSolicitud.CERRADO -> "Cerrado"
    }
}
