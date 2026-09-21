package com.example.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.DashboardScreen
import com.example.ui.DtcScannerScreen
import com.example.ui.InsightsScreen
import com.example.ui.LiveTelemetryScreen
import com.example.ui.SettingsScreen
import com.example.ui.StartStopScreen
import com.example.ui.TripMapScreen
import com.example.ui.UserOnboardingScreen
import com.example.ui.VehicleStatusScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel

data class BottomNavItem(val name: String, val route: Screen, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem("Cluster", Screen.Dashboard, Icons.Default.Speed),
    BottomNavItem("Live", Screen.LiveData, Icons.Default.ShowChart),
    BottomNavItem("ML Insights", Screen.Insights, Icons.Default.Analytics),
    BottomNavItem("Vehicle", Screen.VehicleStatus, Icons.Default.DirectionsCar),
    BottomNavItem("DTCs", Screen.DtcScanner, Icons.Default.Warning)
)

@Composable
fun AutoVueNavGraph(viewModel: SharedTelemetryViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isOnboarding = currentDestination?.route == Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            if (!isOnboarding) {
                NavigationBar(
                    containerColor = CockpitCard,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .background(CockpitBackground)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, CardBorder))
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name,
                                    tint = if (isSelected) CockpitSteel else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = item.name,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CockpitSteel,
                                selectedTextColor = TextPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = CockpitCardElevated
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
            startDestination = Screen.Onboarding.route,
            modifier = Modifier.padding(if (isOnboarding) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                UserOnboardingScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToLiveData = { navController.navigate(Screen.LiveData.route) },
                    onNavigateToDtc = { navController.navigate(Screen.DtcScanner.route) },
                    onNavigateToHistory = { navController.navigate(Screen.VehicleStatus.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToInsights = { navController.navigate(Screen.Insights.route) },
                    onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) }
                )
            }
            composable(Screen.LiveData.route) {
                LiveTelemetryScreen(
                    viewModel = viewModel,
                    onOpenProfile = { navController.navigate(Screen.VehicleStatus.route) }
                )
            }
            composable(Screen.StartStop.route) {
                StartStopScreen(
                    viewModel = viewModel,
                    onOpenProfile = { navController.navigate(Screen.VehicleStatus.route) },
                    onOpenRefueling = { navController.navigate(Screen.VehicleStatus.route) }
                )
            }
            composable(Screen.VehicleStatus.route) {
                VehicleStatusScreen(
                    viewModel = viewModel,
                    onOpenProfile = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.DtcScanner.route) {
                DtcScannerScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.TripMap.route) {
                TripMapScreen(
                    viewModel = viewModel,
                    onOpenProfile = { navController.navigate(Screen.VehicleStatus.route) }
                )
            }
            composable(Screen.Insights.route) {
                InsightsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
