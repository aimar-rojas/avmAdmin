package aimar.rojas.avmadmin.features.shipments.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.data.remote.mapper.toApiFormat
import aimar.rojas.avmadmin.domain.model.Shipment
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShipmentsViewModel @Inject constructor(
    private val shipmentsRepository: ShipmentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShipmentsUiState())
    val uiState: StateFlow<ShipmentsUiState> = _uiState.asStateFlow()

    init {
        loadShipments()
    }

    fun loadShipments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            shipmentsRepository.getShipments()
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shipments = result.shipments,
                        total = result.total,
                        page = result.page,
                        hasNext = result.hasNext,
                        hasPrevious = result.hasPrevious
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar envíos"
                    )
                }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = true,
            createStartDate = "",
            createEndDate = "",
            createStatus = "OPEN"
        )
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun onCreateStartDateChange(date: String) {
        _uiState.value = _uiState.value.copy(createStartDate = date)
    }

    fun onCreateEndDateChange(date: String) {
        _uiState.value = _uiState.value.copy(createEndDate = date)
    }

    fun onCreateStatusChange(status: String) {
        _uiState.value = _uiState.value.copy(createStatus = status)
    }

    fun createShipment() {
        val currentState = _uiState.value
        
        if (currentState.createStartDate.isBlank()) {
            _uiState.value = currentState.copy(error = "La fecha de inicio es requerida")
            return
        }
        
        if (currentState.createStatus !in listOf("OPEN", "CLOSED")) {
            _uiState.value = currentState.copy(error = "El estado debe ser OPEN o CLOSED")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            shipmentsRepository.createShipment(
                startDate = currentState.createStartDate,
                endDate = currentState.createEndDate.takeIf { it.isNotBlank() },
                status = currentState.createStatus
            )
                .onSuccess {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        showCreateDialog = false,
                        createStartDate = "",
                        createEndDate = "",
                        createStatus = "OPEN"
                    )
                    loadShipments()
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al crear envío"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ShipmentsUiState(
    val shipments: List<Shipment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val total: Long = 0,
    val page: Int = 1,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val showCreateDialog: Boolean = false,
    val createStartDate: String = "",
    val createEndDate: String = "",
    val createStatus: String = "OPEN"
)
