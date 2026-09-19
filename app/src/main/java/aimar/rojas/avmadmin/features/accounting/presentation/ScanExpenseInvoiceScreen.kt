package aimar.rojas.avmadmin.features.accounting.presentation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import aimar.rojas.avmadmin.ui.components.AvmButtonSize
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import aimar.rojas.avmadmin.ui.components.AvmSecondaryButton
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ExpenseCategoryItem(
    val code: String,
    val label: String,
    val icon: ImageVector
)

val EXPENSE_CATEGORY_ITEMS = listOf(
    ExpenseCategoryItem("COMBUSTIBLE", "Combustible y Petróleo", Icons.Default.LocalGasStation),
    ExpenseCategoryItem("FLETE", "Flete y Transporte", Icons.Default.LocalShipping),
    ExpenseCategoryItem("HERRAMIENTAS", "Herramientas y Envases", Icons.Default.Handyman),
    ExpenseCategoryItem("FERTILIZANTES", "Fertilizantes y Agroquímicos", Icons.Default.Agriculture),
    ExpenseCategoryItem("MANTENIMIENTO", "Mantenimiento y Reparación", Icons.Default.Build),
    ExpenseCategoryItem("SERVICIOS", "Servicios Públicos (Luz/Agua)", Icons.Default.ElectricBolt),
    ExpenseCategoryItem("VIATICOS", "Alimentación y Viáticos", Icons.Default.Restaurant),
    ExpenseCategoryItem("OTROS", "Otros Gastos Operativos", Icons.AutoMirrored.Filled.ReceiptLong)
)

val DOCUMENT_TYPE_ITEMS = listOf(
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
    var showFullscreenImage by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var docTypeDropdownExpanded by remember { mutableStateOf(false) }

    // Diálogo de Fecha Nativo
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = millis
                        }
                        viewModel.updateIssueDate(sdf.format(utcCal.time))
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Modal de Imagen a Pantalla Completa (Zoom / Inspección)
    if (showFullscreenImage && formState.scannedImageUri != null) {
        Dialog(
            onDismissRequest = { showFullscreenImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
            ) {
                AsyncImage(
                    model = formState.scannedImageUri,
                    contentDescription = "Comprobante en alta resolución",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { showFullscreenImage = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Revisar Comprobante",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Valida o ajusta los datos antes de guardar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (formState.errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = formState.errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AvmSecondaryButton(
                            text = "Descartar",
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.weight(1f)
                        )

                        AvmPrimaryButton(
                            text = "Guardar Factura",
                            onClick = {
                                viewModel.submitInvoice {
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.weight(1.6f),
                            isLoading = formState.isSubmitting,
                            loadingText = "Guardando...",
                            leadingIcon = Icons.Default.Save
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ==========================================
            // 1. HERO PREVIEW DE LA IMAGEN + ESTADO OCR
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (formState.scannedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .clickable { showFullscreenImage = true },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = formState.scannedImageUri,
                                contentDescription = "Foto del comprobante",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            // Botón de Lupa flotante
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Ampliar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Ver Completo",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Estado OCR
                    if (formState.isOcrProcessing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Analizando texto y extrayendo montos con OCR...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (formState.supplierRuc.isNotEmpty() || formState.totalAmount.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Datos detectados con OCR. Toca la foto para cotejar.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. SECCIÓN: DATOS DEL COMPROBANTE & PROVEEDOR
            // ==========================================
            Text(
                text = "1. Comprobante y Proveedor",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tipo de Comprobante (Dropdown limpio)
                    ExposedDropdownMenuBox(
                        expanded = docTypeDropdownExpanded,
                        onExpandedChange = { docTypeDropdownExpanded = !docTypeDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = formState.documentType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Documento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )

                        ExposedDropdownMenu(
                            expanded = docTypeDropdownExpanded,
                            onDismissRequest = { docTypeDropdownExpanded = false }
                        ) {
                            DOCUMENT_TYPE_ITEMS.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        viewModel.updateDocumentType(type)
                                        docTypeDropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        if (formState.documentType == type) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Serie y Número en dos columnas proporcionadas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.series,
                            onValueChange = { viewModel.updateSeries(it) },
                            label = { Text("Serie") },
                            placeholder = { Text("ej. F001") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = formState.number,
                            onValueChange = { viewModel.updateNumber(it) },
                            label = { Text("Número Correlativo") },
                            placeholder = { Text("ej. 00045231") },
                            modifier = Modifier.weight(1.6f),
                            singleLine = true
                        )
                    }

                    // RUC y Razón Social
                    OutlinedTextField(
                        value = formState.supplierRuc,
                        onValueChange = { viewModel.updateRuc(it) },
                        label = { Text("RUC del Emisor (11 dígitos)") },
                        placeholder = { Text("ej. 20601234567") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = formState.supplierName,
                        onValueChange = { viewModel.updateSupplierName(it) },
                        label = { Text("Razón Social / Proveedor") },
                        placeholder = { Text("ej. Estación de Servicios El Sol S.A.C.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.outline) }
                    )

                    // Fecha de Emisión con Picker
                    OutlinedTextField(
                        value = formState.issueDate,
                        onValueChange = { viewModel.updateIssueDate(it) },
                        label = { Text("Fecha de Emisión (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }

            // ==========================================
            // 3. SECCIÓN: CLASIFICACIÓN CONTABLE Y CATEGORÍA
            // ==========================================
            Text(
                text = "2. Clasificación del Gasto",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val currentCategoryItem = EXPENSE_CATEGORY_ITEMS.firstOrNull { it.code == formState.category }
                        ?: EXPENSE_CATEGORY_ITEMS.last()

                    // Dropdown elegante de categoría con icono
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentCategoryItem.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría de Gasto") },
                            leadingIcon = {
                                Icon(
                                    imageVector = currentCategoryItem.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            EXPENSE_CATEGORY_ITEMS.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.label) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = if (formState.category == item.code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    },
                                    trailingIcon = {
                                        if (formState.category == item.code) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateCategory(item.code)
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Moneda
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Moneda:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        FilterChip(
                            selected = formState.currency == "PEN",
                            onClick = { viewModel.updateCurrency("PEN") },
                            label = { Text("Soles (PEN S/)") }
                        )
                        FilterChip(
                            selected = formState.currency == "USD",
                            onClick = { viewModel.updateCurrency("USD") },
                            label = { Text("Dólares (USD $)") }
                        )
                    }
                }
            }

            // ==========================================
            // 4. SECCIÓN: MONTOS E IMPUESTOS (IGV)
            // ==========================================
            Text(
                text = "3. Importes y Crédito Fiscal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Total Principal
                    OutlinedTextField(
                        value = formState.totalAmount,
                        onValueChange = { viewModel.updateTotalAmount(it) },
                        label = { Text("Importe Total a Pagar *") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Text(
                                text = if (formState.currency == "USD") "$" else "S/",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Subtotal e IGV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.subtotal,
                            onValueChange = { viewModel.updateSubtotal(it) },
                            label = { Text("Base Imponible (S/)") },
                            placeholder = { Text("0.00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = formState.taxAmount,
                            onValueChange = { viewModel.updateTaxAmount(it) },
                            label = { Text("IGV 18% Crédito (S/)") },
                            placeholder = { Text("0.00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Text(
                        text = "💡 Al ingresar el Total, el Subtotal e IGV se calculan automáticamente si es Factura.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // ==========================================
            // 5. SECCIÓN: OBSERVACIONES / NOTAS
            // ==========================================
            Text(
                text = "4. Observaciones (Opcional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    placeholder = { Text("Escribe notas adicionales sobre este gasto para el contador...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    minLines = 3,
                    maxLines = 5
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
