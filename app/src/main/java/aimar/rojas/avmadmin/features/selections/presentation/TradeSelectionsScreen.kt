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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
        allSelectionTypes = viewModel.selectionTypes,
        visibleSelectionTypes = viewModel.getVisibleSelectionTypes(),
        onBackClick = { navController.popBackStack() },
        onRetryClick = { viewModel.loadSelections() },
        onSelectionTypeSelected = { viewModel.onSelectionTypeSelected(it) },
        onWeightChange = { viewModel.onWeightInputChange(it) },
        onAmountChange = { viewModel.onAmountInputChange(it) },
        onInsertClick = { viewModel.insertUnitWeight() },
        onCancelEditClick = { viewModel.cancelUnitWeightEdit() },
        onUnitWeightClick = { viewModel.startUnitWeightEdit(it) },
        onDeleteUnitWeight = { viewModel.deleteUnitWeight(it) },
        onDoneClick = { navController.navigate("trade_summary/$tradeId") },
        onShowSelectionManager = { viewModel.showSelectionManagerDialog() },
        onHideSelectionManager = { viewModel.hideSelectionManagerDialog() },
        onToggleSelectionVisibility = { viewModel.toggleSelectionVisibility(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeSelectionsContent(
    tradeId: Int,
    uiState: TradeSelectionsUiState,
    allSelectionTypes: List<SelectionTypeInfo>,
    visibleSelectionTypes: List<SelectionTypeInfo>,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSelectionTypeSelected: (Int) -> Unit,
    onWeightChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onInsertClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onUnitWeightClick: (UnitWeightDetail) -> Unit,
    onDeleteUnitWeight: (UnitWeightDetail) -> Unit,
    onDoneClick: () -> Unit,
    onShowSelectionManager: () -> Unit,
    onHideSelectionManager: () -> Unit,
    onToggleSelectionVisibility: (Int) -> Unit
) {
    val accentColor = selectionColorFor(uiState.selectedSelectionTypeId)
    var unitWeightToDelete by remember { mutableStateOf<UnitWeightDetail?>(null) }

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
                totalGrossWeight = uiState.totalGrossWeight,
                totalNetWeight = uiState.totalNetWeight,
                totalAmount = uiState.totalAmount,
                discountWeightPerTray = uiState.discountWeightPerTray,
                accentColor = accentColor
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (uiState.error != null) {
                ErrorView(error = uiState.error, onRetry = onRetryClick)
            } else {
                SelectionTypeSelector(
                    selectionTypes = visibleSelectionTypes,
                    selectedId = uiState.selectedSelectionTypeId,
                    onSelected = onSelectionTypeSelected,
                    accentColor = accentColor,
                    onManageClick = onShowSelectionManager
                )

                if (uiState.showSelectionManagerDialog) {
                    SelectionManagerDialog(
                        allSelectionTypes = allSelectionTypes,
                        visibleSelectionTypeIds = uiState.visibleSelectionTypeIds,
                        onDismiss = onHideSelectionManager,
                        onToggleVisibility = onToggleSelectionVisibility
                    )
                }

                // Formulario de Inserción
                UnitWeightInputForm(
                    weight = uiState.weightInput,
                    amount = uiState.amountInput,
                    discountWeightPerTray = uiState.discountWeightPerTray,
                    onWeightChange = onWeightChange,
                    onAmountChange = onAmountChange,
                    onInsertClick = onInsertClick,
                    onCancelEditClick = onCancelEditClick,
                    isEditing = uiState.editingUnitWeightId != null,
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
                            UnitWeightItem(
                                unitWeight = unitWeight,
                                discountWeightPerTray = uiState.discountWeightPerTray,
                                accentColor = accentColor,
                                isEditing = unitWeight.unitWeightId == uiState.editingUnitWeightId,
                                onClick = { onUnitWeightClick(unitWeight) },
                                onLongPress = { unitWeightToDelete = unitWeight }
                            )
                        }
                    }
                }
            }
        }
    }

    unitWeightToDelete?.let { unitWeight ->
        DeleteUnitWeightBottomSheet(
            unitWeight = unitWeight,
            discountWeightPerTray = uiState.discountWeightPerTray,
            onDismiss = { unitWeightToDelete = null },
            onConfirm = {
                onDeleteUnitWeight(unitWeight)
                unitWeightToDelete = null
            }
        )
    }
}

@Composable
fun TradeSummaryHeader(
    totalGrossWeight: Double,
    totalNetWeight: Double,
    totalAmount: Int,
    discountWeightPerTray: Double,
    accentColor: Color
) {
    val isDarkMode = isSystemInDarkTheme()
    val textColor = when {
        isLightSelectionColor(accentColor) -> Color.Black
        accentColor == Color.Black && isDarkMode -> Color.White
        else -> accentColor
    }
    
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
                Text(text = "Peso Neto Total", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = String.format(Locale.getDefault(), "%.2f kg", totalNetWeight),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = String.format(Locale.getDefault(), "Bruto %.2f kg", totalGrossWeight),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VerticalDivider(modifier = Modifier.height(40.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Total Jabas", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "$totalAmount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = String.format(Locale.getDefault(), "Desc. %.2f kg c/u", discountWeightPerTray),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    accentColor: Color,
    onManageClick: () -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectionTypes.indexOfFirst { it.id == selectedId }.coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = accentColor,
            divider = {},
            modifier = Modifier.weight(1f)
        ) {
            selectionTypes.forEach { type ->
                val tabTextColor = if (type.id == selectedId) {
                    if (type.id == 1 && accentColor == Color.Black && isDarkMode) {
                        Color.White
                    } else {
                        accentColor
                    }
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
                
                Tab(
                    selected = type.id == selectedId,
                    onClick = { onSelected(type.id) },
                    text = {
                        Text(
                            text = type.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = tabTextColor
                        )
                    }
                )
            }
        }

        IconButton(
            onClick = onManageClick,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Gestionar selecciones",
                tint = accentColor
            )
        }
    }
}

@Composable
fun UnitWeightInputForm(
    weight: String,
    amount: String,
    discountWeightPerTray: Double,
    onWeightChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onInsertClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    isEditing: Boolean,
    accentColor: Color
) {
    val weightFocusRequester = remember { FocusRequester() }
    val buttonContentColor = selectionButtonContentColor(accentColor)

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
                label = { Text("Peso bruto (kg)") },
                supportingText = {
                    Text("Se descontará ${String.format(Locale.getDefault(), "%.2f", discountWeightPerTray)} kg por jaba")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(weightFocusRequester)
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
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onInsertClick()
                    weightFocusRequester.requestFocus()
                },
                modifier = Modifier.height(if (isEditing) 64.dp else 110.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    tint = buttonContentColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isEditing) "Guardar" else "Insertar",
                    color = buttonContentColor
                )
            }
            if (isEditing) {
                OutlinedButton(
                    onClick = {
                        onCancelEditClick()
                        weightFocusRequester.requestFocus()
                    },
                    modifier = Modifier.height(38.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
fun UnitWeightItem(
    unitWeight: UnitWeightDetail,
    discountWeightPerTray: Double,
    accentColor: Color,
    isEditing: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val discount = unitWeight.amount * discountWeightPerTray
    val netWeight = (unitWeight.weight - discount).coerceAtLeast(0.0)
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isEditing) accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(unitWeight.unitWeightId, unitWeight.weight, unitWeight.amount) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f kg bruto", unitWeight.weight),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " - ${unitWeight.amount} jabas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = String.format(Locale.getDefault(), "Descuento: %.2f kg", discount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = String.format(Locale.getDefault(), "%.2f kg neto", netWeight),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isLightSelectionColor(accentColor)) Color.Black else accentColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteUnitWeightBottomSheet(
    unitWeight: UnitWeightDetail,
    discountWeightPerTray: Double,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val discount = unitWeight.amount * discountWeightPerTray
    val netWeight = (unitWeight.weight - discount).coerceAtLeast(0.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Eliminar peso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = String.format(
                    Locale.getDefault(),
                    "%.2f kg bruto - %d jabas - %.2f kg neto",
                    unitWeight.weight,
                    unitWeight.amount,
                    netWeight
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

private fun selectionButtonContentColor(accentColor: Color): Color {
    return if (accentColor == Color.Black) Color.White else Color.Black
}

@Composable
fun SelectionManagerDialog(
    allSelectionTypes: List<SelectionTypeInfo>,
    visibleSelectionTypeIds: Set<Int>,
    onDismiss: () -> Unit,
    onToggleVisibility: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Gestionar Selecciones",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Activa o desactiva los tipos de selección que deseas trabajar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(400.dp)
                ) {
                    items(allSelectionTypes) { selectionType ->
                        SelectionTypeToggleItem(
                            selectionType = selectionType,
                            isVisible = visibleSelectionTypeIds.contains(selectionType.id),
                            onToggle = { onToggleVisibility(selectionType.id) },
                            canDisable = visibleSelectionTypeIds.size > 1
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Cerrar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionTypeToggleItem(
    selectionType: SelectionTypeInfo,
    isVisible: Boolean,
    onToggle: () -> Unit,
    canDisable: Boolean
) {
    val selectionColor = selectionColorFor(selectionType.id, selectionType.name)
    val canToggle = !isVisible || canDisable
    
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isVisible) {
            selectionColor.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = selectionColor,
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp)
                ) {
                    if (isVisible) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (selectionColor == Color.Black) {
                                    Color.White
                                } else {
                                    Color.Black
                                },
                                modifier = Modifier.width(16.dp)
                            )
                        }
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectionType.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isVisible) FontWeight.Bold else FontWeight.Normal,
                        color = if (isVisible) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                    Text(
                        text = if (isVisible) "Visible en tabs" else "Oculta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Switch(
                checked = isVisible,
                onCheckedChange = { if (canToggle) onToggle() },
                enabled = canToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = selectionColor,
                    checkedTrackColor = selectionColor.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
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
                totalGrossWeight = 450.5,
                totalNetWeight = 400.5,
                totalAmount = 25,
                discountWeightPerTray = 2.0,
                selectedSelectionTypeId = 2,
                selections = listOf(
                    SelectionDetail(
                        selectionByTradeId = 1,
                        tradeId = 123,
                        selectionTypeId = 2,
                        price = 15.5,
                        selectionTypeName = "Verde",
                        unitWeights = listOf(
                            UnitWeightDetail(unitWeightId = 1, weight = 18.5, amount = 10),
                            UnitWeightDetail(unitWeightId = 2, weight = 19.0, amount = 5)
                        )
                    )
                )
            ),
            allSelectionTypes = listOf(
                SelectionTypeInfo(1, "Sin pita"),
                SelectionTypeInfo(2, "Verde"),
                SelectionTypeInfo(3, "Blanco")
            ),
            visibleSelectionTypes = listOf(
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
            onCancelEditClick = {},
            onUnitWeightClick = {},
            onDeleteUnitWeight = {},
            onDoneClick = {},
            onShowSelectionManager = {},
            onHideSelectionManager = {},
            onToggleSelectionVisibility = {}
        )
    }
}
