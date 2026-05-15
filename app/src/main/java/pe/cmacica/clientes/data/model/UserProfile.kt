package pe.cmacica.clientes.data.model

data class UserProfile(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val saldo: Double = 0.0
)
