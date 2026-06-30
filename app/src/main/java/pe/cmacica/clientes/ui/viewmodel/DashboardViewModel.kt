package pe.cmacica.clientes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.cmacica.clientes.data.model.Movimiento
import pe.cmacica.clientes.data.model.SolicitudCredito
import pe.cmacica.clientes.data.repository.AuthRepository
import pe.cmacica.clientes.data.repository.CuentaRepository
import pe.cmacica.clientes.data.repository.SolicitudCreditoRepository

data class DashboardUiState(
    val nombre: String = "",
    val apellidos: String = "",
    val documento: String = "",
    val email: String = "",
    val fechaNacimiento: String = "",
    val saldo: Double = 0.0,
    val saldoVisible: Boolean = true,
    val movimientos: List<Movimiento> = emptyList(),
    val movimientosPagina: Int = 0,
    val solicitudes: List<SolicitudCredito> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val movimientosPorPagina: Int = MOVIMIENTOS_POR_PAGINA

    val totalPaginasMovimientos: Int
        get() = if (movimientos.isEmpty()) 1
        else ((movimientos.size + movimientosPorPagina - 1) / movimientosPorPagina)

    val movimientosPaginaActual: List<Movimiento>
        get() {
            if (movimientos.isEmpty()) return emptyList()
            val pagina = movimientosPagina.coerceIn(0, totalPaginasMovimientos - 1)
            val inicio = pagina * movimientosPorPagina
            return movimientos.drop(inicio).take(movimientosPorPagina)
        }

    val nombreCompleto: String
        get() = listOf(nombre, apellidos).filter { it.isNotBlank() }.joinToString(" ")
            .ifBlank { nombre.ifBlank { "Cliente" } }

    companion object {
        const val MOVIMIENTOS_POR_PAGINA = 5
    }
}

class DashboardViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val cuentaRepository: CuentaRepository = CuentaRepository(),
    private val solicitudRepository: SolicitudCreditoRepository = SolicitudCreditoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Sesión no válida.") }
        } else {
            observeCuenta(uid)
            observeSolicitudes(uid)
        }
    }

    private fun observeCuenta(uid: String) {
        viewModelScope.launch {
            cuentaRepository.observePerfil(uid).collect { perfil ->
                _uiState.update {
                    it.copy(
                        nombre = perfil?.nombre.orEmpty().ifBlank { "Cliente" },
                        apellidos = perfil?.apellidos.orEmpty(),
                        documento = perfil?.documento.orEmpty()
                            .ifBlank { authRepository.currentDocumento.orEmpty() },
                        email = perfil?.email.orEmpty(),
                        fechaNacimiento = perfil?.fechaNacimiento.orEmpty(),
                        saldo = perfil?.saldo ?: 0.0,
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            cuentaRepository.observeMovimientos(uid).collect { movimientos ->
                _uiState.update { state ->
                    val totalPaginas = if (movimientos.isEmpty()) 1
                    else ((movimientos.size + DashboardUiState.MOVIMIENTOS_POR_PAGINA - 1) /
                        DashboardUiState.MOVIMIENTOS_POR_PAGINA)
                    val paginaAjustada = state.movimientosPagina.coerceIn(0, (totalPaginas - 1).coerceAtLeast(0))
                    state.copy(
                        movimientos = movimientos,
                        movimientosPagina = paginaAjustada
                    )
                }
            }
        }
    }

    private fun observeSolicitudes(uid: String) {
        viewModelScope.launch {
            solicitudRepository.observeSolicitudesCliente(uid).collect { solicitudes ->
                _uiState.update { it.copy(solicitudes = solicitudes) }
            }
        }
    }

    fun toggleSaldoVisible() {
        _uiState.update { it.copy(saldoVisible = !it.saldoVisible) }
    }

    fun paginaAnteriorMovimientos() {
        _uiState.update { state ->
            state.copy(movimientosPagina = (state.movimientosPagina - 1).coerceAtLeast(0))
        }
    }

    fun paginaSiguienteMovimientos() {
        _uiState.update { state ->
            val maxPagina = state.totalPaginasMovimientos - 1
            state.copy(movimientosPagina = (state.movimientosPagina + 1).coerceAtMost(maxPagina))
        }
    }

    fun logout() {
        authRepository.signOut()
    }
}
