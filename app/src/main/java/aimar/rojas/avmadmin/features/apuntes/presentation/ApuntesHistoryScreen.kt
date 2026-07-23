package aimar.rojas.avmadmin.features.apuntes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte
import aimar.rojas.avmadmin.features.selections.presentation.SelectionTypeInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApuntesHistoryScreen(
    navController: NavController,
    viewModel: ApuntesHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Apuntes", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", modifier = Modifier.size(32.dp))
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.headlineSmall)
            }
        } else if (uiState.records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay apuntes registrados", style = MaterialTheme.typography.headlineSmall)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.records) { record: Apunte ->
                    ApunteHistoryCard(
                        record = record,
                        onClick = { navController.navigate("apuntes/${record.id}") }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ApunteHistoryCard(
    record: Apunte,
    onClick: () -> Unit
) {
    val selectionTypes = listOf(
        SelectionTypeInfo(1, "Sin pita"),
        SelectionTypeInfo(2, "Verde"),
        SelectionTypeInfo(3, "Blanco"),
        SelectionTypeInfo(4, "Rosado"),
        SelectionTypeInfo(5, "Naranja"),
        SelectionTypeInfo(6, "Azul"),
        SelectionTypeInfo(7, "Morado"),
        SelectionTypeInfo(8, "Amarillo")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Fecha: ${formatToTextDate(record.recordDate)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (record.syncState != SyncState.CLEAN) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = record.syncState.toReadableApunteSyncState(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (record.syncState.startsWith("FAILED")) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        containerColor = if (record.syncState.startsWith("FAILED")) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                )
            }
            
            if (!record.observations.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Obs: ${record.observations}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            record.details.filter { it.isEnabled }.forEach { detail ->
                val typeName = selectionTypes.find { it.id == detail.selectionTypeId }?.name ?: "Color ${detail.selectionTypeId}"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = getSelectionColor(detail.selectionTypeId),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = typeName, style = MaterialTheme.typography.headlineSmall)
                    }
                    Text(text = "${detail.jabaCount} Jabas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getSelectionColor(id: Int): androidx.compose.ui.graphics.Color {
    return when (id) {
        1 -> androidx.compose.ui.graphics.Color.Black      // Sin pita
        2 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
        3 -> androidx.compose.ui.graphics.Color(0xFFE0E0E0) // Blanco (Gris claro para visibilidad)
        4 -> androidx.compose.ui.graphics.Color(0xFFE91E63) // Rosado
        5 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Naranja
        6 -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Azul
        7 -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Morado
        8 -> androidx.compose.ui.graphics.Color(0xFFFFEB3B) // Amarillo
        else -> androidx.compose.ui.graphics.Color.Black
    }
}

private fun formatToTextDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr.take(10))
        val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'del' yyyy", Locale.forLanguageTag("es-ES"))
        date.format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}

private fun String.toReadableApunteSyncState(): String {
    return when (this) {
        SyncState.PENDING_CREATE -> "Pendiente de sincronizar"
        SyncState.FAILED_CREATE -> "Falló al sincronizar"
        SyncState.SYNCING -> "Sincronizando"
        SyncState.CONFLICT -> "Conflicto de sincronización"
        else -> "Pendiente de sincronizar"
    }
}
