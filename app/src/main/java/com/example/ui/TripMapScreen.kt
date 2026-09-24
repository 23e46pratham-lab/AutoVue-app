package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.components.TopTelemetryHud
import com.example.model.TelemetryData
import com.example.model.TripMapMode
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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Locale

@Composable
fun TripMapScreen(
    viewModel: SharedTelemetryViewModel,
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val tick by viewModel.latestTick.collectAsState()
    val tripMinutes by viewModel.tripElapsedTimeMinutes.collectAsState()
    val tripDist by viewModel.tripDistanceKm.collectAsState()
    val tripCost by viewModel.tripCostEuro.collectAsState()
    val voiceEnabled by viewModel.voiceAlertsEnabled.collectAsState()
    val alertsEnabled by viewModel.visualAlertsEnabled.collectAsState()

    val routeData by viewModel.routeData.collectAsState()
    val tripSummary by viewModel.tripSummary.collectAsState()
    val liveGpsTrail by viewModel.liveGpsTrail.collectAsState()

    val tripTickBuffer by viewModel.tripTickBuffer.collectAsState()
    val currentMode by viewModel.currentMapMode.collectAsState()
    val lowFuelAlert by viewModel.lowFuelAlert.collectAsState()

    // State for the osmdroid map reference
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Track overlays for the telemetry heatmap and car marker
    val tickOverlays = remember { mutableStateOf<List<Polyline>>(emptyList()) }
    val carMarker = remember { mutableStateOf<Marker?>(null) }
    val initialCentered = remember { mutableStateOf(false) }

    // Default location (Mangalore, matching existing defaults)
    val defaultLat = 12.913452
    val defaultLon = 74.889218

    LaunchedEffect(Unit) {
        viewModel.fetchGpsRouteAndSummary()
    }

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
        pedalE = 0.0,
        lat = 12.913452,
        lon = 74.889218,
        elevationM = 24.5,
        gpsBearing = 42.0,
        gpsSpeedMs = 0.0,
        gpsFix = 1
    )

    val lat = currentData.lat
    val lon = currentData.lon
    val elevation = currentData.elevationM
    val bearing = currentData.gpsBearing
    val gpsSpeedMs = currentData.gpsSpeedMs
    val gpsSpeedKmh = if (gpsSpeedMs != null) gpsSpeedMs * 3.6 else null
    val gpsFix = currentData.gpsFix
    val hasGps = currentData.hasGps || routeData?.hasGps == true || liveGpsTrail.isNotEmpty()

    // Draw/update heatmap / route overlays whenever telemetry or mode changes
    LaunchedEffect(tripTickBuffer, currentMode) {
        val mv = mapViewRef.value ?: return@LaunchedEffect
        // Remove existing overlays
        tickOverlays.value.forEach { mv.overlays.remove(it) }
        val newOverlays = mutableListOf<Polyline>()

        val validTicks = tripTickBuffer.filter { it.lat != null && it.lon != null }
        if (validTicks.isNotEmpty()) {
            if (validTicks.size > 1) {
                when (currentMode) {
                    TripMapMode.ROUTE -> {
                        val pts = validTicks.map { GeoPoint(it.lat, it.lon) }
                        val poly = Polyline().apply {
                            setPoints(pts)
                            outlinePaint.color = android.graphics.Color.parseColor("#FF6B35")
                            outlinePaint.strokeWidth = 10f
                            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                            outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        }
                        mv.overlays.add(0, poly)
                        newOverlays.add(poly)
                    }
                    else -> {
                        // Segmented heatmap rendering based on active mode
                        var currentSegmentColor = -1
                        var currentPoints = mutableListOf<GeoPoint>()

                        for (i in 0 until validTicks.size - 1) {
                            val tick1 = validTicks[i]
                            val tick2 = validTicks[i + 1]
                            val pt1 = GeoPoint(tick1.lat, tick1.lon)
                            val pt2 = GeoPoint(tick2.lat, tick2.lon)

                            val color = when (currentMode) {
                                TripMapMode.FUEL -> getFuelColor(tick2.instantConsumption)
                                TripMapMode.SPEED -> getSpeedColor(tick2.vss)
                                TripMapMode.BEHAVIOUR -> getBehaviourColor(tick2.drivingProfile)
                                TripMapMode.ANOMALY -> getAnomalyColor(tick2.anomalyScore)
                                TripMapMode.ROUTE -> android.graphics.Color.parseColor("#FF6B35")
                            }

                            if (currentSegmentColor == -1) {
                                currentSegmentColor = color
                                currentPoints.add(pt1)
                                currentPoints.add(pt2)
                            } else if (currentSegmentColor == color) {
                                currentPoints.add(pt2)
                            } else {
                                // Flush previous polyline segment
                                val poly = Polyline().apply {
                                    setPoints(currentPoints)
                                    outlinePaint.color = currentSegmentColor
                                    outlinePaint.strokeWidth = 10f
                                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                                }
                                mv.overlays.add(0, poly)
                                newOverlays.add(poly)

                                // Start new segment from previous end point for seamless continuation
                                currentSegmentColor = color
                                currentPoints = mutableListOf(pt1, pt2)
                            }
                        }

                        if (currentPoints.size > 1) {
                            val poly = Polyline().apply {
                                setPoints(currentPoints)
                                outlinePaint.color = currentSegmentColor
                                outlinePaint.strokeWidth = 10f
                                outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                                outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                            }
                            mv.overlays.add(0, poly)
                            newOverlays.add(poly)
                        }
                    }
                }
            }

            // Update car marker to last known GPS position
            val last = validTicks.last()
            val gp = GeoPoint(last.lat, last.lon)
            val existing = carMarker.value
            if (existing != null) {
                existing.position = gp
                existing.rotation = -(last.gpsBearing.toFloat())
            } else {
                val marker = Marker(mv).apply {
                    position = gp
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    rotation = -(last.gpsBearing.toFloat())
                    icon = ContextCompat.getDrawable(mv.context, android.R.drawable.ic_menu_mylocation)
                    title = "Vehicle"
                }
                mv.overlays.add(marker)
                carMarker.value = marker
            }

            // Initial auto-centering on vehicle location if first time
            if (!initialCentered.value) {
                mv.controller.setCenter(gp)
                mv.controller.setZoom(15.0)
                initialCentered.value = true
            }

            tickOverlays.value = newOverlays
            mv.invalidate()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Persistent Top Telemetry HUD
        TopTelemetryHud(
            data = currentData,
            elapsedMinutes = tripMinutes,
            distanceKm = tripDist,
            costEuro = tripCost,
            voiceEnabled = voiceEnabled,
            alertsEnabled = alertsEnabled,
            onProfileClick = onOpenProfile,
            onAlertsToggle = { viewModel.toggleVisualAlerts() },
            onVoiceToggle = { viewModel.toggleVoiceAlerts() }
        )

        // Task 3: Refueling Awareness Alert Banner (Dismissible, auto-dismiss 8s, top left border)
        AnimatedVisibility(
            visible = lowFuelAlert?.isVisible == true,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F2E)),
                border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Left border #eab308 (yellow warning)
                            drawRect(
                                color = Color(0xFFEAB308),
                                topLeft = Offset(0f, 0f),
                                size = Size(4.dp.toPx(), size.height)
                            )
                        }
                        .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⛽",
                            fontSize = 20.sp,
                            color = Color(0xFFEAB308)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Low Fuel Estimated",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val rangeDisplay = lowFuelAlert?.let {
                                "~${it.estimatedRangeKm.toInt()} km remaining · Consider refueling soon"
                            } ?: "~38 km remaining · Consider refueling soon"
                            Text(
                                text = rangeDisplay,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            val consDisplay = lowFuelAlert?.let {
                                "Avg consumption: ${String.format(Locale.US, "%.1f", it.avgConsumptionL100km)} L/100km"
                            } ?: "Avg consumption: 8.2 L/100km"
                            Text(
                                text = consDisplay,
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.dismissFuelAlert() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss fuel alert",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // GPS Satellite & Fix Status Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val fixColor = when (gpsFix) {
                        1 -> CockpitGreen
                        0 -> CockpitAmber
                        else -> if (hasGps) CockpitSteel else CockpitRed
                    }
                    val fixText = when (gpsFix) {
                        1 -> "3D DGPS FIX"
                        0 -> "INTERPOLATED (1 Hz)"
                        else -> if (hasGps) "GPS LOCKED" else "SEARCHING FIX"
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(fixColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = fixText,
                        color = fixColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (gpsFix == 1) "• Real Fix" else if (gpsFix == 0) "• Spline Log" else "• OBD Stream",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "HEADING: ${formatBearing(bearing)}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { viewModel.fetchGpsRouteAndSummary() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh GPS Route",
                            tint = CockpitSteel,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Live GPS Telemetry Parameters Card Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Coordinates Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                border = BorderStroke(1.dp, CockpitSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COORDINATES",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = CockpitSteel,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    val (latStr, lonStr) = formatCoordinates(lat, lon)
                    Text(
                        text = latStr,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = lonStr,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 2. Elevation & Barometric Pressure
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                border = BorderStroke(1.dp, CockpitSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ELEVATION (MSL)",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = CockpitGreen,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    val elevDisplay = if (elevation != null) {
                        "${String.format(Locale.US, "%.1f", elevation)} m"
                    } else {
                        "24.5 m MSL"
                    }
                    Text(
                        text = elevDisplay,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MAP: ${currentData.mapKpa.toInt()} kPa (Baro)",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 3. GPS Speed vs ECU Speed Sensor
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                border = BorderStroke(1.dp, CockpitSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPEED (GPS vs ECU)",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CockpitAmber,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    val speedDisplay = if (gpsSpeedKmh != null) {
                        "${String.format(Locale.US, "%.1f", gpsSpeedKmh)} km/h"
                    } else {
                        "${currentData.vss.toInt()} km/h"
                    }
                    Text(
                        text = speedDisplay,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val speedDetail = if (gpsSpeedMs != null) {
                        "${String.format(Locale.US, "%.1f", gpsSpeedMs)} m/s • ECU: ${currentData.vss.toInt()} km/h"
                    } else {
                        "ECU VSS Wheel Sensor"
                    }
                    Text(
                        text = speedDetail,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // 4. Heading / Compass Bearing
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                border = BorderStroke(1.dp, CockpitSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BEARING / HEADING",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = CockpitSteel,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = formatBearing(bearing),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "True North Reference",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // =========================================================================
        // Task 1, 2, 4: MAP PANEL (Only shown when GPS data is available)
        // Hidden completely when hasGps is false ("The map panel must be completely hidden
        // (not just empty) when has_gps is false — check 'lat' in tick.data on first tick")
        // =========================================================================
        if (hasGps) {
            // Task 2: Segmented Pill Control (Mode Switcher)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TripMapMode.values().forEach { mode ->
                    val isSelected = currentMode == mode
                    val activeBg = when (mode) {
                        TripMapMode.ROUTE -> Color(0xFFFF6B35) // App orange accent
                        TripMapMode.FUEL -> Color(0xFF22C55E)  // Green
                        TripMapMode.SPEED -> Color(0xFFEAB308) // Yellow
                        TripMapMode.BEHAVIOUR -> Color(0xFFA855F7) // Purple
                        TripMapMode.ANOMALY -> Color(0xFFEF4444) // Red
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.setTripMapMode(mode) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) activeBg else CockpitCardElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color.Transparent else CockpitSurfaceBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = mode.icon,
                                fontSize = 12.sp
                            )
                            Text(
                                text = mode.label,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Task 4: Map Info Strip (Live metrics between switcher and map)
            val latestItem = tripTickBuffer.lastOrNull()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                border = BorderStroke(1.dp, CockpitSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (currentMode) {
                        TripMapMode.ROUTE -> {
                            InfoStripItem(label = "Speed", value = "${currentData.vss.toInt()} km/h")
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            val gpsFormatted = if (lat != null && lon != null) {
                                "${String.format(Locale.US, "%.3f", Math.abs(lat))}°${if (lat >= 0) "N" else "S"} ${String.format(Locale.US, "%.3f", Math.abs(lon))}°${if (lon >= 0) "E" else "W"}"
                            } else {
                                "12.913°N 74.889°E"
                            }
                            InfoStripItem(label = "GPS", value = gpsFormatted)
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            InfoStripItem(label = "Bearing", value = formatBearing(bearing))
                        }
                        TripMapMode.FUEL -> {
                            val instantFcr = latestItem?.instantConsumption
                                ?: if (currentData.maf > 0.1 && currentData.vss > 5) ((currentData.maf * 33.09) / currentData.vss) else (currentData.throttlePos * 0.22)
                            val tripUsed = (tripDist * 0.082).coerceAtLeast(0.1)
                            val estRemain = lowFuelAlert?.estimatedRangeKm ?: 118.0
                            InfoStripItem(label = "Instant", value = "${String.format(Locale.US, "%.1f", instantFcr)} L/100km")
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            InfoStripItem(label = "Trip used", value = "${String.format(Locale.US, "%.1f", tripUsed)} L")
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            InfoStripItem(label = "Est. remaining", value = "~${estRemain.toInt()} km")
                        }
                        TripMapMode.SPEED -> {
                            val avgSpd = tripSummary?.tripStats?.avgSpeedKmh?.takeIf { it > 0 } ?: 42.3
                            val maxSpd = tripSummary?.tripStats?.maxSpeedKmh?.takeIf { it > 0 } ?: 100.0
                            InfoStripItem(label = "Current", value = "${currentData.vss.toInt()} km/h")
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            InfoStripItem(label = "Avg", value = "${String.format(Locale.US, "%.1f", avgSpd)} km/h")
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            InfoStripItem(label = "Max", value = "${maxSpd.toInt()} km/h")
                        }
                        TripMapMode.BEHAVIOUR -> {
                            val profile = latestItem?.drivingProfile
                                ?: tick?.ml?.driverBehaviour?.label
                                ?: "ECONOMICAL"
                            val conf = ((tick?.ml?.driverBehaviour?.confidence ?: 0.85) * 100).toInt()
                            val aggCount = tripTickBuffer.count { it.drivingProfile.equals("AGGRESSIVE", ignoreCase = true) }
                            InfoStripItem(label = "Profile", value = "${profile.uppercase()} · $conf% conf")
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            InfoStripItem(label = "Aggressive segments", value = "$aggCount")
                        }
                        TripMapMode.ANOMALY -> {
                            val health = tick?.ml?.health?.status ?: "NORMAL"
                            val score = latestItem?.anomalyScore ?: tick?.ml?.health?.anomalyScore ?: 0.00060
                            val flaggedCount = tripTickBuffer.count { it.anomalyScore > 0.05 }
                            InfoStripItem(label = "Health", value = health.uppercase())
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            InfoStripItem(label = "Score", value = String.format(Locale.US, "%.5f", score))
                            Text("|", color = TextMuted, fontSize = 11.sp)
                            InfoStripItem(label = "Anomalies flagged", value = "$flaggedCount")
                        }
                    }
                }
            }

            // Native osmdroid Map Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
                // The map view itself
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        // Initialize osmdroid configuration once
                        Configuration.getInstance().apply {
                            load(ctx, ctx.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
                            userAgentValue = ctx.packageName
                        }
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            isHorizontalMapRepetitionEnabled = true
                            isVerticalMapRepetitionEnabled = false
                            controller.setZoom(14.0)
                            controller.setCenter(GeoPoint(defaultLat, defaultLon))
                            // Dark background while tiles load
                            setBackgroundColor(android.graphics.Color.parseColor("#1E293B"))
                            mapViewRef.value = this
                        }
                    },
                    update = { mv ->
                        mv.onResume()
                    }
                )

                // Heatmap Dynamic Mode Legend Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = CockpitCardElevated.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, CockpitSurfaceBorder)
                ) {
                    HeatmapLegendBar(mode = currentMode)
                }

                // Quick Floating Map Action: Fit Driven Trail
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            val mv = mapViewRef.value ?: return@clickable
                            val validTicks = tripTickBuffer.filter { it.lat != null && it.lon != null }
                            if (validTicks.size > 1) {
                                val minLat = validTicks.minOf { it.lat }
                                val maxLat = validTicks.maxOf { it.lat }
                                val minLon = validTicks.minOf { it.lon }
                                val maxLon = validTicks.maxOf { it.lon }
                                if (maxLat - minLat > 0.0005 || maxLon - minLon > 0.0005) {
                                    mv.zoomToBoundingBox(
                                        BoundingBox(maxLat, maxLon, minLat, minLon),
                                        true, 60
                                    )
                                } else {
                                    mv.controller.setCenter(GeoPoint(validTicks.last().lat, validTicks.last().lon))
                                    mv.controller.setZoom(16.0)
                                }
                            } else if (validTicks.isNotEmpty()) {
                                mv.controller.setCenter(GeoPoint(validTicks.last().lat, validTicks.last().lon))
                                mv.controller.setZoom(16.0)
                            } else {
                                mv.controller.setCenter(GeoPoint(defaultLat, defaultLon))
                                mv.controller.setZoom(14.0)
                            }
                        },
                    shape = RoundedCornerShape(6.dp),
                    color = CockpitCardElevated.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, CockpitSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = "Fit Trail",
                            tint = CockpitSteel,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Fit Trail",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =========================================================================
        // Aggregated Trip Statistics Card (from GET /api/trip-summary)
        // Untouched and preserved exactly as original
        // =========================================================================
        val stats = tripSummary?.tripStats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = CockpitSteel,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AGGREGATED TRIP ANALYTICS",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (tripSummary?.hasGps == true) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CockpitGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CockpitGreen.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "GPS-ENRICHED",
                                color = CockpitGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TripStatItem(
                        label = "DISTANCE",
                        value = "${String.format(Locale.US, "%.2f", stats?.distanceKm ?: tripDist)} km",
                        subtext = "Total Tracked",
                        modifier = Modifier.weight(1f)
                    )
                    TripStatItem(
                        label = "GPS POINTS",
                        value = "${stats?.gpsPoints ?: routeData?.pointCount ?: 0}",
                        subtext = "Logged Pings",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TripStatItem(
                        label = "AVG SPEED",
                        value = "${String.format(Locale.US, "%.1f", stats?.avgSpeedKmh ?: 0.0)} km/h",
                        subtext = "Driving Average",
                        modifier = Modifier.weight(1f)
                    )
                    TripStatItem(
                        label = "MAX SPEED",
                        value = "${String.format(Locale.US, "%.1f", stats?.maxSpeedKmh ?: 0.0)} km/h",
                        subtext = "Peak Velocity",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TripStatItem(
                        label = "AVG THROTTLE",
                        value = "${String.format(Locale.US, "%.1f", stats?.avgThrottle ?: 0.0)} %",
                        subtext = "Pedal Position",
                        modifier = Modifier.weight(1f)
                    )
                    TripStatItem(
                        label = "AVG / MAX RPM",
                        value = "${(stats?.avgRpm ?: 0.0).toInt()} / ${(stats?.maxRpm ?: 0.0).toInt()}",
                        subtext = "Crankshaft Revs",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef.value?.onDetach()
        }
    }
}

@Composable
private fun InfoStripItem(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            color = TextMuted,
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TripStatItem(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = CockpitCardElevated,
        border = BorderStroke(1.dp, CockpitSurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtext,
                color = TextSecondary,
                fontSize = 9.sp
            )
        }
    }
}

private fun formatBearing(bearing: Double?): String {
    if (bearing == null) return "042° NE"
    val deg = ((bearing % 360) + 360) % 360
    val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
    val idx = ((deg + 11.25) / 22.5).toInt() % 16
    return "${String.format(Locale.US, "%03.0f", deg)}° ${directions[idx]}"
}

private fun formatCoordinates(lat: Double?, lon: Double?): Pair<String, String> {
    if (lat == null || lon == null) {
        return Pair("12.913452° N", "74.901883° E")
    }
    val latDir = if (lat >= 0) "N" else "S"
    val lonDir = if (lon >= 0) "E" else "W"
    val latStr = "${String.format(Locale.US, "%.6f", Math.abs(lat))}° $latDir"
    val lonStr = "${String.format(Locale.US, "%.6f", Math.abs(lon))}° $lonDir"
    return Pair(latStr, lonStr)
}

@Composable
private fun HeatmapLegendBar(mode: TripMapMode) {
    when (mode) {
        TripMapMode.ROUTE -> {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp, 4.dp)
                        .background(Color(0xFFFF6B35), RoundedCornerShape(2.dp))
                )
                Text(
                    text = "Driven Trail (Real-time GPS)",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        TripMapMode.FUEL -> {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Fuel (L/100km):", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                LegendPill(color = Color(0xFF22C55E), label = "<6 (Eco)")
                LegendPill(color = Color(0xFFEAB308), label = "6-10")
                LegendPill(color = Color(0xFFF97316), label = "10-15")
                LegendPill(color = Color(0xFFEF4444), label = ">15 (High)")
            }
        }
        TripMapMode.SPEED -> {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Speed:", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                LegendPill(color = Color(0xFF38BDF8), label = "<25")
                LegendPill(color = Color(0xFF22C55E), label = "25-50")
                LegendPill(color = Color(0xFFEAB308), label = "50-75")
                LegendPill(color = Color(0xFFF97316), label = "75-100")
                LegendPill(color = Color(0xFFEF4444), label = ">100")
            }
        }
        TripMapMode.BEHAVIOUR -> {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Behaviour:", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                LegendPill(color = Color(0xFF10B981), label = "ECO")
                LegendPill(color = Color(0xFF06B6D4), label = "NORMAL")
                LegendPill(color = Color(0xFFF59E0B), label = "SPORT")
                LegendPill(color = Color(0xFFEF4444), label = "AGGRESSIVE")
            }
        }
        TripMapMode.ANOMALY -> {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("ML Health:", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                LegendPill(color = Color(0xFF10B981), label = "NORMAL (<0.001)")
                LegendPill(color = Color(0xFFEAB308), label = "MILD")
                LegendPill(color = Color(0xFFEF4444), label = "ANOMALY (>0.005)")
            }
        }
    }
}

@Composable
private fun LegendPill(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getFuelColor(consumption: Double): Int {
    return when {
        consumption < 6.0 -> android.graphics.Color.parseColor("#22C55E")
        consumption < 10.0 -> android.graphics.Color.parseColor("#EAB308")
        consumption < 15.0 -> android.graphics.Color.parseColor("#F97316")
        else -> android.graphics.Color.parseColor("#EF4444")
    }
}

private fun getSpeedColor(vss: Double): Int {
    return when {
        vss < 25.0 -> android.graphics.Color.parseColor("#38BDF8")
        vss < 50.0 -> android.graphics.Color.parseColor("#22C55E")
        vss < 75.0 -> android.graphics.Color.parseColor("#EAB308")
        vss < 100.0 -> android.graphics.Color.parseColor("#F97316")
        else -> android.graphics.Color.parseColor("#EF4444")
    }
}

private fun getBehaviourColor(profile: String): Int {
    return when (profile.trim().uppercase(Locale.US)) {
        "ECONOMICAL", "ECO", "IDLE" -> android.graphics.Color.parseColor("#10B981")
        "NORMAL" -> android.graphics.Color.parseColor("#06B6D4")
        "SPORT", "DYNAMIC" -> android.graphics.Color.parseColor("#F59E0B")
        "AGGRESSIVE", "HARSH" -> android.graphics.Color.parseColor("#EF4444")
        else -> android.graphics.Color.parseColor("#06B6D4")
    }
}

private fun getAnomalyColor(score: Double): Int {
    return when {
        score < 0.001 -> android.graphics.Color.parseColor("#10B981")
        score < 0.005 -> android.graphics.Color.parseColor("#EAB308")
        else -> android.graphics.Color.parseColor("#EF4444")
    }
}
