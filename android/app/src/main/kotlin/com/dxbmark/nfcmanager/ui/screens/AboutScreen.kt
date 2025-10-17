package com.dxbmark.nfcmanager.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
// import androidx.compose.ui.platform.LocalContext // This import will be unused after the change
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    // val context = LocalContext.current // Removed this line
    val uriHandler = LocalUriHandler.current
    val appVersion = "1.0.0" // Hard-coded for now, can be replaced with BuildConfig.VERSION_NAME when available

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.about_app_title),
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Section Card
            AboutCard(
                title = stringResource(R.string.app_name),
                subtitle = "Version $appVersion",
                useNothingFont = true
            ) {
                Text(
                    text = stringResource(R.string.about_app_description),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.about_copyright),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }


            // Developer Section Card
            AboutCard(
                title = stringResource(R.string.about_development_design_title),
                useNothingFont = true
            ) {
                Text(
                    text = stringResource(R.string.about_development_credit),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                
                // Social Media Links with Icons
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // LinkedIn
                    SocialMediaLink(
                        iconRes = R.drawable.ic_linkedin,
                        text = stringResource(R.string.social_media_linkedin),
                        url = "https://linkedin.com/in/tariqsaidofficial",
                        uriHandler = uriHandler
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // GitHub
                    SocialMediaLink(
                        iconRes = R.drawable.ic_github,
                        text = stringResource(R.string.social_media_github),
                        url = "https://github.com/tariqsaidofficial",
                        uriHandler = uriHandler
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Company Logos Section - Redesigned
                Text(
                    text = stringResource(R.string.partnership_title),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // DXBMark - Main Developer
                    CompanyLogoEnhanced(
                        logoRes = R.drawable.dxbmark_logo,
                        contentDescription = "DXBMark Logo",
                        title = "Development Partner",
                        subtitle = "App Design & Development",
                        url = "https://dxbmark.com",
                        uriHandler = uriHandler,
                        isPrimary = false, // Changed from true
                        isHighlighted = false
                    )
                    
                    // MWHEBA - Official Contributor  
                    CompanyLogoEnhanced(
                        logoRes = R.drawable.mwheba_logo,
                        contentDescription = "MWHEBA Logo",
                        title = "Official Contributor", 
                        subtitle = "Advertising Agency",
                        url = "https://mwheba.com",
                        uriHandler = uriHandler,
                        isPrimary = true, // Changed from false
                        isHighlighted = false
                    )
                }
            }

            // Contact & Support Card
            AboutCard(
                title = stringResource(R.string.about_contact_support_title),
                useNothingFont = true
            ) {
                ContactItem(
                    icon = "📧",
                    text = stringResource(R.string.about_email),
                    onClick = { uriHandler.openUri("mailto:support@dxbmark.com") }
                )
                ContactItem(icon = "⏱️", text = stringResource(R.string.about_response_time))
                ContactItem(icon = "🌐", text = stringResource(R.string.about_languages))
                ContactItem(icon = "🕐", text = stringResource(R.string.about_timezone))
            }

            // Legal & Links Card
            AboutCard(
                title = stringResource(R.string.about_license_title),
                useNothingFont = true
            ) {
                Text(
                    text = stringResource(R.string.about_license_text),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.about_copyright_text),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                ClickableLink(
                    text = stringResource(R.string.about_license_link),
                    url = "https://github.com/tariqsaidofficial/nfcManager/blob/6e293ee9455a67c6d83b381d66b28423cc50d9f4/LICENSE",
                    bottomPadding = 8.dp
                )
                
                Text(
                    text = stringResource(R.string.about_important_links_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                ClickableLink(text = stringResource(R.string.about_terms_service), url = "https://github.com/tariqsaidofficial/nfcManager/blob/6e293ee9455a67c6d83b381d66b28423cc50d9f4/TERMS_OF_SERVICE.md")
                ClickableLink(text = stringResource(R.string.about_privacy_policy), url = "https://github.com/tariqsaidofficial/nfcManager/blob/6e293ee9455a67c6d83b381d66b28423cc50d9f4/PRIVACY_POLICY.md")
                ClickableLink(text = stringResource(R.string.about_full_changelog), url = "https://github.com/tariqsaidofficial/nfcManager/blob/6e293ee9455a67c6d83b381d66b28423cc50d9f4/CHANGELOG.md")
                ClickableLink(text = stringResource(R.string.about_project_repository), url = "https://github.com/tariqsaidofficial/nfcManager", bottomPadding = 0.dp)
            }
        }
    }
}

@Composable
fun AboutCard(
    title: String,
    subtitle: String? = null,
    useNothingFont: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = if (useNothingFont) {
                    NothingTextStyles.HeaderTitle.copy(
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = if (subtitle != null) 4.dp else 12.dp)
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}

@Composable
fun SocialMediaLink(
    iconRes: Int,
    text: String,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { uriHandler.openUri(url) }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CompanyLogo(
    logoRes: Int,
    contentDescription: String,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    maxHeight: Dp = 60.dp
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { uriHandler.openUri(url) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = contentDescription,
            modifier = Modifier
                .heightIn(max = maxHeight)
                .padding(12.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun CompanyLogoWithLabel(
    logoRes: Int,
    contentDescription: String,
    label: String,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    maxHeight: Dp = 60.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(150.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f) // Make it more rectangular/wide
                .clickable { uriHandler.openUri(url) },
            colors = CardDefaults.cardColors(
                containerColor = if (!isSystemInDarkTheme()) {
                    // Light mode - use much darker background for white logos contrast
                    Color(0xFF1A1A1A)
                } else {
                    // Dark mode - use lighter surface
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .heightIn(max = maxHeight)
                        .padding(20.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun CompanyLogoEnhanced(
    logoRes: Int,
    contentDescription: String,
    title: String,
    subtitle: String,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    isPrimary: Boolean = false,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(url) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPrimary -> MaterialTheme.colorScheme.primaryContainer
                isHighlighted -> Color(0xFFD32F2F).copy(alpha = 0.15f) // Red highlight
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPrimary) 12.dp else 8.dp
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo section
            Card(
                modifier = Modifier.size(100.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (!isSystemInDarkTheme()) {
                        Color(0xFF404040) // Dark grey instead of black
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = contentDescription,
                        modifier = Modifier
                            .size(if (isPrimary) 90.dp else 85.dp)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            // Text section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isPrimary -> MaterialTheme.colorScheme.onPrimaryContainer
                        isHighlighted -> Color(0xFFD32F2F) // Red text for highlighted
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isPrimary -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        isHighlighted -> Color(0xFFD32F2F).copy(alpha = 0.8f) // Red subtitle for highlighted
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}

@Composable
fun ContactItem(
    icon: String,
    text: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (onClick != null) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ClickableLink(text: String, url: String, bottomPadding: Dp = 4.dp, removePadding: Boolean = false) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .then(if (!removePadding) Modifier.padding(bottom = bottomPadding) else Modifier)
            .clickable { uriHandler.openUri(url) }
    )
}
