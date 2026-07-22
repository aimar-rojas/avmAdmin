package aimar.rojas.avmadmin.features.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import aimar.rojas.avmadmin.R
import aimar.rojas.avmadmin.ui.components.AvmPrimaryButton
import aimar.rojas.avmadmin.ui.components.AvmSecondaryButton
import aimar.rojas.avmadmin.utils.DateUtils

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
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        UserHeader(
            username = uiState.user?.username,
            email = uiState.user?.email
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "¿Qué vas a hacer?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeActionCard(
                    title = "Envíos",
                    subtitle = "Ver y crear envíos",
                    icon = Icons.Filled.LocalShipping,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { navController.navigate("shipments") }
                )
                HomeActionCard(
                    title = "Productores",
                    subtitle = "Gestionar proveedores",
                    icon = Icons.Filled.Groups,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { navController.navigate("producers") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeActionCard(
                    title = "Compradores",
                    subtitle = "Clientes y datos",
                    icon = Icons.Filled.ShoppingCart,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { navController.navigate("purchases") }
                )
                HomeActionCard(
                    title = "Apuntes",
                    subtitle = "Registro rápido",
                    icon = Icons.Filled.EditNote,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = { navController.navigate("apuntes") }
                )
            }
        }

        val totalPending = uiState.syncStatus.summary.totalPending
        val isSyncing = uiState.syncStatus.isRunning
        val hasError = uiState.syncStatus.state == "error"
        val hasPartialFailure = uiState.syncStatus.state == "partial_failure"
        val syncTitle = when {
            isSyncing -> "Sincronizando datos"
            hasError -> "Error al sincronizar"
            hasPartialFailure -> "Sincronización incompleta"
            totalPending > 0 -> "Hay datos pendientes"
            else -> "Todo sincronizado"
        }
        val syncDescription = when {
            isSyncing -> uiState.syncStatus.phase ?: "Enviando cambios a la nube."
            hasError -> uiState.syncStatus.message ?: "No se pudo completar la sincronización."
            hasPartialFailure -> uiState.syncStatus.message ?: "Algunos datos no pudieron sincronizarse."
            totalPending > 0 -> "$totalPending cambios esperando conexión o sincronización."
            else -> uiState.syncStatus.message ?: "Tus datos están al día."
        }
        val hasSyncProblem = hasError || hasPartialFailure
        val syncContainerColor = if (hasSyncProblem) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
        val syncContentColor = if (hasSyncProblem) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

        SyncStatusPanel(
            title = syncTitle,
            description = syncDescription,
            lastSuccessAt = uiState.syncStatus.lastSuccessAt,
            totalPending = totalPending,
            isSyncing = isSyncing,
            hasSyncProblem = hasSyncProblem,
            containerColor = syncContainerColor,
            contentColor = syncContentColor,
            onSyncClick = { viewModel.syncNow() }
        )

        AvmSecondaryButton(
            text = "Cerrar Sesión",
            onClick = {
                viewModel.logout(
                    onLogoutSuccess = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLogoutError = {
                        // Ya manejado por el diálogo al actualizar el state
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoggingOut,
            isLoading = uiState.isLoggingOut,
            loadingText = "Cerrando sesión...",
            leadingIcon = Icons.AutoMirrored.Filled.Logout
        )
    }
}

@Composable
private fun UserHeader(
    username: String?,
    email: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.log_circular),
                contentDescription = "Logo Productos Aimar Miguel y Violeta",
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (username.isNullOrBlank()) "Bienvenido" else "Hola, $username",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!email.isNullOrBlank()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .heightIn(min = 116.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(34.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun SyncStatusPanel(
    title: String,
    description: String,
    lastSuccessAt: String?,
    totalPending: Int,
    isSyncing: Boolean,
    hasSyncProblem: Boolean,
    containerColor: Color,
    contentColor: Color,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when {
                        isSyncing -> Icons.Filled.CloudSync
                        hasSyncProblem || totalPending > 0 -> Icons.Filled.CloudOff
                        else -> Icons.Filled.CloudDone
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (totalPending > 0 && !hasSyncProblem && !isSyncing) Color(0xFF9A6700) else contentColor,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                }

                if (totalPending > 0) {
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text("$totalPending")
                    }
                }
            }

            if (isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )

            lastSuccessAt?.let { lastSuccess ->
                Text(
                    text = "Última sincronización exitosa: ${DateUtils.formatSyncTimestampToDisplay(lastSuccess) ?: lastSuccess}",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.72f)
                )
            }

            AvmPrimaryButton(
                text = if (hasSyncProblem) "Reintentar sincronización" else "Sincronizar ahora",
                onClick = onSyncClick,
                enabled = !isSyncing,
                isLoading = isSyncing,
                loadingText = "Sincronizando...",
                leadingIcon = Icons.Filled.CloudSync,
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (hasSyncProblem) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}
