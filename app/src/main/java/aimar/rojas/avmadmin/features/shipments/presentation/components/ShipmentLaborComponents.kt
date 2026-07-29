package aimar.rojas.avmadmin.features.shipments.presentation.components

import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentLabor
import aimar.rojas.avmadmin.features.workers.domain.model.Worker
import aimar.rojas.avmadmin.ui.components.AvmButtonSize
import aimar.rojas.avmadmin.ui.components.AvmFormBottomSheet
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import aimar.rojas.avmadmin.ui.components.AvmSecondaryButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateShipmentLaborBottomSheet(
    workers: List<Worker>,
    selectedWorkerId: Int?,
    workDate: String,
    amount: String,
    notes: String,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onWorkerSelected: (Int) -> Unit,
    onWorkDateChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    AvmFormBottomSheet(
        title = "Agregar jornal",
        subtitle = "Pago diario manual para una persona dentro de este envío.",
        leadingIcon = Icons.Filled.Groups,
        onDismiss = onDismiss,
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvmSecondaryButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                )
                AvmPrimaryButton(
                    text = "Guardar",
                    onClick = onSave,
                    isLoading = isSaving,
                    loadingText = "Guardando",
                    leadingIcon = Icons.Filled.Save,
                    size = AvmButtonSize.Large,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) {
        Text(
            text = "Personal",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        if (workers.isEmpty()) {
            Text(
                text = "Primero registra personal desde el inicio de la app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                workers.forEach { worker ->
                    FilterChip(
                        selected = selectedWorkerId == worker.workerId,
                        onClick = { onWorkerSelected(worker.workerId) },
                        label = { Text(worker.fullName) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = workDate,
            onValueChange = onWorkDateChange,
            label = { Text("Fecha de trabajo") },
            supportingText = { Text("Formato: dd-MM-yyyy") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Pago del día") },
            prefix = { Text("S/ ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Nota") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Text(
            text = "Si no trabajó o no corresponde pago ese día, simplemente no registres jornal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ShipmentLaborItem(labor: ShipmentLabor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = labor.workerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = aimar.rojas.avmadmin.utils.DateUtils.convertApiToDisplayDate(labor.workDate) ?: labor.workDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                labor.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = String.format(Locale.getDefault(), "S/ %.2f", labor.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
