package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetryData
import com.example.model.TripHistoryItem
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGaugeTrack
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.GlassDropdown
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel
import java.util.Locale

@Composable
fun VehicleStatusScreen(
    viewModel: SharedTelemetryViewModel,
    onOpenProfile: () -> Unit = {},
    onOpenOverflowMenu: () -> Unit = {}
) {
    val tick by viewModel.latestTick.collectAsState()
    val profile by viewModel.vehicleProfile.collectAsState()
    val trips by viewModel.tripHistory.collectAsState()

    // Active selected trip for inline detail view & contextual embedded map
    var selectedTripId by remember { mutableStateOf<String?>(trips.firstOrNull()?.id) }

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
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header: SmartControl OBD2 + Driver Status & Switch Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SmartControl OBD2",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${profile.profileName}: Connected | System Active",
                    color = CockpitGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenProfile) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Profile",
                        tint = CockpitSteel
                    )
                }
                IconButton(onClick = onOpenOverflowMenu) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = TextSecondary
                    )
                }
            }
        }

        // =========================================================================
        // TOP SECTION: MAIN VEHICLE INFO BOX (Translucent Glassmorphism)
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Profile & Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CockpitCardElevated)
                            .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { onOpenProfile() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.profileName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Seat Leon 1.6 FSI",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Edit Profile",
                            tint = CockpitSteel,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CockpitGreen.copy(alpha = 0.15f))
                            .border(1.dp, CockpitGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(CockpitGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Ready to drive",
                            color = CockpitGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Wireframe Car Visualization + Fuel Vertical Gauge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wireframe Top-Down Vehicle Diagram with Diagnostics
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(138.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val carW = 64.dp.toPx()
                            val carH = 118.dp.toPx()

                            // Outer chassis silhouette
                            drawRoundRect(
                                color = CockpitSteel,
                                topLeft = Offset(cx - carW / 2f, cy - carH / 2f),
                                size = Size(carW, carH),
                                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Windshield front arc
                            val frontWsY = cy - carH * 0.22f
                            drawLine(
                                color = CockpitGaugeTrack,
                                start = Offset(cx - carW * 0.40f, frontWsY),
                                end = Offset(cx + carW * 0.40f, frontWsY),
                                strokeWidth = 2.dp.toPx()
                            )

                            // Rear windshield
                            val rearWsY = cy + carH * 0.22f
                            drawLine(
                                color = CockpitGaugeTrack,
                                start = Offset(cx - carW * 0.38f, rearWsY),
                                end = Offset(cx + carW * 0.38f, rearWsY),
                                strokeWidth = 2.dp.toPx()
                            )

                            // 4 Wheels
                            val wheelW = 8.dp.toPx()
                            val wheelH = 18.dp.toPx()
                            // Front Left & Right
                            drawRoundRect(
                                color = CockpitSteel.copy(alpha = 0.85f),
                                topLeft = Offset(cx - carW / 2f - wheelW / 2f, cy - carH * 0.34f),
                                size = Size(wheelW, wheelH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            drawRoundRect(
                                color = CockpitSteel.copy(alpha = 0.85f),
                                topLeft = Offset(cx + carW / 2f - wheelW / 2f, cy - carH * 0.34f),
                                size = Size(wheelW, wheelH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            // Rear Left & Right
                            drawRoundRect(
                                color = CockpitSteel.copy(alpha = 0.85f),
                                topLeft = Offset(cx - carW / 2f - wheelW / 2f, cy + carH * 0.18f),
                                size = Size(wheelW, wheelH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            drawRoundRect(
                                color = CockpitSteel.copy(alpha = 0.85f),
                                topLeft = Offset(cx + carW / 2f - wheelW / 2f, cy + carH * 0.18f),
                                size = Size(wheelW, wheelH),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }

                        // Diagnostics Telemetry Subsystem Status Overlay
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Systems Ok",
                                tint = CockpitGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ECU OK",
                                color = CockpitGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "14.1V BATTERY",
                                color = TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Vertical Translucent Fuel Gauge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(84.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(96.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CockpitGaugeTrack)
                                .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(6.dp))
                                .padding(2.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val fuelFrac = (profile.fuelLeftPercent.toFloat() / 100f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(92.dp * fuelFrac)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(CockpitAmber, CockpitAmber.copy(alpha = 0.75f))
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = "Fuel",
                                tint = CockpitAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${profile.fuelLeftPercent.toInt()}%",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Auth. 145 km",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Consolidated Key Metrics Ribbon (Translucent Glassmorphism)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Odometer", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = "${profile.odometerKm.toInt()} km",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(text = "Consump.", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = "${profile.consumptionL100km} L/100km",
                            color = CockpitAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(text = "Coolant", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = "${currentData.coolantTemp.toInt()} °C",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(text = "Battery", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = "14.1 V",
                            color = CockpitGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =========================================================================
        // MIDDLE SECTION: VERTICAL PREVIOUS TRIPS FEED
        // Only shown when trips exist (no mock data)
        // =========================================================================
        if (trips.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Previous Trips",
                    tint = CockpitSteel,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Previous Trips",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CockpitCardElevated)
                    .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${trips.size} journeys recorded",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Vertical stack of small translucent trip boxes
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            trips.forEach { trip ->
                val isSelected = selectedTripId == trip.id

                // Small, translucent trip box (clickable button)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selectedTripId = if (isSelected) null else trip.id
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) CockpitCardElevated else CockpitCard
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) CockpitSteel else CardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Top row: Departure -> Destination + Date & Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CockpitSteel.copy(alpha = 0.2f) else CockpitGaugeTrack),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Route,
                                        contentDescription = "Trip",
                                        tint = if (isSelected) TextPrimary else CockpitSteel,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${trip.departure} → ${trip.destination}",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${trip.date} • ${trip.time}",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${trip.fuelCostEuro} €",
                                    color = CockpitAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isSelected) "Collapse" else "Expand",
                                    tint = CockpitSteel,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Quick summary pills row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Distance
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CockpitGaugeTrack)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${trip.distanceKm} km",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Duration
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CockpitGaugeTrack)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${trip.durationMinutes} min",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Avg speed
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CockpitGaugeTrack)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Avg ${trip.avgSpeedKmh.toInt()} km/h",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Drive Score Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CockpitGreen.copy(alpha = 0.15f))
                                    .border(1.dp, CockpitGreen.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${trip.driveScorePercent.toInt()}% Eco",
                                    color = CockpitGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // =========================================================================
                        // SECTION 4: INLINE DETAIL VIEW & CONTEXTUAL EMBEDDED MAP SECTION
                        // Revealed when this specific vertical trip box is clicked
                        // =========================================================================
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Subtle frosted divider line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(CardBorder)
                                )

                                // 1. TRIP DETAILS: Summary of key metrics
                                Text(
                                    text = "Journey Telemetry Summary",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Grid of key telemetry metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TripMetricTile(
                                        label = "Duration",
                                        value = "${trip.durationMinutes} min",
                                        icon = Icons.Default.Timer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TripMetricTile(
                                        label = "Max Speed",
                                        value = "${trip.maxSpeedKmh.toInt()} km/h",
                                        icon = Icons.Default.Speed,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TripMetricTile(
                                        label = "Fuel Used",
                                        value = "${trip.fuelUsedLiters} L",
                                        icon = Icons.Default.LocalGasStation,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TripMetricTile(
                                        label = "Peak RPM",
                                        value = "${trip.maxRpm}",
                                        icon = Icons.Default.Bolt,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TripMetricTile(
                                        label = "Efficiency",
                                        value = "${trip.consumptionKmL} km/L",
                                        icon = Icons.Default.CheckCircle,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TripMetricTile(
                                        label = "Hard Brakes",
                                        value = "${trip.hardBrakes}",
                                        icon = Icons.Default.Warning,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Specific Journey Events
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CockpitGaugeTrack.copy(alpha = 0.6f))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Recorded Journey Events",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    trip.specificEvents.forEach { event ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(CockpitSteel)
                                            )
                                            Text(
                                                text = event,
                                                color = TextPrimary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // 2. EMBEDDED MAP SECTION: Solely visualizing this specific journey's route
                                Text(
                                    text = "Contextual Journey Map",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                EmbeddedTripMapView(trip = trip)
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

/**
 * Small telemetry metric tile with glass styling
 */
@Composable
private fun TripMetricTile(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CockpitGaugeTrack.copy(alpha = 0.5f))
            .border(1.dp, CockpitSurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = CockpitSteel,
                    modifier = Modifier.size(12.dp)
                )
                Text(text = label, color = TextMuted, fontSize = 9.sp)
            }
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Embedded map section dedicated solely to visualizing a specific trip's route and telemetry insights.
 */
@Composable
private fun EmbeddedTripMapView(
    trip: TripHistoryItem
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CockpitCardElevated)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
    ) {
        // Tactical Map Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Secondary tactical grid
            val gridStep = 32.dp.toPx() * zoomLevel
            val startX = (cx % gridStep)
            val startY = (cy % gridStep)

            var x = startX
            while (x < w) {
                drawLine(
                    color = CardBorder,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.dp.toPx()
                )
                x += gridStep
            }

            var y = startY
            while (y < h) {
                drawLine(
                    color = CardBorder,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += gridStep
            }

            // Road Network Artery
            val arteryPath = Path().apply {
                when (trip.routeType) {
                    0 -> {
                        // Curved metropolitan bypass
                        moveTo(20.dp.toPx(), h - 20.dp.toPx())
                        cubicTo(w * 0.3f, h * 0.8f, w * 0.4f, h * 0.25f, w - 24.dp.toPx(), 26.dp.toPx())
                    }
                    1 -> {
                        // S-curve suburban route
                        moveTo(24.dp.toPx(), h * 0.7f)
                        cubicTo(w * 0.4f, h * 0.9f, w * 0.5f, h * 0.2f, w - 24.dp.toPx(), h * 0.35f)
                    }
                    2 -> {
                        // Diagonal highway
                        moveTo(20.dp.toPx(), 30.dp.toPx())
                        cubicTo(w * 0.35f, h * 0.4f, w * 0.65f, h * 0.6f, w - 20.dp.toPx(), h - 30.dp.toPx())
                    }
                    else -> {
                        // Long-distance transit
                        moveTo(24.dp.toPx(), h * 0.85f)
                        cubicTo(w * 0.25f, h * 0.2f, w * 0.75f, h * 0.8f, w - 24.dp.toPx(), 30.dp.toPx())
                    }
                }
            }

            // Draw road outline
            drawPath(
                path = arteryPath,
                color = CockpitSurfaceBorder,
                style = Stroke(width = 10.dp.toPx() * zoomLevel)
            )

            // Draw journey path with telemetry speed colors
            drawPath(
                path = arteryPath,
                color = CockpitGreen,
                style = Stroke(width = 3.5.dp.toPx() * zoomLevel)
            )

            // Start Location Pin (Green Point A)
            val startPoint = when (trip.routeType) {
                0 -> Offset(20.dp.toPx(), h - 20.dp.toPx())
                1 -> Offset(24.dp.toPx(), h * 0.7f)
                2 -> Offset(20.dp.toPx(), 30.dp.toPx())
                else -> Offset(24.dp.toPx(), h * 0.85f)
            }
            drawCircle(
                color = CockpitGreen.copy(alpha = 0.3f),
                radius = 12.dp.toPx(),
                center = startPoint
            )
            drawCircle(
                color = CockpitGreen,
                radius = 6.dp.toPx(),
                center = startPoint
            )

            // End Location Pin (Red Point B)
            val endPoint = when (trip.routeType) {
                0 -> Offset(w - 24.dp.toPx(), 26.dp.toPx())
                1 -> Offset(w - 24.dp.toPx(), h * 0.35f)
                2 -> Offset(w - 20.dp.toPx(), h - 30.dp.toPx())
                else -> Offset(w - 24.dp.toPx(), 30.dp.toPx())
            }
            drawCircle(
                color = CockpitRed.copy(alpha = 0.3f),
                radius = 12.dp.toPx(),
                center = endPoint
            )
            drawCircle(
                color = CockpitRed,
                radius = 6.dp.toPx(),
                center = endPoint
            )

            // Telemetry Insight Waypoint (Peak Speed callout)
            val midPoint = Offset(cx, cy)
            drawCircle(
                color = CockpitAmber.copy(alpha = 0.25f),
                radius = 16.dp.toPx() * zoomLevel,
                center = midPoint
            )
            drawCircle(
                color = CockpitAmber,
                radius = 5.dp.toPx() * zoomLevel,
                center = midPoint
            )
        }

        // Top-left start/end pill
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CockpitCard.copy(alpha = 0.9f))
                .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CockpitGreen)
            )
            Text(
                text = "${trip.departure} to ${trip.destination}",
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Midpoint Telemetry Insight Callout
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(6.dp))
                .background(CockpitCardElevated.copy(alpha = 0.92f))
                .border(1.dp, CockpitAmber.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Peak: ${trip.maxSpeedKmh.toInt()} km/h | 94% Eco",
                color = CockpitAmber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Zoom In & Out Floating Controls
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(CockpitCardElevated)
                    .border(1.dp, CockpitSurfaceBorder, CircleShape)
                    .clickable { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.2f) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(CockpitCardElevated)
                    .border(1.dp, CockpitSurfaceBorder, CircleShape)
                    .clickable { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.7f) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Bottom Bar: GPS Fix & Waypoints
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(CockpitCard.copy(alpha = 0.92f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS",
                        tint = CockpitGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "GPS Logged • ${trip.waypoints.joinToString(" → ")}",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }

                Text(
                    text = "${trip.distanceKm} km",
                    color = CockpitGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
