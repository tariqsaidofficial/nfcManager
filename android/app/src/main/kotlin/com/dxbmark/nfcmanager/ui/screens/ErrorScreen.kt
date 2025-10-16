package com.dxbmark.nfcmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.ui.theme.NothingColors
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles

/**
 * Enhanced error screen with better UX
 * Shows error message with optional retry action
 * Follows Nothing OS design principles
 * 
 * @param message Error message to display
 * @param details Optional detailed error information
 * @param onRetry Optional retry action
 * @param showRetry Whether to show retry button (default: true if onRetry is provided)
 */
@Composable
fun ErrorScreen(
    message: String = "Something went wrong",
    details: String? = null,
    onRetry: (() -> Unit)? = null,
    showRetry: Boolean = onRetry != null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Error icon
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = NothingColors.NothingRed
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message
            Text(
                text = message,
                style = NothingTextStyles.HeaderTitle,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            // Error details if provided
            details?.let { detailText ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
            
            // Retry button if provided and showRetry is true
            if (showRetry && onRetry != null) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NothingColors.NothingRed,
                        contentColor = NothingColors.PureWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.retry_button))
                }
            }
        }
    }
}
