package aimar.rojas.avmadmin.features.apuntes.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
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

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccess() },
            title = { Text("Éxito", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Apunte guardado correctamente.", style = MaterialTheme.typography.bodyLarge) },
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
                title = { Text("Nuevo Apunte", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", modifier = Modifier.size(32.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("apuntes_history") }) {
                        Icon(Icons.Filled.History, contentDescription = "Historial", modifier = Modifier.size(32.dp))
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
                    val cardBgColor by animateColorAsState(targetValue = if (item.isEnabled) getSelectionColor(item.typeInfo.id) else MaterialTheme.colorScheme.surfaceVariant, animationSpec = tween(300))
                    val textColor = if (item.isEnabled) getTextColorForSelection(item.typeInfo.id) else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    val switchBgColor by animateColorAsState(targetValue = if (item.isEnabled) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.4f), animationSpec = tween(300))
                    val circleBgColor by animateColorAsState(targetValue = if (item.isEnabled) Color.White else Color.LightGray, animationSpec = tween(300))
                    val switchOffset by animateDpAsState(targetValue = if (item.isEnabled) 16.dp else 0.dp, animationSpec = tween(300))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Modern Switch (Taller) with Animation
                            Box(
                                modifier = Modifier
                                    .width(56.dp)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(switchBgColor)
                                    .clickable { viewModel.toggleItemEnabled(item.typeInfo.id, !item.isEnabled) }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .offset(x = switchOffset)
                                        .clip(CircleShape)
                                        .background(circleBgColor)
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.typeInfo.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = textColor,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = item.countInput,
                                    onValueChange = { viewModel.onCountChanged(item.typeInfo.id, it) },
                                    label = { Text("Jabas", style = MaterialTheme.typography.titleMedium) },
                                    enabled = item.isEnabled,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.Black),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                                        disabledContainerColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedLabelColor = Color.DarkGray,
                                        unfocusedLabelColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
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
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveApunte() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .navigationBarsPadding(),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                } else {
                    Text("GUARDAR APUNTE", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
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

private fun getTextColorForSelection(id: Int): androidx.compose.ui.graphics.Color {
    return when (id) {
        1, 6, 7 -> androidx.compose.ui.graphics.Color.White // Black, Azul, Morado -> White text
        else -> androidx.compose.ui.graphics.Color.Black // Verde, Blanco, Rosado, Naranja, Amarillo -> Black text
    }
}
