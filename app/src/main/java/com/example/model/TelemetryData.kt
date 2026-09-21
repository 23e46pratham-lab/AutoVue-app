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
    @Json(name = "ml") val ml: MlInferencePayload? = null
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
    @Json(name = "pedal_e") val pedalE: Double = 0.0
)
