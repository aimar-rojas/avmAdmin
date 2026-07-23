package aimar.rojas.avmadmin.features.apuntes.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.features.apuntes.domain.ApuntesRepository
import aimar.rojas.avmadmin.features.apuntes.domain.model.ApunteDetail
import aimar.rojas.avmadmin.features.selections.presentation.SelectionTypeInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApunteItemState(
    val typeInfo: SelectionTypeInfo,
    val isEnabled: Boolean = true,
    val countInput: String = ""
)

data class ApuntesUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val items: List<ApunteItemState> = emptyList(),
    val observations: String = "",
    val editingApunteId: Int? = null
)

@HiltViewModel
class ApuntesViewModel @Inject constructor(
    private val repository: ApuntesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApuntesUiState())
    val uiState: StateFlow<ApuntesUiState> = _uiState.asStateFlow()

    private val selectionTypes = listOf(
        SelectionTypeInfo(1, "Sin pita"),
        SelectionTypeInfo(2, "Verde"),
        SelectionTypeInfo(3, "Blanco"),
        SelectionTypeInfo(4, "Rosado"),
        SelectionTypeInfo(5, "Naranja"),
        SelectionTypeInfo(6, "Azul"),
        SelectionTypeInfo(7, "Morado"),
        SelectionTypeInfo(8, "Amarillo")
    )

    private val editingApunteId = savedStateHandle.get<Int>("apunteId")?.takeIf { it > 0 }

    init {
        _uiState.update { state ->
            state.copy(
                items = selectionTypes.map { ApunteItemState(typeInfo = it) },
                editingApunteId = editingApunteId
            )
        }
        editingApunteId?.let { loadApunteForEdit(it) }
    }

    fun toggleItemEnabled(selectionTypeId: Int, isEnabled: Boolean) {
        _uiState.update { state ->
            val newItems = state.items.map {
                if (it.typeInfo.id == selectionTypeId) {
                    it.copy(isEnabled = isEnabled, countInput = if (!isEnabled) "" else it.countInput)
                } else it
            }
            state.copy(items = newItems)
        }
    }

    fun onCountChanged(selectionTypeId: Int, count: String) {
        _uiState.update { state ->
            val newItems = state.items.map {
                if (it.typeInfo.id == selectionTypeId) it.copy(countInput = count) else it
            }
            state.copy(items = newItems)
        }
    }

    fun onObservationsChanged(obs: String) {
        _uiState.update { it.copy(observations = obs) }
    }

    fun saveApunte() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false) }

            val details = _uiState.value.items.map {
                val count = it.countInput.toIntOrNull() ?: 0
                ApunteDetail(
                    id = 0,
                    selectionTypeId = it.typeInfo.id,
                    jabaCount = count,
                    isEnabled = it.isEnabled,
                    selectionType = null
                )
            }

            val currentState = _uiState.value
            val result = currentState.editingApunteId?.let { localId ->
                repository.updateApunte(localId, currentState.observations, details)
            } ?: repository.createApunte(currentState.observations, details)
            result.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isSuccess = true,
                        items = if (state.editingApunteId == null) {
                            selectionTypes.map { ApunteItemState(typeInfo = it) }
                        } else {
                            state.items
                        },
                        observations = if (state.editingApunteId == null) "" else state.observations
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message ?: "Error al guardar apunte") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    private fun loadApunteForEdit(localId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getApunteById(localId)
                .onSuccess { apunte ->
                    val detailsByType = apunte.details.associateBy { it.selectionTypeId }
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            observations = apunte.observations.orEmpty(),
                            items = selectionTypes.map { typeInfo ->
                                val detail = detailsByType[typeInfo.id]
                                ApunteItemState(
                                    typeInfo = typeInfo,
                                    isEnabled = detail?.isEnabled ?: true,
                                    countInput = detail?.jabaCount?.takeIf { it > 0 }?.toString().orEmpty()
                                )
                            }
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "No se pudo cargar el apunte"
                        )
                    }
                }
        }
    }
}
