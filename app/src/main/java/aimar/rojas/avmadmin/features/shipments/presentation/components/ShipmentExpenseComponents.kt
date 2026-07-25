package aimar.rojas.avmadmin.features.shipments.presentation.components

import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentExpense
import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentExpenseCategory
import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentExpenseSubcategory
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
import androidx.compose.material.icons.filled.AttachMoney
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateShipmentExpenseBottomSheet(
    category: String,
    subcategory: String?,
    amount: String,
    quantity: String,
    unitPrice: String,
    description: String,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onSubcategoryChange: (String?) -> Unit,
    onAmountChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit
) {
    AvmFormBottomSheet(
        title = "Agregar costo",
        subtitle = "Registra gastos operativos propios de este envío.",
        leadingIcon = Icons.Filled.AttachMoney,
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
            text = "Categoría",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShipmentExpenseCategory.all.forEach { option ->
                FilterChip(
                    selected = category == option,
                    onClick = { onCategoryChange(option) },
                    label = { Text(expenseCategoryLabel(option)) }
                )
            }
        }

        if (category == ShipmentExpenseCategory.VIATIC) {
            Text(
                text = "Tipo de viático",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShipmentExpenseSubcategory.viaticOptions.forEach { option ->
                    FilterChip(
                        selected = subcategory == option,
                        onClick = { onSubcategoryChange(option) },
                        label = { Text(expenseSubcategoryLabel(option)) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Monto") },
            prefix = { Text("S/ ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = quantity,
                onValueChange = onQuantityChange,
                label = { Text("Cantidad") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = unitPrice,
                onValueChange = onUnitPriceChange,
                label = { Text("Precio unit.") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Detalle") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Text(
            text = "Este costo queda asociado sólo a este envío y pendiente de sincronización.",
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
fun ShipmentExpenseItem(expense: ShipmentExpense) {
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
                    text = expenseCategoryLabel(expense.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                expense.subcategory?.let {
                    Text(
                        text = expenseSubcategoryLabel(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                expense.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = String.format(Locale.getDefault(), "S/ %.2f", expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun expenseCategoryLabel(category: String): String {
    return when (category) {
        ShipmentExpenseCategory.LABOR -> "Personal / jornal"
        ShipmentExpenseCategory.FUEL -> "Combustible"
        ShipmentExpenseCategory.FREIGHT -> "Flete"
        ShipmentExpenseCategory.STEVEDORE -> "Estivadores"
        ShipmentExpenseCategory.VIATIC -> "Viáticos"
        ShipmentExpenseCategory.TAXI -> "Taxis"
        else -> "Otros"
    }
}

fun expenseSubcategoryLabel(subcategory: String): String {
    return when (subcategory) {
        ShipmentExpenseSubcategory.BREAKFAST -> "Desayuno"
        ShipmentExpenseSubcategory.LUNCH -> "Almuerzo"
        ShipmentExpenseSubcategory.DINNER -> "Cena"
        ShipmentExpenseSubcategory.SNACK -> "Refrigerio"
        ShipmentExpenseSubcategory.LODGING -> "Hospedaje"
        else -> "Otro"
    }
}
