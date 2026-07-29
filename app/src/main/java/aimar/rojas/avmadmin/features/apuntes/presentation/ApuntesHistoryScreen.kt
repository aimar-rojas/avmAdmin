package aimar.rojas.avmadmin.features.apuntes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.apuntes.domain.model.Apunte

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApuntesHistoryScreen(
    navController: NavController,
    viewModel: ApuntesHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var recordToShare by remember { mutableStateOf<Apunte?>(null) }

    recordToShare?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToShare = null },
            title = { Text("Compartir apunte") },
            text = { Text("¿Quieres compartir este apunte por WhatsApp como imagen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        shareApunteCardToWhatsApp(context, record)
                        recordToShare = null
                    }
                ) {
                    Text("Compartir")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToShare = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                        onClick = { navController.navigate("apuntes/${record.id}") },
                        onLongClick = { recordToShare = record }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApunteHistoryCard(
    record: Apunte,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val enabledDetails = record.details.filter { it.isEnabled && it.jabaCount > 0 }
    val detailsByType = enabledDetails.associateBy { it.selectionTypeId }
    val totalJabas = enabledDetails.sumOf { it.jabaCount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatToTextDate(record.recordDate),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!record.observations.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = record.observations,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            ApunteStatusChip(syncState = record.syncState)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ApunteSelectionDefaults.orderedTypes.forEach { type ->
                val detail = detailsByType[type.id] ?: return@forEach
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    color = ApunteSelectionDefaults.colorFor(type.id),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = type.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = detail.jabaCount.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalJabas jabas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ApunteStatusChip(syncState: String) {
    val isClean = syncState == SyncState.CLEAN
    val hasError = syncState.startsWith("FAILED")
    val containerColor = when {
        isClean -> MaterialTheme.colorScheme.secondaryContainer
        hasError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = when {
        isClean -> MaterialTheme.colorScheme.onSecondaryContainer
        hasError -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor
    ) {
        Text(
            text = syncState.toReadableApunteSyncState(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatToTextDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr.take(10))
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        date.format(formatter)
    } catch (e: Exception) {
        dateStr
    }
}

private fun String.toReadableApunteSyncState(): String {
    return when (this) {
        SyncState.CLEAN -> "Sincronizado"
        SyncState.PENDING_CREATE -> "Pendiente de sincronizar"
        SyncState.PENDING_UPDATE -> "Cambios pendientes"
        SyncState.FAILED_CREATE -> "Falló al sincronizar"
        SyncState.FAILED_UPDATE -> "Falló al actualizar"
        SyncState.SYNCING -> "Sincronizando"
        SyncState.CONFLICT -> "Conflicto de sincronización"
        else -> "Pendiente de sincronizar"
    }
}
