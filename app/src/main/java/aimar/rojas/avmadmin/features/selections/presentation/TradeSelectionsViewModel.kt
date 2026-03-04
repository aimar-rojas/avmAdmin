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
    val error: String? = null
)

@HiltViewModel
class TradeSelectionsViewModel @Inject constructor(
    private val selectionsRepository: SelectionsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tradeId: Int = savedStateHandle.get<Int>("tradeId") ?: -1

    private val _uiState = MutableStateFlow(TradeSelectionsUiState())
    val uiState: StateFlow<TradeSelectionsUiState> = _uiState.asStateFlow()

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
                _uiState.update { it.copy(isLoading = false, selections = selections, error = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Error desconocido") }
            }
        }
    }
}
