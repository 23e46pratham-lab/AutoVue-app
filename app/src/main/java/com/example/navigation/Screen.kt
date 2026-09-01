package com.example.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Visualization : Screen("visualization")
    data object Insights : Screen("insights")
    data object Maintenance : Screen("maintenance")
    data object Settings : Screen("settings")
}
