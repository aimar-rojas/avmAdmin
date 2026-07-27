package aimar.rojas.avmadmin.features.selections.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository
import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
import aimar.rojas.avmadmin.features.selections.domain.model.UnitWeightDetail
import aimar.rojas.avmadmin.features.trades.domain.TradesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TradeSelectionsUiState(
    val isLoading: Boolean = true,
    val selections: List<SelectionDetail> = emptyList(),
    val error: String? = null,
    val selectedSelectionTypeId: Int = 1,
    val weightInput: String = "",
    val amountInput: String = "",
    val totalGrossWeight: Double = 0.0,
    val totalNetWeight: Double = 0.0,
    val totalAmount: Int = 0,
    val discountWeightPerTray: Double = 0.0,
    val visibleSelectionTypeIds: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7, 8),
    val showSelectionManagerDialog: Boolean = false,
    val editingUnitWeightId: Int? = null
)

private data class WeightTotals(
    val grossWeight: Double,
    val netWeight: Double,
    val amount: Int
)

data class SelectionTypeInfo(
    val id: Int,
    val name: String
)

@HiltViewModel
class TradeSelectionsViewModel @Inject constructor(
    private val selectionsRepository: SelectionsRepository,
    private val tradesRepository: TradesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var tradeId: Int = savedStateHandle.get<Int>("tradeId") ?: -1

    private val _uiState = MutableStateFlow(TradeSelectionsUiState())
    val uiState: StateFlow<TradeSelectionsUiState> = _uiState.asStateFlow()

    val selectionTypes = listOf(
        SelectionTypeInfo(1, "Sin pita"),
        SelectionTypeInfo(2, "Verde"),
        SelectionTypeInfo(3, "Blanco"),
        SelectionTypeInfo(4, "Rosado"),
        SelectionTypeInfo(5, "Naranja"),
        SelectionTypeInfo(6, "Azul"),
        SelectionTypeInfo(7, "Morado"),
        SelectionTypeInfo(8, "Amarillo")
    )

    init {
        loadSelections()
    }

    fun loadSelections() {
        if (tradeId == -1) {
            _uiState.update { it.copy(isLoading = false, error = "Trade ID no encontrado") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val discount = tradesRepository.getTradeById(tradeId).getOrNull()?.discountWeightPerTray ?: 0.0
            val result = selectionsRepository.getSelections(tradeId = tradeId)
            result.onSuccess { selections ->
                val totals = calculateTotals(selections, discount)
                
                _uiState.update { it.copy(
                    isLoading = false, 
                    selections = selections, 
                    totalGrossWeight = totals.grossWeight,
                    totalNetWeight = totals.netWeight,
                    totalAmount = totals.amount,
                    discountWeightPerTray = discount,
                    error = null
                ) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Error desconocido") }
            }
        }
    }

    fun onSelectionTypeSelected(id: Int) {
        _uiState.update {
            it.copy(
                selectedSelectionTypeId = id,
                weightInput = "",
                amountInput = "",
                editingUnitWeightId = null
            )
        }
    }

    fun onWeightInputChange(value: String) {
        _uiState.update { it.copy(weightInput = value) }
    }

    fun onAmountInputChange(value: String) {
        _uiState.update { it.copy(amountInput = value) }
    }

    fun insertUnitWeight() {
        val weight = _uiState.value.weightInput.toDoubleOrNull()
        val amount = _uiState.value.amountInput.toIntOrNull()
        
        if (weight != null && amount != null) {
            val currentState = _uiState.value
            val currentSelection = currentState.selections.find { it.selectionTypeId == currentState.selectedSelectionTypeId }

            if (currentState.editingUnitWeightId != null && currentSelection != null) {
                updateUnitWeight(
                    currentSelection = currentSelection,
                    unitWeightId = currentState.editingUnitWeightId,
                    weight = weight,
                    amount = amount
                )
                return
            }
            
            if (currentSelection != null) {
                // Add the new unit weight to the current selection
                val newUnitWeight = UnitWeightDetail(
                    unitWeightId = 0,
                    weight = weight,
                    amount = amount
                )
                
                val updatedSelection = currentSelection.copy(
                    unitWeights = currentSelection.unitWeights + newUnitWeight,
                    isPendingSync = true
                )
                
                // Clear inputs
                _uiState.update { it.copy(weightInput = "", amountInput = "") }
                
                // Save locally and reload
                viewModelScope.launch {
                    selectionsRepository.saveSelectionLocal(updatedSelection)
                    loadLocalSelections()
                }
            } else {
                // If the selection doesn't exist yet, we create it
                val newSelection = aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail(
                    selectionByTradeId = 0,
                    tradeId = tradeId,
                    selectionTypeId = currentState.selectedSelectionTypeId,
                    price = null,
                    unitWeights = listOf(
                        aimar.rojas.avmadmin.features.selections.domain.model.UnitWeightDetail(
                            unitWeightId = 0,
                            weight = weight,
                            amount = amount
                        )
                    ),
                    selectionTypeName = selectionTypes.find { it.id == currentState.selectedSelectionTypeId }?.name,
                    isPendingSync = true
                )
                
                _uiState.update { it.copy(weightInput = "", amountInput = "") }
                
                viewModelScope.launch {
                    selectionsRepository.saveSelectionLocal(newSelection)
                    loadLocalSelections()
                }
            }
        }
    }

    fun startUnitWeightEdit(unitWeight: UnitWeightDetail) {
        _uiState.update {
            it.copy(
                weightInput = formatWeightInput(unitWeight.weight),
                amountInput = unitWeight.amount.toString(),
                editingUnitWeightId = unitWeight.unitWeightId
            )
        }
    }

    fun cancelUnitWeightEdit() {
        _uiState.update {
            it.copy(
                weightInput = "",
                amountInput = "",
                editingUnitWeightId = null
            )
        }
    }

    fun deleteUnitWeight(unitWeight: UnitWeightDetail) {
        val currentState = _uiState.value
        val currentSelection = currentState.selections.find { it.selectionTypeId == currentState.selectedSelectionTypeId } ?: return
        val updatedWeights = currentSelection.unitWeights.filterNot { it.unitWeightId == unitWeight.unitWeightId }
        val updatedSelection = currentSelection.copy(
            unitWeights = updatedWeights,
            isPendingSync = true
        )

        if (currentState.editingUnitWeightId == unitWeight.unitWeightId) {
            cancelUnitWeightEdit()
        }

        viewModelScope.launch {
            if (updatedWeights.isEmpty() && currentSelection.remoteId == null) {
                selectionsRepository.deleteSelectionLocal(currentSelection.selectionByTradeId)
            } else {
                selectionsRepository.saveSelectionLocal(updatedSelection)
            }
            loadLocalSelections()
        }
    }

    private fun updateUnitWeight(
        currentSelection: SelectionDetail,
        unitWeightId: Int,
        weight: Double,
        amount: Int
    ) {
        val updatedSelection = currentSelection.copy(
            unitWeights = currentSelection.unitWeights.map { unitWeight ->
                if (unitWeight.unitWeightId == unitWeightId) {
                    unitWeight.copy(weight = weight, amount = amount)
                } else {
                    unitWeight
                }
            },
            isPendingSync = true
        )

        _uiState.update {
            it.copy(
                weightInput = "",
                amountInput = "",
                editingUnitWeightId = null
            )
        }

        viewModelScope.launch {
            selectionsRepository.saveSelectionLocal(updatedSelection)
            loadLocalSelections()
        }
    }

    private fun formatWeightInput(value: Double): String {
        val raw = value.toString()
        return if (raw.endsWith(".0")) raw.dropLast(2) else raw
    }

    private fun loadLocalSelections() {
        if (tradeId == -1) return
        
        viewModelScope.launch {
            val result = selectionsRepository.getLocalSelections(tradeId)
            result.onSuccess { selections ->
                val discount = _uiState.value.discountWeightPerTray
                val totals = calculateTotals(selections, discount)
                
                _uiState.update { it.copy(
                    selections = selections, 
                    totalGrossWeight = totals.grossWeight,
                    totalNetWeight = totals.netWeight,
                    totalAmount = totals.amount
                ) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Error desconocido") }
            }
        }
    }

    // Removed syncWithBackend because sync is now manual from the Trades List

    fun showSelectionManagerDialog() {
        _uiState.update { it.copy(showSelectionManagerDialog = true) }
    }

    fun hideSelectionManagerDialog() {
        _uiState.update { it.copy(showSelectionManagerDialog = false) }
    }

    fun toggleSelectionVisibility(selectionTypeId: Int) {
        _uiState.update { currentState ->
            val newVisibleIds = if (currentState.visibleSelectionTypeIds.contains(selectionTypeId)) {
                if (currentState.visibleSelectionTypeIds.size > 1) {
                    currentState.visibleSelectionTypeIds - selectionTypeId
                } else {
                    currentState.visibleSelectionTypeIds
                }
            } else {
                currentState.visibleSelectionTypeIds + selectionTypeId
            }

            val newSelectedId = if (newVisibleIds.contains(currentState.selectedSelectionTypeId)) {
                currentState.selectedSelectionTypeId
            } else {
                newVisibleIds.minOrNull() ?: currentState.selectedSelectionTypeId
            }
            
            currentState.copy(
                visibleSelectionTypeIds = newVisibleIds,
                selectedSelectionTypeId = newSelectedId
            )
        }
    }

    fun getVisibleSelectionTypes(): List<SelectionTypeInfo> {
        return selectionTypes.filter { it.id in _uiState.value.visibleSelectionTypeIds }
    }

    private fun calculateTotals(
        selections: List<SelectionDetail>,
        discountWeightPerTray: Double
    ): WeightTotals {
        val gross = selections.sumOf { selection -> selection.unitWeights.sumOf { it.weight } }
        val amount = selections.sumOf { selection -> selection.unitWeights.sumOf { it.amount } }
        val discount = amount * discountWeightPerTray
        val net = (gross - discount).coerceAtLeast(0.0)

        return WeightTotals(
            grossWeight = gross,
            netWeight = net,
            amount = amount
        )
    }
}
