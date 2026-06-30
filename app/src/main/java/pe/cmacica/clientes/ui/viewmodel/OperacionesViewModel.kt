package pe.cmacica.clientes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.cmacica.clientes.data.model.CuentaAhorro
import pe.cmacica.clientes.data.model.ServicioPago
import pe.cmacica.clientes.data.repository.AuthRepository
import pe.cmacica.clientes.data.repository.CuentaRepository

data class OperacionesUiState(
    val saldo: Double = 0.0,
    val cuentasAhorro: List<CuentaAhorro> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class OperacionesViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val cuentaRepository: CuentaRepository = CuentaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperacionesUiState())
    val uiState: StateFlow<OperacionesUiState> = _uiState.asStateFlow()

    val serviciosDisponibles = listOf(
        ServicioPago("luz", "Luz del Sur", "1234567890"),
        ServicioPago("agua", "Sedapal", "9876543210"),
        ServicioPago("telefono", "Movistar", "4567891230"),
        ServicioPago("internet", "Claro", "3216549870"),
        ServicioPago("gas", "Cálidda", "7418529630")
    )

    init {
        val uid = authRepository.currentUser?.uid
        if (uid != null) {
            observeSaldo(uid)
            observeCuentasAhorro(uid)
        }
    }

    fun cargarAhorros() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            cuentaRepository.ensureCuentasAhorro(uid)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun observeCuentasAhorro(uid: String) {
        viewModelScope.launch {
            cuentaRepository.observeCuentasAhorro(uid).collect { cuentas ->
                _uiState.update { it.copy(cuentasAhorro = cuentas) }
            }
        }
    }

    fun pagarServicio(servicio: ServicioPago, codigoCliente: String, monto: Double) {
        when {
            codigoCliente.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Ingresa el código de suministro.") }
                return
            }
            monto <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Ingresa un monto válido.") }
                return
            }
        }
        ejecutarOperacion(
            titulo = "Pago ${servicio.nombre} — $codigoCliente",
            monto = -monto,
            esIngreso = false,
            exito = "Pago de ${servicio.nombre} realizado correctamente."
        )
    }

    fun transferir(cuentaDestino: String, titular: String, monto: Double) {
        when {
            cuentaDestino.length < 8 -> {
                _uiState.update { it.copy(errorMessage = "Ingresa una cuenta destino válida.") }
                return
            }
            titular.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Ingresa el nombre del titular.") }
                return
            }
            monto <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Ingresa un monto válido.") }
                return
            }
        }
        ejecutarOperacion(
            titulo = "Transferencia a $titular — $cuentaDestino",
            monto = -monto,
            esIngreso = false,
            exito = "Transferencia enviada a $titular por S/ ${"%.2f".format(monto)}."
        )
    }

    private fun ejecutarOperacion(
        titulo: String,
        monto: Double,
        esIngreso: Boolean,
        exito: String
    ) {
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "Sesión no válida.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            cuentaRepository.registrarOperacion(uid, titulo, monto, esIngreso)
                .onSuccess { nuevoSaldo ->
                    _uiState.update { state ->
                        state.copy(isLoading = false, saldo = nuevoSaldo, successMessage = exito)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo completar la operación."
                        )
                    }
                }
        }
    }

    private fun observeSaldo(uid: String) {
        viewModelScope.launch {
            cuentaRepository.observePerfil(uid).collect { perfil ->
                _uiState.update { it.copy(saldo = perfil?.saldo ?: 0.0) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
