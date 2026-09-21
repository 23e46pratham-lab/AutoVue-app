package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dialogs.AnomaliesHistoryDialog
import com.example.ui.dialogs.AnomalyDetectedHorizontalBlock
import com.example.model.DriverBehaviourResponse
import com.example.model.FuelPredictionResponse
import com.example.model.HealthPredictionResponse
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(viewModel: SharedTelemetryViewModel) {
    val driverBehaviour by viewModel.driverBehaviour.collectAsState()
    val health by viewModel.healthPrediction.collectAsState()
    val fuel by viewModel.fuelPrediction.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val inferenceError by viewModel.inferenceError.collectAsState()
    val lastTimestamp by viewModel.lastInferenceTimestamp.collectAsState()
    val latestTick by viewModel.latestTick.collectAsState()
    val detectedAnomalies by viewModel.detectedAnomalies.collectAsState()

    var showAnomaliesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (health == null && driverBehaviour == null && fuel == null) {
            viewModel.triggerInference()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Header Banner
        AiInsightsHeaderCard(
            isAnalyzing = isAnalyzing,
            lastTimestamp = lastTimestamp,
            onRefresh = { viewModel.triggerInference() }
        )

        // Constant size anomaly detected block small spread horizontally which on clicking shows anomalies obtained till now
        val hasAnomaly = health?.isAnomaly == true || health?.status.equals("Anomaly", ignoreCase = true) || detectedAnomalies.isNotEmpty()
        if (hasAnomaly) {
            AnomalyDetectedHorizontalBlock(
                anomalyCount = detectedAnomalies.size.coerceAtLeast(1),
                latestAnomaly = detectedAnomalies.firstOrNull(),
                onClick = { showAnomaliesDialog = true }
            )
        }

        // Error / Connection Warning Banner
        if (inferenceError != null) {
            InferenceErrorCard(
                errorMessage = inferenceError!!,
                onRetry = { viewModel.triggerInference() },
                onPingBackend = { viewModel.pingBackend() }
            )
        }

        // Empty State when no models have run yet
        if (!isAnalyzing && health == null && driverBehaviour == null && fuel == null && inferenceError == null) {
            EmptyInsightsCard(onRunInference = { viewModel.triggerInference() })
        }

        // Loading State
        if (isAnalyzing && health == null && driverBehaviour == null && fuel == null) {
            AnalyzingPlaceholderCard()
        }

        // Vehicle Health Prediction Section (LSTM Autoencoder)
        if (health != null) {
            VehicleHealthCard(health = health!!)
        }

        // Driver Behaviour Analysis Section (XGBoost Classifier)
        if (driverBehaviour != null) {
            DriverBehaviourCard(behaviour = driverBehaviour!!)
        }

        // Fuel Physics & Mileage Estimation Section
        if (fuel != null) {
            FuelEfficiencyCard(fuel = fuel!!)
        }

        // Snapshot Telemetry Used for Inference
        if (latestTick != null) {
            TelemetrySnapshotCard(tick = latestTick!!)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAnomaliesDialog) {
        AnomaliesHistoryDialog(
            anomalies = detectedAnomalies,
            onDismiss = { showAnomaliesDialog = false },
            onClearAll = { viewModel.clearAnomalies() },
            onSimulateTestAnomaly = { viewModel.simulateTestAnomaly() }
        )
    }
}

@Composable
private fun AiInsightsHeaderCard(
    isAnalyzing: Boolean,
    lastTimestamp: Long?,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CockpitCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Icon",
                            tint = CockpitSteel,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI Telemetry Analytics",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Machine Learning Predictive Models",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = CockpitSteel
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Inference",
                            tint = CockpitSteel
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hasModels = lastTimestamp != null
                val statusText = if (isAnalyzing) "Analyzing Telemetry..." else if (hasModels) "ML Inferences Active" else "Ready to Analyze"
                val statusColor = if (isAnalyzing) CockpitAmber else if (hasModels) CockpitGreen else TextMuted

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                if (lastTimestamp != null) {
                    val timeStr = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(lastTimestamp))
                    Text(
                        text = "Updated: $timeStr",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleHealthCard(health: HealthPredictionResponse) {
    val isAnomaly = health.isAnomaly || health.status.equals("Anomaly", ignoreCase = true)
    val statusColor = if (!isAnomaly) CockpitGreen else CockpitRed
    val statusContainerColor = statusColor.copy(alpha = 0.15f)
    val statusIcon = if (!isAnomaly) Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = "Vehicle Health",
                    tint = CockpitSteel,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "LSTM Autoencoder Diagnostic",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Health Status Hero Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusContainerColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "HEALTH CLASSIFICATION",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = (health.status ?: "Normal").uppercase(Locale.getDefault()),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Anomaly Score",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        val safeScore = if (health.anomalyScore.isNaN() || health.anomalyScore.isInfinite()) 0.0 else health.anomalyScore
                        Text(
                            text = "%.5f".format(Locale.US, safeScore),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Triggered features / Outliers if any
            if (!health.triggeredFeatures.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Triggered Sensor Outliers (${health.triggeredFeatures.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CockpitRed
                    )
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        health.triggeredFeatures.forEach { feature ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CockpitRed.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitRed.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = feature,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CockpitRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Per-sensor reconstruction errors breakdown
            if (!health.featureErrors.isNullOrEmpty()) {
                Text(
                    text = "Sensor Reconstruction Error Loss (MSE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CockpitSteel
                )

                val maxError = health.featureErrors.values
                    .filter { !it.isNaN() && !it.isInfinite() && it > 0 }
                    .maxOrNull() ?: 0.001
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    health.featureErrors.entries.take(6).forEach { (sensorName, errVal) ->
                        val safeErr = if (errVal.isNaN() || errVal.isInfinite() || errVal < 0) 0.0 else errVal
                        val normalizedRatio = (safeErr / maxError).toFloat().let {
                            if (it.isNaN() || it.isInfinite()) 0.05f else it.coerceIn(0.05f, 1f)
                        }
                        val isTriggered = health.triggeredFeatures.any { it.equals(sensorName, ignoreCase = true) }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sensorName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isTriggered) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isTriggered) CockpitRed else TextPrimary,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "%.6f".format(Locale.US, safeErr),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTriggered) CockpitRed else TextSecondary
                                )
                            }

                            LinearProgressIndicator(
                                progress = { normalizedRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isTriggered) CockpitRed else CockpitSteel,
                                trackColor = CockpitCardElevated
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverBehaviourCard(behaviour: DriverBehaviourResponse) {
    val (badgeColor, badgeContainerColor) = when (behaviour.label.lowercase(Locale.US)) {
        "economical", "eco", "cautious" -> Pair(CockpitGreen, CockpitGreen.copy(alpha = 0.15f))
        "moderate", "normal" -> Pair(CockpitSteel, CockpitSteel.copy(alpha = 0.15f))
        else -> Pair(CockpitRed, CockpitRed.copy(alpha = 0.15f)) // Aggressive
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Driver Behaviour",
                    tint = CockpitSteel,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "XGBoost Driver Classification",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Classification Badge Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeContainerColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DRIVING PROFILE",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = behaviour.label.uppercase(Locale.US),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor
                        )
                    }

                    if (behaviour.confidence > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "%.1f%% Conf".format(behaviour.confidence * 100),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Voice Coach Advisory Message Bubble
            if (!behaviour.ttsMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CockpitCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Coach Advisory",
                            tint = CockpitSteel,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "VOICE COACH ADVISORY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CockpitSteel
                            )
                            Text(
                                text = "\"${behaviour.ttsMessage}\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Engineered Features Breakdown
            if (!behaviour.featureValues.isNullOrEmpty()) {
                Text(
                    text = "Feature Analysis (Windowed)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CockpitSteel
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val entries = behaviour.featureValues.entries.toList()
                    for (i in entries.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val first = entries[i]
                            val safeFirst = if (first.value.isNaN() || first.value.isInfinite()) 0.0 else first.value
                            FeatureMetricCard(
                                title = formatFeatureTitle(first.key),
                                value = "%.2f".format(Locale.US, safeFirst),
                                modifier = Modifier.weight(1f)
                            )
                            if (i + 1 < entries.size) {
                                val second = entries[i + 1]
                                val safeSecond = if (second.value.isNaN() || second.value.isInfinite()) 0.0 else second.value
                                FeatureMetricCard(
                                    title = formatFeatureTitle(second.key),
                                    value = "%.2f".format(Locale.US, safeSecond),
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FuelEfficiencyCard(fuel: FuelPredictionResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Fuel Efficiency",
                        tint = CockpitSteel,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Physics Fuel Consumption & Mileage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CockpitGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Tier ${fuel.tier} • ${(fuel.method ?: "maf").uppercase(Locale.US)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CockpitGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Mileage Main Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CockpitCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "INSTANTANEOUS MILEAGE",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        val mileageDisplay = if (fuel.mileageKmpl != null && !fuel.mileageKmpl.isNaN() && !fuel.mileageKmpl.isInfinite() && fuel.vssKmph > 1.0) {
                            "%.1f km/L".format(Locale.US, fuel.mileageKmpl)
                        } else if (fuel.vssKmph <= 1.0) {
                            "Stationary / Idle"
                        } else {
                            "-- km/L"
                        }
                        Text(
                            text = mileageDisplay,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CockpitGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Fuel Consumption Rate",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        val safeFcr = if (fuel.fcrGs.isNaN() || fuel.fcrGs.isInfinite()) 0.0 else fuel.fcrGs
                        Text(
                            text = "%.2f g/s".format(Locale.US, safeFcr),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val safeVss = if (fuel.vssKmph.isNaN() || fuel.vssKmph.isInfinite()) 0.0 else fuel.vssKmph
                val safeFcr = if (fuel.fcrGs.isNaN() || fuel.fcrGs.isInfinite()) 0.0 else fuel.fcrGs
                FeatureMetricCard("Vehicle Speed", "%.0f km/h".format(Locale.US, safeVss), Modifier.weight(1f))
                FeatureMetricCard("Flow Rate (g/s)", "%.3f".format(Locale.US, safeFcr), Modifier.weight(1f))
                FeatureMetricCard("Method Tier", "Tier ${fuel.tier}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FeatureMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CockpitCardElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TelemetrySnapshotCard(tick: com.example.model.TelemetryTick) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Telemetry",
                    tint = CockpitSteel,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Live Telemetry Snapshot Input",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureMetricCard("Engine RPM", "%.0f RPM".format(tick.data.rpm), Modifier.weight(1f))
                FeatureMetricCard("Vehicle Speed", "%.0f km/h".format(tick.data.vss), Modifier.weight(1f))
                FeatureMetricCard("Throttle", "%.1f %%".format(tick.data.throttlePos), Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureMetricCard("Coolant Temp", "%.0f °C".format(tick.data.coolantTemp), Modifier.weight(1f))
                FeatureMetricCard("MAP Pressure", "%.0f kPa".format(tick.data.mapKpa), Modifier.weight(1f))
                FeatureMetricCard("Air Flow (MAF)", "%.1f g/s".format(tick.data.maf), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InferenceErrorCard(
    errorMessage: String,
    onRetry: () -> Unit,
    onPingBackend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitRed.copy(alpha = 0.15f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CockpitRed)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = CockpitRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Diagnostic Connection Notice",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CockpitRed
                )
            }

            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = TextPrimary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Button(
                    onClick = onPingBackend,
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Wake Backend Server", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSteel)
                ) {
                    Text("Retry Analysis", fontSize = 11.sp, color = CockpitSteel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AnalyzingPlaceholderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = CockpitSteel,
                strokeWidth = 3.dp
            )
            Text(
                text = "Executing Machine Learning Models...",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Processing live telemetry through LSTM Autoencoder, XGBoost, and Fuel Physics models.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyInsightsCard(onRunInference: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = CockpitSteel,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "No AI Diagnostics Loaded Yet",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Run machine learning models to analyze vehicle health, classify driving habits, and compute fuel efficiency from current telemetry.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Button(
                onClick = onRunInference,
                colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CockpitSteel,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Diagnostics & Inference", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatFeatureTitle(key: String): String {
    return when (key.lowercase(Locale.US)) {
        "avg_speed" -> "Avg Speed"
        "vs_dev" -> "Speed Variance"
        "mean_rpm" -> "Mean RPM"
        "rpm_std" -> "RPM Variance"
        "mean_pedal" -> "Mean Throttle"
        "pedal_std" -> "Throttle Variance"
        "max_speed" -> "Max Speed"
        "accel_std" -> "Accel Variance"
        "window" -> "Sample Window"
        else -> key.replace("_", " ").capitalizeWords()
    }
}

private fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
