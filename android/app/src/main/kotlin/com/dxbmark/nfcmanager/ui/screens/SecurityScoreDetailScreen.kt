package com.dxbmark.nfcmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles
import com.dxbmark.nfcmanager.viewmodel.SecurityScoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScoreDetailScreen(
    navController: NavController,
    viewModel: SecurityScoreViewModel = hiltViewModel()
) {
    val securityScoreState by viewModel.securityScore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.security_score_detail_title),
                        style = MaterialTheme.typography.headlineSmall, // MODIFIED to match AboutScreen title style
                        fontFamily = NothingTextStyles.HeaderTitle.fontFamily // APPLY NothingFont from a base style
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Align content to the top
        ) {
            // Display current score and level information
            Text(
                text = "${securityScoreState.score}",
                style = NothingTextStyles.HeaderTitle.copy( // APPLY NothingFont, keep existing size/weight
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = securityScoreState.level.color,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = securityScoreState.level.displayName,
                style = NothingTextStyles.HeaderTitle.copy( // APPLY NothingFont, keep existing size/weight
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = securityScoreState.level.color.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 20.dp))
                Text(
                    stringResource(R.string.loading_security_score),
                    modifier = Modifier.padding(bottom = 20.dp)
                    // Optional: Apply NothingFont to loading text if desired
                    // style = NothingTextStyles.BodyLarge.copy(fontFamily = NothingTextStyles.HeaderTitle.fontFamily)
                )
            }

            // Violations List Title
            Text(
                stringResource(R.string.violations_title),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = NothingTextStyles.HeaderTitle.fontFamily),
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .align(Alignment.Start)
            )
            if (securityScoreState.violations.isEmpty() && !isLoading) {
                Text(
                    stringResource(R.string.no_violations_detected),
                    style = MaterialTheme.typography.bodyMedium,
                    // Optional: Apply NothingFont to this text if desired
                    // fontFamily = NothingTextStyles.HeaderTitle.fontFamily,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.Start)
                )
            } else {
                securityScoreState.violations.take(3).forEach { violation -> 
                    Text(
                        text = "Violation: ${violation.name}", 
                        modifier = Modifier.align(Alignment.Start)
                        // Optional: Apply NothingFont to violation items if desired
                        // fontFamily = NothingTextStyles.HeaderTitle.fontFamily
                    )
                }
            }

            // Recommendations List Title
            Text(
                stringResource(R.string.recommendations_title),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = NothingTextStyles.HeaderTitle.fontFamily),
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .align(Alignment.Start)
            )
            if (securityScoreState.recommendations.isEmpty() && !isLoading) {
                Text(
                    stringResource(R.string.no_recommendations_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    // Optional: Apply NothingFont to this text if desired
                    // fontFamily = NothingTextStyles.HeaderTitle.fontFamily,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.Start)
                )
            } else {
                securityScoreState.recommendations.take(3)
                    .forEach { recommendation -> 
                        Text(
                            text = recommendation, 
                            modifier = Modifier.align(Alignment.Start)
                            // Optional: Apply NothingFont to recommendation items if desired
                            // fontFamily = NothingTextStyles.HeaderTitle.fontFamily
                        )
                    }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom

            Button(
                onClick = { viewModel.refreshSecurityScore() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp) 
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.refresh_score_button)
                        // Optional: Apply NothingFont to button text if desired
                        // style = NothingTextStyles.ButtonText.copy(fontFamily = NothingTextStyles.HeaderTitle.fontFamily)
                    )
                }
            }
        }
    }
}