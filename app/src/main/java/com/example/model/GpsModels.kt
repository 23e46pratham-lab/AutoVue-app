package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RouteResponse(
    @Json(name = "has_gps") val hasGps: Boolean = false,
    @Json(name = "point_count") val pointCount: Int = 0,
    @Json(name = "polyline") val polyline: List<List<Double>> = emptyList(),
    @Json(name = "summary") val summary: RouteSummary? = null
)

@JsonClass(generateAdapter = true)
data class RouteSummary(
    @Json(name = "has_gps") val hasGps: Boolean = false,
    @Json(name = "point_count") val pointCount: Int = 0,
    @Json(name = "bbox") val bbox: RouteBBox? = null,
    @Json(name = "center") val center: RouteCenter? = null
)

@JsonClass(generateAdapter = true)
data class RouteBBox(
    @Json(name = "min_lat") val minLat: Double = 0.0,
    @Json(name = "max_lat") val maxLat: Double = 0.0,
    @Json(name = "min_lon") val minLon: Double = 0.0,
    @Json(name = "max_lon") val maxLon: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class RouteCenter(
    @Json(name = "lat") val lat: Double = 0.0,
    @Json(name = "lon") val lon: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class TripSummaryResponse(
    @Json(name = "has_gps") val hasGps: Boolean = false,
    @Json(name = "trip_stats") val tripStats: TripStats = TripStats()
)

@JsonClass(generateAdapter = true)
data class TripStats(
    @Json(name = "avg_rpm") val avgRpm: Double = 0.0,
    @Json(name = "max_rpm") val maxRpm: Double = 0.0,
    @Json(name = "avg_speed_kmh") val avgSpeedKmh: Double = 0.0,
    @Json(name = "max_speed_kmh") val maxSpeedKmh: Double = 0.0,
    @Json(name = "avg_throttle") val avgThrottle: Double = 0.0,
    @Json(name = "distance_km") val distanceKm: Double? = null,
    @Json(name = "gps_points") val gpsPoints: Int? = null,
    @Json(name = "route_summary") val routeSummary: RouteSummary? = null
)

@JsonClass(generateAdapter = true)
data class DtcResponse(
    @Json(name = "dtcs") val dtcs: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DtcRequest(
    @Json(name = "codes") val codes: List<String>
)

enum class TripMapMode(val id: String, val label: String, val icon: String) {
    ROUTE("route", "Route", "🔵"),
    FUEL("fuel", "Fuel", "🌡"),
    SPEED("speed", "Speed", "🚗"),
    BEHAVIOUR("behaviour", "Behaviour", "🧠"),
    ANOMALY("anomaly", "Anomaly", "⚠")
}

data class TripTickItem(
    val lat: Double,
    val lon: Double,
    val vss: Double,
    val rpm: Double,
    val throttlePos: Double,
    val maf: Double,
    val instantConsumption: Double,
    val drivingProfile: String,
    val anomalyScore: Double,
    val gpsBearing: Double,
    val elevationM: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class LowFuelAlertState(
    val remainingFuelLiters: Double,
    val estimatedRangeKm: Double,
    val avgConsumptionL100km: Double,
    val isVisible: Boolean = true
)

