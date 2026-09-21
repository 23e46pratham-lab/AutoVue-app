package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnomalyRecord
import com.example.model.NotificationSettings
import com.example.ui.dialogs.AnomaliesHistoryDialog
import com.example.ui.dialogs.AnomalyDetectedHorizontalBlock
import com.example.ui.dialogs.ApiKeyInitializationDialog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel
import java.util.Locale

enum class SettingsSubTab(val title: String, val icon: ImageVector) {
    PROFILE("Profile", Icons.Default.Person),
    VEHICLE("Vehicle", Icons.Default.DirectionsCar),
    NOTIFICATIONS("Notifications", Icons.Default.NotificationsActive),
    VOCAL("Vocal", Icons.Default.RecordVoiceOver),
    ANOMALIES("Anomalies", Icons.Default.Warning),
    SYSTEM("System", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SharedTelemetryViewModel,
    onBackClick: () -> Unit = {}
) {
    val status by viewModel.simulatorStatus.collectAsState()
    val voiceAlertsEnabled by viewModel.voiceAlertsEnabled.collectAsState()
    val visualAlertsEnabled by viewModel.visualAlertsEnabled.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val vehicleModel by viewModel.vehicleModel.collectAsState()
    val vehicleProfile by viewModel.vehicleProfile.collectAsState()
    val availableDatasets by viewModel.availableDatasets.collectAsState()
    val notifSettings by viewModel.notificationSettings.collectAsState()
    val detectedAnomalies by viewModel.detectedAnomalies.collectAsState()
    val health by viewModel.healthPrediction.collectAsState()
    val latestTick by viewModel.latestTick.collectAsState()

    var activeTab by remember { mutableStateOf(SettingsSubTab.NOTIFICATIONS) }
    var overflowMenuExpanded by remember { mutableStateOf(false) }
    var datasetMenuExpanded by remember { mutableStateOf(false) }
    var showAnomaliesDialog by remember { mutableStateOf(false) }
    var showApiKeysDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadDataset(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
    ) {
        // Top Header: < Settings and Overflow Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBackClick() }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Settings",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box {
                IconButton(onClick = { overflowMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary
                    )
                }

                DropdownMenu(
                    expanded = overflowMenuExpanded,
                    onDismissRequest = { overflowMenuExpanded = false },
                    modifier = Modifier.background(CockpitCard)
                ) {
                    DropdownMenuItem(
                        text = { Text("Initialize API Keys (Supabase)", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            overflowMenuExpanded = false
                            showApiKeysDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset Notifications to Defaults", color = TextPrimary) },
                        onClick = {
                            viewModel.setNotificationSettings(NotificationSettings())
                            overflowMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Enable All Applicable Alerts", color = TextPrimary) },
                        onClick = {
                            viewModel.updateNotificationSettings {
                                it.copy(
                                    generateStartTripMessage = true,
                                    generateEndTripMessage = true,
                                    generateConsumptionMessage = true,
                                    generateStyleDrivingMessage = true,
                                    generateLackOfAutonomyAlert = true,
                                    generateEngineOperatingTempMessage = true,
                                    generateOverspeedingAlert = true,
                                    generateAccel0To100Message = true
                                )
                            }
                            overflowMenuExpanded = false
                        }
                    )
                }
            }
        }

        // Main Scrollable Settings Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Constant size anomaly detected block (when anomalies detected and not on ANOMALIES tab)
            val hasActiveAnomaly = health?.isAnomaly == true || detectedAnomalies.isNotEmpty()
            if (hasActiveAnomaly && activeTab != SettingsSubTab.ANOMALIES) {
                AnomalyDetectedHorizontalBlock(
                    anomalyCount = detectedAnomalies.size.coerceAtLeast(1),
                    latestAnomaly = detectedAnomalies.firstOrNull(),
                    onClick = { activeTab = SettingsSubTab.ANOMALIES }
                )
            }

            when (activeTab) {
                SettingsSubTab.NOTIFICATIONS -> {
                    // ====== NOTIFICATION SETTINGS (Screenshots 1, 2, 5, 6) ======
                    // Section: General & Trip Notifications
                    SettingsCheckboxCard(
                        title = "Generate start trip message",
                        checked = notifSettings.generateStartTripMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateStartTripMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate end trip message",
                        checked = notifSettings.generateEndTripMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateEndTripMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate consumption message",
                        checked = notifSettings.generateConsumptionMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateConsumptionMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Style driving message",
                        checked = notifSettings.generateStyleDrivingMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateStyleDrivingMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate lack of autonomy alert",
                        checked = notifSettings.generateLackOfAutonomyAlert,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateLackOfAutonomyAlert = chk) }
                        }
                    )

                    // Section: Engine Operating Temperature (Screenshot 6)
                    SettingsCheckboxCard(
                        title = "Generate 'Engine operating temperature' message",
                        checked = notifSettings.generateEngineOperatingTempMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateEngineOperatingTempMessage = chk) }
                        }
                    )

                    SettingsTextWarningCard(
                        title = "Warning text of engine operating temperature",
                        text = notifSettings.warningTextEngineOperatingTemp,
                        defaultText = "It has been reached the engine operating temperature",
                        onTextChange = { txt ->
                            viewModel.updateNotificationSettings { it.copy(warningTextEngineOperatingTemp = txt) }
                        }
                    )

                    // Section: Low Engine Temperature (Screenshot 6)
                    SettingsCheckboxCard(
                        title = "Generate 'Low engine temperature' message",
                        checked = notifSettings.generateLowEngineTempMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateLowEngineTempMessage = chk) }
                        }
                    )

                    SettingsTextWarningCard(
                        title = "Warning text of low engine temperature",
                        text = notifSettings.warningTextLowEngineTemp,
                        defaultText = "You are insane, you can't push a cold engine",
                        onTextChange = { txt ->
                            viewModel.updateNotificationSettings { it.copy(warningTextLowEngineTemp = txt) }
                        }
                    )

                    // Section: Limiters & Sliders (Screenshot 5)
                    SettingsStepperSliderCard(
                        title = "Time out display message (sec)",
                        value = notifSettings.timeOutDisplayMessageSec,
                        range = 1f..10f,
                        step = 1,
                        unit = "s",
                        onValueChange = { valSec ->
                            viewModel.updateNotificationSettings { it.copy(timeOutDisplayMessageSec = valSec) }
                        }
                    )

                    SettingsStepperSliderCard(
                        title = "Overspeeding message over (km/h)",
                        value = notifSettings.overspeedingMessageOverKmh,
                        range = 60f..200f,
                        step = 5,
                        unit = "km/h",
                        onValueChange = { valKmh ->
                            viewModel.updateNotificationSettings { it.copy(overspeedingMessageOverKmh = valKmh) }
                        }
                    )

                    SettingsTextWarningCard(
                        title = "Overspeeding alert",
                        text = notifSettings.overspeedingAlertText,
                        defaultText = "Slow down. You are always the usual speeder",
                        onTextChange = { txt ->
                            viewModel.updateNotificationSettings { it.copy(overspeedingAlertText = txt) }
                        }
                    )

                    SettingsStepperSliderCard(
                        title = "Overspeeding alert time out (sec.)",
                        value = notifSettings.overspeedingAlertTimeOutSec,
                        range = 10f..300f,
                        step = 10,
                        unit = "s",
                        onValueChange = { valSec ->
                            viewModel.updateNotificationSettings { it.copy(overspeedingAlertTimeOutSec = valSec) }
                        }
                    )

                    SettingsStepperSliderCard(
                        title = "You have been driving for (min.) message",
                        value = notifSettings.drivingDurationMessageMin,
                        range = 15f..180f,
                        step = 5,
                        unit = "min",
                        onValueChange = { valMin ->
                            viewModel.updateNotificationSettings { it.copy(drivingDurationMessageMin = valMin) }
                        }
                    )

                    // Section: Performance & Benchmarks (Screenshots 1 & 2)
                    SettingsCheckboxCard(
                        title = "Generate overspeeding alert.",
                        checked = notifSettings.generateOverspeedingAlert,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateOverspeedingAlert = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Max power message",
                        checked = notifSettings.generateMaxPowerMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateMaxPowerMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Max torque message",
                        checked = notifSettings.generateMaxTorqueMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateMaxTorqueMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Acceleration 0-50 km/h message",
                        checked = notifSettings.generateAccel0To50Message,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateAccel0To50Message = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Acceleration 0-100 km/h message",
                        checked = notifSettings.generateAccel0To100Message,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateAccel0To100Message = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Pickup 40-70 km/h message",
                        checked = notifSettings.generatePickup40To70Message,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generatePickup40To70Message = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Pickup 60-100 km/h message",
                        checked = notifSettings.generatePickup60To100Message,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generatePickup60To100Message = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Pickup 80-120 km/h message",
                        checked = notifSettings.generatePickup80To120Message,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generatePickup80To120Message = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate Pickup 90-130 km/h message",
                        checked = notifSettings.generatePickup90To130Message,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generatePickup90To130Message = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate braking 100-0 km/h message",
                        checked = notifSettings.generateBraking100To0Message,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateBraking100To0Message = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Generate braking 50-0 km/h message",
                        checked = notifSettings.generateBraking50To0Message,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(generateBraking50To0Message = chk) }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                SettingsSubTab.VOCAL -> {
                    // ====== VOCAL / SPEECH NOTIFICATIONS (Screenshots 3 & 4) ======
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, CockpitSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Text-to-Speech Voice Engine",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Speak critical safety & telemetry alerts through vehicle audio",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = voiceAlertsEnabled,
                                onCheckedChange = { viewModel.toggleVoiceAlerts(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CockpitSteel,
                                    checkedTrackColor = CockpitSteel.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }

                    SettingsCheckboxCard(
                        title = "Start trip vocal message",
                        checked = notifSettings.startTripVocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(startTripVocalMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "End trip vocal message",
                        checked = notifSettings.endTripVocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(endTripVocalMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Consumption vocal message",
                        checked = notifSettings.consumptionVocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(consumptionVocalMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Style driving vocal message",
                        checked = notifSettings.styleDrivingVocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(styleDrivingVocalMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Lack of autonomy vocal alert",
                        checked = notifSettings.lackOfAutonomyVocalAlert,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(lackOfAutonomyVocalAlert = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Read 'Engine operating temperature' message",
                        checked = notifSettings.engineOperatingTempVocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(engineOperatingTempVocalMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Acceleration 0-100 km/h vocal message",
                        checked = notifSettings.acceleration0To100VocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(acceleration0To100VocalMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Pickup 40-70 km/h vocal message",
                        checked = notifSettings.pickup40To70VocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(pickup40To70VocalMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Pickup 60-100 km/h vocal message",
                        checked = notifSettings.pickup60To100VocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(pickup60To100VocalMessage = chk) }
                        }
                    )

                    SettingsCheckboxCard(
                        title = "Pickup 80-120 km/h vocal message",
                        checked = notifSettings.pickup80To120VocalMessage,
                        onCheckedChange = { chk ->
                            viewModel.updateNotificationSettings { it.copy(pickup80To120VocalMessage = chk) }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                SettingsSubTab.PROFILE -> {
                    // Driver & User Profile Settings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, CockpitSurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "DRIVER PROFILE",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = userName,
                                onValueChange = { viewModel.updateUserName(it) },
                                label = { Text("Driver Name", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = CockpitSteel,
                                    unfocusedBorderColor = CockpitSurfaceBorder
                                )
                            )

                            OutlinedTextField(
                                value = vehicleModel,
                                onValueChange = { viewModel.updateVehicleModel(it) },
                                label = { Text("Vehicle Description", color = TextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = CockpitSteel,
                                    unfocusedBorderColor = CockpitSurfaceBorder
                                )
                            )
                        }
                    }
                }

                SettingsSubTab.VEHICLE -> {
                    // Vehicle Profile Specs
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, CockpitSurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "VEHICLE CONFIGURATION",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text("Engine Displacement: ${vehicleProfile.displacementCc} cc", color = TextSecondary, fontSize = 13.sp)
                            Text("Fuel Type: ${vehicleProfile.fuelSupply.uppercase()}", color = TextSecondary, fontSize = 13.sp)
                            Text("Peak Power: ${vehicleProfile.maxPowerHp} HP", color = TextSecondary, fontSize = 13.sp)
                            Text("Total Curb Weight: ${vehicleProfile.totalWeightKg} KG", color = TextSecondary, fontSize = 13.sp)
                            Text("Tank Capacity: ${vehicleProfile.tankCapacityLiters} L", color = TextSecondary, fontSize = 13.sp)
                            Text("VIN: ${vehicleProfile.vin}", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }

                SettingsSubTab.SYSTEM -> {
                    // Simulator & Backend Control
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, CockpitSurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "SIMULATOR SYSTEM CONTROL",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (status != null) {
                                val s = status!!
                                ExposedDropdownMenuBox(
                                    expanded = datasetMenuExpanded,
                                    onExpandedChange = { datasetMenuExpanded = !datasetMenuExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = s.datasetName ?: "Select Dataset",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Active Dataset", color = TextMuted) },
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                        },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = datasetMenuExpanded,
                                        onDismissRequest = { datasetMenuExpanded = false },
                                        modifier = Modifier.background(CockpitCard)
                                    ) {
                                        availableDatasets.forEach { dataset ->
                                            DropdownMenuItem(
                                                text = { Text(dataset.filename, color = TextPrimary) },
                                                onClick = {
                                                    viewModel.changeDataset(dataset.datasetId)
                                                    datasetMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    if (s.state == "running") {
                                        IconButton(onClick = { viewModel.pauseSimulation() }) {
                                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = CockpitSteel)
                                        }
                                    } else if (s.state == "paused") {
                                        IconButton(onClick = { viewModel.resumeSimulation() }) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Emerald400)
                                        }
                                    } else {
                                        IconButton(onClick = { viewModel.startSimulation() }) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Emerald400)
                                        }
                                    }

                                    IconButton(onClick = { viewModel.stopSimulation() }) {
                                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
                                    }

                                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                                        Icon(Icons.Default.Upload, contentDescription = "Upload Dataset", tint = CockpitSteel)
                                    }

                                    IconButton(onClick = { viewModel.fetchDatasets() }) {
                                        Icon(Icons.Default.Sync, contentDescription = "Refresh", tint = CockpitSteel)
                                    }
                                }

                                Text("State: ${s.state}", color = TextSecondary, fontSize = 12.sp)
                                Text("Speed: ${s.speed}x", color = TextSecondary, fontSize = 12.sp)
                                Text("Progress: ${"%.1f".format(s.playbackPercent)}%", color = TextSecondary, fontSize = 12.sp)
                            } else {
                                Text("Simulator status offline or connecting...", color = TextMuted, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.pingBackend() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CockpitSteel)
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = "Ping", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("PING BACKEND SERVER", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SettingsSubTab.ANOMALIES -> {
                    // ====== ANOMALIES MONITORING & HISTORY (Everything Intact) ======
                    val hasAnomaly = health?.isAnomaly == true || detectedAnomalies.isNotEmpty()
                    if (hasAnomaly) {
                        AnomalyDetectedHorizontalBlock(
                            anomalyCount = detectedAnomalies.size.coerceAtLeast(1),
                            latestAnomaly = detectedAnomalies.firstOrNull(),
                            onClick = { showAnomaliesDialog = true }
                        )
                    } else {
                        // Nominal Banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CockpitGreen.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, CockpitGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(CockpitGreen.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Nominal",
                                        tint = CockpitGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "LSTM AUTOENCODER: NOMINAL",
                                        color = CockpitGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "All powertrain and telemetry streams within safe baseline envelope",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Autoencoder Vehicle Health Status Card
                    val isAnomaly = health?.isAnomaly == true
                    val healthColor = if (isAnomaly) CockpitRed else CockpitGreen
                    val healthStatusText = if (isAnomaly) "ANOMALY FLAGGED" else "NORMAL HEALTH (99.8%)"
                    val currentScore = health?.anomalyScore ?: 0.0018
                    val threshold = health?.threshold ?: 0.0040

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, if (isAnomaly) CockpitRed.copy(alpha = 0.8f) else CockpitSurfaceBorder)
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = healthColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "VEHICLE HEALTH (LSTM AUTOENCODER)",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(healthColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = healthStatusText,
                                        color = healthColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            // Reconstruction Error Comparison
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Reconstruction Error:", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        String.format(Locale.US, "%.5f", currentScore),
                                        color = healthColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Baseline Threshold Limit:", color = TextMuted, fontSize = 11.sp)
                                    Text(
                                        String.format(Locale.US, "%.4f", threshold),
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                val progress = (currentScore / (threshold * 1.5)).coerceIn(0.0, 1.0).toFloat()
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = healthColor,
                                    trackColor = CockpitCardElevated
                                )
                            }

                            // Monitored Sensor Envelope Tags
                            Text(
                                text = "MONITORED SENSORS & ENVELOPE",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val coolant = latestTick?.data?.coolantTemp?.toInt() ?: 85
                                val throttle = latestTick?.data?.throttlePos?.toInt() ?: 20
                                val rpm = latestTick?.data?.rpm?.toInt() ?: 750
                                AnomalySensorTag("Coolant: ${coolant}°C", isAnomaly = coolant > 100)
                                AnomalySensorTag("Throttle: ${throttle}%", isAnomaly = throttle > 85)
                                AnomalySensorTag("RPM: $rpm", isAnomaly = rpm > 4500)
                            }
                        }
                    }

                    // Action Buttons (Simulate, Clear, View Modal Dialog)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.simulateTestAnomaly() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CockpitRed.copy(alpha = 0.85f))
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SIMULATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.clearAnomalies() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                            border = BorderStroke(1.dp, CockpitSurfaceBorder)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CLEAR", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAnomaliesDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CockpitSteel)
                        ) {
                            Text("VIEW LOG", color = CockpitBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Detected Anomalies Log List
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, CardBorder)
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
                                Text(
                                    text = "ANOMALIES OBTAINED TILL NOW",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (detectedAnomalies.isNotEmpty()) CockpitRed.copy(alpha = 0.2f) else CockpitCardElevated)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${detectedAnomalies.size} LOGGED",
                                        color = if (detectedAnomalies.isNotEmpty()) CockpitRed else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (detectedAnomalies.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 18.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No anomalies detected yet in this driving session.",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    detectedAnomalies.forEachIndexed { index, anomaly ->
                                        SettingsAnomalyItemCard(index = index + 1, anomaly = anomaly)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sub-Tab Bar (Icons matching screenshots 1, 2, 3, 4, 5, 6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CockpitCard)
                .border(BorderStroke(1.dp, CardBorder))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsSubTab.values().forEach { tab ->
                val isSelected = activeTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { activeTab = tab }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) CockpitSteel else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) CockpitSteel else Color.Transparent)
                    )
                }
            }
        }
    }

    if (showAnomaliesDialog) {
        AnomaliesHistoryDialog(
            anomalies = detectedAnomalies,
            onDismiss = { showAnomaliesDialog = false },
            onClearAll = { viewModel.clearAnomalies() },
            onSimulateTestAnomaly = { viewModel.simulateTestAnomaly() }
        )
    }

    if (showApiKeysDialog) {
        ApiKeyInitializationDialog(
            onDismiss = { showApiKeysDialog = false }
        )
    }
}

@Composable
private fun AnomalySensorTag(label: String, isAnomaly: Boolean) {
    val tagColor = if (isAnomaly) CockpitRed else CockpitSteel
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tagColor.copy(alpha = 0.15f))
            .border(1.dp, tagColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (isAnomaly) CockpitRed else TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsAnomalyItemCard(
    index: Int,
    anomaly: AnomalyRecord
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CockpitCardElevated,
        border = BorderStroke(1.dp, CockpitRed.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                            .size(20.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(CockpitRed.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CockpitRed,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "INCIDENT #$index",
                        color = CockpitRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = anomaly.formattedTime,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Score details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Score: ${String.format(Locale.US, "%.5f", anomaly.anomalyScore)}",
                    color = CockpitRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Threshold: ${String.format(Locale.US, "%.4f", anomaly.threshold)}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Triggered features / Outlier Sensors
            if (anomaly.triggeredFeatures.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    anomaly.triggeredFeatures.forEach { feature ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CockpitRed.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = feature,
                                color = CockpitRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Telemetry Snapshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(CockpitBackground)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Speed: ${anomaly.speedKmh.toInt()} km/h", color = TextSecondary, fontSize = 10.sp)
                Text("RPM: ${anomaly.rpm.toInt()}", color = TextSecondary, fontSize = 10.sp)
                Text("Coolant: ${anomaly.coolantTemp.toInt()}°C", color = TextSecondary, fontSize = 10.sp)
                Text("Throttle: ${anomaly.throttlePos.toInt()}%", color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}

// ====== REUSABLE SETTINGS COMPONENTS ======

@Composable
fun SettingsCheckboxCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = BorderStroke(1.dp, CockpitSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = CockpitSteel,
                    checkmarkColor = TextPrimary,
                    uncheckedColor = TextMuted
                )
            )
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SettingsStepperSliderCard(
    title: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    step: Int = 1,
    unit: String = "",
    onValueChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = BorderStroke(1.dp, CockpitSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            val newVal = (value - step).coerceAtLeast(range.start.toInt())
                            onValueChange(newVal)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // Value box
                Box(
                    modifier = Modifier
                        .widthIn(min = 58.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unit.isNotEmpty()) "$value $unit" else "$value",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Plus button
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            val newVal = (value + step).coerceAtMost(range.endInclusive.toInt())
                            onValueChange(newVal)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = CockpitSteel,
                    activeTrackColor = CockpitSteel,
                    inactiveTrackColor = CockpitSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SettingsTextWarningCard(
    title: String,
    text: String,
    defaultText: String,
    onTextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = BorderStroke(1.dp, CockpitSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reset to default button
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onTextChange(defaultText) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CockpitSteel,
                        unfocusedBorderColor = CockpitSurfaceBorder,
                        focusedContainerColor = CockpitBackground,
                        unfocusedContainerColor = CockpitBackground
                    )
                )
            }
        }
    }
}
