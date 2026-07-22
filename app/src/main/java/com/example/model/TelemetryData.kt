package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistoryResponse(
    @Json(name = "history") val history: List<TelemetryTick>
)

@JsonClass(generateAdapter = true)
data class TelemetryTick(
    @Json(name = "row_index") val rowIndex: Int,
    @Json(name = "total_rows") val totalRows: Int,
    @Json(name = "elapsed_seconds") val elapsedSeconds: Double,
    @Json(name = "playback_percent") val playbackPercent: Double,
    @Json(name = "data") val data: TelemetryData
)

@JsonClass(generateAdapter = true)
data class TelemetryData(
    @Json(name = "coolant_temp") val coolantTemp: Double,
    @Json(name = "map_kpa") val mapKpa: Double,
    @Json(name = "rpm") val rpm: Double,
    @Json(name = "vss") val vss: Double,
    @Json(name = "intake_air_temp") val intakeAirTemp: Double,
    @Json(name = "maf") val maf: Double,
    @Json(name = "throttle_pos") val throttlePos: Double,
    @Json(name = "ambient_temp") val ambientTemp: Double,
    @Json(name = "pedal_d") val pedalD: Double,
    @Json(name = "pedal_e") val pedalE: Double
)
