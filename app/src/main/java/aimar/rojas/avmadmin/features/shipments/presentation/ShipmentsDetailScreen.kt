package aimar.rojas.avmadmin.features.shipments.presentation

import aimar.rojas.avmadmin.features.shipments.presentation.components.CreateTradeDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.features.shipments.presentation.components.TradeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentsDetailScreen(
    navController: NavController,
    viewModel: ShipmentsDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Compras", "Ventas")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Envío") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

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
                        val isPurchaseTab = selectedTabIndex == 0
                        val currentList = if (isPurchaseTab) uiState.purchases else uiState.sales
                        val tradeType = if (isPurchaseTab) "PURCHASE" else "SALE"
                        val buttonText = if (isPurchaseTab) "Agregar nueva compra" else "Agregar nueva venta"
                        
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (currentList.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay ${tabs[selectedTabIndex].lowercase()} registradas",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(currentList) { trade ->
                                        TradeItem(trade = trade)
                                    }
                                }
                            }
                            
                            Button(
                                onClick = { viewModel.showCreateDialog(tradeType) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(buttonText)
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
    }
}
