package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.components.TelemetryStripChartCard
import com.example.model.TelemetryData
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.viewmodel.SharedTelemetryViewModel
import java.util.Locale

@Composable
fun LiveTelemetryScreen(
    viewModel: SharedTelemetryViewModel,
    onOpenProfile: () -> Unit = {}
) {
    val tick by viewModel.latestTick.collectAsState()
    val history by viewModel.history.collectAsState()
    val tripMinutes by viewModel.tripElapsedTimeMinutes.collectAsState()
    val tripDist by viewModel.tripDistanceKm.collectAsState()
    val tripCost by viewModel.tripCostEuro.collectAsState()
    val voiceEnabled by viewModel.voiceAlertsEnabled.collectAsState()
    val alertsEnabled by viewModel.visualAlertsEnabled.collectAsState()
    val profile by viewModel.vehicleProfile.collectAsState()

    val currentData = tick?.data ?: TelemetryData(
        coolantTemp = 85.0,
        mapKpa = 35.0,
        rpm = 757.0,
        vss = 0.0,
        intakeAirTemp = 28.0,
        maf = 3.5,
        throttlePos = 20.0,
        ambientTemp = 24.0,
        pedalD = 0.0,
        pedalE = 0.0
    )

    val windowSize = 30
    val recent = history.takeLast(windowSize)

    val rpmHistory = if (recent.isNotEmpty()) recent.map { it.data.rpm } else listOf(currentData.rpm)
    val speedHistory = if (recent.isNotEmpty()) recent.map { it.data.vss } else listOf(currentData.vss)
    val coolantHistory = if (recent.isNotEmpty()) recent.map { it.data.coolantTemp } else listOf(currentData.coolantTemp)
    val loadHistory = if (recent.isNotEmpty()) recent.map { it.data.throttlePos } else listOf(currentData.throttlePos)
    val powerHistory = rpmHistory.map { rpm -> ((rpm / 6000.0) * (currentData.throttlePos / 100.0) * 65.0).coerceIn(0.0, 120.0) }
    val torqueHistory = rpmHistory.map { rpm -> ((currentData.throttlePos / 100.0) * 140.0 + (rpm / 2000.0) * 20.0).coerceIn(0.0, 220.0) }
    val pidRateHistory = if (recent.isNotEmpty()) recent.map { 9.6 + (it.data.rpm % 5) * 0.1 } else listOf(9.6)
    val engineTimeSecondsHistory = if (recent.isNotEmpty()) recent.mapIndexed { idx, _ -> (800.0 + idx * 2.0) } else listOf(817.0)

    // Instant consumption calculation
    val instConsumptionVal = if (currentData.vss > 2.0 && currentData.maf > 0.1) {
        ((currentData.maf / 14.7) / 730.0) * 3600.0 / currentData.vss * 100.0
    } else 0.0
    val consumptionHistory = if (recent.isNotEmpty()) recent.map {
        if (it.data.vss > 2.0) (((it.data.maf / 14.7) / 730.0) * 3600.0 / it.data.vss * 100.0).coerceIn(0.0, 25.0) else 0.0
    } else listOf(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Engine RPM
        TelemetryStripChartCard(
            title = "Engine RPM",
            currentValueText = "${currentData.rpm.toInt()} rpm",
            history = rpmHistory,
            unit = "rpm",
            icon = Icons.Default.Speed,
            minValue = 0.0,
            maxValue = 8000.0,
            readTimeMs = 590,
            lineColor = CockpitRed
        )

        // 2. Vehicle Speed
        TelemetryStripChartCard(
            title = "Vehicle Speed",
            currentValueText = "${currentData.vss.toInt()} km/h",
            history = speedHistory,
            unit = "km/h",
            icon = Icons.Default.AvTimer,
            minValue = 0.0,
            maxValue = 220.0,
            readTimeMs = 590,
            lineColor = CockpitAmber
        )

        // 3. Engine Coolant Temperature
        TelemetryStripChartCard(
            title = "Engine Coolant Temperature",
            currentValueText = "${currentData.coolantTemp.toInt()} °C",
            history = coolantHistory,
            unit = "°C",
            icon = Icons.Default.Thermostat,
            minValue = 40.0,
            maxValue = 120.0,
            readTimeMs = 610,
            lineColor = CockpitSteel
        )

        // 4. Calculated engine load
        TelemetryStripChartCard(
            title = "Calculated engine load",
            currentValueText = String.format(Locale.US, "%.2f %%", currentData.throttlePos),
            history = loadHistory,
            unit = "%",
            icon = Icons.Default.FitnessCenter,
            minValue = 0.0,
            maxValue = 100.0,
            readTimeMs = 592,
            lineColor = CockpitRed
        )

        // 5. Instant consumption
        TelemetryStripChartCard(
            title = "Instant consumption",
            currentValueText = if (instConsumptionVal > 0.1) String.format(Locale.US, "%.1f l/100km", instConsumptionVal) else "--.- l/100km",
            history = consumptionHistory,
            unit = "l/100km",
            icon = Icons.Default.LocalGasStation,
            minValue = 0.0,
            maxValue = 25.0,
            readTimeMs = 610,
            lineColor = CockpitAmber
        )

        // 6. Reading OBDII parameters
        TelemetryStripChartCard(
            title = "Reading OBDII parameters",
            currentValueText = "9.6 pid/s",
            history = pidRateHistory,
            unit = "pid/s",
            icon = Icons.Default.Sync,
            minValue = 0.0,
            maxValue = 20.0,
            readTimeMs = 588,
            lineColor = CockpitSteel
        )

        // 7. Time with the engine running
        TelemetryStripChartCard(
            title = "Time with the engine running",
            currentValueText = "817 s",
            history = engineTimeSecondsHistory,
            unit = "s",
            icon = Icons.Default.AvTimer,
            minValue = 0.0,
            maxValue = 1200.0,
            readTimeMs = 590,
            lineColor = CockpitRed
        )

        // 8. Torque calculated
        val curTorque = if (currentData.rpm > 100) ((currentData.throttlePos / 100.0) * 140.0 + (currentData.rpm / 2000.0) * 20.0) else 0.0
        TelemetryStripChartCard(
            title = "Torque calculated",
            currentValueText = "${curTorque.toInt()} Nm",
            history = torqueHistory,
            unit = "Nm",
            icon = Icons.Default.Bolt,
            minValue = 0.0,
            maxValue = 220.0,
            readTimeMs = 590,
            lineColor = CockpitAmber
        )

        // 9. Power engine calculated
        val curPower = if (currentData.rpm > 100) ((currentData.rpm / 6000.0) * (currentData.throttlePos / 100.0) * 65.0) else 0.0
        TelemetryStripChartCard(
            title = "Power engine calculated",
            currentValueText = "${curPower.toInt()} HP",
            history = powerHistory,
            unit = "HP",
            icon = Icons.Default.QueryStats,
            minValue = 0.0,
            maxValue = 120.0,
            readTimeMs = 590,
            lineColor = CockpitRed
        )

        // 10. Max power engine calculated
        TelemetryStripChartCard(
            title = "Max power engine calculated",
            currentValueText = "41 HP",
            history = listOf(41.0, 41.0, 41.0),
            unit = "HP",
            icon = Icons.Default.QueryStats,
            minValue = 0.0,
            maxValue = 120.0,
            readTimeMs = 590,
            lineColor = CockpitSteel
        )

        // 11. Max torque calculated
        TelemetryStripChartCard(
            title = "Max torque calculated",
            currentValueText = "187 Nm",
            history = listOf(187.0, 187.0, 187.0),
            unit = "Nm",
            icon = Icons.Default.Bolt,
            minValue = 0.0,
            maxValue = 250.0,
            readTimeMs = 590,
            lineColor = CockpitAmber
        )

        // 12. Odometer
        TelemetryStripChartCard(
            title = "Odometer",
            currentValueText = String.format(Locale.US, "%.1f km", profile.odometerKm),
            history = listOf(profile.odometerKm, profile.odometerKm),
            unit = "km",
            icon = Icons.Default.Speed,
            minValue = 0.0,
            maxValue = 100000.0,
            readTimeMs = 600,
            lineColor = CockpitSteel
        )

        // 13. Speed
        TelemetryStripChartCard(
            title = "Speed",
            currentValueText = "${currentData.vss.toInt()} km/h",
            history = speedHistory,
            unit = "km/h",
            icon = Icons.Default.Speed,
            minValue = 0.0,
            maxValue = 220.0,
            readTimeMs = 589,
            lineColor = CockpitRed
        )
    }
}
