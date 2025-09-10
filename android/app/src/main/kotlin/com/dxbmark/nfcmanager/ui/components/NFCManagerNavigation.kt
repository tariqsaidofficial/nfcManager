package com.dxbmark.nfcmanager.ui.components

import androidx.compose.foundation.layout.*
// import androidx.compose.material.icons.Icons // Will use fully qualified name for Icons.Filled
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController // Added NavController import
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.dxbmark.nfcmanager.R
import com.dxbmark.nfcmanager.ui.screens.*
import com.dxbmark.nfcmanager.ui.theme.NothingTextStyles
import com.dxbmark.nfcmanager.viewmodel.*

// Route for the new screen
object AppRoutes {
    const val HOME = "home"
    const val ACTIVITY = "activity"
    const val SETTINGS = "settings"
    const val NOTIFICATION_SOUND_SETTINGS = "notification_sound_settings" // <<< NEW ROUTE
    const val SECURITY_SCORE = "security_score" // <<< NEW ROUTE
    const val ABOUT = "about"
}

/**
 * Main Navigation Component with Bottom Navigation
 * Built with official Navigation Compose
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFCManagerNavigation(
    mainViewModel: MainViewModel,
    activityViewModel: ActivityViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.titleRes)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.titleRes),
                                style = NothingTextStyles.HeaderTitle // Updated to use HeaderTitle
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME, // Use constant
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoutes.HOME) { // Use constant
                HomeScreen(viewModel = mainViewModel)
            }
            composable(AppRoutes.ACTIVITY) { // Use constant
                ActivityScreen(viewModel = activityViewModel)
            }
            composable(AppRoutes.SETTINGS) { // Use constant
                SettingsScreen(
                    viewModel = settingsViewModel,
                    navController = navController // <<< PASS NAVCONTROLLER
                )
            }
            // <<< NEW COMPOSABLE DESTINATION
            composable(AppRoutes.NOTIFICATION_SOUND_SETTINGS) {
                NotificationSoundSettingsScreen(
                    viewModel = settingsViewModel, // Can reuse or create a dedicated one if needed
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.SECURITY_SCORE) {
                SecurityScoreScreen()
            }
            composable(AppRoutes.ABOUT) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Bottom Navigation Items
 */
private val bottomNavItems = listOf(
    BottomNavItem(
        titleRes = R.string.nav_nfc_manager,
        icon = androidx.compose.material.icons.Icons.Filled.Nfc, 
        route = AppRoutes.HOME // Use constant
    ),
    BottomNavItem(
        titleRes = R.string.nav_activity,
        icon = androidx.compose.material.icons.Icons.Filled.History, 
        route = AppRoutes.ACTIVITY // Use constant
    ),
    BottomNavItem(
        titleRes = R.string.nav_settings,
        icon = androidx.compose.material.icons.Icons.Filled.Settings, 
        route = AppRoutes.SETTINGS // Use constant
    )
)

/**
 * Bottom Navigation Item Data Class
 */
private data class BottomNavItem(
    val titleRes: Int,
    val icon: ImageVector,
    val route: String
)
