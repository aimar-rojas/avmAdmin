package aimar.rojas.avmadmin.features.parties.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.domain.model.Party
import aimar.rojas.avmadmin.features.parties.presentation.components.CreatePartyDialog
import aimar.rojas.avmadmin.features.parties.presentation.components.PartyCreateUiState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.features.parties.domain.PartiesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProducersScreen(
    navController: NavController,
    viewModel: ProducersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productores") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar productor")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.parties.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                uiState.error != null && uiState.parties.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    if (uiState.parties.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No hay productores registrados",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.parties) { party ->
                                PartyCard(party = party)
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            CreatePartyDialog(
                uiState = uiState.createState,
                partyRole = "producer",
                onDismiss = { viewModel.hideCreateDialog() },
                onAliasNameChange = { viewModel.onAliasNameChange(it) },
                onFirstNameChange = { viewModel.onFirstNameChange(it) },
                onLastNameChange = { viewModel.onLastNameChange(it) },
                onDniChange = { viewModel.onDniChange(it) },
                onRucChange = { viewModel.onRucChange(it) },
                onPhoneChange = { viewModel.onPhoneChange(it) },
                onCreate = { viewModel.createParty() }
            )
        }
    }
}

@Composable
private fun PartyCard(party: Party) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = party.aliasName ?: "${party.firstName} ${party.lastName.orEmpty()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!party.firstName.isNullOrBlank() || !party.lastName.isNullOrBlank()) {
                Text(
                    text = listOfNotNull(party.firstName, party.lastName).joinToString(" "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            listOfNotNull(
                party.dni?.let { "DNI: $it" },
                party.ruc?.let { "RUC: $it" },
                party.phone?.let { "Tel: $it" }
            ).forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class PartiesUiState(
    val parties: List<Party> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val total: Int = 0,
    val showCreateDialog: Boolean = false,
    val createState: PartyCreateUiState = PartyCreateUiState()
)

@HiltViewModel
class ProducersViewModel @Inject constructor(
    private val partiesRepository: PartiesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartiesUiState())
    val uiState: StateFlow<PartiesUiState> = _uiState.asStateFlow()

    init {
        loadProducers()
    }

    fun loadProducers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            partiesRepository.getParties(partyRole = "producer")
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        parties = result.parties,
                        total = result.total
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar productores"
                    )
                }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = true,
            createState = PartyCreateUiState()
        )
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun onAliasNameChange(aliasName: String) {
        _uiState.value = _uiState.value.copy(
            createState = _uiState.value.createState.copy(aliasName = aliasName)
        )
    }

    fun onFirstNameChange(firstName: String) {
        _uiState.value = _uiState.value.copy(
            createState = _uiState.value.createState.copy(firstName = firstName)
        )
    }

    fun onLastNameChange(lastName: String) {
        _uiState.value = _uiState.value.copy(
            createState = _uiState.value.createState.copy(lastName = lastName)
        )
    }

    fun onDniChange(dni: String) {
        _uiState.value = _uiState.value.copy(
            createState = _uiState.value.createState.copy(dni = dni)
        )
    }

    fun onRucChange(ruc: String) {
        _uiState.value = _uiState.value.copy(
            createState = _uiState.value.createState.copy(ruc = ruc)
        )
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(
            createState = _uiState.value.createState.copy(phone = phone)
        )
    }

    fun createParty() {
        val currentState = _uiState.value
        val createState = currentState.createState

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                createState = createState.copy(isLoading = true, error = null)
            )

            partiesRepository.createParty(
                partyRole = "producer",
                aliasName = createState.aliasName.takeIf { it.isNotBlank() },
                firstName = createState.firstName.takeIf { it.isNotBlank() },
                lastName = createState.lastName.takeIf { it.isNotBlank() },
                dni = createState.dni.takeIf { it.isNotBlank() },
                ruc = createState.ruc.takeIf { it.isNotBlank() },
                phone = createState.phone.takeIf { it.isNotBlank() }
            )
                .onSuccess {
                    _uiState.value = currentState.copy(
                        showCreateDialog = false,
                        createState = PartyCreateUiState()
                    )
                    loadProducers()
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        createState = createState.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al crear productor"
                        )
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

