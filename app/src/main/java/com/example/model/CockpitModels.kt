package com.example.model

data class UserProfileItem(
    val id: String,
    val name: String,
    val vehicleModel: String = "Seat Leon 1.6 FSI",
    val isDefault: Boolean = false
)

data class VehicleProfile(
    val profileName: String = "pratham",
    val fuelSupply: String = "gasoline",
    val startAndStopEquipped: Boolean = false,
    val displacementCc: Int = 1700,
    val maxPowerHp: Double = 89.0,
    val odometerKm: Double = 59477.0,
    val totalWeightKg: Double = 1250.0,
    val consumptionL100km: Double = 6.5,
    val correctiveConsumption: Double = 1.0,
    val tankCapacityLiters: Double = 45.0,
    val fuelLeftPercent: Double = 24.0,
    val vin: String = "WVWZZZ3CZWE098124",
    val isActive: Boolean = true
)

data class RefuelingEntry(
    val id: String,
    val profile: String = "pratham",
    val dateTime: String,
    val costEuro: Double,
    val pricePerLiter: Double,
    val fuelLiters: Double,
    val odometerKm: Double,
    val afterRefuelingPercent: Double
)

data class TripHistoryItem(
    val id: String,
    val date: String,
    val time: String,
    val departure: String,
    val destination: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val driveScorePercent: Double,
    val fuelCostEuro: Double,
    val consumptionKmL: Double,
    val avgSpeedKmh: Double = 46.0,
    val maxSpeedKmh: Double = 88.0,
    val fuelUsedLiters: Double = 1.2,
    val maxRpm: Int = 3400,
    val hardBrakes: Int = 0,
    val rapidAccels: Int = 1,
    val specificEvents: List<String> = listOf(
        "Engine cold start @ 18°C",
        "Expressway cruising @ 82 km/h",
        "Regenerative coasting: 1.8 km",
        "Gear shift efficiency: 94%"
    ),
    val waypoints: List<String> = listOf("Start", "Midway Arterial", "Arrival"),
    val routeType: Int = 0
)

data class VehicleEventLog(
    val id: String,
    val date: String,
    val time: String,
    val description: String,
    val profile: String = "pratham"
)

data class DtcCode(
    val code: String,
    val system: String,
    val description: String,
    val mode: String = "Mode 03",
    val severity: String = "Low Risk",
    val plainMeaning: String = "Sensor reading is outside optimal target.",
    val plainAction: String = "Inspect sensor connectors and wiring."
)

enum class AlertSeverity {
    INFO, WARNING, CRITICAL
}

data class ActiveAlert(
    val id: String,
    val title: String,
    val message: String,
    val actionAdvice: String,
    val severity: AlertSeverity = AlertSeverity.WARNING,
    val timestamp: Long = System.currentTimeMillis()
)
