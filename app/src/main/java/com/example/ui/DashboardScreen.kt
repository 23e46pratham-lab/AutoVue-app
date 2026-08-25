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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.AnalogGauge
import com.example.components.CircularGauge
import com.example.components.ValueCard
import com.example.repository.ConnectionStatus
import com.example.ui.theme.Amber400
import com.example.ui.theme.CardBorder
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo900
import com.example.ui.theme.Slate800
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow
import com.example.viewmodel.SharedTelemetryViewModel

@Composable
fun DashboardScreen(viewModel: SharedTelemetryViewModel) {
    val tick by viewModel.latestTick.collectAsState()
    val status by viewModel.connectionStatus.collectAsState()
    val health by viewModel.healthPrediction.collectAsState()
    val simulatorStatus by viewModel.simulatorStatus.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row {
                    Text("Auto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Vue", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Indigo400)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (status) {
                        ConnectionStatus.CONNECTED -> Emerald500
                        ConnectionStatus.CONNECTING -> StatusYellow
                        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> StatusRed
                    }
                    val statusText = when (status) {
                        ConnectionStatus.CONNECTED -> "LIVE TELEMETRY"
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
                        letterSpacing = 1.sp
                    )
                }
            }

            if (status != ConnectionStatus.CONNECTED) {
                Button(
                    onClick = { viewModel.pingBackend() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reconnect", fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val data = tick?.data ?: com.example.model.TelemetryData(
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
            
        // Primary Analog Instrument Cluster (RPM, Speedometer in Center, Coolant)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Analog Tachometer (RPM Meter - Left)
                AnalogGauge(
                    title = "RPM",
                    value = data.rpm.toFloat(),
                    maxValue = 8000f,
                    unit = "rpm",
                    majorStep = 2000f,
                    minorDivisions = 2,
                    redlineStart = 6000f,
                    gaugeColor = if (data.rpm > 6000) StatusRed else Emerald500,
                    needleColor = Color(0xFFFF9100),
                    valueTextOverride = "%.0f".format(data.rpm),
                    tickLabelFormatter = { (it / 1000f).toInt().toString() },
                    modifier = Modifier.weight(0.9f)
                )

                // Analog Speedometer (Center - Larger)
                AnalogGauge(
                    title = "Speed",
                    value = data.vss.toFloat(),
                    maxValue = 240f,
                    unit = "km/h",
                    majorStep = 30f,
                    minorDivisions = 2,
                    redlineStart = 160f,
                    gaugeColor = if (data.vss > 120) StatusRed else Indigo500,
                    needleColor = Color(0xFFFF3D00),
                    modifier = Modifier.weight(1.25f)
                )

                // Analog Coolant Gauge (Temp Meter - Right)
                AnalogGauge(
                    title = "Coolant",
                    value = data.coolantTemp.toFloat(),
                    maxValue = 130f,
                    unit = "°C",
                    majorStep = 30f,
                    minorDivisions = 2,
                    redlineStart = 100f,
                    gaugeColor = if (data.coolantTemp > 100) StatusRed else Amber400,
                    needleColor = Color(0xFF00E676),
                    valueTextOverride = "%.0f".format(data.coolantTemp),
                    tickLabelFormatter = { it.toInt().toString() },
                    modifier = Modifier.weight(0.9f)
                )
            }
        }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Telemetry Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ValueCard(
                        title = "Load",
                        value = "%.0f".format(data.throttlePos),
                        unit = "%",
                        progress = (data.throttlePos / 100.0).toFloat(),
                        progressColor = Indigo400,
                        modifier = Modifier.weight(1f)
                    )
                    ValueCard(
                        title = "Coolant",
                        value = "%.0f".format(data.coolantTemp),
                        unit = "°C",
                        progress = (data.coolantTemp / 130.0).toFloat(),
                        progressColor = Amber400,
                        modifier = Modifier.weight(1f)
                    )
                    ValueCard(
                        title = "Intake",
                        value = "%.0f".format(data.intakeAirTemp),
                        unit = "°C",
                        progress = (data.intakeAirTemp / 100.0).toFloat(),
                        progressColor = Emerald400,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ValueCard(
                        title = "Ambient",
                        value = "%.0f".format(data.ambientTemp),
                        unit = "°C",
                        progress = (data.ambientTemp / 50.0).toFloat().coerceIn(0f, 1f),
                        progressColor = Indigo400,
                        modifier = Modifier.weight(1f)
                    )
                    ValueCard(
                        title = "MAP",
                        value = "%.0f".format(data.mapKpa),
                        unit = "kPa",
                        progress = (data.mapKpa / 255.0).toFloat().coerceIn(0f, 1f),
                        progressColor = Amber400,
                        modifier = Modifier.weight(1f)
                    )
                    ValueCard(
                        title = "MAF",
                        value = "%.1f".format(data.maf),
                        unit = "g/s",
                        progress = (data.maf / 200.0).toFloat().coerceIn(0f, 1f),
                        progressColor = Emerald400,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ValueCard(
                        title = "Pedal D",
                        value = "%.0f".format(data.pedalD),
                        unit = "%",
                        progress = (data.pedalD / 100.0).toFloat(),
                        progressColor = Indigo400,
                        modifier = Modifier.weight(1f)
                    )
                    ValueCard(
                        title = "Pedal E",
                        value = "%.0f".format(data.pedalE),
                        unit = "%",
                        progress = (data.pedalE / 100.0).toFloat(),
                        progressColor = Amber400,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
    }
}
