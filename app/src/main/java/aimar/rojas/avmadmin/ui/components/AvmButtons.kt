package aimar.rojas.avmadmin.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AvmButtonSize {
    Default,
    Large
}

private val AvmButtonSize.minHeight: Dp
    get() = when (this) {
        AvmButtonSize.Default -> 56.dp
        AvmButtonSize.Large -> 72.dp
    }

private val AvmButtonSize.iconSize: Dp
    get() = when (this) {
        AvmButtonSize.Default -> 20.dp
        AvmButtonSize.Large -> 24.dp
    }

private val AvmButtonSize.progressSize: Dp
    get() = when (this) {
        AvmButtonSize.Default -> 22.dp
        AvmButtonSize.Large -> 28.dp
    }

@Composable
fun AvmPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    leadingIcon: ImageVector? = null,
    size: AvmButtonSize = AvmButtonSize.Default,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color? = null
) {
    val resolvedContentColor = contentColor ?: contentColorFor(containerColor)
    val disabledContainerColor = if (isLoading) {
        containerColor.copy(alpha = 0.82f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }
    val disabledContentColor = if (isLoading) {
        resolvedContentColor
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Button(
        onClick = {
            if (!isLoading) {
                onClick()
            }
        },
        modifier = modifier.heightIn(min = size.minHeight),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = resolvedContentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.progressSize),
                color = resolvedContentColor,
                strokeWidth = 2.dp
            )

            if (loadingText != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = loadingText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(size.iconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                style = if (size == AvmButtonSize.Large) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.labelLarge
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AvmSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    leadingIcon: ImageVector? = null,
    size: AvmButtonSize = AvmButtonSize.Default
) {
    OutlinedButton(
        onClick = {
            if (!isLoading) {
                onClick()
            }
        },
        modifier = modifier.heightIn(min = size.minHeight),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.progressSize),
                strokeWidth = 2.dp
            )

            if (loadingText != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = loadingText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(size.iconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
