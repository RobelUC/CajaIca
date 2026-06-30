package pe.cmacica.clientes.ui.util

import java.text.NumberFormat
import java.util.Locale

fun formatSoles(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("es", "PE"))
    nf.minimumFractionDigits = 2
    nf.maximumFractionDigits = 2
    return "S/ ${nf.format(value)}"
}

fun formatSolesSigned(value: Double): String {
    val abs = kotlin.math.abs(value)
    val sign = when {
        value > 0 -> "+"
        value < 0 -> "-"
        else -> ""
    }
    val nf = NumberFormat.getNumberInstance(Locale("es", "PE"))
    nf.minimumFractionDigits = 2
    nf.maximumFractionDigits = 2
    return "$sign S/ ${nf.format(abs)}"
}
