package aimar.rojas.avmadmin.features.shipments.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.domain.model.Shipment
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentsRepository
import aimar.rojas.avmadmin.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
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
            createStatus = "OPEN",
            showStartDatePicker = false,
            showEndDatePicker = false
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

    fun onStartDateSelected(date: Date) {
        _uiState.value = _uiState.value.copy(
            createStartDate = DateUtils.formatToApiDate(date),
            showStartDatePicker = false
        )
    }

    fun onEndDateSelected(date: Date) {
        _uiState.value = _uiState.value.copy(
            createEndDate = DateUtils.formatToApiDate(date),
            showEndDatePicker = false
        )
    }

    fun showStartDatePicker() {
        _uiState.value = _uiState.value.copy(showStartDatePicker = true)
    }

    fun hideStartDatePicker() {
        _uiState.value = _uiState.value.copy(showStartDatePicker = false)
    }

    fun showEndDatePicker() {
        _uiState.value = _uiState.value.copy(showEndDatePicker = true)
    }

    fun hideEndDatePicker() {
        _uiState.value = _uiState.value.copy(showEndDatePicker = false)
    }

    fun onCreateStatusChange(status: String) {
        _uiState.value = _uiState.value.copy(
            createStatus = status,
            createEndDate = if (status == "OPEN") "" else _uiState.value.createEndDate
        )
    }

    fun createShipment() {
        val currentState = _uiState.value
        
        if (currentState.createStartDate.isBlank()) {
            _uiState.value = currentState.copy(error = "La fecha de inicio es requerida")
            return
        }
        
        if (currentState.createStatus == "CLOSED" && currentState.createEndDate.isBlank()) {
            _uiState.value = currentState.copy(error = "La fecha de fin es requerida para envíos pasados")
            return
        }
        
        if (currentState.createStatus !in listOf("OPEN", "CLOSED")) {
            _uiState.value = currentState.copy(error = "El estado debe ser Nuevo (OPEN) o Pasado (CLOSED)")
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
    val createStatus: String = "OPEN",
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false
)
