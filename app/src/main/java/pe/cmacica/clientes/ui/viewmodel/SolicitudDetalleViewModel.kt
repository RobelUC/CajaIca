package pe.cmacica.clientes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.cmacica.clientes.data.model.SolicitudCredito
import pe.cmacica.clientes.data.repository.SolicitudCreditoRepository

data class SolicitudDetalleUiState(
    val solicitud: SolicitudCredito? = null,
    val isLoading: Boolean = true
)

class SolicitudDetalleViewModel(
    private val solicitudRepository: SolicitudCreditoRepository = SolicitudCreditoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudDetalleUiState())
    val uiState: StateFlow<SolicitudDetalleUiState> = _uiState.asStateFlow()

    fun observe(solicitudId: String) {
        viewModelScope.launch {
            solicitudRepository.observeSolicitud(solicitudId).collect { solicitud ->
                _uiState.update {
                    it.copy(solicitud = solicitud, isLoading = solicitud == null)
                }
            }
        }
    }
}
