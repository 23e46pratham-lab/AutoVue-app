package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriverBehaviourRequest(
    @Json(name = "rpm_values") val rpmValues: List<Double>,
    @Json(name = "speed_values") val speedValues: List<Double>,
    @Json(name = "throttle_values") val throttleValues: List<Double>,
    @Json(name = "window_index") val windowIndex: Int = 0
)

@JsonClass(generateAdapter = true)
data class DriverBehaviourResponse(
    @Json(name = "label") val label: String = "Moderate",
    @Json(name = "confidence") val confidence: Double = 0.0,
    @Json(name = "tts_message") val ttsMessage: String? = null,
    @Json(name = "feature_values") val featureValues: Map<String, Double>? = null,
    @Json(name = "probabilities") val probabilities: Map<String, Double> = emptyMap()
) {
    // Backward-compatibility helpers for existing UI components
    val behaviourClass: String
        get() = label

    val clusterId: Int
        get() = when (label.lowercase()) {
            "economical", "eco" -> 0
            "moderate", "normal" -> 1
            else -> 2
        }
}
