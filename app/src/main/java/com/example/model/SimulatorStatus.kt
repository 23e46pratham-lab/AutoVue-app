package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SimulatorStatus(
    @Json(name = "state") val state: String,
    @Json(name = "dataset_id") val datasetId: String?,
    @Json(name = "dataset_name") val datasetName: String?,
    @Json(name = "current_row") val currentRow: Int,
    @Json(name = "total_rows") val totalRows: Int,
    @Json(name = "speed") val speed: Double,
    @Json(name = "loop") val loop: Boolean,
    @Json(name = "elapsed_playback_seconds") val elapsedPlaybackSeconds: Double,
    @Json(name = "dataset_duration_seconds") val datasetDurationSeconds: Double,
    @Json(name = "playback_percent") val playbackPercent: Double
)

@JsonClass(generateAdapter = true)
data class StartSimulationRequest(
    @Json(name = "dataset_id") val datasetId: String? = null,
    @Json(name = "speed") val speed: Double? = null,
    @Json(name = "loop") val loop: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class SpeedRequest(
    @Json(name = "speed") val speed: Double
)

@JsonClass(generateAdapter = true)
data class LoopRequest(
    @Json(name = "loop") val loop: Boolean
)
