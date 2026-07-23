package aimar.rojas.avmadmin.features.apuntes.presentation

import aimar.rojas.avmadmin.ui.components.AvmButtonSize
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApuntesScreen(
    navController: NavController,
    viewModel: ApuntesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = uiState.editingApunteId != null
    val totalJabas = uiState.items
        .filter { it.isEnabled }
        .sumOf { it.countInput.toIntOrNull() ?: 0 }

    LaunchedEffect(uiState.isSuccess, isEditing) {
        if (uiState.isSuccess && isEditing) {
            viewModel.clearSuccess()
            navController.navigateUp()
        }
    }

    if (uiState.isSuccess && !isEditing) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccess() },
            title = { Text("Éxito", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    if (isEditing) "Apunte actualizado correctamente." else "Apunte guardado correctamente.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSuccess() }) {
                    Text("OK", style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error) },
            text = { Text(uiState.error!!, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK", style = MaterialTheme.typography.bodyLarge)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Editar Apunte" else "Nuevo Apunte",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", modifier = Modifier.size(32.dp))
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { navController.navigate("apuntes_history") }) {
                            Icon(Icons.Filled.History, contentDescription = "Historial", modifier = Modifier.size(32.dp))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.items) { item: ApunteItemState ->
                    ApunteSelectionInputCard(
                        item = item,
                        onEnabledChanged = { enabled ->
                            viewModel.toggleItemEnabled(item.typeInfo.id, enabled)
                        },
                        onCountChanged = { count ->
                            viewModel.onCountChanged(item.typeInfo.id, count)
                        },
                        onIncrement = {
                            viewModel.incrementCount(item.typeInfo.id)
                        },
                        onDecrement = {
                            viewModel.decrementCount(item.typeInfo.id)
                        }
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = uiState.observations,
                        onValueChange = { viewModel.onObservationsChanged(it) },
                        label = { Text("Observaciones (Opcional)", style = MaterialTheme.typography.titleLarge) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        minLines = 3
                    )
                }

                item {
                    TotalSummary(totalJabas = totalJabas)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            AvmPrimaryButton(
                text = if (isEditing) "ACTUALIZAR APUNTE" else "GUARDAR APUNTE",
                onClick = { viewModel.saveApunte() },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading,
                size = AvmButtonSize.Large
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ApunteSelectionInputCard(
    item: ApunteItemState,
    onEnabledChanged: (Boolean) -> Unit,
    onCountChanged: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val count = item.countInput.toIntOrNull() ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isEnabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(
                            color = ApunteSelectionDefaults.colorFor(item.typeInfo.id),
                            shape = CircleShape
                        )
                )

                Text(
                    text = item.typeInfo.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = onEnabledChanged
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onDecrement,
                    enabled = item.isEnabled && count > 0,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Restar")
                }

                OutlinedTextField(
                    value = item.countInput,
                    onValueChange = onCountChanged,
                    enabled = item.isEnabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        color = if (item.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "0",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    suffix = {
                        Text(
                            text = "jabas",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )

                FilledTonalIconButton(
                    onClick = onIncrement,
                    enabled = item.isEnabled,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Sumar")
                }
            }
        }
    }
}

@Composable
private fun TotalSummary(totalJabas: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$totalJabas jabas",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
