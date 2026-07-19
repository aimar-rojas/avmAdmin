package aimar.rojas.avmadmin.features.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.R
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.logoutError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearLogoutError() },
            icon = { Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Error al cerrar sesión") },
            text = { Text(uiState.logoutError!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearLogoutError() }) {
                    Text("Entendido")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.log_circular),
            contentDescription = "Logo Productos Aimar Miguel y Violeta",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.user != null) {
            Text(
                text = "Usuario: ${uiState.user!!.username}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Email: ${uiState.user!!.email}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OptionCard(
                title = "Envíos",
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { navController.navigate("shipments") }
            )
            OptionCard(
                title = "Productores",
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { navController.navigate("producers") }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OptionCard(
                title = "Compradores",
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { navController.navigate("purchases") }
            )
            OptionCard(
                title = "Apuntes",
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = { navController.navigate("apuntes") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        val totalPending = uiState.syncStatus.summary.totalPending
        val isSyncing = uiState.syncStatus.isRunning
        val hasError = uiState.syncStatus.state == "error"
        
        val syncContainerColor = if (hasError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        val syncContentColor = if (hasError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = syncContainerColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when {
                            isSyncing -> Icons.Filled.CloudSync
                            totalPending > 0 -> Icons.Filled.CloudOff
                            else -> Icons.Filled.CloudDone
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Estado de Sincronización",
                            tint = if (totalPending > 0 && !isSyncing) Color(0xFFFFA500) else syncContentColor
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Estado Nube",
                            style = MaterialTheme.typography.titleMedium,
                            color = syncContentColor
                        )
                    }
                    if (totalPending > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("$totalPending pendientes", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    } else if (!isSyncing) {
                        Badge(containerColor = Color(0xFF4CAF50)) {
                            Text("Sincronizado", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
                
                if (isSyncing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    uiState.syncStatus.phase?.let { phase ->
                        Text(
                            text = phase,
                            style = MaterialTheme.typography.bodySmall,
                            color = syncContentColor
                        )
                    }
                } else {
                    uiState.syncStatus.message?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = syncContentColor
                        )
                    }
                }
                
                uiState.syncStatus.lastSuccessAt?.let { lastSuccess ->
                    Text(
                        text = "Último sync exitoso: $lastSuccess",
                        style = MaterialTheme.typography.labelSmall,
                        color = syncContentColor.copy(alpha = 0.7f)
                    )
                }

                AvmPrimaryButton(
                    text = if (hasError) "Reintentar" else "Sincronizar ahora",
                    onClick = { viewModel.syncNow() },
                    enabled = !isSyncing,
                    isLoading = isSyncing,
                    loadingText = "Sincronizando...",
                    leadingIcon = Icons.Filled.CloudSync,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AvmPrimaryButton(
            text = "Cerrar Sesión",
            onClick = {
                viewModel.logout(
                    onLogoutSuccess = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLogoutError = { errorMsg ->
                        // Ya manejado por el diálogo al actualizar el state
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoggingOut,
            isLoading = uiState.isLoggingOut,
            loadingText = "Cerrando sesión..."
        )
    }
}

@Composable
fun OptionCard(
    title: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = contentColor
            )
        }
    }
}
