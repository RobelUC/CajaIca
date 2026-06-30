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
import pe.cmacica.clientes.data.repository.AuthRepository

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(documento: String, password: String) {
        when {
            documento.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Ingresa tu documento de identidad.") }
                return
            }
            password.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Ingresa tu clave.") }
                return
            }
            documento.length !in 8..12 -> {
                _uiState.update { it.copy(errorMessage = "El documento debe tener entre 8 y 12 dígitos.") }
                return
            }
            !password.all { it.isDigit() } -> {
                _uiState.update { it.copy(errorMessage = "La clave solo debe contener números.") }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "La clave debe tener al menos 6 números.") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.signIn(documento.trim(), password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Error al iniciar sesión.")
                    }
                }
        }
    }

    fun consumeLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(application) as T
                }
            }
    }
}
