package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FuelPredictionRequest(
    @Json(name = "ticks") val ticks: List<FuelSensorTick>
)

@JsonClass(generateAdapter = true)
data class FuelSensorTick(
    @Json(name = "rpm") val rpm: Double = 0.0,
    @Json(name = "vss") val vss: Double = 0.0,
    @Json(name = "maf") val maf: Double = 0.0,
    @Json(name = "throttle_pos") val throttlePos: Double = 0.0,
    @Json(name = "map_kpa") val mapKpa: Double = 0.0,
    @Json(name = "pedal_d") val pedalD: Double = 0.0,
    @Json(name = "pedal_e") val pedalE: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class FuelPredictionResponse(
    @Json(name = "fcr_gs") val fcrGs: Double = 0.0,
    @Json(name = "mileage_kmpl") val mileageKmpl: Double? = null,
    @Json(name = "vss_kmph") val vssKmph: Double = 0.0,
    @Json(name = "fuel_flow_liters_per_hour") val fuelFlowLitersPerHour: Double = 0.0,
    @Json(name = "method") val method: String = "maf",
    @Json(name = "tier") val tier: Int = 1
)
