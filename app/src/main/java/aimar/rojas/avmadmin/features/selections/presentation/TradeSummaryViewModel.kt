package aimar.rojas.avmadmin.features.selections.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.domain.model.Trade
import aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository
import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
import aimar.rojas.avmadmin.features.trades.domain.TradesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TradeSummaryUiState(
    val isLoading: Boolean = true,
    val trade: Trade? = null,
    val workedSelections: List<SelectionSummaryItem> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

data class SelectionSummaryItem(
    val selectionDetail: SelectionDetail,
    val grossWeight: Double,
    val crateCount: Int,
    val netWeight: Double,
    val pricePerKg: String = "",
    val totalToPay: Double = 0.0
)

@HiltViewModel
class TradeSummaryViewModel @Inject constructor(
    private val tradesRepository: TradesRepository,
    private val selectionsRepository: SelectionsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tradeId: Int = savedStateHandle.get<Int>("tradeId") ?: -1

    private val _uiState = MutableStateFlow(TradeSummaryUiState())
    val uiState: StateFlow<TradeSummaryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        if (tradeId == -1) {
            _uiState.update { it.copy(isLoading = false, error = "Trade ID no encontrado") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val tradeResult = tradesRepository.getTradeById(tradeId)
            val selectionsResult = selectionsRepository.getLocalSelections(tradeId)

            if (tradeResult.isSuccess && selectionsResult.isSuccess) {
                val trade = tradeResult.getOrNull()
                val selections = selectionsResult.getOrNull() ?: emptyList()
                val worked = selections.filter { it.unitWeights.isNotEmpty() }.map { selection ->
                    val gross = selection.unitWeights.sumOf { it.weight * it.amount }
                    val crates = selection.unitWeights.sumOf { it.amount }
                    val discount = trade?.discountWeightPerTray ?: 0.0
                    val net = gross - (crates * discount)
                    val price = selection.price?.toString() ?: ""
                    val total = if (price.isNotEmpty()) net * (price.toDoubleOrNull() ?: 0.0) else 0.0
                    
                    SelectionSummaryItem(
                        selectionDetail = selection,
                        grossWeight = gross,
                        crateCount = crates,
                        netWeight = net,
                        pricePerKg = price,
                        totalToPay = total
                    )
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    trade = trade,
                    workedSelections = worked
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al cargar datos del negocio o selecciones"
                ) }
            }
        }
    }

    fun onPriceChange(selectionByTradeId: Int, newPrice: String) {
        _uiState.update { currentState ->
            val updatedWorked = currentState.workedSelections.map { item ->
                if (item.selectionDetail.selectionByTradeId == selectionByTradeId) {
                    val priceDouble = newPrice.toDoubleOrNull() ?: 0.0
                    item.copy(
                        pricePerKg = newPrice,
                        totalToPay = item.netWeight * priceDouble
                    )
                } else {
                    item
                }
            }
            currentState.copy(workedSelections = updatedWorked)
        }
    }

    fun saveAndFinish() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                _uiState.value.workedSelections.forEach { item ->
                    val newPrice = item.pricePerKg.toDoubleOrNull()
                    val oldPrice = item.selectionDetail.price
                    
                    // Only update and mark as pending if there's a difference
                    if (newPrice != oldPrice || item.selectionDetail.isPendingSync) {
                        val updatedSelection = item.selectionDetail.copy(
                            price = newPrice,
                            isPendingSync = true
                        )
                        selectionsRepository.saveSelectionLocal(updatedSelection)
                    }
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Error al guardar los precios") }
            }
        }
    }
}
