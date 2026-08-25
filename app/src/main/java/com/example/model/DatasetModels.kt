package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatasetInfo(
    @Json(name = "dataset_id") val datasetId: String,
    @Json(name = "filename") val filename: String,
    @Json(name = "row_count") val rowCount: Int,
    @Json(name = "duration_seconds") val durationSeconds: Double
)

@JsonClass(generateAdapter = true)
data class DatasetListResponse(
    @Json(name = "datasets") val datasets: List<DatasetInfo>
)

@JsonClass(generateAdapter = true)
data class ChangeDatasetRequest(
    @Json(name = "dataset_id") val datasetId: String
)
