package pe.cmacica.clientes.data.model

data class UserProfile(
    val uid: String = "",
    val documento: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val fechaNacimiento: String = "",
    val email: String = "",
    val saldo: Double = 0.0
) {
    val nombreCompleto: String
        get() = listOf(nombre, apellidos).filter { it.isNotBlank() }.joinToString(" ")
}
