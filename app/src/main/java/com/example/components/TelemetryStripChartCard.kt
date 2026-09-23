package com.example.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGaugeTrack
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TelemetryStripChartCard(
    title: String,
    currentValueText: String,
    history: List<Double>,
    unit: String,
    icon: ImageVector,
    minValue: Double = 0.0,
    maxValue: Double = 100.0,
    readTimeMs: Int = 590,
    lineColor: Color = CockpitRed,
    isStepPlot: Boolean = false,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cleanHistory = if (history.isEmpty()) listOf(0.0) else history
    val minVal = cleanHistory.minOrNull() ?: minValue
    val maxVal = cleanHistory.maxOrNull() ?: maxValue
    val avgVal = if (cleanHistory.isNotEmpty()) cleanHistory.average() else 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header Row: Title, Icon, Graph... Button, Edit icon, Live Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = CockpitSteel,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // "Graph..." pill button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CockpitCardElevated)
                            .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(4.dp))
                            .clickable { }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Graph...",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Edit pencil icon
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(15.dp)
                            .clickable { onEditClick() }
                    )

                    // Current Value Highlight
                    Text(
                        text = currentValueText,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Strip Chart Canvas (Oscilloscope View)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CockpitCardElevated)
                    .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(4.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val padding = 4.dp.toPx()

                    // Grid Lines (Horizontal)
                    val gridLines = 3
                    for (i in 1..gridLines) {
                        val y = (h / (gridLines + 1)) * i
                        drawLine(
                            color = CockpitGaugeTrack.copy(alpha = 0.6f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Grid Lines (Vertical Timeline)
                    val vLines = 5
                    for (i in 1..vLines) {
                        val x = (w / (vLines + 1)) * i
                        drawLine(
                            color = CockpitGaugeTrack.copy(alpha = 0.4f),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Plot Waveform
                    val effectiveMax = if (maxValue > minValue) maxValue else 100.0
                    val effectiveRange = (effectiveMax - minValue).coerceAtLeast(1.0)

                    if (cleanHistory.size > 1) {
                        val stepX = w / (cleanHistory.size - 1).toFloat()
                        val path = Path()

                        for (i in cleanHistory.indices) {
                            val normY = ((cleanHistory[i] - minValue) / effectiveRange).coerceIn(0.0, 1.0)
                            val px = i * stepX
                            val py = h - padding - (normY * (h - 2 * padding)).toFloat()

                            if (i == 0) {
                                path.moveTo(px, py)
                            } else {
                                if (isStepPlot) {
                                    val prevY = h - padding - (((cleanHistory[i - 1] - minValue) / effectiveRange).coerceIn(0.0, 1.0) * (h - 2 * padding)).toFloat()
                                    path.lineTo(px, prevY)
                                    path.lineTo(px, py)
                                } else {
                                    path.lineTo(px, py)
                                }
                            }
                        }

                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw current tip dot
                        val lastIndex = cleanHistory.size - 1
                        val lastNormY = ((cleanHistory[lastIndex] - minValue) / effectiveRange).coerceIn(0.0, 1.0)
                        val lastPx = lastIndex * stepX
                        val lastPy = h - padding - (lastNormY * (h - 2 * padding)).toFloat()

                        drawCircle(
                            color = lineColor,
                            radius = 3.dp.toPx(),
                            center = Offset(lastPx, lastPy)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-metrics: Min, Avg, Max, Read Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "min: ${String.format(Locale.US, "%.0f", minVal)}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "avg: ${String.format(Locale.US, "%.0f", avgVal)}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "max: ${String.format(Locale.US, "%.0f", maxVal)}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "read in $readTimeMs ms",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
