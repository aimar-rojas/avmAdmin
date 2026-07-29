package aimar.rojas.avmadmin.features.shipments.presentation

import aimar.rojas.avmadmin.domain.model.Trade
import aimar.rojas.avmadmin.features.shipments.presentation.components.CreateTradeDialog
import aimar.rojas.avmadmin.features.shipments.presentation.components.CreateShipmentLaborBottomSheet
import aimar.rojas.avmadmin.features.shipments.presentation.components.CreateShipmentExpenseBottomSheet
import aimar.rojas.avmadmin.features.shipments.presentation.components.ShipmentExpenseItem
import aimar.rojas.avmadmin.features.shipments.presentation.components.ShipmentLaborItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.features.shipments.presentation.components.TradeItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentsDetailScreen(
    navController: NavController,
    viewModel: ShipmentsDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Envío") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.error != null -> {
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
                            Button(onClick = { viewModel.loadTrades() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                ShipmentControlSummary(uiState = uiState)
                            }

                            item {
                                ShipmentSectionHeader(
                                    title = "Compras",
                                    subtitle = "${uiState.purchases.size} registradas",
                                    buttonText = "Agregar compra",
                                    onClick = { viewModel.showCreateDialog("PURCHASE") }
                                )
                            }
                            if (uiState.purchases.isEmpty()) {
                                item { EmptySectionText("Aún no hay compras en este envío") }
                            } else {
                                items(uiState.purchases) { trade ->
                                    val partyName = uiState.partyNameForTrade(trade)
                                    TradeItem(
                                        trade = trade,
                                        partyName = partyName,
                                        isPendingSync = uiState.pendingSyncTradeIds.contains(trade.tradeId),
                                        onSyncClick = { viewModel.syncTrade(trade.tradeId) },
                                        onClick = { navController.navigate("trade_selections/${trade.tradeId}") }
                                    )
                                }
                            }

                            item {
                                ShipmentSectionHeader(
                                    title = "Ventas",
                                    subtitle = "${uiState.sales.size} registradas",
                                    buttonText = "Agregar venta",
                                    onClick = { viewModel.showCreateDialog("SALE") }
                                )
                            }
                            if (uiState.sales.isEmpty()) {
                                item { EmptySectionText("Aún no hay ventas en este envío") }
                            } else {
                                items(uiState.sales) { trade ->
                                    val partyName = uiState.partyNameForTrade(trade)
                                    TradeItem(
                                        trade = trade,
                                        partyName = partyName,
                                        isPendingSync = uiState.pendingSyncTradeIds.contains(trade.tradeId),
                                        onSyncClick = { viewModel.syncTrade(trade.tradeId) },
                                        onClick = { navController.navigate("trade_selections/${trade.tradeId}") }
                                    )
                                }
                            }

                            item {
                                ShipmentSectionHeader(
                                    title = "Jornales",
                                    subtitle = "${uiState.labor.size} pagos registrados",
                                    buttonText = "Agregar jornal",
                                    onClick = { viewModel.showCreateLaborSheet() }
                                )
                            }
                            if (uiState.labor.isEmpty()) {
                                item { EmptySectionText("Registra pagos diarios por persona cuando haya trabajo en este envío") }
                            } else {
                                items(uiState.labor) { labor ->
                                    ShipmentLaborItem(labor = labor)
                                }
                            }

                            item {
                                ShipmentSectionHeader(
                                    title = "Costos operativos",
                                    subtitle = "${uiState.expenses.size} registrados",
                                    buttonText = "Agregar costo",
                                    onClick = { viewModel.showCreateExpenseSheet() }
                                )
                            }
                            if (uiState.expenses.isEmpty()) {
                                item { EmptySectionText("Registra combustible, flete, estibadores, viáticos, taxis u otros") }
                            } else {
                                items(uiState.expenses) { expense ->
                                    ShipmentExpenseItem(expense = expense)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            CreateTradeDialog(
                uiState = uiState,
                onDismiss = { viewModel.hideCreateDialog() },
                onPartySelected = { viewModel.onPartySelected(it) },
                onStartDatetimeChange = { viewModel.onStartDatetimeChange(it) },
                onEndDatetimeChange = { viewModel.onEndDatetimeChange(it) },
                onDiscountWeightChange = { viewModel.onDiscountWeightChange(it) },
                onVarietyChange = { viewModel.onVarietyAvocadoChange(it) },
                onStatusChange = { viewModel.onCreateStatusChange(it) },
                onCreate = { viewModel.createTrade() },
                onStartDatetimeSelected = { viewModel.onStartDatetimeSelected(it) },
                onEndDatetimeSelected = { viewModel.onEndDatetimeSelected(it) },
                onShowStartDateTimePicker = { viewModel.showStartDateTimePicker() },
                onHideStartDateTimePicker = { viewModel.hideStartDateTimePicker() },
                onShowEndDateTimePicker = { viewModel.showEndDateTimePicker() },
                onHideEndDateTimePicker = { viewModel.hideEndDateTimePicker() }
            )
        }

        if (uiState.showCreateExpenseSheet) {
            CreateShipmentExpenseBottomSheet(
                category = uiState.expenseCategory,
                subcategory = uiState.expenseSubcategory,
                amount = uiState.expenseAmount,
                quantity = uiState.expenseQuantity,
                unitPrice = uiState.expenseUnitPrice,
                description = uiState.expenseDescription,
                isSaving = uiState.isSavingExpense,
                error = uiState.expenseError,
                onDismiss = { viewModel.hideCreateExpenseSheet() },
                onCategoryChange = { viewModel.onExpenseCategoryChange(it) },
                onSubcategoryChange = { viewModel.onExpenseSubcategoryChange(it) },
                onAmountChange = { viewModel.onExpenseAmountChange(it) },
                onQuantityChange = { viewModel.onExpenseQuantityChange(it) },
                onUnitPriceChange = { viewModel.onExpenseUnitPriceChange(it) },
                onDescriptionChange = { viewModel.onExpenseDescriptionChange(it) },
                onSave = { viewModel.createExpense() }
            )
        }

        if (uiState.showCreateLaborSheet) {
            CreateShipmentLaborBottomSheet(
                workers = uiState.workers,
                selectedWorkerId = uiState.laborWorkerId,
                workDate = uiState.laborDate,
                amount = uiState.laborAmount,
                notes = uiState.laborNotes,
                isSaving = uiState.isSavingLabor,
                error = uiState.laborError,
                onDismiss = { viewModel.hideCreateLaborSheet() },
                onWorkerSelected = { viewModel.onLaborWorkerSelected(it) },
                onWorkDateChange = { viewModel.onLaborDateChange(it) },
                onAmountChange = { viewModel.onLaborAmountChange(it) },
                onNotesChange = { viewModel.onLaborNotesChange(it) },
                onSave = { viewModel.createLabor() }
            )
        }
    }
}

@Composable
private fun ShipmentControlSummary(uiState: ShipmentsDetailUiState) {
    val laborTotal = uiState.labor.sumOf { it.amount }
    val costsTotal = uiState.expenses.sumOf { it.amount } + laborTotal
    val pendingCount = uiState.pendingSyncTradeIds.size +
        uiState.expenses.count { it.syncState != aimar.rojas.avmadmin.core.sync.SyncState.CLEAN } +
        uiState.labor.count { it.syncState != aimar.rojas.avmadmin.core.sync.SyncState.CLEAN }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Centro de control del envío",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetric("Compras", uiState.purchases.size.toString(), Modifier.weight(1f))
                SummaryMetric("Ventas", uiState.sales.size.toString(), Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetric("Costos", String.format(Locale.getDefault(), "S/ %.2f", costsTotal), Modifier.weight(1f))
                SummaryMetric("Pendientes", pendingCount.toString(), Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetric("Jornales", String.format(Locale.getDefault(), "S/ %.2f", laborTotal), Modifier.weight(1f))
                SummaryMetric("Personal", uiState.labor.map { it.workerId }.distinct().size.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ShipmentSectionHeader(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FilledTonalButton(onClick = onClick) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(buttonText)
        }
    }
}

@Composable
private fun EmptySectionText(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ShipmentsDetailUiState.partyNameForTrade(trade: Trade): String {
    val parties = if (trade.tradeType == "PURCHASE") suppliers else clients
    val party = parties.find { it.partyId == trade.partyId }
    return party?.let { it.aliasName ?: "${it.firstName} ${it.lastName ?: ""}".trim() } ?: "Desconocido"
}
