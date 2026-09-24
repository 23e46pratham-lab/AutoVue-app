package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistoryResponse(
    @Json(name = "history") val history: List<TelemetryTick>? = null
)

@JsonClass(generateAdapter = true)
data class TelemetryTick(
    @Json(name = "row_index") val rowIndex: Int = 0,
    @Json(name = "total_rows") val totalRows: Int = 0,
    @Json(name = "elapsed_seconds") val elapsedSeconds: Double = 0.0,
    @Json(name = "playback_percent") val playbackPercent: Double = 0.0,
    @Json(name = "data") val data: TelemetryData = TelemetryData(),
    @Json(name = "dataset_data") val datasetData: TelemetryData? = null,
    @Json(name = "overrides") val overrides: List<String>? = null,
    @Json(name = "ml") val ml: MlInferencePayload? = null,
    @Json(name = "dtcs") val dtcs: List<String>? = null,
    @Json(name = "anomaly_score") val anomalyScore: Double? = null,
    @Json(name = "health_status") val healthStatus: String? = null,
    @Json(name = "driving_profile") val drivingProfile: String? = null,
    @Json(name = "confidence") val confidence: Double? = null
)

@JsonClass(generateAdapter = true)
data class MlInferencePayload(
    @Json(name = "driver_behaviour") val driverBehaviour: DriverBehaviourResponse? = null,
    @Json(name = "health") val health: HealthPredictionResponse? = null,
    @Json(name = "fuel") val fuel: FuelPredictionResponse? = null
)

@JsonClass(generateAdapter = true)
data class TelemetryData(
    @Json(name = "coolant_temp") val coolantTemp: Double = 0.0,
    @Json(name = "map_kpa") val mapKpa: Double = 0.0,
    @Json(name = "rpm") val rpm: Double = 0.0,
    @Json(name = "vss") val vss: Double = 0.0,
    @Json(name = "intake_air_temp") val intakeAirTemp: Double = 0.0,
    @Json(name = "maf") val maf: Double = 0.0,
    @Json(name = "throttle_pos") val throttlePos: Double = 0.0,
    @Json(name = "ambient_temp") val ambientTemp: Double = 0.0,
    @Json(name = "pedal_d") val pedalD: Double = 0.0,
    @Json(name = "pedal_e") val pedalE: Double = 0.0,
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lon") val lon: Double? = null,
    @Json(name = "elevation_m") val elevationM: Double? = null,
    @Json(name = "gps_bearing") val gpsBearing: Double? = null,
    @Json(name = "gps_speed_ms") val gpsSpeedMs: Double? = null,
    @Json(name = "gps_fix") val gpsFix: Int? = null,
    @Json(name = "anomaly_score") val anomalyScore: Double? = null,
    @Json(name = "health_status") val healthStatus: String? = null,
    @Json(name = "driving_profile") val drivingProfile: String? = null,
    @Json(name = "confidence") val confidence: Double? = null
) {
    val hasGps: Boolean
        get() = lat != null && lon != null
}
