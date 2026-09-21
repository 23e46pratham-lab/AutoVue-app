package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.CockpitInstrumentCluster
import com.example.components.TopTelemetryHud
import com.example.model.ActiveAlert
import com.example.model.AlertSeverity
import com.example.model.TelemetryData
import com.example.repository.ConnectionStatus
import com.example.ui.dialogs.ApiKeyInitializationDialog
import com.example.ui.dialogs.CockpitOverflowMenu
import com.example.ui.dialogs.ObdConnectionDialog
import com.example.ui.dialogs.QuickGuideDialog
import com.example.ui.dialogs.RefuelingDialog
import com.example.ui.dialogs.VehicleProfileDialog
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
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SharedTelemetryViewModel,
    onNavigateToLiveData: () -> Unit = {},
    onNavigateToDtc: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {}
) {
    val tick by viewModel.latestTick.collectAsState()
    val status by viewModel.connectionStatus.collectAsState()
    val profile by viewModel.vehicleProfile.collectAsState()
    val tripMinutes by viewModel.tripElapsedTimeMinutes.collectAsState()
    val tripDist by viewModel.tripDistanceKm.collectAsState()
    val tripCost by viewModel.tripCostEuro.collectAsState()
    val voiceEnabled by viewModel.voiceAlertsEnabled.collectAsState()
    val alertsEnabled by viewModel.visualAlertsEnabled.collectAsState()
    val driverBehaviour by viewModel.driverBehaviour.collectAsState()
    val fuelPrediction by viewModel.fuelPrediction.collectAsState()
    val activeAlert by viewModel.activeAlert.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showRefuelingDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showObdDialog by remember { mutableStateOf(false) }
    var showApiKeysDialog by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // App Title & Connection Header (Screenshot 1 & 9)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SmartControl",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OBD2",
                        color = CockpitSteel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (status) {
                        ConnectionStatus.CONNECTED -> CockpitGreen
                        ConnectionStatus.CONNECTING -> CockpitAmber
                        ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> CockpitRed
                    }
                    val statusText = when (status) {
                        ConnectionStatus.CONNECTED -> "${profile.profileName}: connected | 12 min"
                        ConnectionStatus.CONNECTING -> "Connecting to ECU..."
                        else -> "Disconnected"
                    }
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = statusText,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Connect OBD Option in Cluster
                val isConnected = status == ConnectionStatus.CONNECTED
                val isConnecting = status == ConnectionStatus.CONNECTING
                val obdBadgeColor = when {
                    isConnected -> CockpitGreen
                    isConnecting -> CockpitAmber
                    else -> CockpitSteel
                }

                Surface(
                    onClick = { showObdDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = obdBadgeColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, obdBadgeColor.copy(alpha = 0.45f)),
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                            contentDescription = "Connect OBD",
                            tint = obdBadgeColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = when {
                                isConnected -> "OBD"
                                isConnecting -> "Pairing"
                                else -> "Connect OBD"
                            },
                            color = if (isConnected) CockpitGreen else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = { showProfileDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Profile",
                        tint = CockpitSteel
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = TextSecondary
                        )
                    }
                    CockpitOverflowMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        onSelectQuickGuide = { showGuideDialog = true },
                        onSelectRefueling = { showRefuelingDialog = true },
                        onSelectSettings = onNavigateToSettings,
                        onSelectDtc = onNavigateToDtc,
                        onSelectLiveData = onNavigateToLiveData,
                        onSelectHistory = onNavigateToHistory,
                        onSelectSwitchUser = onNavigateToOnboarding,
                        onSelectConnectObd = { showObdDialog = true },
                        onSelectInitializeApiKeys = { showApiKeysDialog = true }
                    )
                }
            }
        }

        // Simplified Warning & System Status Banner
        SimplifiedAlertBanner(
            activeAlert = activeAlert,
            onDismiss = { viewModel.dismissAlert() },
            onTestAlert = { viewModel.triggerTestAlert() }
        )

        // Persistent Top Telemetry HUD (Screenshot 1)
        TopTelemetryHud(
            data = currentData,
            elapsedMinutes = tripMinutes,
            distanceKm = tripDist,
            costEuro = tripCost,
            voiceEnabled = voiceEnabled,
            alertsEnabled = alertsEnabled,
            onProfileClick = { showProfileDialog = true },
            onAlertsToggle = { viewModel.toggleVisualAlerts() },
            onVoiceToggle = { viewModel.toggleVoiceAlerts() }
        )

        // Centerpiece: Cockpit Analog Tachometer & Peripheral Gauges (Screenshot 1)
        CockpitInstrumentCluster(
            data = currentData,
            fuelPercent = profile.fuelLeftPercent,
            peakPowerHp = profile.maxPowerHp.takeIf { it > 0 } ?: 41.0,
            peakPowerRpm = 1629,
            peakTorqueNm = 187.0,
            peakTorqueRpm = 850
        )

        // Quick Telemetry Actions Bar (S&S removed, ML Insights added)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToInsights,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "ML Insights",
                    tint = CockpitSteel,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ML INSIGHTS",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { showRefuelingDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = "Refuel",
                    tint = CockpitAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REFUELING",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { showGuideDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Guide",
                    tint = CockpitSteel,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GUIDE",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Previous ML Inferences Section
        DashboardMlInferencesCard(
            behaviour = driverBehaviour,
            fuel = fuelPrediction,
            isAnalyzing = isAnalyzing,
            onRunInference = { viewModel.triggerInference() },
            onViewFullInsights = onNavigateToInsights
        )

        // Subsystem Telemetry Status Strip
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Engine Status",
                        tint = CockpitGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ECU Protocol: ISO 15765-4 (CAN 11/500)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "9.6 PID/s",
                    color = CockpitSteel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialogs
    if (showProfileDialog) {
        VehicleProfileDialog(
            currentProfile = profile,
            onDismiss = { showProfileDialog = false },
            onSave = { updated -> viewModel.updateVehicleProfile(updated) }
        )
    }

    if (showRefuelingDialog) {
        RefuelingDialog(
            profileName = profile.profileName,
            currentOdometer = profile.odometerKm,
            onDismiss = { showRefuelingDialog = false },
            onSave = { entry -> viewModel.addRefuelingEntry(entry) }
        )
    }

    if (showGuideDialog) {
        QuickGuideDialog(
            onDismiss = { showGuideDialog = false },
            onConnectClick = { viewModel.pingBackend() }
        )
    }

    if (showObdDialog) {
        ObdConnectionDialog(
            status = status,
            profileName = profile.profileName,
            onConnect = { viewModel.connectObd() },
            onDisconnect = { /* Disconnect or reset */ },
            onDismiss = { showObdDialog = false }
        )
    }

    if (showApiKeysDialog) {
        ApiKeyInitializationDialog(
            onDismiss = { showApiKeysDialog = false }
        )
    }
}

@Composable
private fun SimplifiedAlertBanner(
    activeAlert: ActiveAlert?,
    onDismiss: () -> Unit,
    onTestAlert: () -> Unit
) {
    if (activeAlert != null) {
        val isCritical = activeAlert.severity == AlertSeverity.CRITICAL
        val borderColor = if (isCritical) CockpitRed else CockpitAmber
        val containerColor = if (isCritical) CockpitRed.copy(alpha = 0.2f) else CockpitAmber.copy(alpha = 0.15f)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(borderColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = borderColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = activeAlert.title,
                            color = borderColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(borderColor.copy(alpha = 0.3f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isCritical) "CRITICAL" else "WARNING",
                                color = borderColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = activeAlert.message,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Action: ${activeAlert.actionAdvice}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    } else {
        // Simplified Normal Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CockpitCard)
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "System Status",
                    tint = CockpitGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "STATUS: ALL SYSTEMS NOMINAL",
                    color = CockpitGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "0 ACTIVE WARNINGS",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DashboardMlInferencesCard(
    behaviour: com.example.model.DriverBehaviourResponse?,
    fuel: com.example.model.FuelPredictionResponse?,
    isAnalyzing: Boolean,
    onRunInference: () -> Unit,
    onViewFullInsights: () -> Unit
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
            // Header
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
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "ML Models",
                        tint = CockpitSteel,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PREVIOUS ML INFERENCES",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onViewFullInsights,
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "DETAILED →",
                        color = CockpitSteel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. Driver Behaviour (XGBoost)
            val behaviourLabel = behaviour?.label ?: "Economical"
            val confidencePct = ((behaviour?.confidence ?: 0.94) * 100).toInt()
            val behaviourColor = when (behaviourLabel.lowercase(Locale.US)) {
                "aggressive" -> CockpitRed
                "economical", "eco" -> CockpitGreen
                else -> CockpitAmber
            }
            val behaviourMessage = behaviour?.ttsMessage ?: "Smooth eco-driving observed. Preserving fuel."

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CockpitCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = behaviourColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "XGBoost Driver Behaviour",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${behaviourLabel.uppercase(Locale.US)} ($confidencePct%)",
                            color = behaviourColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = behaviourMessage,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(behaviourColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = behaviourLabel.uppercase(Locale.US),
                            color = behaviourColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // 3. Fuel Physics (Tier 1 Physics)
            val mileageText = if (fuel?.mileageKmpl != null && !fuel.mileageKmpl.isNaN() && fuel.vssKmph > 1.0) {
                String.format(Locale.US, "%.1f km/L", fuel.mileageKmpl)
            } else if ((fuel?.vssKmph ?: 0.0) <= 1.0) {
                "Stationary / Idle"
            } else {
                "16.4 km/L"
            }
            val flowRateText = if (fuel != null) {
                String.format(Locale.US, "%.2f g/s", fuel.fcrGs)
            } else "1.84 g/s"

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CockpitCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = CockpitSteel,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Physics Fuel & Mileage (Tier 1)",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = mileageText,
                            color = CockpitGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instant flow: $flowRateText • Method: MAF Tier 1",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CockpitGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TIER 1",
                            color = CockpitGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Quick Trigger Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRunInference,
                    enabled = !isAnalyzing,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            color = CockpitSteel,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RUNNING INFERENCE...", color = CockpitSteel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = CockpitSteel,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RE-RUN ML INFERENCE", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
