package com.dxbmark.nfcmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
 * Loading screen shown during app initialization and transitions
 * Follows Nothing OS design principles
 */
@Composable
fun LoadingScreen(
    message: String = "Loading..."
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
            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = NothingColors.NothingRed,
                strokeWidth = 3.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Loading text
            Text(
                text = message,
                style = NothingTextStyles.HeaderTitle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
