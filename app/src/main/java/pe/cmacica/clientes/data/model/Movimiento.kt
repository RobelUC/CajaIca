package pe.cmacica.clientes.data.model

data class Movimiento(
    val id: String = "",
    val titulo: String = "",
    val fecha: String = "",
    val monto: Double = 0.0,
    val esIngreso: Boolean = false,
    val timestamp: Long = 0L
)
