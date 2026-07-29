package aimar.rojas.avmadmin.features.workers.presentation

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.workers.domain.WorkersRepository
import aimar.rojas.avmadmin.features.workers.domain.model.Worker
import aimar.rojas.avmadmin.features.workers.presentation.components.CreateWorkerBottomSheet
import aimar.rojas.avmadmin.features.workers.presentation.components.WorkerCreateUiState
import aimar.rojas.avmadmin.features.workers.presentation.components.WorkerSummaryCard
import aimar.rojas.avmadmin.ui.components.AvmCatalogHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun WorkersScreen(
    navController: NavController,
    viewModel: WorkersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AvmCatalogHeader(
                title = "Personal",
                subtitle = "Personas disponibles para registrar jornales diarios.",
                countLabel = "${uiState.total} registrados",
                statusLabel = uiState.pendingSyncLabel(),
                actionText = "Agregar personal",
                actionIcon = Icons.Filled.Add,
                leadingIcon = Icons.Filled.Groups,
                onBackClick = { navController.popBackStack() },
                onActionClick = { viewModel.showCreateSheet() }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading && uiState.workers.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    uiState.workers.isEmpty() -> {
                        Text(
                            text = "Aún no hay personal registrado",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.workers) { worker ->
                                WorkerSummaryCard(worker = worker)
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showCreateSheet) {
            CreateWorkerBottomSheet(
                uiState = uiState.createState,
                onDismiss = { viewModel.hideCreateSheet() },
                onFullNameChange = { viewModel.onFullNameChange(it) },
                onDniChange = { viewModel.onDniChange(it) },
                onPhoneChange = { viewModel.onPhoneChange(it) },
                onNotesChange = { viewModel.onNotesChange(it) },
                onCreate = { viewModel.createWorker() }
            )
        }
    }
}

data class WorkersUiState(
    val workers: List<Worker> = emptyList(),
    val isLoading: Boolean = false,
    val total: Int = 0,
    val showCreateSheet: Boolean = false,
    val createState: WorkerCreateUiState = WorkerCreateUiState()
)

private fun WorkersUiState.pendingSyncLabel(): String {
    val pendingCount = workers.count { it.syncState != null && it.syncState != SyncState.CLEAN }
    return if (pendingCount > 0) "$pendingCount pendientes" else "Sincronizado"
}

@HiltViewModel
class WorkersViewModel @Inject constructor(
    private val workersRepository: WorkersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkersUiState(isLoading = true))
    val uiState: StateFlow<WorkersUiState> = _uiState.asStateFlow()

    init {
        observeWorkers()
    }

    private fun observeWorkers() {
        viewModelScope.launch {
            workersRepository.observeWorkers().collect { workers ->
                _uiState.value = _uiState.value.copy(
                    workers = workers,
                    total = workers.size,
                    isLoading = false
                )
            }
        }
    }

    fun showCreateSheet() {
        _uiState.value = _uiState.value.copy(
            showCreateSheet = true,
            createState = WorkerCreateUiState()
        )
    }

    fun hideCreateSheet() {
        _uiState.value = _uiState.value.copy(showCreateSheet = false)
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(fullName = value))
    }

    fun onDniChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(dni = value))
    }

    fun onPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(phone = value))
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(notes = value))
    }

    fun createWorker() {
        val current = _uiState.value
        val create = current.createState
        if (create.fullName.isBlank()) {
            _uiState.value = current.copy(createState = create.copy(error = "Ingresa el nombre del personal"))
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(createState = create.copy(isLoading = true, error = null))
            workersRepository.createWorker(
                fullName = create.fullName,
                dni = create.dni.takeIf { it.isNotBlank() },
                phone = create.phone.takeIf { it.isNotBlank() },
                notes = create.notes.takeIf { it.isNotBlank() }
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        showCreateSheet = false,
                        createState = WorkerCreateUiState()
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        createState = create.copy(
                            isLoading = false,
                            error = exception.message ?: "No se pudo guardar el personal"
                        )
                    )
                }
        }
    }
}
