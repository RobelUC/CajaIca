package pe.cmacica.clientes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.cmacica.clientes.data.model.Movimiento
import pe.cmacica.clientes.data.repository.AuthRepository
import pe.cmacica.clientes.data.repository.CuentaRepository

data class DashboardUiState(
    val nombre: String = "",
    val saldo: Double = 0.0,
    val movimientos: List<Movimiento> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val cuentaRepository: CuentaRepository = CuentaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Sesión no válida.") }
        } else {
            observeCuenta(uid)
        }
    }

    private fun observeCuenta(uid: String) {
        viewModelScope.launch {
            cuentaRepository.observePerfil(uid).collect { perfil ->
                _uiState.update {
                    it.copy(
                        nombre = perfil?.nombre.orEmpty().ifBlank { "Cliente" },
                        saldo = perfil?.saldo ?: 0.0,
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            cuentaRepository.observeMovimientos(uid).collect { movimientos ->
                _uiState.update { it.copy(movimientos = movimientos) }
            }
        }
    }

    fun logout() {
        authRepository.signOut()
    }
}
