package pe.cmacica.clientes.data

object AuthCredentials {
    private const val EMAIL_DOMAIN = "@clientes.cmacica.pe"

    fun documentoToEmail(documento: String): String =
        "${documento.trim()}${EMAIL_DOMAIN}"

    fun emailToDocumento(email: String?): String =
        email?.substringBefore(EMAIL_DOMAIN)?.trim().orEmpty()
}
