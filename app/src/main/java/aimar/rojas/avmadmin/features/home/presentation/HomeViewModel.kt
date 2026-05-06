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
        syncNow() // Auto-sync on app open
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = sessionDataStore.getUser()
            _uiState.update { it.copy(user = user) }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit, onLogoutError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            val status = manualSyncManager.status.value
            val totalPending = status.summary.partyPending + status.summary.shipmentPending + 
                               status.summary.tradePending + status.summary.selectionPending
            
            if (totalPending > 0) {
                val syncResult = manualSyncManager.syncNow()
                if (syncResult.state == "success") {
                    authRepository.logout()
                    onLogoutSuccess()
                } else {
                    _uiState.update { it.copy(isLoggingOut = false) }
                    onLogoutError("No se puede cerrar sesión. Tienes datos pendientes de sincronizar y la sincronización falló. Por favor, conéctate a internet e inténtalo de nuevo.")
                }
            } else {
                authRepository.logout()
                onLogoutSuccess()
            }
        }
    }

    fun clearLogoutError() {
        _uiState.update { it.copy(logoutError = null) }
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
    val syncStatus: SyncStatus = SyncStatus(),
    val logoutError: String? = null,
    val isLoggingOut: Boolean = false
)
