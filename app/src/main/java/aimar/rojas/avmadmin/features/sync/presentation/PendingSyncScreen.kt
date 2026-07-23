package aimar.rojas.avmadmin.features.sync.presentation

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingSyncScreen(
    navController: NavController,
    viewModel: PendingSyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val failedCount = uiState.items.count { it.hasError }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos pendientes") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Surface(
                    tonalElevation = 3.dp
                ) {
                    AvmPrimaryButton(
                        text = "Reintentar sincronización",
                        onClick = viewModel::retrySync,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        enabled = !uiState.isSyncing,
                        isLoading = uiState.isSyncing,
                        loadingText = "Sincronizando...",
                        leadingIcon = Icons.Filled.CloudSync
                    )
                }
            }
        }
    ) { padding ->
        when {
            uiState.items.isEmpty() -> EmptyPendingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PendingSummaryCard(
                        totalCount = uiState.items.size,
                        failedCount = failedCount
                    )
                }

                items(
                    items = uiState.items,
                    key = { it.id }
                ) { item ->
                    PendingSyncItemCard(item = item)
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyPendingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CloudDone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "No hay datos pendientes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Todo lo que está en este dispositivo ya fue sincronizado.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PendingSummaryCard(
    totalCount: Int,
    failedCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (failedCount > 0) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (failedCount > 0) "Hay datos con error" else "Hay datos pendientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (failedCount > 0) {
                    "$failedCount de $totalCount necesitan atención o reintento."
                } else {
                    "$totalCount cambios esperan sincronización."
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun PendingSyncItemCard(
    item: PendingSyncItem
) {
    val containerColor = if (item.hasError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (item.hasError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (item.hasError) Icons.Filled.Error else Icons.Filled.PendingActions,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(30.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.entityLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor.copy(alpha = 0.78f)
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }
            }

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )

            Text(
                text = item.syncState.toReadableSyncState(),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.82f)
            )

            item.lastAttemptAt?.let { lastAttempt ->
                Text(
                    text = "Último intento: $lastAttempt",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.82f)
                )
            }

            if (!item.error.isNullOrBlank()) {
                Text(
                    text = "Error: ${item.error}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun String.toReadableSyncState(): String {
    return when (this) {
        "PENDING_CREATE" -> "Pendiente: crear en la nube"
        "PENDING_UPDATE" -> "Pendiente: actualizar en la nube"
        "FAILED_CREATE" -> "Falló al crear en la nube"
        "FAILED_UPDATE" -> "Falló al actualizar en la nube"
        SyncState.CONFLICT -> "Conflicto: cambió aquí y en la nube"
        SyncState.SYNCING -> "Sincronizando ahora"
        else -> this
    }
}
