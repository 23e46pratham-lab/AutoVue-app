package com.example.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object StartStop : Screen("start_stop")
    data object LiveData : Screen("live_data")
    data object VehicleStatus : Screen("vehicle_status")
    data object DtcScanner : Screen("dtc_scanner")
    data object TripMap : Screen("trip_map")
    data object Insights : Screen("insights")
    data object Settings : Screen("settings")
}
