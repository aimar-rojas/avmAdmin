package aimar.rojas.avmadmin.features.shipments.presentation.components

import aimar.rojas.avmadmin.domain.model.Shipment
import aimar.rojas.avmadmin.domain.model.Trade
import aimar.rojas.avmadmin.features.shipments.presentation.ShipmentsDetailUiState
import aimar.rojas.avmadmin.features.shipments.presentation.ShipmentsUiState
import aimar.rojas.avmadmin.utils.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
fun CreateShipmentDialog(
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
        initialSelectedDateMillis = uiState.createStartDate.takeIf { it.isNotBlank() }
            ?.let { DateUtils.parseApiDate(it)?.time }
            ?: System.currentTimeMillis()
    )

    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.createEndDate.takeIf { it.isNotBlank() }
            ?.let { DateUtils.parseApiDate(it)?.time }
            ?: System.currentTimeMillis()
    )

    if (uiState.showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = onHideStartDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
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
                            val date = Date(millis)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nuevo Envío / Carga",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tipo de envío",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.createStatus == "OPEN",
                        onClick = { onStatusChange("OPEN") },
                        label = { Text("Nuevo") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = uiState.createStatus == "CLOSED",
                        onClick = { onStatusChange("CLOSED") },
                        label = { Text("Pasado") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.createStartDate,
                        onValueChange = onStartDateChange,
                        label = { Text("Fecha de inicio (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    IconButton(
                        onClick = onShowStartDatePicker,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = "Seleccionar fecha de inicio"
                        )
                    }
                }
                
                if (uiState.createStatus == "CLOSED") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.createEndDate,
                            onValueChange = onEndDateChange,
                            label = { Text("Fecha de fin (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        IconButton(
                            onClick = onShowEndDatePicker,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Seleccionar fecha de fin"
                            )
                        }
                    }
                }
                
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTradeDialog(
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
        initialSelectedDateMillis = uiState.createStartDatetime.takeIf { it.isNotBlank() }
            ?.let { DateUtils.parseApiDate(it.substringBefore("T"))?.time }
            ?: System.currentTimeMillis()
    )

    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.createEndDatetime.takeIf { it.isNotBlank() }
            ?.let { DateUtils.parseApiDate(it.substringBefore("T"))?.time }
            ?: System.currentTimeMillis()
    )

    if (uiState.showStartDateTimePicker) {
        DatePickerDialog(
            onDismissRequest = onHideStartDateTimePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { millis ->
                            onStartDatetimeSelected(Date(millis))
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
                            onEndDatetimeSelected(Date(millis))
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
                    text = if (uiState.createTradeType == "PURCHASE") "Nueva Compra" else "Nueva Venta",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Party selection Dropdown
                var expanded by remember { mutableStateOf(false) }
                val partiesList = remember(uiState.createTradeType, uiState.suppliers, uiState.clients) {
                    if (uiState.createTradeType == "PURCHASE") uiState.suppliers else uiState.clients
                }
                val selectedParty = remember(partiesList, uiState.createPartyId) {
                    partiesList.find { it.partyId == uiState.createPartyId }
                }
                val labelParty = if (uiState.createTradeType == "PURCHASE") "Proveedor" else "Cliente"
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { 
                        expanded = !expanded
                        if (expanded) focusManager.clearFocus() 
                    }
                ) {
                    OutlinedTextField(
                        value = selectedParty?.aliasName ?: selectedParty?.firstName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(labelParty) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.None)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        partiesList.forEach { party ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(party.aliasName ?: "${party.firstName} ${party.lastName}") },
                                onClick = {
                                    onPartySelected(party.partyId)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Status
                Text("Estado", style = MaterialTheme.typography.labelLarge)
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

                // Variety and Discount (Row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var varietyExpanded by remember { mutableStateOf(false) }
                    val varieties = remember { listOf("Fuerte", "Hass", "Naval", "Villacampa", "Corriente") }
                    
                    ExposedDropdownMenuBox(
                        expanded = varietyExpanded,
                        onExpandedChange = { 
                            varietyExpanded = !varietyExpanded 
                            if (varietyExpanded) focusManager.clearFocus()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.createVarietyAvocado,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Variedad") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
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
                        label = { Text("Desc Peso") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.createStartDatetime.substringBefore("T"),
                        onValueChange = onStartDatetimeChange,
                        label = { Text("Inicio") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        readOnly = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.None)
                    )
                    IconButton(onClick = { 
                        focusManager.clearFocus()
                        onShowStartDateTimePicker() 
                    }) {
                        Icon(Icons.Filled.CalendarToday, "Fecha inicio")
                    }
                }

                if (uiState.createStatus == "CLOSED") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.createEndDatetime.substringBefore("T"),
                            onValueChange = onEndDatetimeChange,
                            label = { Text("Fin") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            readOnly = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.None)
                        )
                        IconButton(onClick = { 
                            focusManager.clearFocus()
                            onShowEndDateTimePicker() 
                        }) {
                            Icon(Icons.Filled.CalendarToday, "Fecha fin")
                        }
                    }
                }

                if (uiState.error != null) {
                    Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                
                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onCreate,
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Crear")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TradeItem(trade: Trade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "ID: ${trade.tradeId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Monto: S/ ${String.format("%.2f", trade.amountPerTrade)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Peso desc: ${trade.discountWeightPerTray} kg/bandeja",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = trade.startDatetime.substringBefore("T"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
