package aimar.rojas.avmadmin.features.selections.presentation

import aimar.rojas.avmadmin.features.selections.domain.model.SelectionDetail
import aimar.rojas.avmadmin.features.selections.domain.model.UnitWeightDetail
import aimar.rojas.avmadmin.ui.theme.AVMAdminTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.util.Locale

@Composable
fun TradeSelectionsScreen(
    navController: NavController,
    tradeId: Int,
    viewModel: TradeSelectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TradeSelectionsContent(
        tradeId = tradeId,
        uiState = uiState,
        selectionTypes = viewModel.selectionTypes,
        onBackClick = { navController.popBackStack() },
        onRetryClick = { viewModel.loadSelections() },
        onSelectionTypeSelected = { viewModel.onSelectionTypeSelected(it) },
        onWeightChange = { viewModel.onWeightInputChange(it) },
        onAmountChange = { viewModel.onAmountInputChange(it) },
        onInsertClick = { viewModel.insertUnitWeight() },
        onDoneClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeSelectionsContent(
    tradeId: Int,
    uiState: TradeSelectionsUiState,
    selectionTypes: List<SelectionTypeInfo>,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSelectionTypeSelected: (Int) -> Unit,
    onWeightChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onInsertClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    val accentColor = getSelectionColor(uiState.selectedSelectionTypeId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trade #$tradeId - Selecciones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = accentColor,
                    titleContentColor = if (accentColor == Color.Black) Color.White else Color.Black,
                    navigationIconContentColor = if (accentColor == Color.Black) Color.White else Color.Black
                )
            )
        },
        bottomBar = {
            Button(
                onClick = onDoneClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Listo",
                    color = if (accentColor == Color.Black) Color.White else Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Información del Trade (Resumen)
            TradeSummaryHeader(
                totalWeight = uiState.totalWeight,
                totalAmount = uiState.totalAmount,
                accentColor = accentColor
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (uiState.error != null) {
                ErrorView(error = uiState.error, onRetry = onRetryClick)
            } else {
                // Selector de Tipo de Selección
                SelectionTypeSelector(
                    selectionTypes = selectionTypes,
                    selectedId = uiState.selectedSelectionTypeId,
                    onSelected = onSelectionTypeSelected,
                    accentColor = accentColor
                )

                // Formulario de Inserción
                UnitWeightInputForm(
                    weight = uiState.weightInput,
                    amount = uiState.amountInput,
                    onWeightChange = onWeightChange,
                    onAmountChange = onAmountChange,
                    onInsertClick = onInsertClick,
                    accentColor = accentColor
                )

                // Lista de UnitWeights para la selección actual
                val currentSelection = uiState.selections.find { it.selectionTypeId == uiState.selectedSelectionTypeId }
                val unitWeights = currentSelection?.unitWeights ?: emptyList()

                Text(
                    text = "Registros de Pesos",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = accentColor
                )

                if (unitWeights.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay pesos registrados para esta selección",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unitWeights) { unitWeight ->
                            UnitWeightItem(unitWeight = unitWeight, accentColor = accentColor)
                        }
                    }
                }
            }
        }
    }
}

private fun getSelectionColor(id: Int): Color {
    return when (id) {
        1 -> Color.Black      // Sin pita
        2 -> Color(0xFF4CAF50) // Verde
        3 -> Color(0xFFE0E0E0) // Blanco (Gris claro para visibilidad)
        4 -> Color(0xFFF44336) // Rojo
        5 -> Color(0xFF2196F3) // Azul
        6 -> Color(0xFF9C27B0) // Morado
        7 -> Color(0xFFFFEB3B) // Amarillo
        else -> Color.Black
    }
}

@Composable
fun TradeSummaryHeader(totalWeight: Double, totalAmount: Int, accentColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Peso Bruto Total", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = String.format(Locale.getDefault(), "%.2f kg", totalWeight),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (accentColor == Color(0xFFE0E0E0)) Color.Black else accentColor
                )
            }
            VerticalDivider(modifier = Modifier.height(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Total Jabas", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "$totalAmount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (accentColor == Color(0xFFE0E0E0)) Color.Black else accentColor
                )
            }
        }
    }
}

@Composable
fun SelectionTypeSelector(
    selectionTypes: List<SelectionTypeInfo>,
    selectedId: Int,
    onSelected: (Int) -> Unit,
    accentColor: Color
) {
    ScrollableTabRow(
        selectedTabIndex = selectionTypes.indexOfFirst { it.id == selectedId }.coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        contentColor = accentColor,
        divider = {}
    ) {
        selectionTypes.forEach { type ->
            Tab(
                selected = type.id == selectedId,
                onClick = { onSelected(type.id) },
                text = {
                    Text(
                        text = type.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (type.id == selectedId) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            )
        }
    }
}

@Composable
fun UnitWeightInputForm(
    weight: String,
    amount: String,
    onWeightChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onInsertClick: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("Cant. Jabas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            onClick = onInsertClick,
            modifier = Modifier.height(110.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Icon(
                Icons.Default.Add, 
                contentDescription = null,
                tint = if (accentColor == Color.Black) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Insertar",
                color = if (accentColor == Color.Black) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun UnitWeightItem(unitWeight: UnitWeightDetail, accentColor: Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${unitWeight.weight} kg",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = " x ${unitWeight.amount} jabas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = String.format(Locale.getDefault(), "%.2f kg", unitWeight.weight * unitWeight.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (accentColor == Color(0xFFE0E0E0)) Color.Black else accentColor
            )
        }
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = error, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TradeSelectionsPreview() {
    AVMAdminTheme {
        TradeSelectionsContent(
            tradeId = 123,
            uiState = TradeSelectionsUiState(
                isLoading = false,
                totalWeight = 450.5,
                totalAmount = 25,
                selectedSelectionTypeId = 2,
                selections = listOf(
                    SelectionDetail(
                        selectionByTradeId = 1,
                        tradeId = 123,
                        selectionTypeId = 2,
                        price = 15.5,
                        selectionTypeName = "Verde",
                        unitWeights = listOf(
                            UnitWeightDetail(1, 18.5, 10),
                            UnitWeightDetail(2, 19.0, 5)
                        )
                    )
                )
            ),
            selectionTypes = listOf(
                SelectionTypeInfo(1, "Sin pita"),
                SelectionTypeInfo(2, "Verde"),
                SelectionTypeInfo(3, "Blanco")
            ),
            onBackClick = {},
            onRetryClick = {},
            onSelectionTypeSelected = {},
            onWeightChange = {},
            onAmountChange = {},
            onInsertClick = {},
            onDoneClick = {}
        )
    }
}
