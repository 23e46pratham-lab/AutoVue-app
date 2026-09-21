package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun DtcScannerScreen(
    viewModel: SharedTelemetryViewModel,
    onBackClick: () -> Unit = {}
) {
    val dtcCodes by viewModel.dtcCodes.collectAsState()
    val isScanning by viewModel.isScanningDtcs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explanatory Mode Description (Screenshot 18)
        Text(
            text = "This tab shows the diagnostic trouble codes currently stored in the ECU read with mode 03 and 07 OBDII.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        // DTC Scan State Card (Screenshot 18)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        color = CockpitAmber,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Scanning ECU diagnostic memory...",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Querying PID Mode 03 & 07",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                } else if (dtcCodes.isEmpty()) {
                    // Big Green Checkmark matching Screenshot 18
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(CockpitGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "No DTCs",
                            tint = CockpitGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Reading from ECU has been successfully completed. Zero diagnostic trouble codes found!",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                } else {
                    // Display found DTCs with simplified, easy-to-read warnings
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        dtcCodes.forEach { dtc ->
                            val isCritical = dtc.severity.equals("CRITICAL", ignoreCase = true) || dtc.severity.equals("HIGH", ignoreCase = true)
                            val accentColor = if (isCritical) CockpitRed else CockpitAmber
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = CockpitCardElevated),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(accentColor.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = dtc.code,
                                                    color = accentColor,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                            Text(
                                                text = dtc.description,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(accentColor.copy(alpha = 0.25f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isCritical) "HIGH PRIORITY" else "ATTENTION",
                                                color = accentColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Simplified plain-English explanation
                                    Text(
                                        text = "Meaning: ${dtc.plainMeaning}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )

                                    // Simplified recommended action
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Handyman,
                                            contentDescription = null,
                                            tint = CockpitSteel,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Action: ${dtc.plainAction}",
                                            color = CockpitSteel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.scanDtcs() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Scan",
                    tint = CockpitSteel,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RE-SCAN",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { viewModel.simulateTestDtc() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CockpitCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitSurfaceBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Test Fault",
                    tint = CockpitAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TEST FAULT",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (dtcCodes.isNotEmpty()) {
                Button(
                    onClick = { viewModel.clearDtcs() },
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitRed.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CockpitRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear",
                        tint = CockpitRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CLEAR",
                        color = CockpitRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
