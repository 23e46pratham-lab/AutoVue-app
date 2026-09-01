package com.example.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max
import kotlin.math.min

@Composable
fun TelemetryGraphCard(
    title: String,
    currentValue: Double,
    unit: String,
    dataPoints: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    fixedMinY: Double? = null,
    fixedMaxY: Double? = null,
    secondaryDataPoints: List<Double>? = null,
    secondaryLineColor: Color = Color(0xFFF43F5E),
    secondaryLabel: String? = null,
    valueFormat: String = "%.2f",
    testTag: String = "telemetry_graph_${title.lowercase().replace(" ", "_")}"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Compute min, max, avg for statistics
    val allPoints = if (secondaryDataPoints != null) dataPoints + secondaryDataPoints else dataPoints
    val dataMin = if (allPoints.isNotEmpty()) allPoints.minOrNull() ?: 0.0 else 0.0
    val dataMax = if (allPoints.isNotEmpty()) allPoints.maxOrNull() ?: 1.0 else 1.0
    val dataAvg = if (allPoints.isNotEmpty()) allPoints.average() else 0.0

    val effectiveMinY = fixedMinY ?: (if (dataMin == dataMax) 0.0 else (dataMin * 0.9).coerceAtLeast(0.0))
    val effectiveMaxY = fixedMaxY ?: (if (dataMin == dataMax) (dataMax + 10.0) else (dataMax * 1.1))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Bar (Title on Left, Live Value on Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(lineColor.copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = valueFormat.format(currentValue),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = lineColor,
                        fontSize = 18.sp
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = lineColor.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // Sub-metrics / stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "MIN: ${"%.1f".format(dataMin)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "AVG: ${"%.1f".format(dataAvg)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "MAX: ${"%.1f".format(dataMax)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                if (secondaryLabel != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(secondaryLineColor))
                        Text(
                            text = secondaryLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryLineColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Real-Time Canvas Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D16))
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 40f
                    val paddingRight = 16f
                    val paddingTop = 12f
                    val paddingBottom = 22f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    val rangeY = (effectiveMaxY - effectiveMinY).coerceAtLeast(0.001)

                    // Draw Horizontal Grid lines and Y-axis Labels
                    val gridSteps = 3
                    for (i in 0..gridSteps) {
                        val yFraction = i.toFloat() / gridSteps
                        val yPos = paddingTop + chartHeight * (1f - yFraction)
                        val valAtStep = effectiveMinY + (rangeY * yFraction)

                        // Dotted gridline
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(paddingLeft, yPos),
                            end = Offset(width - paddingRight, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Y Axis text
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(140, 148, 163, 184)
                                textSize = 9.sp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                            drawText(
                                if (valAtStep >= 1000) "${(valAtStep / 1000).toInt()}k" else "%.0f".format(valAtStep),
                                paddingLeft - 6f,
                                yPos + 3.dp.toPx(),
                                paint
                            )
                        }
                    }

                    // Draw Vertical Time Grid lines and X-axis Labels (e.g., sample checkpoints 0, 2, 4, 6, 8)
                    val xSteps = 5
                    for (i in 0..xSteps) {
                        val xFraction = i.toFloat() / xSteps
                        val xPos = paddingLeft + chartWidth * xFraction

                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(xPos, paddingTop),
                            end = Offset(xPos, height - paddingBottom),
                            strokeWidth = 1.dp.toPx()
                        )

                        // X Axis label (relative points / seconds)
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(120, 100, 116, 139)
                                textSize = 8.5.sp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            val label = "${i * 2}"
                            drawText(
                                label,
                                xPos,
                                height - 4f,
                                paint
                            )
                        }
                    }

                    // Helper to draw a data series
                    fun drawSeries(points: List<Double>, strokeColor: Color, fillGradient: Boolean) {
                        if (points.isEmpty()) return

                        val path = Path()
                        val fillPath = Path()

                        val pointCount = points.size
                        val stepX = if (pointCount > 1) chartWidth / (pointCount - 1) else chartWidth

                        var lastX = paddingLeft
                        var lastY = paddingTop + chartHeight

                        points.forEachIndexed { index, value ->
                            val normalizedY = ((value - effectiveMinY) / rangeY).coerceIn(0.0, 1.0)
                            val x = paddingLeft + (index * stepX)
                            val y = paddingTop + (chartHeight * (1f - normalizedY.toFloat()))

                            if (index == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, paddingTop + chartHeight)
                                fillPath.lineTo(x, y)
                            } else {
                                // Smooth cubic bezier curve for aesthetic look
                                val prevNormalizedY = ((points[index - 1] - effectiveMinY) / rangeY).coerceIn(0.0, 1.0)
                                val prevX = paddingLeft + ((index - 1) * stepX)
                                val prevY = paddingTop + (chartHeight * (1f - prevNormalizedY.toFloat()))

                                val controlPointX1 = prevX + (x - prevX) / 2f
                                val controlPointY1 = prevY
                                val controlPointX2 = prevX + (x - prevX) / 2f
                                val controlPointY2 = y

                                path.cubicTo(controlPointX1, controlPointY1, controlPointX2, controlPointY2, x, y)
                                fillPath.cubicTo(controlPointX1, controlPointY1, controlPointX2, controlPointY2, x, y)
                            }
                            lastX = x
                            lastY = y
                        }

                        if (fillGradient && points.isNotEmpty()) {
                            fillPath.lineTo(lastX, paddingTop + chartHeight)
                            fillPath.close()
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        strokeColor.copy(alpha = 0.30f),
                                        strokeColor.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight
                                )
                            )
                        }

                        // Draw Stroke Path
                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(
                                width = 2.2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Draw latest reading indicator dot
                        if (points.isNotEmpty()) {
                            drawCircle(
                                color = strokeColor.copy(alpha = 0.35f),
                                radius = 6.dp.toPx(),
                                center = Offset(lastX, lastY)
                            )
                            drawCircle(
                                color = strokeColor,
                                radius = 3.dp.toPx(),
                                center = Offset(lastX, lastY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.5.dp.toPx(),
                                center = Offset(lastX, lastY)
                            )
                        }
                    }

                    // Draw secondary series if present (e.g. Pedal E)
                    if (secondaryDataPoints != null && secondaryDataPoints.isNotEmpty()) {
                        drawSeries(secondaryDataPoints, secondaryLineColor, fillGradient = false)
                    }

                    // Draw primary series
                    drawSeries(dataPoints, lineColor, fillGradient = true)
                }
            }
        }
    }
}
