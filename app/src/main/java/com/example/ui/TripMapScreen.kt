package com.example.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
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

    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

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
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Persistent Top Telemetry HUD (Screenshot 20)
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

        // GPS Satellite Status Sub-Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CockpitCard)
                .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "GPS",
                    tint = CockpitGreen,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GPS: 11 Satellites (3D Fix)",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Heading: 042° NE",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Map Canvas Viewport with Overlay Zoom Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF10151E))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
        ) {
            // Tactical Dark Map Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                // Draw secondary road grid
                val gridStep = 40.dp.toPx() * zoomLevel
                val startX = (cx % gridStep)
                val startY = (cy % gridStep)

                var x = startX
                while (x < w) {
                    drawLine(
                        color = Color(0xFF1A2332),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx()
                    )
                    x += gridStep
                }

                var y = startY
                while (y < h) {
                    drawLine(
                        color = Color(0xFF1A2332),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    y += gridStep
                }

                // Draw major arterial highway
                val highwayPath = Path().apply {
                    moveTo(0f, cy + 60.dp.toPx() * zoomLevel)
                    cubicTo(
                        cx * 0.5f, cy + 40.dp.toPx() * zoomLevel,
                        cx * 0.8f, cy - 20.dp.toPx() * zoomLevel,
                        w, cy - 80.dp.toPx() * zoomLevel
                    )
                }
                drawPath(
                    path = highwayPath,
                    color = Color(0xFF263548),
                    style = Stroke(width = 8.dp.toPx() * zoomLevel)
                )

                // Active Trip Path Breadcrumb Trail
                val routePath = Path().apply {
                    moveTo(cx - 100.dp.toPx() * zoomLevel, cy + 80.dp.toPx() * zoomLevel)
                    lineTo(cx - 40.dp.toPx() * zoomLevel, cy + 30.dp.toPx() * zoomLevel)
                    lineTo(cx, cy)
                }
                drawPath(
                    path = routePath,
                    color = CockpitSteel,
                    style = Stroke(width = 3.dp.toPx() * zoomLevel)
                )

                // Current Vehicle Position Marker (Cyan/Steel arrow)
                drawCircle(
                    color = CockpitSteel.copy(alpha = 0.25f),
                    radius = 18.dp.toPx() * zoomLevel,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = CockpitSteel,
                    radius = 8.dp.toPx() * zoomLevel,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx() * zoomLevel,
                    center = Offset(cx, cy)
                )
            }

            // Zoom In & Out Floating Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, CircleShape)
                        .clickable { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.5f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, CircleShape)
                        .clickable { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.6f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom Location Status Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(CockpitCard.copy(alpha = 0.92f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Industrial Park Blvd / Sector 4",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "45°28'N 9°11'E",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
