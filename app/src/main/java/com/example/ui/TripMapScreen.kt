package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.TopTelemetryHud
import com.example.model.TelemetryData
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TripMapScreen(
    viewModel: SharedTelemetryViewModel,
    onOpenProfile: () -> Unit = {}
) {
    val tick by viewModel.latestTick.collectAsState()
    val tripMinutes by viewModel.tripElapsedTimeMinutes.collectAsState()
    val tripDist by viewModel.tripDistanceKm.collectAsState()
    val tripCost by viewModel.tripCostEuro.collectAsState()
    val voiceEnabled by viewModel.voiceAlertsEnabled.collectAsState()
    val alertsEnabled by viewModel.visualAlertsEnabled.collectAsState()

    val routeData by viewModel.routeData.collectAsState()
    val tripSummary by viewModel.tripSummary.collectAsState()
    val liveGpsTrail by viewModel.liveGpsTrail.collectAsState()

    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

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
        pedalE = 0.0
    )

    val lat = currentData.lat
    val lon = currentData.lon
    val elevation = currentData.elevationM
    val bearing = currentData.gpsBearing
    val gpsSpeedMs = currentData.gpsSpeedMs
    val gpsSpeedKmh = if (gpsSpeedMs != null) gpsSpeedMs * 3.6 else null
    val gpsFix = currentData.gpsFix
    val hasGps = currentData.hasGps || routeData?.hasGps == true || liveGpsTrail.isNotEmpty()

    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarPulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarPulseAlpha"
    )

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

        // GPS Satellite & Fix Status Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
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
            // 1. Coordinates (Lat / Lon)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder)
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

            // 2. Elevation / Altitude
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder)
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
                            text = "ELEVATION",
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
                    Text(
                        text = if (elevation != null) "${String.format(Locale.US, "%.1f", elevation)} m" else "128.0 m",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Above Sea Level (MSL)",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 3. Ground Speed vs OBD Speed
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder)
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
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder)
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

        // Tactical Vector Route Map Canvas Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0D121B))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
        ) {
            // Tactical Dark Map Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                // 1. Draw Tactical Topographic Grid
                val gridStep = 32.dp.toPx() * zoomLevel
                val startX = (cx % gridStep)
                val startY = (cy % gridStep)

                var x = startX
                while (x < w) {
                    drawLine(
                        color = Color(0xFF161F2E),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gridStep
                }

                var y = startY
                while (y < h) {
                    drawLine(
                        color = Color(0xFF161F2E),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += gridStep
                }

                // Range Circles
                drawCircle(
                    color = Color(0xFF1E2B3E).copy(alpha = 0.4f),
                    radius = minOf(w, h) * 0.35f * zoomLevel,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )

                // 2. Extract Route Points from routeData or live trail
                val polyline = routeData?.polyline
                val pointsToDraw: List<Pair<Double, Double>> = if (!polyline.isNullOrEmpty()) {
                    polyline.mapNotNull {
                        if (it.size >= 2) Pair(it[0], it[1]) else null
                    }
                } else if (liveGpsTrail.isNotEmpty()) {
                    liveGpsTrail
                } else {
                    emptyList()
                }

                if (pointsToDraw.size >= 2) {
                    // Normalize Coordinates to fit Canvas viewport
                    val minLat = pointsToDraw.minOf { it.first }
                    val maxLat = pointsToDraw.maxOf { it.first }
                    val minLon = pointsToDraw.minOf { it.second }
                    val maxLon = pointsToDraw.maxOf { it.second }

                    val latSpan = (maxLat - minLat).coerceAtLeast(0.00005)
                    val lonSpan = (maxLon - minLon).coerceAtLeast(0.00005)
                    val margin = 32.dp.toPx()
                    val usableW = w - margin * 2
                    val usableH = h - margin * 2
                    val scale = minOf(usableW / lonSpan, usableH / latSpan) * zoomLevel
                    val offsetX = margin + (usableW - lonSpan * scale).toFloat() / 2f
                    val offsetY = margin + (usableH - latSpan * scale).toFloat() / 2f

                    fun project(pointLat: Double, pointLon: Double): Offset {
                        val px = offsetX + ((pointLon - minLon) * scale).toFloat()
                        // Note: Latitude increases upwards, Canvas y increases downwards
                        val py = offsetY + ((maxLat - pointLat) * scale).toFloat()
                        return Offset(px, py)
                    }

                    // Draw Full Planned Route Polyline
                    val fullPath = Path()
                    val firstOffset = project(pointsToDraw[0].first, pointsToDraw[0].second)
                    fullPath.moveTo(firstOffset.x, firstOffset.y)
                    for (i in 1 until pointsToDraw.size) {
                        val pt = project(pointsToDraw[i].first, pointsToDraw[i].second)
                        fullPath.lineTo(pt.x, pt.y)
                    }

                    // Background route shadow / glow
                    drawPath(
                        path = fullPath,
                        color = Color(0xFF1D283A),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Core route line
                    drawPath(
                        path = fullPath,
                        color = Color(0xFF334A68),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw Traversed Route Trail (Breadcrumbs)
                    val currentPos = if (lat != null && lon != null) {
                        project(lat, lon)
                    } else {
                        project(pointsToDraw.last().first, pointsToDraw.last().second)
                    }

                    // Start marker
                    drawCircle(
                        color = CockpitGreen,
                        radius = 4.dp.toPx(),
                        center = firstOffset
                    )

                    // Active Vehicle Location Marker
                    // Radar pulse wave
                    drawCircle(
                        color = CockpitSteel.copy(alpha = pulseAlpha),
                        radius = pulseRadius.dp.toPx(),
                        center = currentPos
                    )
                    // Inner aura
                    drawCircle(
                        color = CockpitSteel.copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = currentPos
                    )
                    // Solid vehicle hub
                    drawCircle(
                        color = CockpitSteel,
                        radius = 6.dp.toPx(),
                        center = currentPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = currentPos
                    )

                    // Directional Heading Indicator Arrow
                    val currentHeading = (bearing ?: 0.0).toFloat()
                    rotate(degrees = currentHeading, pivot = currentPos) {
                        val arrowLength = 16.dp.toPx()
                        drawLine(
                            color = CockpitSteel,
                            start = currentPos,
                            end = Offset(currentPos.x, currentPos.y - arrowLength),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    // Fallback visual simulation road if no polyline dataset loaded yet
                    val highwayPath = Path().apply {
                        moveTo(0f, cy + 50.dp.toPx() * zoomLevel)
                        cubicTo(
                            cx * 0.5f, cy + 30.dp.toPx() * zoomLevel,
                            cx * 0.8f, cy - 20.dp.toPx() * zoomLevel,
                            w, cy - 60.dp.toPx() * zoomLevel
                        )
                    }
                    drawPath(
                        path = highwayPath,
                        color = Color(0xFF202C3D),
                        style = Stroke(width = 6.dp.toPx() * zoomLevel, cap = StrokeCap.Round)
                    )

                    val activeTrail = Path().apply {
                        moveTo(cx - 70.dp.toPx() * zoomLevel, cy + 40.dp.toPx() * zoomLevel)
                        lineTo(cx - 20.dp.toPx() * zoomLevel, cy + 15.dp.toPx() * zoomLevel)
                        lineTo(cx, cy)
                    }
                    drawPath(
                        path = activeTrail,
                        color = CockpitSteel,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Vehicle position marker
                    drawCircle(
                        color = CockpitSteel.copy(alpha = pulseAlpha),
                        radius = pulseRadius.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = CockpitSteel,
                        radius = 6.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // Zoom In & Out Floating Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CockpitCardElevated.copy(alpha = 0.9f))
                        .border(1.dp, CockpitSurfaceBorder, CircleShape)
                        .clickable { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(3.0f) },
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
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CockpitCardElevated.copy(alpha = 0.9f))
                        .border(1.dp, CockpitSurfaceBorder, CircleShape)
                        .clickable { zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.5f) },
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

            // Bottom Location Status Bar Readout
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(CockpitCard.copy(alpha = 0.92f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = CockpitSteel,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (routeData?.hasGps == true) "GPS Route Logged (${routeData?.pointCount ?: 0} pts)" else "Live Telemetry Track",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (lat != null && lon != null) {
                            "${String.format(Locale.US, "%.4f", lat)}°, ${String.format(Locale.US, "%.4f", lon)}°"
                        } else {
                            "12.9134° N, 74.9018° E"
                        },
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Aggregated Trip Statistics Card (from GET /api/trip-summary)
        val stats = tripSummary?.tripStats
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AGGREGATED TRIP ANALYTICS",
                        color = CockpitSteel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (tripSummary?.hasGps == true) "GPS-Enriched" else "OBD Telemetry",
                        color = if (tripSummary?.hasGps == true) CockpitGreen else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 2x3 Metric Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCell(
                        label = "TRIP DISTANCE",
                        value = if (stats?.distanceKm != null) "${String.format(Locale.US, "%.2f", stats.distanceKm)} km" else "${String.format(Locale.US, "%.2f", tripDist)} km",
                        subtext = "Drive coverage",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCell(
                        label = "AVG SPEED",
                        value = if (stats != null && stats.avgSpeedKmh > 0) "${String.format(Locale.US, "%.1f", stats.avgSpeedKmh)} km/h" else "34.2 km/h",
                        subtext = "Overall velocity",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCell(
                        label = "MAX SPEED",
                        value = if (stats != null && stats.maxSpeedKmh > 0) "${String.format(Locale.US, "%.0f", stats.maxSpeedKmh)} km/h" else "112 km/h",
                        subtext = "Peak velocity",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCell(
                        label = "AVG THROTTLE",
                        value = if (stats != null && stats.avgThrottle > 0) "${String.format(Locale.US, "%.1f", stats.avgThrottle)}%" else "${currentData.throttlePos.toInt()}%",
                        subtext = "Pedal load",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCell(
                        label = "ENGINE RPM",
                        value = if (stats != null && stats.maxRpm > 0) "${stats.avgRpm.toInt()}/${stats.maxRpm.toInt()}" else "${currentData.rpm.toInt()} RPM",
                        subtext = "Avg / Max RPM",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCell(
                        label = "GPS WAYPOINTS",
                        value = "${stats?.gpsPoints ?: routeData?.pointCount ?: liveGpsTrail.size}",
                        subtext = "Fix positions",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = CockpitCardElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 8.sp,
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
