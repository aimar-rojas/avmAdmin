package aimar.rojas.avmadmin.features.accounting.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import aimar.rojas.avmadmin.ui.components.AvmSecondaryButton
import coil.compose.AsyncImage

val EXPENSE_CATEGORIES = listOf(
    "COMBUSTIBLE",
    "FLETE",
    "HERRAMIENTAS",
    "FERTILIZANTES",
    "MANTENIMIENTO",
    "SERVICIOS",
    "VIATICOS",
    "OTROS"
)

val DOCUMENT_TYPES = listOf(
    "FACTURA",
    "BOLETA",
    "TICKET",
    "RECIBO_HONORARIOS",
    "OTRO"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanExpenseInvoiceScreen(
    navController: NavController,
    viewModel: ExpenseInvoicesViewModel
) {
    val formState by viewModel.formState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Revisar Comprobante",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Vista previa de la imagen escaneada
            if (formState.scannedImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = formState.scannedImageUri,
                        contentDescription = "Foto del comprobante",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // 2. Banner de estado OCR
            if (formState.isOcrProcessing) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Extrayendo datos de la factura con OCR...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else if (formState.supplierRuc.isNotEmpty() || formState.totalAmount.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Datos pre-completados con OCR. Verifica antes de guardar.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // 3. Tipo de Comprobante
            Text(
                text = "Tipo de Comprobante",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DOCUMENT_TYPES.take(3).forEach { type ->
                    FilterChip(
                        selected = formState.documentType == type,
                        onClick = { viewModel.updateDocumentType(type) },
                        label = { Text(type) },
                        leadingIcon = if (formState.documentType == type) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // 4. Serie y Número
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = formState.series,
                    onValueChange = { viewModel.updateSeries(it) },
                    label = { Text("Serie (ej. F001)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = formState.number,
                    onValueChange = { viewModel.updateNumber(it) },
                    label = { Text("Número (ej. 000123)") },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true
                )
            }

            // 5. RUC y Proveedor
            OutlinedTextField(
                value = formState.supplierRuc,
                onValueChange = { viewModel.updateRuc(it) },
                label = { Text("RUC del Proveedor (11 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = formState.supplierName,
                onValueChange = { viewModel.updateSupplierName(it) },
                label = { Text("Razón Social / Proveedor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 6. Fecha de Emisión
            OutlinedTextField(
                value = formState.issueDate,
                onValueChange = { viewModel.updateIssueDate(it) },
                label = { Text("Fecha de Emisión (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 7. Categoría de Gasto
            Text(
                text = "Categoría de Gasto",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EXPENSE_CATEGORIES.take(4).forEach { cat ->
                        FilterChip(
                            selected = formState.category == cat,
                            onClick = { viewModel.updateCategory(cat) },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EXPENSE_CATEGORIES.drop(4).forEach { cat ->
                        FilterChip(
                            selected = formState.category == cat,
                            onClick = { viewModel.updateCategory(cat) },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // 8. Montos (Total, Subtotal, IGV)
            OutlinedTextField(
                value = formState.totalAmount,
                onValueChange = { viewModel.updateTotalAmount(it) },
                label = { Text("Importe Total (S/) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = formState.subtotal,
                    onValueChange = { viewModel.updateSubtotal(it) },
                    label = { Text("Subtotal (S/)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = formState.taxAmount,
                    onValueChange = { viewModel.updateTaxAmount(it) },
                    label = { Text("IGV 18% (S/)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // 9. Notas / Descripción
            OutlinedTextField(
                value = formState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Descripción / Observaciones") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Mensaje de Error
            if (formState.errorMessage != null) {
                Text(
                    text = formState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 10. Botones de Acción
            AvmPrimaryButton(
                text = "Guardar Factura",
                onClick = {
                    viewModel.submitInvoice {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isLoading = formState.isSubmitting,
                loadingText = "Guardando..."
            )

            AvmSecondaryButton(
                text = "Cancelar",
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
