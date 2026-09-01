package com.example.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.DashboardScreen
import com.example.ui.InsightsScreen
import com.example.ui.MaintenanceScreen
import com.example.ui.SettingsScreen
import com.example.ui.VisualizationScreen
import com.example.ui.theme.BottomNavBackground
import com.example.ui.theme.Indigo400
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel

data class BottomNavItem(val name: String, val route: Screen, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem("Dash", Screen.Dashboard, Icons.Default.Dashboard),
    BottomNavItem("Graphs", Screen.Visualization, Icons.Default.ShowChart),
    BottomNavItem("Insights", Screen.Insights, Icons.Default.Analytics),
    BottomNavItem("Maint", Screen.Maintenance, Icons.Default.Build),
    BottomNavItem("User", Screen.Settings, Icons.Default.Settings)
)

@Composable
fun AutoVueNavGraph(viewModel: SharedTelemetryViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = true

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar(
                    containerColor = BottomNavBackground
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.name) },
                            label = { Text(item.name.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Indigo400,
                                selectedTextColor = Indigo400,
                                unselectedIconColor = TextSecondary.copy(alpha = 0.6f),
                                unselectedTextColor = TextSecondary.copy(alpha = 0.6f),
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                navController.navigate(item.route.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel)
            }
            composable(Screen.Visualization.route) {
                VisualizationScreen(viewModel)
            }
            composable(Screen.Insights.route) {
                InsightsScreen(viewModel)
            }
            composable(Screen.Maintenance.route) {
                MaintenanceScreen(viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel)
            }
        }
    }
}
