package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.model.RefuelingEntry
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RefuelingDialog(
    profileName: String,
    currentOdometer: Double,
    onDismiss: () -> Unit,
    onSave: (RefuelingEntry) -> Unit
) {
    val defaultDate = SimpleDateFormat("dd/MM/yy HH:mm", Locale.US).format(Date())
    var dateTime by remember { mutableStateOf(defaultDate) }
    var cost by remember { mutableStateOf("52.50") }
    var pricePerLiter by remember { mutableStateOf("1.75") }
    var fuelLiters by remember { mutableStateOf("30.0") }
    var odometer by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentOdometer)) }
    var afterRefuelPercent by remember { mutableStateOf("100.0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
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
                // Header: < Creation (Screenshot 17)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = "Refuel",
                            tint = CockpitAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Creation (Refueling)",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Profile
                OutlinedTextField(
                    value = profileName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Profile*", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CockpitSteel,
                        unfocusedBorderColor = CockpitSurfaceBorder
                    )
                )

                // Date Time
                OutlinedTextField(
                    value = dateTime,
                    onValueChange = { dateTime = it },
                    label = { Text("Date time", color = TextMuted, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CockpitSteel,
                        unfocusedBorderColor = CockpitSurfaceBorder
                    )
                )

                // Cost & Price / l
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Cost (€)*", color = TextMuted, fontSize = 11.sp) },
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
                        value = pricePerLiter,
                        onValueChange = { pricePerLiter = it },
                        label = { Text("Price €/l*", color = TextMuted, fontSize = 11.sp) },
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

                // Fuel (l) & Odometer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fuelLiters,
                        onValueChange = { fuelLiters = it },
                        label = { Text("Fuel (l)", color = TextMuted, fontSize = 11.sp) },
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
                }

                // After refueling (%)
                OutlinedTextField(
                    value = afterRefuelPercent,
                    onValueChange = { afterRefuelPercent = it },
                    label = { Text("After refueling (%)", color = TextMuted, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CockpitSteel,
                        unfocusedBorderColor = CockpitSurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val entry = RefuelingEntry(
                            id = "ref-${System.currentTimeMillis()}",
                            profile = profileName,
                            dateTime = dateTime,
                            costEuro = cost.toDoubleOrNull() ?: 0.0,
                            pricePerLiter = pricePerLiter.toDoubleOrNull() ?: 0.0,
                            fuelLiters = fuelLiters.toDoubleOrNull() ?: 0.0,
                            odometerKm = odometer.toDoubleOrNull() ?: currentOdometer,
                            afterRefuelingPercent = afterRefuelPercent.toDoubleOrNull() ?: 100.0
                        )
                        onSave(entry)
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
                        text = "SAVE REFUELING ENTRY",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
