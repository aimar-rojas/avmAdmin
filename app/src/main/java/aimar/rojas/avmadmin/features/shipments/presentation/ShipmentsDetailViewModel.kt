package aimar.rojas.avmadmin.features.shipments.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.domain.model.Trade
import aimar.rojas.avmadmin.features.trades.domain.TradesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShipmentsDetailViewModel @Inject constructor(
    private val tradesRepository: TradesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shipmentId: Int = checkNotNull(savedStateHandle["shipmentId"])

    private val _uiState = MutableStateFlow(ShipmentsDetailUiState())
    val uiState: StateFlow<ShipmentsDetailUiState> = _uiState.asStateFlow()

    init {
        loadTrades()
    }

    fun loadTrades() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            tradesRepository.getTrades(shipmentId = shipmentId)
                .onSuccess { result ->
                    val purchases = result.trades.filter { it.tradeType == "PURCHASE" }
                    val sales = result.trades.filter { it.tradeType == "SALE" }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        purchases = purchases,
                        sales = sales
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar transacciones"
                    )
                }
        }
    }
}

data class ShipmentsDetailUiState(
    val purchases: List<Trade> = emptyList(),
    val sales: List<Trade> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
