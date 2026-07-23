package aimar.rojas.avmadmin.features.shipments.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalShipping
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
import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.ui.components.AvmCatalogHeader
import aimar.rojas.avmadmin.features.shipments.presentation.components.CreateShipmentBottomSheet
import aimar.rojas.avmadmin.features.shipments.presentation.components.ShipmentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentsScreen(
    navController: NavController,
    viewModel: ShipmentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AvmCatalogHeader(
                title = "Envíos",
                subtitle = "Cargas abiertas y cerradas para organizar operaciones.",
                countLabel = "${uiState.total} registrados",
                statusLabel = uiState.pendingSyncLabel(),
                actionText = "Nuevo envío",
                actionIcon = Icons.Filled.Add,
                leadingIcon = Icons.Filled.LocalShipping,
                onBackClick = { navController.popBackStack() },
                onActionClick = { viewModel.showCreateDialog() }
            )

            Box(modifier = Modifier.fillMaxSize()) {
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
                                ShipmentCard(
                                    shipment = shipment,
                                    onClick = { navController.navigate("shipments_detail/${shipment.shipmentId}") }
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        if (uiState.showCreateDialog) {
            CreateShipmentBottomSheet(
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

private fun ShipmentsUiState.pendingSyncLabel(): String {
    val pendingCount = shipments.count { it.syncState != null && it.syncState != SyncState.CLEAN }
    return if (pendingCount > 0) "$pendingCount pendientes" else "Sincronizado"
}
