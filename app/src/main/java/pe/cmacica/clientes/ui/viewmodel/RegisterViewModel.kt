package pe.cmacica.clientes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.cmacica.clientes.data.repository.AuthRepository
import pe.cmacica.clientes.data.repository.CuentaRepository

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val cuentaRepository: CuentaRepository = CuentaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(nombre: String, email: String, password: String, confirmPassword: String) {
        when {
            nombre.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Ingresa tu nombre.") }
                return
            }
            email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Ingresa tu correo.") }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
                return
            }
            password != confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden.") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.signUp(email.trim(), password)
                .onSuccess { user ->
                    cuentaRepository.crearPerfilInicial(
                        uid = user.uid,
                        nombre = nombre.trim(),
                        email = email.trim()
                    )
                    _uiState.update {
                        it.copy(isLoading = false, registerSuccess = true)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al registrarse."
                        )
                    }
                }
        }
    }

    fun consumeRegisterSuccess() {
        _uiState.update { it.copy(registerSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
