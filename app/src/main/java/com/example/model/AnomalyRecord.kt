package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AnomalyRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp)),
    val anomalyScore: Double,
    val threshold: Double = 0.0025,
    val status: String = "Anomaly",
    val triggeredFeatures: List<String> = emptyList(),
    val featureErrors: Map<String, Double> = emptyMap(),
    val speedKmh: Double = 0.0,
    val rpm: Double = 0.0,
    val coolantTemp: Double = 0.0,
    val throttlePos: Double = 0.0
)
