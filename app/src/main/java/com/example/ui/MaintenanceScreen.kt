package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EcuCodeInfo
import com.example.model.ServiceTicket
import com.example.model.TicketStatus
import com.example.model.UrgencyLevel
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate800
import com.example.ui.theme.StatusRed
import com.example.viewmodel.SharedTelemetryViewModel

val commonEcuCodes = listOf(
    EcuCodeInfo("P0300", "Engine Misfire Detected", "Random/multiple cylinder misfire detected in ECU logs.", UrgencyLevel.HIGH),
    EcuCodeInfo("P0118", "Coolant Temp Circuit High", "Engine coolant temperature sensor signal exceeded safe threshold.", UrgencyLevel.HIGH),
    EcuCodeInfo("P0102", "MAF Circuit Low Input", "Mass air flow sensor reporting below normal air intake levels.", UrgencyLevel.MEDIUM),
    EcuCodeInfo("P0122", "Throttle Position Low", "Throttle/Pedal position sensor circuit low voltage input.", UrgencyLevel.MEDIUM),
    EcuCodeInfo("P0171", "Fuel System Too Lean", "Air-fuel mixture too lean (Bank 1) indicating possible vacuum leak.", UrgencyLevel.MEDIUM),
    EcuCodeInfo("P0420", "Catalyst Efficiency Low", "Catalyst system efficiency below threshold (Bank 1).", UrgencyLevel.LOW)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(viewModel: SharedTelemetryViewModel) {
    val context = LocalContext.current
    val latestTick by viewModel.latestTick.collectAsState()
    val healthPrediction by viewModel.healthPrediction.collectAsState()
    val driverBehaviour by viewModel.driverBehaviour.collectAsState()
    val tickets by viewModel.tickets.collectAsState()
    val isTransmitting by viewModel.isTransmittingTicket.collectAsState()
    val telegramGatewayEnabled by viewModel.telegramGatewayEnabled.collectAsState()

    var selectedFaultCode by remember { mutableStateOf("") }
    var symptomsText by remember { mutableStateOf("") }
    var selectedUrgency by remember { mutableStateOf(UrgencyLevel.MEDIUM) }
    var isFaultDropdownExpanded by remember { mutableStateOf(false) }

    // Auto-detect DTC from telemetry if coolant is high or health is abnormal
    val activeFaultWarning = remember(latestTick, healthPrediction) {
        val coolant = latestTick?.data?.coolantTemp ?: 0.0
        val healthStatus = healthPrediction?.status ?: "Normal"
        when {
            coolant > 100.0 -> "P0118 - High Coolant Temperature (%.0f°C)".format(coolant)
            !healthStatus.equals("Normal", ignoreCase = true) -> "P0300 - Engine Diagnostic Alert ($healthStatus)"
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portal Header Banner
        PortalHeader(
            driverBehaviourClass = driverBehaviour?.behaviourClass ?: "NORMAL",
            elapsedSeconds = latestTick?.elapsedSeconds ?: 0.0,
            healthStatus = healthPrediction?.status ?: "Normal"
        )

        // Ticket Registration Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Ticket",
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "REGISTER A VEHICLE SERVICE FAULT TICKET",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Active ECU Sensor Status Banner
                if (activeFaultWarning == null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1B382B),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Healthy",
                                tint = Emerald400,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "ALL ECU SENSORS HEALTHY",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald400
                                )
                                Text(
                                    text = "No active trouble codes are present. You can still register custom issues (e.g., oil level, fluid leaks, transmission sound, squeaking brakes) manually below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFFC8E6C9),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF3E2723),
                        border = BorderStroke(1.dp, StatusRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = StatusRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "ECU FAULT ALERT DETECTED",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StatusRed
                                    )
                                    Text(
                                        text = activeFaultWarning,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFCCBC)
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    selectedFaultCode = activeFaultWarning.substringBefore(" -")
                                    selectedUrgency = UrgencyLevel.HIGH
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, StatusRed)
                            ) {
                                Text("Attach Code", fontSize = 10.sp, color = StatusRed)
                            }
                        }
                    }
                }

                // ECU Fault Codes Selection Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ECU FAULT CODES TO ATTACH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Indigo400,
                        letterSpacing = 1.sp
                    )

                    ExposedDropdownMenuBox(
                        expanded = isFaultDropdownExpanded,
                        onExpandedChange = { isFaultDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (selectedFaultCode.isEmpty()) "No active trouble codes to attach" else selectedFaultCode,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFaultDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedBorderColor = Indigo400
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isFaultDropdownExpanded,
                            onDismissRequest = { isFaultDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No active trouble codes to attach (Custom Issue)") },
                                onClick = {
                                    selectedFaultCode = ""
                                    isFaultDropdownExpanded = false
                                }
                            )
                            commonEcuCodes.forEach { code ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${code.code} - ${code.title}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(code.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedFaultCode = "${code.code} - ${code.title}"
                                        selectedUrgency = code.severity
                                        isFaultDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Detailed Symptoms & Issue Description Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "DETAILED SYMPTOMS & ISSUE DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Indigo400,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = symptomsText,
                        onValueChange = { symptomsText = it },
                        placeholder = {
                            Text(
                                text = "Describe symptoms or service required (e.g. Engine knocking at idle, fluid leakage on garage floor, brake squeal under light load)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedBorderColor = Indigo400
                        )
                    )
                }

                // Urgency Level Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "URGENCY LEVEL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Indigo400,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UrgencyLevel.values().forEach { level ->
                            val isSelected = selectedUrgency == level
                            val activeColor = when (level) {
                                UrgencyLevel.LOW -> Emerald400
                                UrgencyLevel.MEDIUM -> Color(0xFFFFB74D)
                                UrgencyLevel.HIGH -> StatusRed
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedUrgency = level }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit Transmit Button
                Button(
                    onClick = {
                        val finalDesc = symptomsText.ifBlank { "Routine inspection request" }
                        viewModel.submitServiceTicket(
                            faultCode = selectedFaultCode,
                            description = finalDesc,
                            urgency = selectedUrgency,
                            onSuccess = {
                                symptomsText = ""
                                Toast.makeText(context, "Ticket Transmitted to Apex Auto Services!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !isTransmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF880E4F),
                        contentColor = Color.White
                    )
                ) {
                    if (isTransmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TRANSMITTING FAULT TICKET...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Transmit",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TRANSMIT TICKET TO APEX AUTO SERVICES", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Current Service Partner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Partner",
                        tint = Indigo400,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "CURRENT SERVICE PARTNER",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "COMPANY NAME",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Apex Auto Services",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF29B6F6)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+15550192834"))
                                context.startActivity(intent)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SUPPORT CONTACT PHONE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.Gray)
                            Text("+1 (555) 019-2834", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Emerald400, modifier = Modifier.size(18.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:service@apexauto.com"))
                                context.startActivity(intent)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SUPPORT CONTACT EMAIL", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.Gray)
                            Text("service@apexauto.com", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Indigo400)
                        }
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = Indigo400, modifier = Modifier.size(18.dp))
                    }

                    Column {
                        Text("FACILITY / DISPATCH CENTER", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.Gray)
                        Text("404 Performance Blvd, Detroit, MI", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }

                Text(
                    text = "Need to change mechanic or roadside assistance partner? Update provider records in system configuration.",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // Telegram Gateway Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        Icon(
                            imageVector = if (telegramGatewayEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Telegram",
                            tint = if (telegramGatewayEnabled) Emerald400 else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "TELEGRAM TELEMETRY GATEWAY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = telegramGatewayEnabled,
                        onCheckedChange = { viewModel.toggleTelegramGateway(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald400)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (telegramGatewayEnabled) Color(0xFF1B382B) else Slate800,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = if (telegramGatewayEnabled) "GATEWAY STATUS: ENABLED / ONLINE" else "GATEWAY STATUS: MUTED / DISABLED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (telegramGatewayEnabled) Emerald400 else Color.LightGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "Telegram integration for real-time fault tickets. Enable the bot gateway to receive instant notifications on your mobile device.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Registered Vehicle Service Records Log Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REGISTERED VEHICLE SERVICE RECORDS LOG (${tickets.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (tickets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Empty",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "NO CURRENT TICKETS ON FILE WITH YOUR SERVICE PROVIDER.",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "SUBMIT A TICKET ABOVE WHEN A PROBLEM ARISES.",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        tickets.forEach { ticket ->
                            ServiceTicketItem(
                                ticket = ticket,
                                onResolve = { viewModel.resolveTicket(ticket.id) },
                                onDelete = { viewModel.deleteTicket(ticket.id) }
                            )
                        }
                    }
                }
            }
        }

        // ECU Trouble Codes Decoder Reference Card
        EcuTroubleCodesDecoderCard(
            onAttachCode = { code ->
                selectedFaultCode = "${code.code} - ${code.title}"
                selectedUrgency = code.severity
                Toast.makeText(context, "Attached ${code.code} to fault ticket form above", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PortalHeader(
    driverBehaviourClass: String,
    elapsedSeconds: Double,
    healthStatus: String
) {
    val mins = (elapsedSeconds / 60).toInt()
    val secs = (elapsedSeconds % 60).toInt()
    val tripStr = "%02d:%02d".format(mins, secs)
    val distKm = "%.1f".format(elapsedSeconds * 0.015)

    val isHealthy = healthStatus.equals("Normal", ignoreCase = true)
    val statusPillColor = if (isHealthy) Color(0xFF1B382B) else Color(0xFF3E2723)
    val statusTextColor = if (isHealthy) Emerald400 else StatusRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VEHICLE SERVICE & DISPATCH PORTAL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "REGISTER FAULTS, MONITOR SENSOR INCIDENTS AND TRANSMIT TICKETS TO ROADSIDE PARTNERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusPillColor,
                    border = BorderStroke(1.dp, statusTextColor)
                ) {
                    Text(
                        text = if (isHealthy) "✓ 95% HEALTHY" else "⚠ SENSOR ISSUE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Quick Status Pill Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge("DRIVE MODE", driverBehaviourClass.uppercase(), Color(0xFFFFB74D))
                StatusBadge("TRIP", tripStr, Color(0xFF29B6F6))
                StatusBadge("DIST", "$distKm KM", Emerald400)
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ServiceTicketItem(
    ticket: ServiceTicket,
    onResolve: () -> Unit,
    onDelete: () -> Unit
) {
    val urgencyColor = when (ticket.urgency) {
        UrgencyLevel.LOW -> Emerald400
        UrgencyLevel.MEDIUM -> Color(0xFFFFB74D)
        UrgencyLevel.HIGH -> StatusRed
    }

    val isResolved = ticket.status == TicketStatus.RESOLVED

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isResolved) Emerald400.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Text(
                        text = ticket.id,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Indigo400
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = urgencyColor.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, urgencyColor)
                    ) {
                        Text(
                            text = ticket.urgency.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = urgencyColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isResolved) Color(0xFF1B382B) else Color(0xFF1A237E)
                ) {
                    Text(
                        text = ticket.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isResolved) Emerald400 else Color(0xFF9FA8DA),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Fault Attached: ${ticket.faultCode}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Partner: ${ticket.servicePartner} • ${ticket.timestamp}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = Color.Gray
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!isResolved) {
                        OutlinedButton(
                            onClick = onResolve,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp),
                            border = BorderStroke(1.dp, Emerald400)
                        ) {
                            Text("Resolve", fontSize = 10.sp, color = Emerald400)
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EcuTroubleCodesDecoderCard(onAttachCode: (EcuCodeInfo) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, Indigo500.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Decoder",
                    tint = StatusRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "ECU TROUBLE CODES DECODER",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                commonEcuCodes.forEach { codeInfo ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = codeInfo.code,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StatusRed
                                    )
                                    Text(
                                        text = codeInfo.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = codeInfo.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onAttachCode(codeInfo) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Attach",
                                    tint = Indigo400
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
