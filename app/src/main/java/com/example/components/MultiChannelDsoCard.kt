package com.example.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * Highlighting band for specific physiological or thermal operating ranges on the DSO.
 */
data class DsoZoneHighlight(
    val minY: Double,
    val maxY: Double,
    val color: Color,
    val label: String
)

/**
 * Multi-Channel Diagnostic Strip Chart Recorder Card
 * Designed after high-end professional automotive digital storage oscilloscopes (DSO).
 * Clean, consistent cockpit styling matching all other application tabs.
 */
@Composable
fun MultiChannelDsoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    // Channel 1 (Primary)
    channel1Name: String,
    channel1ValueText: String,
    channel1Unit: String,
    channel1History: List<Double>,
    channel1Color: Color = CockpitRed,
    channel1Min: Double,
    channel1Max: Double,
    // Channel 2 (Secondary / Correlation)
    channel2Name: String? = null,
    channel2ValueText: String? = null,
    channel2Unit: String? = null,
    channel2History: List<Double>? = null,
    channel2Color: Color = CockpitSteel,
    channel2Min: Double = 0.0,
    channel2Max: Double = 100.0,
    // Diagnostic Overlays
    referenceValue: Double? = null,
    referenceLabel: String? = null,
    referenceColor: Color = Color(0xFF9E9E9E),
    zoneHighlights: List<DsoZoneHighlight>? = null,
    correlationMetricLabel: String? = null,
    correlationMetricValue: String? = null,
    diagnosticNote: String,
    isPaused: Boolean = false,
    windowSeconds: Int = 30,
    modifier: Modifier = Modifier
) {
    var ch1Visible by remember { mutableStateOf(true) }
    var ch2Visible by remember { mutableStateOf(true) }
    var showInfoNote by remember { mutableStateOf(false) }

    val cleanCh1 = if (channel1History.isEmpty()) listOf(0.0) else channel1History
    val cleanCh2 = if (channel2History.isNullOrEmpty()) listOf(0.0) else channel2History

    val ch1MinVal = cleanCh1.minOrNull() ?: channel1Min
    val ch1MaxVal = cleanCh1.maxOrNull() ?: channel1Max
    val ch1AvgVal = cleanCh1.average()

    val ch2MinVal = if (channel2History != null) cleanCh2.minOrNull() ?: channel2Min else 0.0
    val ch2MaxVal = if (channel2History != null) cleanCh2.maxOrNull() ?: channel2Max else 0.0
    val ch2AvgVal = if (channel2History != null) cleanCh2.average() else 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Title, Subtitle, Info toggle
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CockpitCardElevated)
                            .border(1.dp, CardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = CockpitSteel,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = subtitle,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Info toggle button
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Diagnostic Insight",
                    tint = if (showInfoNote) CockpitAmber else TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showInfoNote = !showInfoNote }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Channel Badges and Correlation Stat Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Channel 1 Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (ch1Visible) channel1Color.copy(alpha = 0.12f) else CockpitCardElevated)
                            .border(
                                1.dp,
                                if (ch1Visible) channel1Color.copy(alpha = 0.7f) else CardBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { ch1Visible = !ch1Visible }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (ch1Visible) channel1Color else TextMuted)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "CH1: $channel1ValueText",
                                color = if (ch1Visible) channel1Color else TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // Channel 2 Pill Badge (if present)
                    if (channel2Name != null && channel2ValueText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (ch2Visible) channel2Color.copy(alpha = 0.12f) else CockpitCardElevated)
                                .border(
                                    1.dp,
                                    if (ch2Visible) channel2Color.copy(alpha = 0.7f) else CardBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { ch2Visible = !ch2Visible }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (ch2Visible) channel2Color else TextMuted)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "CH2: $channel2ValueText",
                                    color = if (ch2Visible) channel2Color else TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Correlation / State Readout Pill
                if (correlationMetricLabel != null && correlationMetricValue != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CockpitCardElevated)
                            .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = correlationMetricLabel.uppercase(Locale.US),
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = correlationMetricValue,
                                color = CockpitSteel,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==============================================================
            // DSO Oscilloscope Reticle Screen (High-Contrast Precision Display)
            // ==============================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1B1D22))
                    .border(1.dp, Color(0xFF2C3038), RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val paddingLeft = 34.dp.toPx()
                    val paddingRight = if (channel2Name != null) 34.dp.toPx() else 8.dp.toPx()
                    val paddingTop = 8.dp.toPx()
                    val paddingBottom = 16.dp.toPx()

                    val plotW = (w - paddingLeft - paddingRight).coerceAtLeast(10f)
                    val plotH = (h - paddingTop - paddingBottom).coerceAtLeast(10f)

                    // 1. Draw Zone Highlights (e.g. Cold / Optimal / Overheat)
                    zoneHighlights?.forEach { zone ->
                        val effectiveMax1 = (channel1Max - channel1Min).coerceAtLeast(1.0)
                        val normMin = ((zone.minY - channel1Min) / effectiveMax1).coerceIn(0.0, 1.0)
                        val normMax = ((zone.maxY - channel1Min) / effectiveMax1).coerceIn(0.0, 1.0)

                        val topY = paddingTop + (1f - normMax.toFloat()) * plotH
                        val bottomY = paddingTop + (1f - normMin.toFloat()) * plotH
                        val zoneH = (bottomY - topY).coerceAtLeast(2f)

                        drawRect(
                            color = zone.color.copy(alpha = 0.14f),
                            topLeft = Offset(paddingLeft, topY),
                            size = Size(plotW, zoneH)
                        )
                    }

                    // 2. Oscilloscope Grid Reticle (Horizontal parameter divisions)
                    val hDivisions = 4
                    for (i in 0..hDivisions) {
                        val y = paddingTop + (plotH / hDivisions) * i
                        drawLine(
                            color = Color(0xFF282C34),
                            start = Offset(paddingLeft, y),
                            end = Offset(paddingLeft + plotW, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                    }

                    // 3. Oscilloscope Grid Reticle (Vertical Timebase divisions)
                    val vDivisions = 6
                    for (i in 0..vDivisions) {
                        val x = paddingLeft + (plotW / vDivisions) * i
                        drawLine(
                            color = Color(0xFF282C34),
                            start = Offset(x, paddingTop),
                            end = Offset(x, paddingTop + plotH),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                    }

                    // 4. Reference Level Line (e.g. 100 kPa Baro or 87°C Thermostat)
                    if (referenceValue != null) {
                        val effectiveMax1 = (channel1Max - channel1Min).coerceAtLeast(1.0)
                        val normRef = ((referenceValue - channel1Min) / effectiveMax1).coerceIn(0.0, 1.0)
                        val refY = paddingTop + (1f - normRef.toFloat()) * plotH

                        drawLine(
                            color = referenceColor.copy(alpha = 0.7f),
                            start = Offset(paddingLeft, refY),
                            end = Offset(paddingLeft + plotW, refY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                        )
                    }

                    // 5. Channel 1 Waveform (Primary)
                    if (ch1Visible && cleanCh1.size > 1) {
                        val effectiveMax1 = (channel1Max - channel1Min).coerceAtLeast(1.0)
                        val stepX = plotW / (cleanCh1.size - 1).toFloat()
                        val path = Path()
                        val fillPath = Path()

                        fillPath.moveTo(paddingLeft, paddingTop + plotH)

                        for (i in cleanCh1.indices) {
                            val normY = ((cleanCh1[i] - channel1Min) / effectiveMax1).coerceIn(0.0, 1.0)
                            val px = paddingLeft + (i * stepX)
                            val py = paddingTop + (1f - normY.toFloat()) * plotH

                            if (i == 0) {
                                path.moveTo(px, py)
                                fillPath.lineTo(px, py)
                            } else {
                                path.lineTo(px, py)
                                fillPath.lineTo(px, py)
                            }
                        }

                        fillPath.lineTo(paddingLeft + plotW, paddingTop + plotH)
                        fillPath.close()

                        // Gradient fill under CH1
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(channel1Color.copy(alpha = 0.16f), Color.Transparent),
                                startY = paddingTop,
                                endY = paddingTop + plotH
                            )
                        )

                        // CH1 Stroke
                        drawPath(
                            path = path,
                            color = channel1Color,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Leading edge pulse dot
                        val lastIdx = cleanCh1.size - 1
                        val lastNorm = ((cleanCh1[lastIdx] - channel1Min) / effectiveMax1).coerceIn(0.0, 1.0)
                        val dotX = paddingLeft + (lastIdx * stepX)
                        val dotY = paddingTop + (1f - lastNorm.toFloat()) * plotH

                        drawCircle(
                            color = channel1Color.copy(alpha = 0.35f),
                            radius = 5.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                        drawCircle(
                            color = channel1Color,
                            radius = 2.5.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                    }

                    // 6. Channel 2 Waveform (Secondary)
                    if (channel2Name != null && ch2Visible && cleanCh2.size > 1) {
                        val effectiveMax2 = (channel2Max - channel2Min).coerceAtLeast(1.0)
                        val stepX = plotW / (cleanCh2.size - 1).toFloat()
                        val path2 = Path()

                        for (i in cleanCh2.indices) {
                            val normY = ((cleanCh2[i] - channel2Min) / effectiveMax2).coerceIn(0.0, 1.0)
                            val px = paddingLeft + (i * stepX)
                            val py = paddingTop + (1f - normY.toFloat()) * plotH

                            if (i == 0) {
                                path2.moveTo(px, py)
                            } else {
                                path2.lineTo(px, py)
                            }
                        }

                        // CH2 Stroke
                        drawPath(
                            path = path2,
                            color = channel2Color,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Leading edge dot CH2
                        val lastIdx2 = cleanCh2.size - 1
                        val lastNorm2 = ((cleanCh2[lastIdx2] - channel2Min) / effectiveMax2).coerceIn(0.0, 1.0)
                        val dotX2 = paddingLeft + (lastIdx2 * stepX)
                        val dotY2 = paddingTop + (1f - lastNorm2.toFloat()) * plotH

                        drawCircle(
                            color = channel2Color.copy(alpha = 0.35f),
                            radius = 5.dp.toPx(),
                            center = Offset(dotX2, dotY2)
                        )
                        drawCircle(
                            color = channel2Color,
                            radius = 2.5.dp.toPx(),
                            center = Offset(dotX2, dotY2)
                        )
                    }
                }

                // CRT / Oscilloscope Scale Overlays
                // Left Scale Labels (CH1)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp, top = 6.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${channel1Max.toInt()}",
                        color = channel1Color,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${((channel1Max + channel1Min) / 2).toInt()}",
                        color = channel1Color.copy(alpha = 0.7f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${channel1Min.toInt()}",
                        color = channel1Color,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Right Scale Labels (CH2)
                if (channel2Name != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp, top = 6.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${channel2Max.toInt()}",
                            color = channel2Color,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${((channel2Max + channel2Min) / 2).toInt()}",
                            color = channel2Color.copy(alpha = 0.7f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${channel2Min.toInt()}",
                            color = channel2Color,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reference line label watermark
                if (referenceLabel != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .background(Color(0xFF1B1D22).copy(alpha = 0.85f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = referenceLabel,
                            color = referenceColor,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Timebase sweep markers along bottom (dynamically computed from windowSeconds)
                val tThird = (windowSeconds / 3)
                val tTwoThird = ((windowSeconds * 2) / 3)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 38.dp, end = 38.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("-${windowSeconds}s", color = Color(0xFF6B7280), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text("-${tTwoThird}s", color = Color(0xFF6B7280), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text("-${tThird}s", color = Color(0xFF6B7280), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text("LIVE", color = Color(0xFFE5E7EB), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                // FROZEN / HOLD Watermark if paused
                if (isPaused) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF111215).copy(alpha = 0.88f))
                            .border(1.dp, CockpitAmber, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Paused",
                                tint = CockpitAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DSO HOLD • BUFFER FROZEN",
                                color = CockpitAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-metrics Bar: Min, Avg, Max for both active channels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CH1 Summary
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CH1 min: ${String.format(Locale.US, "%.0f", ch1MinVal)}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "avg: ${String.format(Locale.US, "%.0f", ch1AvgVal)}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "max: ${String.format(Locale.US, "%.0f", ch1MaxVal)}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // CH2 Summary (if available)
                if (channel2Name != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CH2 max: ${String.format(Locale.US, "%.0f", ch2MaxVal)} $channel2Unit",
                            color = channel2Color,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Expandable Diagnostic Note
            AnimatedVisibility(
                visible = showInfoNote,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CockpitCardElevated)
                        .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Insight",
                            tint = CockpitSteel,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AUTOMOTIVE DSO ANALYSIS",
                            color = CockpitSteel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = diagnosticNote,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
