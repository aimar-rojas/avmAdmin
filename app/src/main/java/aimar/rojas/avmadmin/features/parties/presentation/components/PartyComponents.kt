package aimar.rojas.avmadmin.features.parties.presentation.components

import aimar.rojas.avmadmin.core.sync.SyncState
import aimar.rojas.avmadmin.domain.model.Party
import aimar.rojas.avmadmin.ui.components.AvmActionBottomSheet
import aimar.rojas.avmadmin.ui.components.AvmButtonSize
import aimar.rojas.avmadmin.ui.components.AvmFormBottomSheet
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import aimar.rojas.avmadmin.ui.components.AvmSecondaryButton
import aimar.rojas.avmadmin.ui.components.AvmSheetAction
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun PartySummaryCard(
    party: Party,
    roleLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = party.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = party.secondaryLine(roleLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SyncStateChip(syncState = party.syncState)
                    party.phone?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyActionsBottomSheet(
    party: Party,
    roleLabel: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    fun copyValue(label: String, value: String?) {
        val cleanValue = value?.takeIf { it.isNotBlank() } ?: return
        clipboardManager.setText(AnnotatedString(cleanValue))
        Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show()
    }

    AvmActionBottomSheet(
        title = party.displayName(),
        subtitle = roleLabel,
        onDismiss = onDismiss,
        actions = listOf(
            AvmSheetAction(
                title = "Editar datos",
                description = "Nombre, documentos, teléfono y cuenta",
                icon = Icons.Filled.Edit,
                onClick = onEdit
            ),
            AvmSheetAction(
                title = "Copiar teléfono",
                description = party.phone?.takeIf { it.isNotBlank() } ?: "Sin teléfono registrado",
                icon = Icons.Filled.Phone,
                enabled = !party.phone.isNullOrBlank(),
                onClick = { copyValue("Teléfono", party.phone) }
            ),
            AvmSheetAction(
                title = "Copiar cuenta",
                description = party.accountNumber?.takeIf { it.isNotBlank() } ?: "Sin cuenta registrada",
                icon = Icons.Filled.AccountBalance,
                enabled = !party.accountNumber.isNullOrBlank(),
                onClick = { copyValue("Cuenta", party.accountNumber) }
            ),
            AvmSheetAction(
                title = "Copiar nombre",
                description = "Para pegarlo en otra pantalla o mensaje",
                icon = Icons.Filled.ContentCopy,
                onClick = { copyValue("Nombre", party.displayName()) }
            )
        ),
        content = {
            PartyDetailsSection(party = party)
        },
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvmSecondaryButton(
                    text = "Cerrar",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                AvmPrimaryButton(
                    text = "Editar",
                    onClick = onEdit,
                    leadingIcon = Icons.Filled.Edit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    )
}

@Composable
private fun PartyDetailsSection(party: Party) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Datos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        DetailLine(
            label = "Nombre",
            value = listOf(party.firstName, party.lastName)
                .filterNot { it.isNullOrBlank() }
                .joinToString(" ")
        )
        DetailLine(label = "DNI", value = party.dni)
        DetailLine(label = "RUC", value = party.ruc)
        DetailLine(label = "Teléfono", value = party.phone)
        DetailLine(label = "Cuenta", value = party.accountNumber)
    }
}

@Composable
private fun DetailLine(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SyncStateChip(syncState: String?) {
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

private fun Party.displayName(): String {
    return aliasName?.takeIf { it.isNotBlank() }
        ?: listOf(firstName, lastName).filter { !it.isNullOrBlank() }.joinToString(" ")
            .ifBlank { "Sin nombre" }
}

private fun Party.secondaryLine(roleLabel: String): String {
    val document = ruc?.takeIf { it.isNotBlank() }?.let { "RUC $it" }
        ?: dni?.takeIf { it.isNotBlank() }?.let { "DNI $it" }
    return listOfNotNull(roleLabel, document).joinToString(" • ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePartyBottomSheet(
    uiState: PartyCreateUiState,
    partyRole: String,
    onDismiss: () -> Unit,
    onAliasNameChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDniChange: (String) -> Unit,
    onRucChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAccountNumberChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    val roleName = if (partyRole == "producer") "productor" else "comprador"
    val title = if (partyRole == "producer") "Nuevo productor" else "Nuevo comprador"

    AvmFormBottomSheet(
        title = title,
        subtitle = "Registra los datos principales para encontrarlo rápido después.",
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
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormSectionTitle("Datos principales")

                OutlinedTextField(
                    value = uiState.aliasName,
                    onValueChange = onAliasNameChange,
                    label = { Text("Nombre comercial") },
                    supportingText = { Text("Ejemplo: Fundo Santa Rosa, Miguel, Comercial AVM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = onFirstNameChange,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = onLastNameChange,
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                FormSectionTitle("Documento y contacto")

                OutlinedTextField(
                    value = uiState.dni,
                    onValueChange = onDniChange,
                    label = { Text("DNI") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.ruc,
                    onValueChange = onRucChange,
                    label = { Text("RUC") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = uiState.accountNumber,
                    onValueChange = onAccountNumberChange,
                    label = { Text("Nro de cuenta") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Se guardará como $roleName y quedará pendiente de sincronización si estás sin internet.",
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
    )
}

data class PartyCreateUiState(
    val aliasName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dni: String = "",
    val ruc: String = "",
    val phone: String = "",
    val accountNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@Composable
private fun FormSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

data class PartyEditUiState(
    val id: Int = 0,
    val aliasName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dni: String = "",
    val ruc: String = "",
    val phone: String = "",
    val accountNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPartyBottomSheet(
    uiState: PartyEditUiState,
    onDismiss: () -> Unit,
    onAliasNameChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDniChange: (String) -> Unit,
    onRucChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAccountNumberChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Editar Datos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            OutlinedTextField(
                value = uiState.aliasName,
                onValueChange = onAliasNameChange,
                label = { Text("Nombre comercial (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.firstName,
                onValueChange = onFirstNameChange,
                label = { Text("Nombre (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.lastName,
                onValueChange = onLastNameChange,
                label = { Text("Apellido (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.dni,
                onValueChange = onDniChange,
                label = { Text("DNI (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.ruc,
                onValueChange = onRucChange,
                label = { Text("RUC (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                label = { Text("Teléfono (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.accountNumber,
                onValueChange = onAccountNumberChange,
                label = { Text("Nro de cuenta (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSave,
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
                        Text("Guardar")
                    }
                }
            }
            // Espacio extra por los insets del navigation bar
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
