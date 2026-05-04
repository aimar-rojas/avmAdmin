package aimar.rojas.avmadmin.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.core.sync.ManualSyncManager
import aimar.rojas.avmadmin.core.sync.SyncStatus
import aimar.rojas.avmadmin.data.local.SessionDataStore
import aimar.rojas.avmadmin.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val sessionDataStore: SessionDataStore,
    private val manualSyncManager: ManualSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUser()
        observeSyncStatus()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = sessionDataStore.getUser()
            _uiState.update { it.copy(user = user) }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutSuccess()
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            manualSyncManager.status.collect { status ->
                _uiState.update { it.copy(syncStatus = status) }
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            manualSyncManager.syncNow()
        }
    }
}

data class HomeUiState(
    val user: aimar.rojas.avmadmin.domain.model.User? = null,
    val syncStatus: SyncStatus = SyncStatus()
)
