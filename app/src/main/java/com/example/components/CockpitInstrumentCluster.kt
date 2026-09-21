package com.example.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import kotlin.math.cos
import kotlin.math.sin
import java.util.Locale

@Composable
fun CockpitInstrumentCluster(
    data: TelemetryData,
    fuelPercent: Double = 24.0,
    peakPowerHp: Double = 41.0,
    peakPowerRpm: Int = 1629,
    peakTorqueNm: Double = 187.0,
    peakTorqueRpm: Int = 850,
    modifier: Modifier = Modifier
) {
    // Current animated readings
    val animatedRpm by animateFloatAsState(
        targetValue = data.rpm.toFloat().coerceIn(0f, 8000f),
        animationSpec = tween(durationMillis = 180),
        label = "ClusterRpm"
    )
    val speed = data.vss.toInt()

    // Calculated power in cv / HP: (torque * rpm) / 7120 approx or derived from load and rpm
    val calculatedLoadFrac = (data.throttlePos / 100.0).coerceIn(0.0, 1.0)
    val currentPowerHp = if (data.rpm > 100) {
        ((data.rpm / 6000.0) * calculatedLoadFrac * 65.0).coerceIn(0.0, 120.0)
    } else {
        0.0
    }
    val currentTorqueNm = if (data.rpm > 100) {
        (calculatedLoadFrac * 140.0 + (data.rpm / 2000.0) * 20.0).coerceIn(0.0, 220.0)
    } else {
        0.0
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CockpitCard)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Cluster Canvas: Central Tachometer + 4 Peripheral Arcs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.15f),
            contentAlignment = Alignment.Center
        ) {
            val textMeasurer = rememberTextMeasurer()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f - 8.dp.toPx())
                val dialRadius = (size.width * 0.34f).coerceAtMost(size.height * 0.40f)

                // ------------------------------------------------------------
                // 1. FOUR CORNER PERIPHERAL ARCS
                // ------------------------------------------------------------
                val cornerRadius = dialRadius * 1.28f
                val cornerArcStroke = 4.dp.toPx()

                // TOP-LEFT: Coolant Temp (0 to 120 °C)
                val coolantFrac = (data.coolantTemp.toFloat() / 120f).coerceIn(0f, 1f)
                val tlStartAngle = 160f
                val tlSweepAngle = 55f
                // Track
                drawArc(
                    color = CockpitGaugeTrack,
                    startAngle = tlStartAngle,
                    sweepAngle = tlSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - cornerRadius, center.y - cornerRadius),
                    size = Size(cornerRadius * 2f, cornerRadius * 2f),
                    style = Stroke(width = cornerArcStroke, cap = StrokeCap.Round)
                )
                // Active fill
                drawArc(
                    color = if (data.coolantTemp > 105) CockpitRed else CockpitSteel,
                    startAngle = tlStartAngle,
                    sweepAngle = tlSweepAngle * coolantFrac,
                    useCenter = false,
                    topLeft = Offset(center.x - cornerRadius, center.y - cornerRadius),
                    size = Size(cornerRadius * 2f, cornerRadius * 2f),
                    style = Stroke(width = cornerArcStroke, cap = StrokeCap.Round)
                )

                // BOTTOM-LEFT: Fuel Tank (0 to 100 %)
                val fuelFrac = (fuelPercent.toFloat() / 100f).coerceIn(0f, 1f)
                val blStartAngle = 145f
                val blSweepAngle = -55f
                drawArc(
                    color = CockpitGaugeTrack,
                    startAngle = blStartAngle,
                    sweepAngle = blSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - cornerRadius, center.y - cornerRadius),
                    size = Size(cornerRadius * 2f, cornerRadius * 2f),
                    style = Stroke(width = cornerArcStroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = if (fuelPercent < 15) CockpitRed else CockpitAmber,
                    startAngle = blStartAngle,
                    sweepAngle = blSweepAngle * fuelFrac,
                    useCenter = false,
                    topLeft = Offset(center.x - cornerRadius, center.y - cornerRadius),
                    size = Size(cornerRadius * 2f, cornerRadius * 2f),
                    style = Stroke(width = cornerArcStroke, cap = StrokeCap.Round)
                )

                // TOP-RIGHT: Engine Load (0 to 100 %)
                val loadFrac = (data.throttlePos.toFloat() / 100f).coerceIn(0f, 1f)
                val trStartAngle = 20f
                val trSweepAngle = -55f
                drawArc(
                    color = CockpitGaugeTrack,
                    startAngle = trStartAngle,
                    sweepAngle = trSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - cornerRadius, center.y - cornerRadius),
                    size = Size(cornerRadius * 2f, cornerRadius * 2f),
                    style = Stroke(width = cornerArcStroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = CockpitSteel,
                    startAngle = trStartAngle,
                    sweepAngle = trSweepAngle * loadFrac,
                    useCenter = false,
                    topLeft = Offset(center.x - cornerRadius, center.y - cornerRadius),
                    size = Size(cornerRadius * 2f, cornerRadius * 2f),
                    style = Stroke(width = cornerArcStroke, cap = StrokeCap.Round)
                )

                // BOTTOM-RIGHT: Intake / Ambient Temp (0 to 45 °C)
                val intakeFrac = (data.intakeAirTemp.toFloat() / 45f).coerceIn(0f, 1f)
                val brStartAngle = 35f
                val brSweepAngle = 55f
                drawArc(
                    color = CockpitGaugeTrack,
                    startAngle = brStartAngle,
                    sweepAngle = brSweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - cornerRadius, center.y - cornerRadius),
                    size = Size(cornerRadius * 2f, cornerRadius * 2f),
                    style = Stroke(width = cornerArcStroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = CockpitSteel,
                    startAngle = brStartAngle,
                    sweepAngle = brSweepAngle * intakeFrac,
                    useCenter = false,
                    topLeft = Offset(center.x - cornerRadius, center.y - cornerRadius),
                    size = Size(cornerRadius * 2f, cornerRadius * 2f),
                    style = Stroke(width = cornerArcStroke, cap = StrokeCap.Round)
                )

                // ------------------------------------------------------------
                // 2. CENTRAL TACHOMETER DIAL (0 to 8 x 1000 RPM)
                // ------------------------------------------------------------
                // Dial background ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF161E2A), Color(0xFF0F141D)),
                        center = center,
                        radius = dialRadius
                    ),
                    radius = dialRadius,
                    center = center
                )
                drawCircle(
                    color = CockpitSurfaceBorder,
                    radius = dialRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Tachometer Sweep: 135 deg to 405 deg (270 total degrees)
                val startAngle = 135f
                val sweepAngle = 270f
                val maxRpm = 8000f

                // Dial Outer Arc Track
                drawArc(
                    color = CockpitGaugeTrack,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - dialRadius + 8.dp.toPx(), center.y - dialRadius + 8.dp.toPx()),
                    size = Size((dialRadius - 8.dp.toPx()) * 2f, (dialRadius - 8.dp.toPx()) * 2f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Redline arc from 6.5k to 8.0k (fraction 6.5/8.0 = 0.8125)
                val redlineFrac = 6.5f / 8.0f
                val redlineStart = startAngle + redlineFrac * sweepAngle
                val redlineSweep = (1f - redlineFrac) * sweepAngle
                drawArc(
                    color = CockpitRed.copy(alpha = 0.85f),
                    startAngle = redlineStart,
                    sweepAngle = redlineSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - dialRadius + 8.dp.toPx(), center.y - dialRadius + 8.dp.toPx()),
                    size = Size((dialRadius - 8.dp.toPx()) * 2f, (dialRadius - 8.dp.toPx()) * 2f),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Ticks (0, 1, 2, 3, 4, 5, 6, 7, 8) + half ticks
                val tickRadius = dialRadius - 8.dp.toPx()
                val totalMajor = 8
                val totalTicks = totalMajor * 2 // half ticks

                for (i in 0..totalTicks) {
                    val frac = i.toFloat() / totalTicks.toFloat()
                    val angleDeg = startAngle + frac * sweepAngle
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val isMajor = (i % 2 == 0)
                    val tickLen = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                    val isRedline = (i.toFloat() / 2f) >= 6.5f

                    val tickColor = when {
                        isRedline -> CockpitRed
                        isMajor -> TextPrimary
                        else -> TextMuted
                    }

                    val outerX = center.x + tickRadius * cos(angleRad).toFloat()
                    val outerY = center.y + tickRadius * sin(angleRad).toFloat()
                    val innerX = center.x + (tickRadius - tickLen) * cos(angleRad).toFloat()
                    val innerY = center.y + (tickRadius - tickLen) * sin(angleRad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(outerX, outerY),
                        end = Offset(innerX, innerY),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                    )

                    // Draw numeral for major ticks
                    if (isMajor) {
                        val num = (i / 2).toString()
                        val numRadius = tickRadius - tickLen - 10.dp.toPx()
                        val numX = center.x + numRadius * cos(angleRad).toFloat()
                        val numY = center.y + numRadius * sin(angleRad).toFloat()

                        val measured = textMeasurer.measure(
                            text = num,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isRedline) CockpitRed else TextPrimary
                            )
                        )
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(numX - measured.size.width / 2f, numY - measured.size.height / 2f)
                        )
                    }
                }

                // Dial label: "x 1000 RPM"
                val rpmLabel = textMeasurer.measure(
                    text = "x 1000 RPM",
                    style = TextStyle(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                )
                drawText(
                    textLayoutResult = rpmLabel,
                    topLeft = Offset(center.x - rpmLabel.size.width / 2f, center.y - dialRadius * 0.40f)
                )

                // ------------------------------------------------------------
                // 3. MECHANICAL RED NEEDLE
                // ------------------------------------------------------------
                val needleFrac = (animatedRpm / maxRpm).coerceIn(0f, 1f)
                val needleAngleDeg = startAngle + needleFrac * sweepAngle
                val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())

                val needleLen = tickRadius - 16.dp.toPx()
                val needleTipX = center.x + needleLen * cos(needleAngleRad).toFloat()
                val needleTipY = center.y + needleLen * sin(needleAngleRad).toFloat()

                val perpAngleRad = needleAngleRad + Math.PI / 2
                val baseHalfWidth = 4.dp.toPx()
                val baseLeftX = center.x + baseHalfWidth * cos(perpAngleRad).toFloat()
                val baseLeftY = center.y + baseHalfWidth * sin(perpAngleRad).toFloat()
                val baseRightX = center.x - baseHalfWidth * cos(perpAngleRad).toFloat()
                val baseRightY = center.y - baseHalfWidth * sin(perpAngleRad).toFloat()

                // Counterweight tail
                val tailLen = 12.dp.toPx()
                val tailX = center.x - tailLen * cos(needleAngleRad).toFloat()
                val tailY = center.y - tailLen * sin(needleAngleRad).toFloat()

                val needlePath = Path().apply {
                    moveTo(baseLeftX, baseLeftY)
                    lineTo(needleTipX, needleTipY)
                    lineTo(baseRightX, baseRightY)
                    lineTo(tailX, tailY)
                    close()
                }
                drawPath(path = needlePath, color = CockpitRed)

                // Center pivot cap
                drawCircle(color = Color(0xFF1E2633), radius = 8.dp.toPx(), center = center)
                drawCircle(color = CockpitRed, radius = 4.dp.toPx(), center = center)
            }

            // ------------------------------------------------------------
            // CORNER LABELS & ICONS (OVERLAY)
            // ------------------------------------------------------------
            // Top Left: Coolant Temp
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 8.dp, start = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Coolant",
                        tint = CockpitSteel,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${data.coolantTemp.toInt()} °C",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "120 °C",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }

            // Bottom Left: Fuel
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 12.dp, start = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Fuel",
                        tint = CockpitAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${fuelPercent.toInt()} %",
                        color = CockpitAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "0 %",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }

            // Top Right: Load
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${data.throttlePos.toInt()} %",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Load",
                        tint = CockpitSteel,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = "100 %",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }

            // Bottom Right: Ambient / Intake
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp, end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${data.intakeAirTemp.toInt()} °C",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Intake Temp",
                        tint = CockpitSteel,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = "45 °C",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }

            // Center Digital Speed Pod (Below pivot)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F141D))
                    .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$speed",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "km/h",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ------------------------------------------------------------
        // BOTTOM POWER & TORQUE DYNAMIC METERS (As seen in Screenshot 1)
        // ------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CockpitCardElevated)
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Power meter bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Power: ",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentPowerHp.toInt()} cv",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "peak: ${peakPowerHp.toInt()} cv at $peakPowerRpm rpm",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                // Horizontal bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CockpitGaugeTrack)
                ) {
                    val powerFrac = (currentPowerHp.toFloat() / peakPowerHp.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = powerFrac)
                            .fillMaxHeight()
                            .background(CockpitSteel)
                    )
                }
            }

            // Torque meter bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Torque: ",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentTorqueNm.toInt()} nm",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "peak: ${peakTorqueNm.toInt()} nm at $peakTorqueRpm rpm",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CockpitGaugeTrack)
                ) {
                    val torqueFrac = (currentTorqueNm.toFloat() / peakTorqueNm.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = torqueFrac)
                            .fillMaxHeight()
                            .background(CockpitAmber)
                    )
                }
            }
        }
    }
}
