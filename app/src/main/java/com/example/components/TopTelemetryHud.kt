package com.example.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetryData
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TopTelemetryHud(
    data: TelemetryData,
    elapsedMinutes: Int = 8,
    distanceKm: Double = 0.0,
    costEuro: Double = 0.0,
    voiceEnabled: Boolean = true,
    alertsEnabled: Boolean = true,
    onProfileClick: () -> Unit = {},
    onAlertsToggle: () -> Unit = {},
    onVoiceToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val speed = data.vss.toInt()
    // Calculate instantaneous consumption (l/100km): if speed > 2 km/h, (MAF / 14.7 / 720) * 3600 / speed * 100
    val instConsumptionStr = if (speed > 2 && data.maf > 0.1) {
        val l100 = ((data.maf / 14.7) / 730.0) * 3600.0 / speed * 100.0
        String.format(Locale.US, "%.1f l/100km", l100.coerceIn(0.0, 30.0))
    } else {
        "--.- l/100km"
    }

    val avgConsumptionStr = "--.- l/100km"
    val driveScoreStr = if (speed > 0) "88.5 %" else "--.- %"
    val costStr = if (costEuro > 0.01) String.format(Locale.US, "%.2f €", costEuro) else "--.- €"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CockpitCard)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Time, Distance, Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$elapsedMinutes min",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Distance",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f km", distanceKm),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cost
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Cost",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = costStr,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: 2x2 Grid of values + Right quick icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 2x2 Grid
                Column(modifier = Modifier.weight(1f)) {
                    // Grid Row 1: Consumption & Inst.speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = "Consumption",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = instConsumptionStr,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(0.9f)) {
                            Text(
                                text = "Inst.speed",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$speed km/h",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Grid Row 2: Consumption Ø & Drive
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(
                                text = "Consumption Ø",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = avgConsumptionStr,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(modifier = Modifier.weight(0.9f)) {
                            Text(
                                text = "Drive",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = driveScoreStr,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right 3 Quick Icons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Profile quick icon
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(CockpitSteel.copy(alpha = 0.18f))
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Vehicle Profile",
                            tint = CockpitSteel,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Alerts toggle icon
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (alertsEnabled) CockpitAmber.copy(alpha = 0.18f) else Color.Transparent)
                            .clickable { onAlertsToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alertsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Alerts",
                            tint = if (alertsEnabled) CockpitAmber else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Voice speaker toggle icon
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (voiceEnabled) CockpitGreen.copy(alpha = 0.18f) else Color.Transparent)
                            .clickable { onVoiceToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (voiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Voice Alert",
                            tint = if (voiceEnabled) CockpitGreen else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
