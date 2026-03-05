package aimar.rojas.avmadmin.features.selections.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository
import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
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
    val totalWeight: Double = 0.0,
    val totalAmount: Int = 0
)

data class SelectionTypeInfo(
    val id: Int,
    val name: String
)

@HiltViewModel
class TradeSelectionsViewModel @Inject constructor(
    private val selectionsRepository: SelectionsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tradeId: Int = savedStateHandle.get<Int>("tradeId") ?: -1

    private val _uiState = MutableStateFlow(TradeSelectionsUiState())
    val uiState: StateFlow<TradeSelectionsUiState> = _uiState.asStateFlow()

    val selectionTypes = listOf(
        SelectionTypeInfo(1, "Sin pita"),
        SelectionTypeInfo(2, "Verde"),
        SelectionTypeInfo(3, "Blanco"),
        SelectionTypeInfo(4, "Rojo"),
        SelectionTypeInfo(5, "Azul"),
        SelectionTypeInfo(6, "Morado"),
        SelectionTypeInfo(7, "Amarillo")
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
            val result = selectionsRepository.getSelections(tradeId = tradeId)
            result.onSuccess { selections ->
                val totalW = selections.sumOf { s -> s.unitWeights.sumOf { it.weight * it.amount } }
                val totalA = selections.sumOf { s -> s.unitWeights.sumOf { it.amount } }
                
                _uiState.update { it.copy(
                    isLoading = false, 
                    selections = selections, 
                    totalWeight = totalW,
                    totalAmount = totalA,
                    error = null
                ) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Error desconocido") }
            }
        }
    }

    fun onSelectionTypeSelected(id: Int) {
        _uiState.update { it.copy(selectedSelectionTypeId = id) }
    }

    fun onWeightInputChange(value: String) {
        _uiState.update { it.copy(weightInput = value) }
    }

    fun onAmountInputChange(value: String) {
        _uiState.update { it.copy(amountInput = value) }
    }

    fun insertUnitWeight() {
        // TODO: Implementar llamada al repositorio para guardar el nuevo unit weight
        // Una vez guardado exitosamente, recargaríamos las selecciones
        _uiState.update { it.copy(weightInput = "", amountInput = "") }
        loadSelections() // Recargar para ver los cambios
    }
}
