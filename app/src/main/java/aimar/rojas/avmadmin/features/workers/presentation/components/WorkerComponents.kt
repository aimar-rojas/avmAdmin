package aimar.rojas.avmadmin.features.workers.presentation.components

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.features.workers.domain.model.Worker
import aimar.rojas.avmadmin.ui.components.AvmButtonSize
import aimar.rojas.avmadmin.ui.components.AvmFormBottomSheet
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import aimar.rojas.avmadmin.ui.components.AvmSecondaryButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

data class WorkerCreateUiState(
    val fullName: String = "",
    val dni: String = "",
    val phone: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@Composable
fun WorkerSummaryCard(
    worker: Worker,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = worker.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = listOfNotNull(
                        worker.dni?.takeIf { it.isNotBlank() }?.let { "DNI $it" },
                        worker.phone?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank { "Sin documento ni teléfono" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WorkerSyncChip(syncState = worker.syncState)
            }
        }
    }
}

@Composable
private fun WorkerSyncChip(syncState: String?) {
    val label = when (syncState) {
        SyncState.PENDING_CREATE -> "Por subir"
        SyncState.PENDING_UPDATE -> "Cambios pendientes"
        SyncState.FAILED_CREATE, SyncState.FAILED_UPDATE -> "Reintentar sync"
        SyncState.SYNCING -> "Sincronizando"
        else -> "Sincronizado"
    }
    val containerColor = when (syncState) {
        SyncState.PENDING_CREATE, SyncState.PENDING_UPDATE -> MaterialTheme.colorScheme.tertiaryContainer
        SyncState.FAILED_CREATE, SyncState.FAILED_UPDATE -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (syncState) {
        SyncState.FAILED_CREATE, SyncState.FAILED_UPDATE -> MaterialTheme.colorScheme.onErrorContainer
        SyncState.PENDING_CREATE, SyncState.PENDING_UPDATE -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkerBottomSheet(
    uiState: WorkerCreateUiState,
    onDismiss: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onDniChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    AvmFormBottomSheet(
        title = "Nuevo personal",
        subtitle = "Registra a la persona para asignarle jornales por día en cada envío.",
        leadingIcon = Icons.Filled.PersonAdd,
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
                    text = "Guardar",
                    onClick = onCreate,
                    isLoading = uiState.isLoading,
                    loadingText = "Guardando",
                    leadingIcon = Icons.Filled.Save,
                    size = AvmButtonSize.Large,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) {
        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = onFullNameChange,
            label = { Text("Nombre completo") },
            leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Badge, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.dni,
                onValueChange = onDniChange,
                label = { Text("DNI") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                label = { Text("Teléfono") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChange,
            label = { Text("Nota") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Text(
            text = "El pago no se fija aquí. Cada jornal se registra manualmente por día dentro de un envío.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
