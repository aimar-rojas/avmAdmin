package aimar.rojas.avmadmin.features.apuntes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.features.apuntes.domain.ApuntesRepository
import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApuntesHistoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val records: List<Apunte> = emptyList()
)

@HiltViewModel
class ApuntesHistoryViewModel @Inject constructor(
    private val repository: ApuntesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApuntesHistoryUiState())
    val uiState: StateFlow<ApuntesHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getApuntes()
            result.onSuccess { list ->
                _uiState.update { it.copy(isLoading = false, records = list) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message ?: "Error al cargar historial") }
            }
        }
    }
}
