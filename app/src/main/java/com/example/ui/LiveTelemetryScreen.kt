package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.DsoZoneHighlight
import com.example.components.MultiChannelDsoCard
import com.example.model.TelemetryData
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel
import java.util.Locale

/**
 * Live Telemetry Tab:
 * Multi-Channel Diagnostic Strip Chart Recorder
 * Modeled after professional automotive digital storage oscilloscopes (DSO).
 *
 * Displays live, rolling multi-channel waveforms for:
 * 1. RPM vs Vehicle Speed correlation (Powertrain lockup).
 * 2. Throttle Position vs Intake Manifold Pressure response (Induction transients).
 * 3. Coolant Temperature warm-up curve vs Intake Air Temp (Thermal regulation).
 */
@Composable
fun LiveTelemetryScreen(
    viewModel: SharedTelemetryViewModel,
    onOpenProfile: () -> Unit = {}
) {
    val tick by viewModel.latestTick.collectAsState()
    val history by viewModel.history.collectAsState()

    val currentData = tick?.data ?: TelemetryData(
        coolantTemp = 88.0,
        mapKpa = 36.0,
        rpm = 1850.0,
        vss = 54.0,
        intakeAirTemp = 26.0,
        maf = 12.4,
        throttlePos = 24.5,
        ambientTemp = 22.0
    )

    val activeHistory = history

    // 1. Channel 1 & 2: RPM vs Vehicle Speed Correlation
    val rpmHistory = if (activeHistory.isNotEmpty()) {
        activeHistory.map { it.data.rpm }
    } else {
        listOf(currentData.rpm)
    }
    val speedHistory = if (activeHistory.isNotEmpty()) {
        activeHistory.map { it.data.vss }
    } else {
        listOf(currentData.vss)
    }

    val couplingRatioText = if (currentData.vss > 3.0) {
        String.format(Locale.US, "%.1f RPM/kph", currentData.rpm / currentData.vss)
    } else {
        "IDLE"
    }

    // 2. Channel 1 & 2: Throttle Position vs Intake Manifold Pressure Response
    val throttleHistory = if (activeHistory.isNotEmpty()) {
        activeHistory.map { it.data.throttlePos }
    } else {
        listOf(currentData.throttlePos)
    }
    val mapHistory = if (activeHistory.isNotEmpty()) {
        activeHistory.map { it.data.mapKpa }
    } else {
        listOf(currentData.mapKpa)
    }

    val inductionStateText = when {
        currentData.throttlePos > 70.0 -> "WOT (Full Open)"
        currentData.mapKpa < 40.0 -> "High Vacuum"
        currentData.mapKpa > 95.0 -> "Boost/Ambient"
        else -> "Part Cruise"
    }

    // 3. Channel 1 & 2: Coolant Temperature Warm-Up Curve & IAT
    val coolantHistory = if (activeHistory.isNotEmpty()) {
        activeHistory.map { it.data.coolantTemp }
    } else {
        listOf(currentData.coolantTemp)
    }
    val iatHistory = if (activeHistory.isNotEmpty()) {
        activeHistory.map { it.data.intakeAirTemp }
    } else {
        listOf(currentData.intakeAirTemp)
    }

    val thermalStageText = when {
        currentData.coolantTemp < 65.0 -> "Cold Enrichment"
        currentData.coolantTemp < 82.0 -> "Warming Up"
        currentData.coolantTemp <= 96.0 -> "Closed Loop"
        else -> "High Temp Alert"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==============================================================
        // DSO Master Header Panel
        // ==============================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Top Status & Protocol Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(CockpitGreen)
                        )
                        Text(
                            text = "LIVE OBD-II DSO STREAM",
                            color = CockpitGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // CAN Throughput Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CockpitCardElevated)
                            .border(1.dp, CardBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ISO 15765-4 • 10.2 PID/s",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title & Subtitle
                Text(
                    text = "Multi-Channel Diagnostic Strip Chart Recorder",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Designed after high-end professional automotive digital storage oscilloscopes (DSO).",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // ==============================================================
        // Waveform 1: RPM vs Vehicle Speed correlation
        // ==============================================================
        MultiChannelDsoCard(
            title = "RPM vs Vehicle Speed correlation",
            subtitle = "Powertrain Coupling & Transmission Lockup",
            icon = Icons.Default.Speed,
            // CH1: Engine RPM (0 - 8000 RPM)
            channel1Name = "Engine RPM",
            channel1ValueText = "${currentData.rpm.toInt()} RPM",
            channel1Unit = "rpm",
            channel1History = rpmHistory,
            channel1Color = CockpitRed,
            channel1Min = 0.0,
            channel1Max = 8000.0,
            // CH2: Vehicle Speed (0 - 220 km/h)
            channel2Name = "Vehicle Speed",
            channel2ValueText = "${currentData.vss.toInt()} km/h",
            channel2Unit = "km/h",
            channel2History = speedHistory,
            channel2Color = Color(0xFF0284C7), // Slate Blue
            channel2Min = 0.0,
            channel2Max = 220.0,
            // Overlays & Correlation Metric
            correlationMetricLabel = "Coupling Ratio",
            correlationMetricValue = couplingRatioText,
            diagnosticNote = "Correlates crankshaft rotational speed (CH1) directly against road velocity (CH2) on synchronized oscilloscope channels. Analyzes transmission gear ratio engagement, clutch lockup efficiency, torque converter slip, and power band acceleration delivery under load."
        )

        // ==============================================================
        // Waveform 2: Throttle position vs Intake Manifold Pressure response
        // ==============================================================
        MultiChannelDsoCard(
            title = "Throttle position vs Intake Manifold Pressure response",
            subtitle = "Induction Dynamics & Vacuum/Boost Transients",
            icon = Icons.Default.Bolt,
            // CH1: Throttle Position (0 - 100%)
            channel1Name = "Throttle Position",
            channel1ValueText = String.format(Locale.US, "%.1f%%", currentData.throttlePos),
            channel1Unit = "%",
            channel1History = throttleHistory,
            channel1Color = CockpitAmber,
            channel1Min = 0.0,
            channel1Max = 100.0,
            // CH2: Intake Manifold Pressure (20 - 140 kPa)
            channel2Name = "Manifold Pressure",
            channel2ValueText = "${currentData.mapKpa.toInt()} kPa",
            channel2Unit = "kPa",
            channel2History = mapHistory,
            channel2Color = CockpitSteel,
            channel2Min = 20.0,
            channel2Max = 140.0,
            // Atmospheric reference line
            referenceValue = 101.3,
            referenceLabel = "101.3 kPa (1.0 ATM BARO)",
            referenceColor = Color(0xFF9E9E9E),
            correlationMetricLabel = "Induction State",
            correlationMetricValue = inductionStateText,
            diagnosticNote = "Monitors manifold vacuum collapse and throttle actuator transient response. A sluggish MAP rise during snap-throttle indicates intake restrictions or electronic throttle body delay, while abnormally high idle MAP indicates intake plenum vacuum leaks."
        )

        // ==============================================================
        // Waveform 3: Coolant Temperature warm-up curve
        // ==============================================================
        MultiChannelDsoCard(
            title = "Coolant Temperature warm-up curve",
            subtitle = "Thermal Operating Curve & Thermostat Cycle",
            icon = Icons.Default.Thermostat,
            // CH1: Engine Coolant Temperature (30 - 120 °C)
            channel1Name = "Coolant Temp (ECT)",
            channel1ValueText = "${currentData.coolantTemp.toInt()} °C",
            channel1Unit = "°C",
            channel1History = coolantHistory,
            channel1Color = CockpitRed,
            channel1Min = 30.0,
            channel1Max = 120.0,
            // CH2: Intake Air Temperature (0 - 60 °C)
            channel2Name = "Intake Air Temp (IAT)",
            channel2ValueText = "${currentData.intakeAirTemp.toInt()} °C",
            channel2Unit = "°C",
            channel2History = iatHistory,
            channel2Color = Color(0xFF0284C7), // Sky Blue
            channel2Min = 0.0,
            channel2Max = 60.0,
            // Thermostat opening threshold line
            referenceValue = 87.0,
            referenceLabel = "87°C THERMOSTAT OPENING",
            referenceColor = CockpitAmber,
            // Thermal Zones
            zoneHighlights = listOf(
                DsoZoneHighlight(30.0, 70.0, Color(0xFF0284C7), "Cold Enrichment"),
                DsoZoneHighlight(82.0, 96.0, CockpitGreen, "Optimal Closed-Loop"),
                DsoZoneHighlight(100.0, 120.0, CockpitRed, "Overheat Warning")
            ),
            correlationMetricLabel = "Thermal Phase",
            correlationMetricValue = thermalStageText,
            diagnosticNote = "Plots engine thermal progression from cold start through thermostat opening. Verifies cooling system regulation, prevents high-load cold-engine wear, and monitors the temperature drop dip when the thermostat opens into the radiator."
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
