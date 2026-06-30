package pe.cmacica.clientes.security

enum class UserRole(val value: String) {
    CLIENTE("cliente"),
    ASESOR("asesor"),
    SUPERVISOR("supervisor"),
    ADMINISTRADOR("administrador"),
    COMITE("comite");

    companion object {
        fun from(value: String?): UserRole =
            entries.find { it.value == value } ?: CLIENTE

        fun inferFromEmail(email: String?): UserRole {
            if (email.isNullOrBlank()) return CLIENTE
            val e = email.lowercase()
            val local = e.substringBefore("@")
            return when {
                e.endsWith("@clientes.cmacica.pe") -> CLIENTE
                e.endsWith("@comite.cmacica.pe") -> COMITE
                e.endsWith("@ventas.cmacica.pe") && local.startsWith("adm-") -> ADMINISTRADOR
                e.endsWith("@ventas.cmacica.pe") && local.startsWith("sup-") -> SUPERVISOR
                e.endsWith("@ventas.cmacica.pe") -> ASESOR
                else -> CLIENTE
            }
        }
    }
}
