package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.VehicleProfile
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun VehicleProfileDialog(
    currentProfile: VehicleProfile,
    onDismiss: () -> Unit,
    onSave: (VehicleProfile) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.profileName) }
    var fuelSupply by remember { mutableStateOf(currentProfile.fuelSupply) }
    var startStopEquipped by remember { mutableStateOf(currentProfile.startAndStopEquipped) }
    var displacement by remember { mutableStateOf(currentProfile.displacementCc.toString()) }
    var maxPower by remember { mutableStateOf(currentProfile.maxPowerHp.toString()) }
    var odometer by remember { mutableStateOf(currentProfile.odometerKm.toString()) }
    var weight by remember { mutableStateOf(currentProfile.totalWeightKg.toString()) }
    var consumption by remember { mutableStateOf(currentProfile.consumptionL100km.toString()) }
    var correctiveConsump by remember { mutableStateOf(currentProfile.correctiveConsumption.toString()) }
    var tankCapacity by remember { mutableStateOf(currentProfile.tankCapacityLiters.toString()) }
    var fuelLeft by remember { mutableStateOf(currentProfile.fuelLeftPercent.toString()) }
    var vin by remember { mutableStateOf(currentProfile.vin) }
    var isActive by remember { mutableStateOf(currentProfile.isActive) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(12.dp)),
            color = CockpitCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header (Screenshots 8 & 10: "Modify pratham")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modify ${currentProfile.profileName}",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Profile Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 8) name = it },
                    label = { Text("Profile name*", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CockpitSteel,
                        unfocusedBorderColor = CockpitSurfaceBorder
                    )
                )

                // Fuel Supply
                OutlinedTextField(
                    value = fuelSupply,
                    onValueChange = { fuelSupply = it },
                    label = { Text("Fuel supply* (gasoline / diesel)", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CockpitSteel,
                        unfocusedBorderColor = CockpitSurfaceBorder
                    )
                )

                // Displacement & Max Power Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = displacement,
                        onValueChange = { displacement = it },
                        label = { Text("Displacement (cc)*", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CockpitSurfaceBorder
                        )
                    )
                    OutlinedTextField(
                        value = maxPower,
                        onValueChange = { maxPower = it },
                        label = { Text("Max power (HP)", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CockpitSurfaceBorder
                        )
                    )
                }

                // Odometer & Total Weight Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = odometer,
                        onValueChange = { odometer = it },
                        label = { Text("Odometer (km)", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CockpitSurfaceBorder
                        )
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Total weight (KG)", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CockpitSurfaceBorder
                        )
                    )
                }

                // Consumption & Corrective Ratio Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = consumption,
                        onValueChange = { consumption = it },
                        label = { Text("Consump. (L/100km)", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CockpitSurfaceBorder
                        )
                    )
                    OutlinedTextField(
                        value = correctiveConsump,
                        onValueChange = { correctiveConsump = it },
                        label = { Text("Corrective consump.", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CockpitSurfaceBorder
                        )
                    )
                }

                // Tank capacity & Fuel left Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tankCapacity,
                        onValueChange = { tankCapacity = it },
                        label = { Text("Tank capacity (l)", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CockpitSurfaceBorder
                        )
                    )
                    OutlinedTextField(
                        value = fuelLeft,
                        onValueChange = { fuelLeft = it },
                        label = { Text("Fuel left (%)", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = CockpitSteel,
                            unfocusedBorderColor = CockpitSurfaceBorder
                        )
                    )
                }

                // VIN
                OutlinedTextField(
                    value = vin,
                    onValueChange = { vin = it },
                    label = { Text("VIN", color = TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CockpitSteel,
                        unfocusedBorderColor = CockpitSurfaceBorder
                    )
                )

                // Active Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = CheckboxDefaults.colors(checkedColor = CockpitSteel)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active profile (all movement data will be associated with this profile)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Save Button (Screenshot 8)
                Button(
                    onClick = {
                        val updated = currentProfile.copy(
                            profileName = name.ifBlank { "pratham" },
                            fuelSupply = fuelSupply,
                            startAndStopEquipped = startStopEquipped,
                            displacementCc = displacement.toIntOrNull() ?: currentProfile.displacementCc,
                            maxPowerHp = maxPower.toDoubleOrNull() ?: currentProfile.maxPowerHp,
                            odometerKm = odometer.toDoubleOrNull() ?: currentProfile.odometerKm,
                            totalWeightKg = weight.toDoubleOrNull() ?: currentProfile.totalWeightKg,
                            consumptionL100km = consumption.toDoubleOrNull() ?: currentProfile.consumptionL100km,
                            correctiveConsumption = correctiveConsump.toDoubleOrNull() ?: currentProfile.correctiveConsumption,
                            tankCapacityLiters = tankCapacity.toDoubleOrNull() ?: currentProfile.tankCapacityLiters,
                            fuelLeftPercent = fuelLeft.toDoubleOrNull() ?: currentProfile.fuelLeftPercent,
                            vin = vin,
                            isActive = isActive
                        )
                        onSave(updated)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitSteel),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE PROFILE",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
