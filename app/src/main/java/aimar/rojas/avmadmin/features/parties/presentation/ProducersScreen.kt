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
import aimar.rojas.avmadmin.features.parties.presentation.components.EditPartyBottomSheet
import aimar.rojas.avmadmin.features.parties.presentation.components.PartyEditUiState
import androidx.compose.foundation.clickable
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
                                PartyCard(
                                    party = party,
                                    onClick = { viewModel.showEditBottomSheet(party) }
                                )
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
                onCreate = { viewModel.createParty() }
            )
        }

        if (uiState.showEditBottomSheet) {
            EditPartyBottomSheet(
                uiState = uiState.editState,
                onDismiss = { viewModel.hideEditBottomSheet() },
                onAliasNameChange = { viewModel.onEditAliasNameChange(it) },
                onFirstNameChange = { viewModel.onEditFirstNameChange(it) },
                onLastNameChange = { viewModel.onEditLastNameChange(it) },
                onDniChange = { viewModel.onEditDniChange(it) },
                onRucChange = { viewModel.onEditRucChange(it) },
                onPhoneChange = { viewModel.onEditPhoneChange(it) },
                onAccountNumberChange = { viewModel.onEditAccountNumberChange(it) },
                onSave = { viewModel.updateParty() }
            )
        }
    }
}

@Composable
private fun PartyCard(party: Party, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
    val createState: PartyCreateUiState = PartyCreateUiState(),
    val showEditBottomSheet: Boolean = false,
    val editState: PartyEditUiState = PartyEditUiState()
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

    fun onEditAliasNameChange(value: String) {
        _uiState.value = _uiState.value.copy(editState = _uiState.value.editState.copy(aliasName = value))
    }

    fun onEditFirstNameChange(value: String) {
        _uiState.value = _uiState.value.copy(editState = _uiState.value.editState.copy(firstName = value))
    }

    fun onEditLastNameChange(value: String) {
        _uiState.value = _uiState.value.copy(editState = _uiState.value.editState.copy(lastName = value))
    }

    fun onEditDniChange(value: String) {
        _uiState.value = _uiState.value.copy(editState = _uiState.value.editState.copy(dni = value))
    }

    fun onEditRucChange(value: String) {
        _uiState.value = _uiState.value.copy(editState = _uiState.value.editState.copy(ruc = value))
    }

    fun onEditPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(editState = _uiState.value.editState.copy(phone = value))
    }

    fun onEditAccountNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(editState = _uiState.value.editState.copy(accountNumber = value))
    }

    fun showEditBottomSheet(party: Party) {
        _uiState.value = _uiState.value.copy(
            showEditBottomSheet = true,
            editState = PartyEditUiState(
                id = party.partyId,
                aliasName = party.aliasName ?: "",
                firstName = party.firstName,
                lastName = party.lastName ?: "",
                dni = party.dni ?: "",
                ruc = party.ruc ?: "",
                phone = party.phone ?: "",
                accountNumber = party.accountNumber ?: ""
            )
        )
    }

    fun hideEditBottomSheet() {
        _uiState.value = _uiState.value.copy(showEditBottomSheet = false)
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
                aliasName = createState.aliasName.takeIf { it.isNotBlank() }
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

    fun updateParty() {
        val st = _uiState.value.editState
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                editState = st.copy(isLoading = true, error = null)
            )

            partiesRepository.updateParty(
                id = st.id,
                partyRole = "producer",
                aliasName = st.aliasName.takeIf { it.isNotBlank() },
                firstName = st.firstName.takeIf { it.isNotBlank() },
                lastName = st.lastName.takeIf { it.isNotBlank() },
                dni = st.dni.takeIf { it.isNotBlank() },
                ruc = st.ruc.takeIf { it.isNotBlank() },
                phone = st.phone.takeIf { it.isNotBlank() },
                accountNumber = st.accountNumber.takeIf { it.isNotBlank() }
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        showEditBottomSheet = false,
                        editState = PartyEditUiState()
                    )
                    loadProducers()
                }
                .onFailure { exception ->
                    val errorMsg = if (exception.message == "Party not found locally") {
                         "El productor fue sincronizado. Se ha recargado la lista."
                    } else exception.message ?: "Error al actualizar"
                    
                    _uiState.value = _uiState.value.copy(
                        editState = st.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                    )
                    
                    if (exception.message == "Party not found locally") {
                        loadProducers()
                    }
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

