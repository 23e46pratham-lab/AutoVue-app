package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthPredictionRequest(
    @Json(name = "ticks") val ticks: List<HealthSensorTick>
)

@JsonClass(generateAdapter = true)
data class HealthSensorTick(
    @Json(name = "rpm") val rpm: Double = 0.0,
    @Json(name = "vss") val vss: Double = 0.0,
    @Json(name = "maf") val maf: Double = 0.0,
    @Json(name = "throttle_pos") val throttlePos: Double = 0.0,
    @Json(name = "map_kpa") val mapKpa: Double = 0.0,
    @Json(name = "coolant_temp") val coolantTemp: Double = 0.0,
    @Json(name = "intake_air_temp") val intakeAirTemp: Double = 0.0,
    @Json(name = "ambient_temp") val ambientTemp: Double = 0.0,
    @Json(name = "pedal_d") val pedalD: Double = 0.0,
    @Json(name = "pedal_e") val pedalE: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class HealthPredictionResponse(
    @Json(name = "is_anomaly") val isAnomaly: Boolean = false,
    @Json(name = "status") val status: String = "Normal",
    @Json(name = "anomaly_score") val anomalyScore: Double = 0.0,
    @Json(name = "threshold") val threshold: Double = 0.0025,
    @Json(name = "feature_errors") val featureErrors: Map<String, Double> = emptyMap(),
    @Json(name = "triggered_features") val triggeredFeatures: List<String> = emptyList()
) {
    // Backward-compatibility helpers for older UI components
    val confidence: Double
        get() = if (isAnomaly) 0.92 else 0.98

    val probabilities: Map<String, Double>
        get() = if (isAnomaly) {
            mapOf("Anomaly" to 0.92, "Normal" to 0.08)
        } else {
            mapOf("Normal" to 0.98, "Anomaly" to 0.02)
        }
}
