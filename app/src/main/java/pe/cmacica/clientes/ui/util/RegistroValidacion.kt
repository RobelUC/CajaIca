package pe.cmacica.clientes.ui.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

object RegistroValidacion {

    private val nombreRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'-]{2,}$")
    private val fechaStorage = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val fechaDisplay = SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE"))

    fun validarDocumento(documento: String): String? = when {
        documento.isBlank() -> "Ingresa tu documento de identidad."
        documento.length !in 8..12 -> "El documento debe tener entre 8 y 12 dígitos."
        !documento.all { it.isDigit() } -> "El documento solo debe contener números."
        else -> null
    }

    fun validarNombre(nombre: String, campo: String = "nombre"): String? = when {
        nombre.isBlank() -> "Ingresa tu $campo."
        nombre.length < 2 -> "El $campo debe tener al menos 2 caracteres."
        !nombreRegex.matches(nombre.trim()) -> "El $campo solo puede contener letras."
        else -> null
    }

    fun validarFechaNacimiento(fechaIso: String): String? {
        if (fechaIso.isBlank()) return "Selecciona tu fecha de nacimiento."
        val fecha = runCatching { fechaStorage.parse(fechaIso) }.getOrNull()
            ?: return "La fecha de nacimiento no es válida."
        if (fecha.after(Date())) return "La fecha de nacimiento no puede ser futura."

        val edad = calcularEdad(fecha)
        return when {
            edad < 18 -> "Debes ser mayor de 18 años para registrarte."
            edad > 100 -> "Verifica tu fecha de nacimiento."
            else -> null
        }
    }

    fun validarClaveNumerica(clave: String, confirmar: String): String? = when {
        clave.isBlank() -> "Ingresa tu clave numérica."
        !clave.all { it.isDigit() } -> "La clave solo debe contener números."
        clave.length < 6 -> "La clave debe tener al menos 6 números."
        clave != confirmar -> "Las claves no coinciden."
        else -> null
    }

    fun formatearFechaDisplay(fechaIso: String): String =
        runCatching {
            val date = fechaStorage.parse(fechaIso) ?: return fechaIso
            fechaDisplay.format(date)
        }.getOrElse { fechaIso }

    fun fechaDesdeMillis(millis: Long): String {
        val local = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        return local.toString()
    }

    /** Máscara dd/MM/yyyy mientras el usuario escribe. */
    fun filtrarEntradaFecha(input: String): String {
        val digits = input.filter { it.isDigit() }.take(8)
        return buildString {
            digits.forEachIndexed { index, char ->
                if (index == 2 || index == 4) append('/')
                append(char)
            }
        }
    }

    /** Convierte dd/MM/yyyy → yyyy-MM-dd o null si incompleta/inválida. */
    fun parseDisplayToIso(display: String): String? {
        if (!display.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) return null
        val parts = display.split("/")
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        if (month !in 1..12 || day !in 1..31 || year < 1900) return null
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    fun millisDesdeIso(fechaIso: String): Long? = runCatching {
        val parts = fechaIso.split("-")
        if (parts.size != 3) return null
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }.getOrNull()

    private fun calcularEdad(fechaNacimiento: Date): Int {
        val hoy = Calendar.getInstance()
        val nacimiento = Calendar.getInstance().apply { time = fechaNacimiento }
        var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
        if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        return edad
    }
}
