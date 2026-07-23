package aimar.rojas.avmadmin.features.parties.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.domain.model.Party
import aimar.rojas.avmadmin.features.parties.presentation.components.CreatePartyBottomSheet
import aimar.rojas.avmadmin.features.parties.presentation.components.PartyCreateUiState
import aimar.rojas.avmadmin.features.parties.presentation.components.EditPartyBottomSheet
import aimar.rojas.avmadmin.features.parties.presentation.components.PartyActionsBottomSheet
import aimar.rojas.avmadmin.features.parties.presentation.components.PartyEditUiState
import aimar.rojas.avmadmin.features.parties.presentation.components.PartySummaryCard
import aimar.rojas.avmadmin.ui.components.AvmCatalogHeader
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.features.parties.domain.PartiesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    navController: NavController,
    viewModel: PurchasesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AvmCatalogHeader(
                title = "Compradores",
                subtitle = "Clientes y negocios a quienes se vende mercadería.",
                countLabel = "${uiState.total} registrados",
                statusLabel = uiState.pendingSyncLabel(),
                actionText = "Agregar comprador",
                actionIcon = Icons.Filled.Add,
                leadingIcon = Icons.Filled.ShoppingCart,
                onBackClick = { navController.popBackStack() },
                onActionClick = { viewModel.showCreateDialog() }
            )

            Box(modifier = Modifier.fillMaxSize()) {
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
                                text = "No hay compradores registrados",
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
                                PartySummaryCard(
                                    party = party,
                                    roleLabel = "Comprador",
                                    onClick = { viewModel.showActionsBottomSheet(party) }
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        if (uiState.showCreateDialog) {
            CreatePartyBottomSheet(
                uiState = uiState.createState,
                partyRole = "buyer",
                onDismiss = { viewModel.hideCreateDialog() },
                onAliasNameChange = { viewModel.onAliasNameChange(it) },
                onFirstNameChange = { viewModel.onCreateFirstNameChange(it) },
                onLastNameChange = { viewModel.onCreateLastNameChange(it) },
                onDniChange = { viewModel.onCreateDniChange(it) },
                onRucChange = { viewModel.onCreateRucChange(it) },
                onPhoneChange = { viewModel.onCreatePhoneChange(it) },
                onAccountNumberChange = { viewModel.onCreateAccountNumberChange(it) },
                onCreate = { viewModel.createParty() }
            )
        }

        uiState.selectedParty?.let { party ->
            if (uiState.showActionsBottomSheet) {
                PartyActionsBottomSheet(
                    party = party,
                    roleLabel = "Comprador",
                    onDismiss = { viewModel.hideActionsBottomSheet() },
                    onEdit = { viewModel.showEditBottomSheetFromActions(party) }
                )
            }
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

data class PurchasesUiState(
    val parties: List<Party> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val total: Int = 0,
    val showCreateDialog: Boolean = false,
    val createState: PartyCreateUiState = PartyCreateUiState(),
    val showEditBottomSheet: Boolean = false,
    val editState: PartyEditUiState = PartyEditUiState(),
    val selectedParty: Party? = null,
    val showActionsBottomSheet: Boolean = false
)

private fun PurchasesUiState.pendingSyncLabel(): String {
    val pendingCount = parties.count { it.syncState != null && it.syncState != aimar.rojas.avmadmin.core.sync.SyncState.CLEAN }
    return if (pendingCount > 0) "$pendingCount pendientes" else "Sincronizado"
}

@HiltViewModel
class PurchasesViewModel @Inject constructor(
    private val partiesRepository: PartiesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchasesUiState())
    val uiState: StateFlow<PurchasesUiState> = _uiState.asStateFlow()

    init {
        loadBuyers()
    }

    fun loadBuyers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            partiesRepository.getParties(partyRole = "buyer")
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
                        error = exception.message ?: "Error al cargar compradores"
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

    fun onCreateFirstNameChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(firstName = value))
    }

    fun onCreateLastNameChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(lastName = value))
    }

    fun onCreateDniChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(dni = value))
    }

    fun onCreateRucChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(ruc = value))
    }

    fun onCreatePhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(phone = value))
    }

    fun onCreateAccountNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(createState = _uiState.value.createState.copy(accountNumber = value))
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

    fun showActionsBottomSheet(party: Party) {
        _uiState.value = _uiState.value.copy(
            selectedParty = party,
            showActionsBottomSheet = true
        )
    }

    fun hideActionsBottomSheet() {
        _uiState.value = _uiState.value.copy(showActionsBottomSheet = false)
    }

    fun showEditBottomSheetFromActions(party: Party) {
        _uiState.value = _uiState.value.copy(showActionsBottomSheet = false)
        showEditBottomSheet(party)
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
                partyRole = "buyer",
                aliasName = createState.aliasName.takeIf { it.isNotBlank() },
                firstName = createState.firstName.takeIf { it.isNotBlank() },
                lastName = createState.lastName.takeIf { it.isNotBlank() },
                dni = createState.dni.takeIf { it.isNotBlank() },
                ruc = createState.ruc.takeIf { it.isNotBlank() },
                phone = createState.phone.takeIf { it.isNotBlank() },
                accountNumber = createState.accountNumber.takeIf { it.isNotBlank() }
            )
                .onSuccess {
                    _uiState.value = currentState.copy(
                        showCreateDialog = false,
                        createState = PartyCreateUiState()
                    )
                    loadBuyers()
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        createState = createState.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al crear comprador"
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
                partyRole = "buyer",
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
                    loadBuyers()
                }
                .onFailure { exception ->
                    val errorMsg = if (exception.message == "Party not found locally") {
                         "El comprador fue sincronizado. Se ha recargado la lista."
                    } else exception.message ?: "Error al actualizar"
                    
                    _uiState.value = _uiState.value.copy(
                        editState = st.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                    )
                    
                    if (exception.message == "Party not found locally") {
                        loadBuyers()
                    }
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
