package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.AnalogGauge
import com.example.components.TelemetryGraphCard
import com.example.components.ValueCard
import com.example.model.TelemetryData
import com.example.repository.ConnectionStatus
import com.example.ui.theme.Amber400
import com.example.ui.theme.CardBorder
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel

@Composable
fun VisualizationScreen(viewModel: SharedTelemetryViewModel) {
    val tick by viewModel.latestTick.collectAsState()
    val history by viewModel.history.collectAsState()
    val status by viewModel.connectionStatus.collectAsState()
    val simulatorStatus by viewModel.simulatorStatus.collectAsState()

    val scrollState = rememberScrollState()

    val currentData = tick?.data ?: TelemetryData(
        coolantTemp = 0.0,
        mapKpa = 0.0,
        rpm = 0.0,
        vss = 0.0,
        intakeAirTemp = 0.0,
        maf = 0.0,
        throttlePos = 0.0,
        ambientTemp = 0.0,
        pedalD = 0.0,
        pedalE = 0.0
    )

    // Extract recent history points (last 40 points for smooth scrolling graphs)
    val windowSize = 40
    val recentHistory = history.takeLast(windowSize)

    val rpmHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.rpm } else listOf(currentData.rpm)
    val speedHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.vss } else listOf(currentData.vss)
    val intakeTempHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.intakeAirTemp } else listOf(currentData.intakeAirTemp)
    val loadHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.throttlePos } else listOf(currentData.throttlePos)
    val coolantHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.coolantTemp } else listOf(currentData.coolantTemp)
    val mapHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.mapKpa } else listOf(currentData.mapKpa)
    val mafHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.maf } else listOf(currentData.maf)
    val ambientHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.ambientTemp } else listOf(currentData.ambientTemp)
    val pedalDHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.pedalD } else listOf(currentData.pedalD)
    val pedalEHistory = if (recentHistory.isNotEmpty()) recentHistory.map { it.data.pedalE } else listOf(currentData.pedalE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top App Bar with Controls (as per reference style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Indigo500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Graphs",
                            tint = Indigo400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Engine Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Visualisation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Amber400
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val statusColor = when (status) {
                        ConnectionStatus.CONNECTED -> Emerald500
                        ConnectionStatus.CONNECTING -> StatusYellow
                        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> StatusRed
                    }
                    val statusText = when (status) {
                        ConnectionStatus.CONNECTED -> "STREAMING LIVE (${history.size} SAMPLES)"
                        ConnectionStatus.CONNECTING -> "CONNECTING..."
                        else -> "OFFLINE"
                    }
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Quick Playback Action Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val simState = simulatorStatus?.state ?: "running"
                if (simState == "running") {
                    IconButton(
                        onClick = { viewModel.pauseSimulation() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause Simulation",
                            tint = Indigo400
                        )
                    }
                } else if (simState == "paused") {
                    IconButton(
                        onClick = { viewModel.resumeSimulation() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume Simulation",
                            tint = Emerald400
                        )
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.startSimulation() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Simulation",
                            tint = Emerald400
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.stopSimulation() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Simulation",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = { viewModel.pingBackend() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = TextSecondary
                    )
                }
            }
        }

        // =========================================================================
        // DASHBOARD OVERVIEW SECTION (All contents in the dashboard above graphs)
        // =========================================================================

        // 1. Primary Analog Instrument Cluster (RPM Tachometer, Speedometer, Coolant Gauge)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE CLUSTER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = simulatorStatus?.datasetName ?: "Active Feed",
                        style = MaterialTheme.typography.labelSmall,
                        color = Indigo400,
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Analog Tachometer (RPM Meter - Left)
                    AnalogGauge(
                        title = "RPM",
                        value = currentData.rpm.toFloat(),
                        maxValue = 8000f,
                        unit = "rpm",
                        majorStep = 2000f,
                        minorDivisions = 2,
                        redlineStart = 6000f,
                        gaugeColor = if (currentData.rpm > 6000) StatusRed else Emerald500,
                        needleColor = Color(0xFFFF9100),
                        valueTextOverride = "%.0f".format(currentData.rpm),
                        tickLabelFormatter = { (it / 1000f).toInt().toString() },
                        modifier = Modifier.weight(0.9f)
                    )

                    // Analog Speedometer (Center - Larger)
                    AnalogGauge(
                        title = "Speed",
                        value = currentData.vss.toFloat(),
                        maxValue = 240f,
                        unit = "km/h",
                        majorStep = 30f,
                        minorDivisions = 2,
                        redlineStart = 160f,
                        gaugeColor = if (currentData.vss > 120) StatusRed else Indigo500,
                        needleColor = Color(0xFFFF3D00),
                        modifier = Modifier.weight(1.25f)
                    )

                    // Analog Coolant Gauge (Temp Meter - Right)
                    AnalogGauge(
                        title = "Coolant",
                        value = currentData.coolantTemp.toFloat(),
                        maxValue = 130f,
                        unit = "°C",
                        majorStep = 30f,
                        minorDivisions = 2,
                        redlineStart = 100f,
                        gaugeColor = if (currentData.coolantTemp > 100) StatusRed else Amber400,
                        needleColor = Color(0xFF00E676),
                        valueTextOverride = "%.0f".format(currentData.coolantTemp),
                        tickLabelFormatter = { it.toInt().toString() },
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
        }

        // 2. Telemetry Value Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ValueCard(
                    title = "Load",
                    value = "%.0f".format(currentData.throttlePos),
                    unit = "%",
                    progress = (currentData.throttlePos / 100.0).toFloat().coerceIn(0f, 1f),
                    progressColor = Indigo400,
                    modifier = Modifier.weight(1f)
                )
                ValueCard(
                    title = "Coolant",
                    value = "%.0f".format(currentData.coolantTemp),
                    unit = "°C",
                    progress = (currentData.coolantTemp / 130.0).toFloat().coerceIn(0f, 1f),
                    progressColor = Amber400,
                    modifier = Modifier.weight(1f)
                )
                ValueCard(
                    title = "Intake",
                    value = "%.0f".format(currentData.intakeAirTemp),
                    unit = "°C",
                    progress = (currentData.intakeAirTemp / 100.0).toFloat().coerceIn(0f, 1f),
                    progressColor = Emerald400,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ValueCard(
                    title = "Ambient",
                    value = "%.0f".format(currentData.ambientTemp),
                    unit = "°C",
                    progress = (currentData.ambientTemp / 50.0).toFloat().coerceIn(0f, 1f),
                    progressColor = Indigo400,
                    modifier = Modifier.weight(1f)
                )
                ValueCard(
                    title = "MAP",
                    value = "%.0f".format(currentData.mapKpa),
                    unit = "kPa",
                    progress = (currentData.mapKpa / 255.0).toFloat().coerceIn(0f, 1f),
                    progressColor = Amber400,
                    modifier = Modifier.weight(1f)
                )
                ValueCard(
                    title = "MAF",
                    value = "%.1f".format(currentData.maf),
                    unit = "g/s",
                    progress = (currentData.maf / 200.0).toFloat().coerceIn(0f, 1f),
                    progressColor = Emerald400,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ValueCard(
                    title = "Pedal D",
                    value = "%.0f".format(currentData.pedalD),
                    unit = "%",
                    progress = (currentData.pedalD / 100.0).toFloat().coerceIn(0f, 1f),
                    progressColor = Indigo400,
                    modifier = Modifier.weight(1f)
                )
                ValueCard(
                    title = "Pedal E",
                    value = "%.0f".format(currentData.pedalE),
                    unit = "%",
                    progress = (currentData.pedalE / 100.0).toFloat().coerceIn(0f, 1f),
                    progressColor = Amber400,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Section Title: Real-Time Oscilloscope Telemetry Graphs
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
                    imageVector = Icons.Default.AutoGraph,
                    contentDescription = null,
                    tint = Indigo400,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "REAL-TIME OSCILLOSCOPE GRAPHS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = "Scroll down for all PIDs ↓",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }

        // =========================================================================
        // REAL-TIME SCROLLABLE ENGINE DATA GRAPHS (As per reference image)
        // =========================================================================

        // 1. Engine Speed - RPM (Red curve, reference top graph)
        TelemetryGraphCard(
            title = "Engine Speed - RPM",
            currentValue = currentData.rpm,
            unit = "rpm",
            dataPoints = rpmHistory,
            lineColor = Color(0xFFFF453A),
            fixedMinY = 0.0,
            fixedMaxY = 8000.0,
            valueFormat = "%.2f"
        )

        // 2. Vehicle Speed (Green curve, reference second graph)
        TelemetryGraphCard(
            title = "Vehicle Speed",
            currentValue = currentData.vss,
            unit = "km/h",
            dataPoints = speedHistory,
            lineColor = Color(0xFF30D158),
            fixedMinY = 0.0,
            fixedMaxY = 220.0,
            valueFormat = "%.1f"
        )

        // 3. Intake Air Temperature (Blue curve, reference third graph)
        TelemetryGraphCard(
            title = "Intake Air Temperature",
            currentValue = currentData.intakeAirTemp,
            unit = "°C",
            dataPoints = intakeTempHistory,
            lineColor = Color(0xFF0A84FF),
            fixedMinY = 0.0,
            fixedMaxY = 120.0,
            valueFormat = "%.1f"
        )

        // 4. Calculated Load Value / Throttle Position (Purple curve, reference fourth graph)
        TelemetryGraphCard(
            title = "Calculated Load Value",
            currentValue = currentData.throttlePos,
            unit = "%",
            dataPoints = loadHistory,
            lineColor = Color(0xFFBF5AF2),
            fixedMinY = 0.0,
            fixedMaxY = 100.0,
            valueFormat = "%.1f"
        )

        // 5. Engine Coolant Temperature
        TelemetryGraphCard(
            title = "Engine Coolant Temperature",
            currentValue = currentData.coolantTemp,
            unit = "°C",
            dataPoints = coolantHistory,
            lineColor = Color(0xFFFF9F0A),
            fixedMinY = 0.0,
            fixedMaxY = 130.0,
            valueFormat = "%.1f"
        )

        // 6. Manifold Absolute Pressure (MAP)
        TelemetryGraphCard(
            title = "Manifold Absolute Pressure",
            currentValue = currentData.mapKpa,
            unit = "kPa",
            dataPoints = mapHistory,
            lineColor = Color(0xFF64D2FF),
            fixedMinY = 0.0,
            fixedMaxY = 255.0,
            valueFormat = "%.1f"
        )

        // 7. Mass Air Flow (MAF)
        TelemetryGraphCard(
            title = "Mass Air Flow (MAF)",
            currentValue = currentData.maf,
            unit = "g/s",
            dataPoints = mafHistory,
            lineColor = Color(0xFF34D399),
            fixedMinY = 0.0,
            fixedMaxY = 150.0,
            valueFormat = "%.2f"
        )

        // 8. Ambient Air Temperature
        TelemetryGraphCard(
            title = "Ambient Air Temperature",
            currentValue = currentData.ambientTemp,
            unit = "°C",
            dataPoints = ambientHistory,
            lineColor = Color(0xFF7DD3FC),
            fixedMinY = -10.0,
            fixedMaxY = 50.0,
            valueFormat = "%.1f"
        )

        // 9. Accelerator Pedal Position (Pedal D & Pedal E Dual-Line)
        TelemetryGraphCard(
            title = "Accelerator Pedal Position",
            currentValue = currentData.pedalD,
            unit = "%",
            dataPoints = pedalDHistory,
            lineColor = Color(0xFF818CF8),
            secondaryDataPoints = pedalEHistory,
            secondaryLineColor = Color(0xFFF43F5E),
            secondaryLabel = "Pedal E (%.1f %%)".format(currentData.pedalE),
            fixedMinY = 0.0,
            fixedMaxY = 100.0,
            valueFormat = "%.1f"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
