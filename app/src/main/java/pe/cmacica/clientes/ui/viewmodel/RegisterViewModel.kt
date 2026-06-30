package pe.cmacica.clientes.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.cmacica.clientes.data.AuthCredentials
import pe.cmacica.clientes.data.repository.AuthRepository
import pe.cmacica.clientes.data.repository.CuentaRepository
import pe.cmacica.clientes.ui.util.RegistroValidacion

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

class RegisterViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(application),
    private val cuentaRepository: CuentaRepository = CuentaRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        documento: String,
        nombre: String,
        apellidos: String,
        fechaNacimiento: String,
        password: String,
        confirmPassword: String
    ) {
        val error = RegistroValidacion.validarDocumento(documento.trim())
            ?: RegistroValidacion.validarNombre(nombre.trim(), "nombre")
            ?: RegistroValidacion.validarNombre(apellidos.trim(), "apellido")
            ?: RegistroValidacion.validarFechaNacimiento(fechaNacimiento)
            ?: RegistroValidacion.validarClaveNumerica(password, confirmPassword)

        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.signUp(documento.trim(), password)
                .onSuccess { user ->
                    val email = AuthCredentials.documentoToEmail(documento.trim())
                    cuentaRepository.crearPerfilInicial(
                        uid = user.uid,
                        documento = documento.trim(),
                        nombre = nombre.trim(),
                        apellidos = apellidos.trim(),
                        fechaNacimiento = fechaNacimiento,
                        email = email
                    )
                    _uiState.update {
                        it.copy(isLoading = false, registerSuccess = true)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = traducirErrorFirebase(error.message)
                        )
                    }
                }
        }
    }

    private fun traducirErrorFirebase(mensaje: String?): String = when {
        mensaje.isNullOrBlank() -> "Error al registrarse."
        mensaje.contains("email address is already in use", ignoreCase = true) ->
            "Ya existe una cuenta con este documento."
        mensaje.contains("weak password", ignoreCase = true) ->
            "La clave debe tener al menos 6 números."
        else -> mensaje
    }

    fun consumeRegisterSuccess() {
        _uiState.update { it.copy(registerSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RegisterViewModel(application) as T
                }
            }
    }
}
