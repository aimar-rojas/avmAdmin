package aimar.rojas.avmadmin.features.shipments.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.features.shipments.presentation.components.CreateShipmentDialog
import aimar.rojas.avmadmin.features.shipments.presentation.components.ShipmentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentsScreen(
    navController: NavController,
    viewModel: ShipmentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Envíos / Cargas") },
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
                Icon(Icons.Default.Add, contentDescription = "Agregar envío")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.shipments.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                uiState.error != null && uiState.shipments.isEmpty() -> {
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadShipments() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    if (uiState.shipments.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No hay envíos registrados",
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
                            items(uiState.shipments) { shipment ->
                                ShipmentCard(shipment = shipment)
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            CreateShipmentDialog(
                uiState = uiState,
                onDismiss = { viewModel.hideCreateDialog() },
                onStartDateChange = { viewModel.onCreateStartDateChange(it) },
                onEndDateChange = { viewModel.onCreateEndDateChange(it) },
                onStatusChange = { viewModel.onCreateStatusChange(it) },
                onCreate = { viewModel.createShipment() },
                onStartDateSelected = { viewModel.onStartDateSelected(it) },
                onEndDateSelected = { viewModel.onEndDateSelected(it) },
                onShowStartDatePicker = { viewModel.showStartDatePicker() },
                onHideStartDatePicker = { viewModel.hideStartDatePicker() },
                onShowEndDatePicker = { viewModel.showEndDatePicker() },
                onHideEndDatePicker = { viewModel.hideEndDatePicker() }
            )
        }

        uiState.error?.let { error ->
            if (!uiState.showCreateDialog) {
                LaunchedEffect(error) {
                    // El error se mostrará en la UI principal
                }
            }
        }
    }
}

