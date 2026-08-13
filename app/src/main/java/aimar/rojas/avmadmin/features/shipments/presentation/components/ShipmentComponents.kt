package aimar.rojas.avmadmin.features.shipments.presentation.components

import aimar.rojas.avmadmin.domain.model.Shipment
import aimar.rojas.avmadmin.domain.model.Trade
import aimar.rojas.avmadmin.features.shipments.presentation.ShipmentsDetailUiState
import aimar.rojas.avmadmin.features.shipments.presentation.ShipmentsUiState
import aimar.rojas.avmadmin.ui.components.AvmButtonSize
import aimar.rojas.avmadmin.ui.components.AvmFormBottomSheet
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import aimar.rojas.avmadmin.ui.components.AvmSecondaryButton
import aimar.rojas.avmadmin.utils.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Date

@Composable
fun ShipmentCard(
    shipment: Shipment,
    onClick: () -> Unit = {}
) {
    val statusColor = when (shipment.status) {
        "OPEN" -> MaterialTheme.colorScheme.primary
        "CLOSED" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Envío #${shipment.shipmentId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = shipment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Inicio: ${DateUtils.formatToDisplayDate(shipment.startDate)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            shipment.endDate?.let { endDate ->
                Text(
                    text = "Fin: ${DateUtils.formatToDisplayDate(endDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShipmentBottomSheet(
    uiState: ShipmentsUiState,
    onDismiss: () -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCreate: () -> Unit,
    onStartDateSelected: (Date) -> Unit,
    onEndDateSelected: (Date) -> Unit,
    onShowStartDatePicker: () -> Unit,
    onHideStartDatePicker: () -> Unit,
    onShowEndDatePicker: () -> Unit,
    onHideEndDatePicker: () -> Unit
) {
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.pickerMillisFromApiDate(uiState.createStartDate)
            ?: System.currentTimeMillis()
    )

    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.pickerMillisFromApiDate(uiState.createEndDate)
            ?: System.currentTimeMillis()
    )

    if (uiState.showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = onHideStartDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val date = DateUtils.dateFromPickerMillis(millis)
                            onStartDateSelected(date)
                        }
                    }
                ) {
                    Text("Seleccionar")
                }
            },
            dismissButton = {
                TextButton(onClick = onHideStartDatePicker) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (uiState.showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = onHideEndDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            val date = DateUtils.dateFromPickerMillis(millis)
                            onEndDateSelected(date)
                        }
                    }
                ) {
                    Text("Seleccionar")
                }
            },
            dismissButton = {
                TextButton(onClick = onHideEndDatePicker) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    val startDateLabel = DateUtils.convertApiToDisplayDate(uiState.createStartDate) ?: "Seleccionar fecha"
    val endDateLabel = DateUtils.convertApiToDisplayDate(uiState.createEndDate) ?: "Seleccionar fecha"
    val isClosed = uiState.createStatus == "CLOSED"
    val previewText = if (isClosed) {
        "Se creará un envío cerrado del $startDateLabel al $endDateLabel."
    } else {
        "Se creará un envío abierto desde $startDateLabel."
    }

    AvmFormBottomSheet(
        title = "Nuevo envío",
        subtitle = "Define el periodo de trabajo antes de registrar compras o ventas.",
        leadingIcon = Icons.Filled.LocalShipping,
        onDismiss = onDismiss,
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvmSecondaryButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                )
                AvmPrimaryButton(
                    text = "Crear",
                    onClick = onCreate,
                    isLoading = uiState.isLoading,
                    loadingText = "Creando",
                    leadingIcon = Icons.Filled.Save,
                    size = AvmButtonSize.Large,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Estado del envío",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.createStatus == "OPEN",
                        onClick = { onStatusChange("OPEN") },
                        label = { Text("Abierto") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.createStatus == "CLOSED",
                        onClick = { onStatusChange("CLOSED") },
                        label = { Text("Cerrado") },
                        modifier = Modifier.weight(1f)
                    )
                }

                ShipmentDateField(
                    label = "Fecha de inicio",
                    value = uiState.createStartDate,
                    displayValue = startDateLabel,
                    onValueChange = onStartDateChange,
                    onOpenPicker = onShowStartDatePicker
                )

                if (uiState.createStatus == "CLOSED") {
                    ShipmentDateField(
                        label = "Fecha de fin",
                        value = uiState.createEndDate,
                        displayValue = endDateLabel,
                        onValueChange = onEndDateChange,
                        onOpenPicker = onShowEndDatePicker
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = previewText,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}

@Composable
private fun ShipmentDateField(
    label: String,
    value: String,
    displayValue: String,
    onValueChange: (String) -> Unit,
    onOpenPicker: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = displayValue.takeUnless { it == "Seleccionar fecha" } ?: "",
            onValueChange = onValueChange,
            label = { Text(label) },
            supportingText = { Text(displayValue) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        IconButton(
            onClick = onOpenPicker,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Seleccionar $label"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTradeBottomSheet(
    uiState: ShipmentsDetailUiState,
    onDismiss: () -> Unit,
    onPartySelected: (Int) -> Unit,
    onStartDatetimeChange: (String) -> Unit,
    onEndDatetimeChange: (String) -> Unit,
    onDiscountWeightChange: (String) -> Unit,
    onVarietyChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCreate: () -> Unit,
    onStartDatetimeSelected: (Date) -> Unit,
    onEndDatetimeSelected: (Date) -> Unit,
    onShowStartDateTimePicker: () -> Unit,
    onHideStartDateTimePicker: () -> Unit,
    onShowEndDateTimePicker: () -> Unit,
    onHideEndDateTimePicker: () -> Unit
) {
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.pickerMillisFromApiDate(uiState.createStartDatetime.substringBefore("T").takeIf { it.isNotBlank() })
            ?: System.currentTimeMillis()
    )

    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.pickerMillisFromApiDate(uiState.createEndDatetime.substringBefore("T").takeIf { it.isNotBlank() })
            ?: System.currentTimeMillis()
    )

    if (uiState.showStartDateTimePicker) {
        DatePickerDialog(
            onDismissRequest = onHideStartDateTimePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            onStartDatetimeSelected(DateUtils.dateFromPickerMillis(millis))
                        }
                    }
                ) { Text("Seleccionar") }
            },
            dismissButton = {
                TextButton(onClick = onHideStartDateTimePicker) { Text("Cancelar") }
            }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (uiState.showEndDateTimePicker) {
        DatePickerDialog(
            onDismissRequest = onHideEndDateTimePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { millis ->
                            onEndDatetimeSelected(DateUtils.dateFromPickerMillis(millis))
                        }
                    }
                ) { Text("Seleccionar") }
            },
            dismissButton = {
                TextButton(onClick = onHideEndDateTimePicker) { Text("Cancelar") }
            }
        ) { DatePicker(state = endDatePickerState) }
    }

    val focusManager = LocalFocusManager.current
    val isPurchase = uiState.createTradeType == "PURCHASE"
    val tradeLabel = if (isPurchase) "compra" else "venta"
    val title = if (isPurchase) "Nueva compra" else "Nueva venta"
    val partyLabel = if (isPurchase) "Productor" else "Comprador"
    val partiesList = remember(uiState.createTradeType, uiState.suppliers, uiState.clients) {
        if (isPurchase) uiState.suppliers else uiState.clients
    }
    val selectedParty = remember(partiesList, uiState.createPartyId) {
        partiesList.find { it.partyId == uiState.createPartyId }
    }
    val selectedPartyName = selectedParty?.aliasName
        ?: selectedParty?.let { listOf(it.firstName, it.lastName).filter { name -> !name.isNullOrBlank() }.joinToString(" ") }
        ?: ""
    val selectedStartDate = DateUtils.convertApiToDisplayDate(uiState.createStartDatetime.substringBefore("T")).orEmpty()
    val selectedEndDate = DateUtils.convertApiToDisplayDate(uiState.createEndDatetime.substringBefore("T")).orEmpty()
    val previewText = buildString {
        append(if (isPurchase) "Compra" else "Venta")
        if (uiState.createVarietyAvocado.isNotBlank()) {
            append(" de ")
            append(uiState.createVarietyAvocado)
        }
        if (selectedStartDate.isNotBlank()) {
            append(" desde ")
            append(selectedStartDate)
        }
        if (uiState.createStatus == "CLOSED" && selectedEndDate.isNotBlank()) {
            append(" hasta ")
            append(selectedEndDate)
        }
    }

    AvmFormBottomSheet(
        title = title,
        subtitle = "Registra los datos principales para continuar con selecciones, precios y pesas.",
        leadingIcon = Icons.Filled.ShoppingCart,
        onDismiss = onDismiss,
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvmSecondaryButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                )
                AvmPrimaryButton(
                    text = "Crear",
                    onClick = onCreate,
                    isLoading = uiState.isLoading,
                    loadingText = "Creando",
                    leadingIcon = Icons.Filled.Save,
                    size = AvmButtonSize.Large,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var partyExpanded by remember { mutableStateOf(false) }
            var varietyExpanded by remember { mutableStateOf(false) }
            val varieties = remember { listOf("Fuerte", "Hass", "Naval", "Villacampa", "Corriente") }

            FormSectionTitle("Contacto")
            ExposedDropdownMenuBox(
                expanded = partyExpanded,
                onExpandedChange = {
                    partyExpanded = !partyExpanded
                    if (partyExpanded) focusManager.clearFocus()
                }
            ) {
                OutlinedTextField(
                    value = selectedPartyName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(partyLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyExpanded)
                    },
                    supportingText = {
                        Text(if (partiesList.isEmpty()) "No hay ${partyLabel.lowercase()}es registrados" else "Selecciona el ${partyLabel.lowercase()} de esta $tradeLabel")
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.None)
                )
                ExposedDropdownMenu(
                    expanded = partyExpanded,
                    onDismissRequest = { partyExpanded = false }
                ) {
                    partiesList.forEach { party ->
                        val displayName = party.aliasName
                            ?: listOf(party.firstName, party.lastName).filter { !it.isNullOrBlank() }.joinToString(" ")
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(displayName) },
                            onClick = {
                                onPartySelected(party.partyId)
                                partyExpanded = false
                            }
                        )
                    }
                }
            }

            FormSectionTitle("Estado")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.createStatus == "OPEN",
                    onClick = { onStatusChange("OPEN") },
                    label = { Text("Nueva") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.createStatus == "CLOSED",
                    onClick = { onStatusChange("CLOSED") },
                    label = { Text("Pasada") },
                    modifier = Modifier.weight(1f)
                )
            }

            FormSectionTitle("Producto")
            ExposedDropdownMenuBox(
                expanded = varietyExpanded,
                onExpandedChange = {
                    varietyExpanded = !varietyExpanded
                    if (varietyExpanded) focusManager.clearFocus()
                }
            ) {
                OutlinedTextField(
                    value = uiState.createVarietyAvocado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Variedad") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = varietyExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.None)
                )
                ExposedDropdownMenu(
                    expanded = varietyExpanded,
                    onDismissRequest = { varietyExpanded = false }
                ) {
                    varieties.forEach { selectionOption ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                onVarietyChange(selectionOption)
                                varietyExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.createDiscountWeightPerTray,
                onValueChange = onDiscountWeightChange,
                label = { Text("Descuento por jaba") },
                suffix = { Text("kg") },
                supportingText = { Text("Kg descontados por cada jaba") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            FormSectionTitle("Fechas")
            TradeDateField(
                label = "Fecha de inicio",
                value = selectedStartDate,
                onOpenPicker = {
                    focusManager.clearFocus()
                    onShowStartDateTimePicker()
                }
            )

            if (uiState.createStatus == "CLOSED") {
                TradeDateField(
                    label = "Fecha de fin",
                    value = selectedEndDate,
                    onOpenPicker = {
                        focusManager.clearFocus()
                        onShowEndDateTimePicker()
                    }
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = previewText.ifBlank { "Completa los datos para crear la $tradeLabel." },
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FormSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun TradeDateField(
    label: String,
    value: String,
    onOpenPicker: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            readOnly = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        IconButton(
            onClick = onOpenPicker,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Seleccionar $label"
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TradeItem(
    trade: Trade,
    partyName: String,
    isPendingSync: Boolean = false,
    onSyncClick: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (trade.tradeType == "PURCHASE") "Compra" else "Venta",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (trade.tradeType == "PURCHASE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                
                if (isPendingSync) {
                    Surface(
                        color = Color(0xFFFFA500).copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = "Pendiente de Sincronización",
                                tint = Color(0xFFFFA500),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pendiente",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFA500),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = partyName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Variedad: ${trade.varietyAvocado}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            val startDate = DateUtils.convertApiToDisplayDate(trade.startDatetime.substringBefore("T")) ?: trade.startDatetime.substringBefore("T")
            val endDate = if (trade.endDatetime.isNotBlank()) {
                DateUtils.convertApiToDisplayDate(trade.endDatetime.substringBefore("T"))
            } else null
            
            val dateText = if (endDate != null) "$startDate - $endDate" else startDate
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
