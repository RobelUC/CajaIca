package pe.cmacica.clientes.data.model

data class CuentaAhorro(
    val id: String = "",
    val nombre: String = "",
    val numero: String = "",
    val saldo: Double = 0.0,
    val tea: Double = 0.0
)

data class ServicioPago(
    val id: String,
    val nombre: String,
    val codigoEjemplo: String
)
