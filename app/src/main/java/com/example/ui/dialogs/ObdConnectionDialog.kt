package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.repository.ConnectionStatus
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitRed
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ObdConnectionDialog(
    status: ConnectionStatus,
    profileName: String,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            color = CockpitCard,
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CockpitCardElevated)
                                .border(1.dp, CockpitSurfaceBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (status == ConnectionStatus.CONNECTED) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                contentDescription = "OBD Connection",
                                tint = if (status == ConnectionStatus.CONNECTED) CockpitGreen else CockpitSteel,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "OBD-II Connection",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ELM327 Bluetooth Adapter",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Connection Status Glass Banner
                val statusColor = when (status) {
                    ConnectionStatus.CONNECTED -> CockpitGreen
                    ConnectionStatus.CONNECTING -> CockpitAmber
                    ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> CockpitRed
                }
                val statusLabel = when (status) {
                    ConnectionStatus.CONNECTED -> "CONNECTED & STREAMING"
                    ConnectionStatus.CONNECTING -> "CONNECTING TO ECU..."
                    ConnectionStatus.DISCONNECTED -> "DISCONNECTED"
                    ConnectionStatus.ERROR -> "CONNECTION ERROR"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = statusLabel,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = profileName,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Adapter Specs Table (Glass container)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ObdSpecRow(label = "Protocol", value = "ISO 15765-4 CAN (11/500)")
                    ObdSpecRow(label = "Hardware", value = "ELM327 Bluetooth v1.5")
                    ObdSpecRow(label = "Baud Rate", value = "38,400 bps")
                    ObdSpecRow(label = "Port Pinout", value = "Standard 16-pin J1962")
                    ObdSpecRow(label = "ECU Latency", value = if (status == ConnectionStatus.CONNECTED) "18 ms" else "---")
                }

                // Instructions note
                Text(
                    text = "Ensure your ELM327 adapter is firmly seated into your car's OBD-II port under the steering column with ignition turned ON.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (status == ConnectionStatus.CONNECTED) {
                        OutlinedButton(
                            onClick = {
                                onDisconnect()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            border = BorderStroke(1.dp, CockpitRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Disconnect", color = CockpitRed, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            onConnect()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CockpitSteel
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (status == ConnectionStatus.CONNECTED) "Reconnect OBD" else "Connect OBD",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ObdSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
