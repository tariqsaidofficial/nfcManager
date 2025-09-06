package com.nothingos.nfcmanager.ui.components

import androidx.compose.foundation.layout.*
// import androidx.compose.material.icons.Icons // Will use fully qualified name for Icons.Filled
import androidx.compose.material.icons.filled.* // Re-added star import, fully qualified names will be used below
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.nothingos.nfcmanager.ui.screens.*
import com.nothingos.nfcmanager.ui.theme.NothingTextStyles
import com.nothingos.nfcmanager.viewmodel.*

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
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
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
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(viewModel = mainViewModel)
            }
            composable("activity") {
                ActivityScreen(viewModel = activityViewModel)
            }
            composable("settings") {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

/**
 * Bottom Navigation Items
 */
private val bottomNavItems = listOf(
    BottomNavItem(
        title = "NFC MANAGER",
        icon = androidx.compose.material.icons.Icons.Filled.Nfc, // Used fully qualified name
        route = "home"
    ),
    BottomNavItem(
        title = "ACTIVITY",
        icon = androidx.compose.material.icons.Icons.Filled.History, // Used fully qualified name
        route = "activity"
    ),
    BottomNavItem(
        title = "SETTINGS",
        icon = androidx.compose.material.icons.Icons.Filled.Settings, // Used fully qualified name
        route = "settings"
    )
)

/**
 * Bottom Navigation Item Data Class
 */
private data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
