package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate800
import com.example.ui.theme.StatusRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogGauge(
    title: String,
    value: Float,
    maxValue: Float,
    unit: String,
    modifier: Modifier = Modifier,
    majorStep: Float = maxValue / 8f,
    minorDivisions: Int = 2,
    redlineStart: Float? = maxValue * 0.75f,
    gaugeColor: Color = Indigo500,
    needleColor: Color = Color(0xFFFF3D00),
    valueTextOverride: String? = null,
    tickLabelFormatter: ((Float) -> String) = { it.toInt().toString() }
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0f, maxValue),
        animationSpec = tween(durationMillis = 250),
        label = "NeedleAngle"
    )

    val textMeasurer = rememberTextMeasurer()
    val startAngle = 135f
    val sweepAngle = 270f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 14.dp.toPx()
            val strokeWidth = 3.dp.toPx()

            // 1. Draw outer gauge arc (track)
            drawArc(
                color = Slate800.copy(alpha = 0.5f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2f, outerRadius * 2f),
                style = Stroke(width = strokeWidth)
            )

            // Redline arc if specified
            if (redlineStart != null && redlineStart < maxValue) {
                val redlineFrac = (redlineStart / maxValue).coerceIn(0f, 1f)
                val redlineStartAngle = startAngle + redlineFrac * sweepAngle
                val redlineSweepAngle = (1f - redlineFrac) * sweepAngle

                drawArc(
                    color = StatusRed.copy(alpha = 0.8f),
                    startAngle = redlineStartAngle,
                    sweepAngle = redlineSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2f, outerRadius * 2f),
                    style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Active value arc track
            val currentProgress = (animatedValue / maxValue).coerceIn(0f, 1f)
            drawArc(
                color = gaugeColor,
                startAngle = startAngle,
                sweepAngle = currentProgress * sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2f, outerRadius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Draw Ticks & Numbers
            val totalMajorTicks = ((maxValue / majorStep) + 1).toInt()
            val totalTicks = (totalMajorTicks - 1) * minorDivisions

            for (i in 0..totalTicks) {
                val tickVal = (i.toFloat() / totalTicks.toFloat()) * maxValue
                val frac = tickVal / maxValue
                val angleDeg = startAngle + frac * sweepAngle
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val isMajor = (i % minorDivisions == 0)
                val tickLength = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                val tickColor = if (isMajor) onSurfaceColor.copy(alpha = 0.85f) else labelColor.copy(alpha = 0.4f)

                val innerR = outerRadius - tickLength
                val startX = center.x + outerRadius * cos(angleRad).toFloat()
                val startY = center.y + outerRadius * sin(angleRad).toFloat()
                val endX = center.x + innerR * cos(angleRad).toFloat()
                val endY = center.y + innerR * sin(angleRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth
                )

                // Tick Labels for major ticks
                if (isMajor) {
                    val labelText = tickLabelFormatter(tickVal)
                    val labelR = outerRadius - tickLength - 10.dp.toPx()
                    val labelX = center.x + labelR * cos(angleRad).toFloat()
                    val labelY = center.y + labelR * sin(angleRad).toFloat()

                    val measuredText = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor.copy(alpha = 0.9f)
                        )
                    )

                    drawText(
                        textLayoutResult = measuredText,
                        topLeft = Offset(
                            labelX - measuredText.size.width / 2f,
                            labelY - measuredText.size.height / 2f
                        )
                    )
                }
            }

            // 3. Draw Needle / Pointer
            val needleFrac = (animatedValue / maxValue).coerceIn(0f, 1f)
            val needleAngleDeg = startAngle + needleFrac * sweepAngle
            val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())

            val needleLength = outerRadius - 20.dp.toPx()
            val needleBaseWidth = 5.dp.toPx()

            val endX = center.x + needleLength * cos(needleAngleRad).toFloat()
            val endY = center.y + needleLength * sin(needleAngleRad).toFloat()

            val perpAngleRad = needleAngleRad + Math.PI / 2
            val baseLeftX = center.x + (needleBaseWidth / 2f) * cos(perpAngleRad).toFloat()
            val baseLeftY = center.y + (needleBaseWidth / 2f) * sin(perpAngleRad).toFloat()
            val baseRightX = center.x - (needleBaseWidth / 2f) * cos(perpAngleRad).toFloat()
            val baseRightY = center.y - (needleBaseWidth / 2f) * sin(perpAngleRad).toFloat()

            val needlePath = Path().apply {
                moveTo(baseLeftX, baseLeftY)
                lineTo(endX, endY)
                lineTo(baseRightX, baseRightY)
                close()
            }

            drawPath(
                path = needlePath,
                color = needleColor
            )

            // Needle pivot cap (center circles)
            drawCircle(
                color = Slate800,
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = needleColor,
                radius = 3.5.dp.toPx(),
                center = center
            )
        }

        // 4. Digital Readout inside gauge (positioned below pivot in dial face)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 28.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = valueTextOverride ?: value.toInt().toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Indigo400
            )
        }
    }
}
