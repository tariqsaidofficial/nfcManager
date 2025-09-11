package com.dxbmark.nfcmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dxbmark.nfcmanager.ui.theme.NothingColors
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles

/**
 * Error screen shown when something goes wrong
 * Follows Nothing OS design principles
 */
@Composable
fun ErrorScreen(
    message: String = "Something went wrong",
    onRetry: (() -> Unit)? = null
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
                fontWeight = FontWeight.Normal
            )
            
            // Retry button if provided
            onRetry?.let {
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = it,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NothingColors.NothingRed,
                        contentColor = NothingColors.PureWhite
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}
