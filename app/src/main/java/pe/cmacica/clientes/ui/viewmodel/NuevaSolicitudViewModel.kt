package pe.cmacica.clientes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.cmacica.clientes.data.model.SolicitudCredito
import pe.cmacica.clientes.data.repository.AuthRepository
import pe.cmacica.clientes.data.repository.CuentaRepository
import pe.cmacica.clientes.data.repository.SolicitudCreditoRepository

data class NuevaSolicitudUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val solicitudCreada: SolicitudCredito? = null
)

class NuevaSolicitudViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val cuentaRepository: CuentaRepository = CuentaRepository(),
    private val solicitudRepository: SolicitudCreditoRepository = SolicitudCreditoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevaSolicitudUiState())
    val uiState: StateFlow<NuevaSolicitudUiState> = _uiState.asStateFlow()

    fun registrarSolicitud(
        monto: Double,
        plazoMeses: Int,
        destino: String,
        garantia: String
    ) {
        when {
            monto <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Ingresa un monto válido mayor a cero.") }
                return
            }
            plazoMeses <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Ingresa un plazo en meses válido.") }
                return
            }
            destino.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Selecciona o ingresa el destino del crédito.") }
                return
            }
            garantia.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Selecciona o ingresa el tipo de garantía.") }
                return
            }
        }

        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "Sesión no válida. Vuelve a iniciar sesión.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val perfil = cuentaRepository.getPerfil(uid)
            val documento = perfil?.documento?.ifBlank {
                authRepository.currentDocumento.orEmpty()
            }.orEmpty()
            val nombre = perfil?.nombreCompleto.orEmpty().ifBlank { "Cliente" }

            solicitudRepository.registrarSolicitudCliente(
                clienteUid = uid,
                documento = documento,
                nombre = nombre,
                monto = monto,
                plazoMeses = plazoMeses,
                destino = destino.trim(),
                garantia = garantia.trim()
            ).onSuccess { solicitud ->
                _uiState.update {
                    it.copy(isLoading = false, solicitudCreada = solicitud)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo registrar la solicitud."
                    )
                }
            }
        }
    }

    fun consumeSolicitudCreada() {
        _uiState.update { it.copy(solicitudCreada = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
