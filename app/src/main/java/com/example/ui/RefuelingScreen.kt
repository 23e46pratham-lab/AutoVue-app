package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.RefuelingEntry
import com.example.model.RegionalFuelPreset
import com.example.ui.theme.AtherMint
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefuelingScreen(
    viewModel: SharedTelemetryViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCreationMode by remember { mutableStateOf(false) }
    var showRegionalPriceDialog by remember { mutableStateOf(false) }
    var showTopUpConfirmDialog by remember { mutableStateOf(false) }
    var showResetTripFuelConfirm by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<RefuelingEntry?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // Intercept hardware/system back button if in Creation mode
    BackHandler {
        if (isCreationMode) {
            isCreationMode = false
        } else {
            onBackClick()
        }
    }

    if (isCreationMode) {
        RefuelingCreationScreen(
            viewModel = viewModel,
            onBack = { isCreationMode = false },
            onSaved = { isCreationMode = false }
        )
    } else {
        val profile by viewModel.vehicleProfile.collectAsStateWithLifecycle()
        val currentFuelLiters by viewModel.currentFuelLiters.collectAsStateWithLifecycle()
        val currentFuelPercent by viewModel.currentFuelPercent.collectAsStateWithLifecycle()
        val estimatedRangeKm by viewModel.estimatedRangeKm.collectAsStateWithLifecycle()
        val instantEconomyKmL by viewModel.instantFuelEconomyKmL.collectAsStateWithLifecycle()
        val instantEconomyL100km by viewModel.instantFuelEconomyL100km.collectAsStateWithLifecycle()
        val avgEconomyKmL by viewModel.averageFuelEconomyKmL.collectAsStateWithLifecycle()
        val avgEconomyL100km by viewModel.averageFuelEconomyL100km.collectAsStateWithLifecycle()
        val fuelConsumedTrip by viewModel.fuelConsumedTripLiters.collectAsStateWithLifecycle()
        val tripCost by viewModel.tripCostEuro.collectAsStateWithLifecycle()
        val costPerKm by viewModel.costPerKm.collectAsStateWithLifecycle()
        val fuelPrice by viewModel.fuelPricePerLiter.collectAsStateWithLifecycle()
        val currency by viewModel.fuelCurrency.collectAsStateWithLifecycle()
        val entries by viewModel.refuelingEntries.collectAsStateWithLifecycle()
        val latestTick by viewModel.latestTick.collectAsStateWithLifecycle()
        val isCustomPrice by viewModel.isCustomFuelPrice.collectAsStateWithLifecycle()
        val selectedPreset by viewModel.selectedFuelPreset.collectAsStateWithLifecycle()

        val tankCapacity = profile.tankCapacityLiters.coerceAtLeast(1.0)
        val burnRateLph = latestTick?.ml?.fuel?.fuelFlowLitersPerHour
            ?: if (latestTick?.data?.maf != null && latestTick!!.data.maf > 0.1) {
                ((latestTick!!.data.maf / 14.7) / 730.0) * 3600.0
            } else 0.65

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Refueling",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${profile.profileName} · ${profile.vehicleModel} (${tankCapacity.toInt()}L Tank)",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.speakVehicleRange() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak Range",
                                tint = AtherMint
                            )
                        }
                        IconButton(onClick = { showRegionalPriceDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Regional Fuel Price",
                                tint = CockpitSteel
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = TextSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Regional Fuel Price") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Public, contentDescription = null, tint = CockpitSteel)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showRegionalPriceDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Top-Up to Full Tank (100%)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = CockpitGreen)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showTopUpConfirmDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reset Trip Fuel & Economy") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = CockpitAmber)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showResetTripFuelConfirm = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Speak Vehicle Range & Burn Rate") },
                                    leadingIcon = {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = AtherMint)
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.speakVehicleRange()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CockpitBackground
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { isCreationMode = true },
                    containerColor = AtherMint,
                    contentColor = CockpitBackground,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Refueling")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Refueling",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            containerColor = CockpitBackground,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                // Section 1: Live Animated Fuel Level & Gauge Card
                item {
                    LiveFuelGaugeCard(
                        fuelLiters = currentFuelLiters,
                        fuelPercent = currentFuelPercent,
                        tankCapacity = tankCapacity,
                        burnRateLph = burnRateLph,
                        tripConsumedLiters = fuelConsumedTrip
                    )
                }

                // Section 2: Estimated Remaining Range Hero Card with Voice Speak
                item {
                    EstimatedRangeCard(
                        rangeKm = estimatedRangeKm,
                        fuelLiters = currentFuelLiters,
                        fuelPercent = currentFuelPercent,
                        tankCapacity = tankCapacity,
                        burnRateLph = burnRateLph,
                        avgKmL = avgEconomyKmL,
                        onSpeakRange = { viewModel.speakVehicleRange() }
                    )
                }

                // Section 3: Real-Time Economy & Financial Metrics (2x3 Grid)
                item {
                    FuelMetricsGrid(
                        instantKmL = instantEconomyKmL,
                        instantL100km = instantEconomyL100km,
                        avgKmL = avgEconomyKmL,
                        avgL100km = avgEconomyL100km,
                        tripConsumedL = fuelConsumedTrip,
                        tripCost = tripCost,
                        costPerKm = costPerKm,
                        fuelPrice = fuelPrice,
                        currency = currency,
                        isCustomPrice = isCustomPrice,
                        presetName = selectedPreset.regionName,
                        onChangePrice = { showRegionalPriceDialog = true }
                    )
                }

                // Section 4: Quick Action Controls Bar
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showTopUpConfirmDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CockpitGreen.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CockpitGreen),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Full Tank (100%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showResetTripFuelConfirm = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CockpitSteel.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showRegionalPriceDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CockpitAmber.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CockpitAmber),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Price / L", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 5: Refueling Diary / History Header & Stats
                item {
                    val totalLiters = entries.sumOf { it.fuelLiters }
                    val totalSpent = entries.sumOf { it.costEuro }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = AtherMint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Refueling Diary",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CockpitCardElevated,
                                border = BorderStroke(1.dp, CockpitSurfaceBorder)
                            ) {
                                Text(
                                    text = "${entries.size}",
                                    color = AtherMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (entries.isNotEmpty()) {
                            Text(
                                text = String.format(Locale.US, "Total: %.1f L · %.2f %s", totalLiters, totalSpent, currency),
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Section 6: Diary Entries List
                if (entries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CockpitCard),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalGasStation,
                                    contentDescription = null,
                                    tint = CockpitSteel.copy(alpha = 0.5f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Refueling Entries Recorded Yet",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap the 'Add Refueling' button to log your first refueling receipt, price, and fuel level.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(entries, key = { it.id }) { entry ->
                        RefuelingDiaryItemCard(
                            entry = entry,
                            currency = currency,
                            onDelete = { entryToDelete = entry }
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showTopUpConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showTopUpConfirmDialog = false },
            title = { Text("Reset Fuel Level to Full Tank?", color = TextPrimary) },
            text = {
                Text(
                    "This will set your tracked fuel level to 100% capacity (${viewModel.vehicleProfile.value.tankCapacityLiters.toInt()} Liters) and recalculate your driving range autonomy.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetFuelLevelToFull()
                        showTopUpConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitGreen)
                ) {
                    Text("Top-Up to 100%", color = CockpitBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CockpitCard,
            tonalElevation = 6.dp
        )
    }

    if (showResetTripFuelConfirm) {
        AlertDialog(
            onDismissRequest = { showResetTripFuelConfirm = false },
            title = { Text("Reset Trip Fuel Session?", color = TextPrimary) },
            text = {
                Text(
                    "This will clear the current session's accumulated fuel burn, trip cost, and distance counters for this drive.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetTripSession()
                        showResetTripFuelConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitAmber)
                ) {
                    Text("Reset Session", color = CockpitBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetTripFuelConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CockpitCard
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Refueling Record?", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete the refueling entry from ${entry.dateTime} (+${entry.fuelLiters}L)?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRefuelingEntry(entry.id)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CockpitRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CockpitCard
        )
    }

    if (showRegionalPriceDialog) {
        RegionalFuelPriceDialog(
            viewModel = viewModel,
            onDismiss = { showRegionalPriceDialog = false }
        )
    }
}

/**
 * The "Creation" Screen (Inspired by reference screenshot 3).
 * Clean layout: Profile, Date, Time, Cost, Price, Fuel, Odometer, After Refueling %, Save GPS checkbox, and Floppy Disk Save Button.
 * Inputs start empty with helpful placeholders as instructed by user.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefuelingCreationScreen(
    viewModel: SharedTelemetryViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val profile by viewModel.vehicleProfile.collectAsStateWithLifecycle()
    val currentFuelLiters by viewModel.currentFuelLiters.collectAsStateWithLifecycle()
    val currentFuelPercent by viewModel.currentFuelPercent.collectAsStateWithLifecycle()
    val activePrice by viewModel.fuelPricePerLiter.collectAsStateWithLifecycle()
    val currency by viewModel.fuelCurrency.collectAsStateWithLifecycle()
    val tripDist by viewModel.tripDistanceKm.collectAsStateWithLifecycle()
    val latestTick by viewModel.latestTick.collectAsStateWithLifecycle()

    val tankCapacity = profile.tankCapacityLiters.coerceAtLeast(1.0)
    val estimatedOdometer = profile.odometerKm + tripDist

    val now = remember { Date() }
    val defaultDate = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US).format(now) }
    val defaultTime = remember { SimpleDateFormat("HH:mm", Locale.US).format(now) }

    var dateStr by remember { mutableStateOf(defaultDate) }
    var timeStr by remember { mutableStateOf(defaultTime) }
    var costInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", activePrice)) }
    var fuelInput by remember { mutableStateOf("") }
    var odometerInput by remember { mutableStateOf(String.format(Locale.US, "%.0f", estimatedOdometer)) }
    var afterRefuelSliderPercent by remember { mutableDoubleStateOf(-1.0) }
    var saveGpsPosition by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // Reactive calculations:
    // If Cost and Price are entered, calculate added fuel
    // If Fuel and Price are entered, calculate cost
    val parsedPrice = priceInput.toDoubleOrNull() ?: activePrice
    val parsedCost = costInput.toDoubleOrNull()
    val parsedFuel = fuelInput.toDoubleOrNull()

    val effectiveAddedLiters = when {
        parsedFuel != null && parsedFuel > 0.0 -> parsedFuel
        parsedCost != null && parsedCost > 0.0 && parsedPrice > 0.0 -> parsedCost / parsedPrice
        else -> 0.0
    }

    val computedPostRefuelPercent = if (afterRefuelSliderPercent >= 0.0) {
        afterRefuelSliderPercent
    } else {
        val newLiters = (currentFuelLiters + effectiveAddedLiters).coerceAtMost(tankCapacity)
        ((newLiters / tankCapacity) * 100.0).coerceIn(0.0, 100.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Creation",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Quick fill to 100%
                        afterRefuelSliderPercent = 100.0
                        val neededLiters = (tankCapacity - currentFuelLiters).coerceAtLeast(0.0)
                        if (neededLiters > 0.0) {
                            fuelInput = String.format(Locale.US, "%.1f", neededLiters)
                            if (parsedPrice > 0.0) {
                                costInput = String.format(Locale.US, "%.2f", neededLiters * parsedPrice)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = "Full Tank Shortcut",
                            tint = CockpitGreen
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = TextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Fill to 100% Full Tank") },
                                onClick = {
                                    showMenu = false
                                    afterRefuelSliderPercent = 100.0
                                    val neededLiters = (tankCapacity - currentFuelLiters).coerceAtLeast(0.0)
                                    fuelInput = String.format(Locale.US, "%.1f", neededLiters)
                                    costInput = String.format(Locale.US, "%.2f", neededLiters * parsedPrice)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Inputs") },
                                onClick = {
                                    showMenu = false
                                    costInput = ""
                                    fuelInput = ""
                                    afterRefuelSliderPercent = -1.0
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CockpitBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val finalAddedFuel = when {
                        parsedFuel != null && parsedFuel > 0.0 -> parsedFuel
                        parsedCost != null && parsedCost > 0.0 && parsedPrice > 0.0 -> parsedCost / parsedPrice
                        afterRefuelSliderPercent > currentFuelPercent -> {
                            val targetL = (afterRefuelSliderPercent / 100.0) * tankCapacity
                            (targetL - currentFuelLiters).coerceAtLeast(0.0)
                        }
                        else -> 0.0
                    }

                    if (finalAddedFuel <= 0.0 && afterRefuelSliderPercent < 0.0) {
                        errorMessage = "Please enter either the total cost, liters added, or adjust the fuel level."
                        return@FloatingActionButton
                    }

                    val finalCost = when {
                        parsedCost != null && parsedCost > 0.0 -> parsedCost
                        finalAddedFuel > 0.0 && parsedPrice > 0.0 -> finalAddedFuel * parsedPrice
                        else -> 0.0
                    }

                    val finalOdometer = odometerInput.toDoubleOrNull() ?: estimatedOdometer

                    val locationNotes = if (saveGpsPosition && latestTick?.data?.hasGps == true) {
                        val lat = latestTick?.data?.lat ?: 0.0
                        val lon = latestTick?.data?.lon ?: 0.0
                        "GPS: ${String.format(Locale.US, "%.4f, %.4f", lat, lon)}"
                    } else {
                        "Refueling Station"
                    }

                    val entry = RefuelingEntry(
                        id = "ref-${System.currentTimeMillis()}",
                        profile = profile.profileName,
                        dateTime = "$dateStr $timeStr",
                        costEuro = finalCost,
                        pricePerLiter = parsedPrice,
                        fuelLiters = finalAddedFuel,
                        odometerKm = finalOdometer,
                        afterRefuelingPercent = computedPostRefuelPercent,
                        currency = currency,
                        savePosition = saveGpsPosition,
                        locationNotes = locationNotes
                    )

                    viewModel.performRefueling(
                        entry = entry,
                        addedLiters = finalAddedFuel,
                        afterPercent = computedPostRefuelPercent
                    )
                    onSaved()
                },
                containerColor = AtherMint,
                contentColor = CockpitBackground,
                shape = CircleShape,
                modifier = Modifier
                    .size(58.dp)
                    .padding(bottom = 6.dp, end = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save Refueling",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        containerColor = CockpitBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            errorMessage?.let { msg ->
                Surface(
                    color = CockpitRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CockpitRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CockpitRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }

            // 1. Profile Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Profile", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "${profile.profileName} (${profile.vehicleModel})",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CockpitCardElevated,
                        border = BorderStroke(1.dp, CockpitSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = CockpitSteel, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${tankCapacity.toInt()}L Tank", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // 2. Date & Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Date") },
                    placeholder = { Text("dd/MM/yyyy") },
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = timeStr,
                    onValueChange = { timeStr = it },
                    label = { Text("Time") },
                    placeholder = { Text("HH:mm") },
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Cost & Price Row (with automatic dual calculation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = costInput,
                    onValueChange = {
                        costInput = it
                        errorMessage = null
                        val c = it.toDoubleOrNull()
                        val p = priceInput.toDoubleOrNull() ?: activePrice
                        if (c != null && c > 0.0 && p > 0.0) {
                            fuelInput = String.format(Locale.US, "%.2f", c / p)
                        }
                    },
                    label = { Text("Cost ($currency)") },
                    placeholder = { Text("e.g. 50.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = {
                        priceInput = it
                        val p = it.toDoubleOrNull()
                        val c = costInput.toDoubleOrNull()
                        if (p != null && p > 0.0 && c != null && c > 0.0) {
                            fuelInput = String.format(Locale.US, "%.2f", c / p)
                        }
                    },
                    label = { Text("Price / L ($currency)") },
                    placeholder = { Text("e.g. 1.78") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. Fuel Liters Added Input
            OutlinedTextField(
                value = fuelInput,
                onValueChange = {
                    fuelInput = it
                    errorMessage = null
                    val f = it.toDoubleOrNull()
                    val p = priceInput.toDoubleOrNull() ?: activePrice
                    if (f != null && f > 0.0 && p > 0.0) {
                        costInput = String.format(Locale.US, "%.2f", f * p)
                    }
                },
                label = { Text("Fuel Added (Liters)") },
                placeholder = { Text("e.g. 28.5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    Text(
                        text = "Liters",
                        color = CockpitAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Odometer Field
            OutlinedTextField(
                value = odometerInput,
                onValueChange = { odometerInput = it },
                label = { Text("Odometer (km)") },
                placeholder = { Text("e.g. 59477") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = outlinedFieldColors(),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    Text(
                        text = "km",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // 6. After Refueling Level Preview & Slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "After Refueling Level",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f %%", computedPostRefuelPercent),
                            color = if (computedPostRefuelPercent > 35) AtherMint else CockpitAmber,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Level Bar preview
                    LinearProgressIndicator(
                        progress = { (computedPostRefuelPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (computedPostRefuelPercent > 35) AtherMint else CockpitAmber,
                        trackColor = CockpitCardElevated
                    )

                    Slider(
                        value = computedPostRefuelPercent.toFloat(),
                        onValueChange = { afterRefuelSliderPercent = it.toDouble() },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = AtherMint,
                            activeTrackColor = AtherMint,
                            inactiveTrackColor = CockpitCardElevated
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                afterRefuelSliderPercent = 100.0
                                val neededL = (tankCapacity - currentFuelLiters).coerceAtLeast(0.0)
                                fuelInput = String.format(Locale.US, "%.1f", neededL)
                                costInput = String.format(Locale.US, "%.2f", neededL * parsedPrice)
                            }
                        ) {
                            Text("Full Tank (100%)", color = CockpitGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = {
                                val currentPct = currentFuelPercent
                                afterRefuelSliderPercent = (currentPct + 25.0).coerceAtMost(100.0)
                                val addL = (tankCapacity * 0.25)
                                fuelInput = String.format(Locale.US, "%.1f", addL)
                                costInput = String.format(Locale.US, "%.2f", addL * parsedPrice)
                            }
                        ) {
                            Text("+25% Tank", color = CockpitSteel, fontSize = 11.sp)
                        }

                        TextButton(
                            onClick = {
                                afterRefuelSliderPercent = -1.0
                            }
                        ) {
                            Text("Auto-Compute", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }

            // 7. Save current position Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { saveGpsPosition = !saveGpsPosition }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveGpsPosition,
                    onCheckedChange = { saveGpsPosition = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AtherMint,
                        uncheckedColor = CockpitSteel,
                        checkmarkColor = CockpitBackground
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Save current position",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (latestTick?.data?.hasGps == true) "GPS Active: Live Coordinates Recorded" else "Location will be tagged with date & odometer",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

/**
 * Centerpiece Live Fuel Gauge Card
 * Shows remaining liters, dynamic level progress bar with segment ticks, and active burn rate.
 */
@Composable
private fun LiveFuelGaugeCard(
    fuelLiters: Double,
    fuelPercent: Double,
    tankCapacity: Double,
    burnRateLph: Double,
    tripConsumedLiters: Double,
    modifier: Modifier = Modifier
) {
    val levelColor = when {
        fuelPercent > 35.0 -> AtherMint
        fuelPercent > 15.0 -> CockpitAmber
        else -> CockpitRed
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Status & Capacity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Fuel Level",
                        tint = levelColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FUEL TANK STATUS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CockpitCardElevated,
                    border = BorderStroke(0.75.dp, CockpitSurfaceBorder)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f / %.0f L Capacity", fuelLiters, tankCapacity),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Big Level Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = String.format(Locale.US, "%.1f%%", fuelPercent),
                        color = levelColor,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", fuelLiters)} Liters Remaining",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (burnRateLph > 0.05) AtherMint.copy(alpha = 0.12f) else CockpitCardElevated,
                        border = BorderStroke(0.75.dp, if (burnRateLph > 0.05) AtherMint.copy(alpha = 0.4f) else CockpitSurfaceBorder)
                    ) {
                        Text(
                            text = String.format(Locale.US, "🔥 %.2f L/h Burn", burnRateLph),
                            color = if (burnRateLph > 0.05) AtherMint else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "-%.3f L consumed this drive", tripConsumedLiters),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Visual Segmented Gauge Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(CockpitCardElevated)
                        .border(1.dp, CockpitSurfaceBorder, RoundedCornerShape(9.dp))
                ) {
                    val fillFraction = (fuelPercent / 100.0).toFloat().coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillFraction)
                            .height(18.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(levelColor.copy(alpha = 0.7f), levelColor)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Scale Markings: [E / 0%]  [1/4]  [1/2]  [3/4]  [F / 100%]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("E (0%)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("1/4", color = TextMuted, fontSize = 9.sp)
                    Text("1/2", color = TextMuted, fontSize = 9.sp)
                    Text("3/4", color = TextMuted, fontSize = 9.sp)
                    Text("F (100%)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Autonomy / Estimated Range Hero Card with Voice Speak action
 */
@Composable
private fun EstimatedRangeCard(
    rangeKm: Double,
    fuelLiters: Double,
    fuelPercent: Double,
    tankCapacity: Double,
    burnRateLph: Double,
    avgKmL: Double,
    onSpeakRange: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLowRange = rangeKm < 50.0 || fuelPercent < 15.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLowRange) CockpitAmber.copy(alpha = 0.12f) else CockpitCard),
        border = BorderStroke(1.dp, if (isLowRange) CockpitAmber else CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Range",
                        tint = if (isLowRange) CockpitAmber else AtherMint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ESTIMATED RANGE AUTONOMY",
                        color = if (isLowRange) CockpitAmber else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Button(
                    onClick = onSpeakRange,
                    shape = RoundedCornerShape(20),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CockpitCardElevated,
                        contentColor = AtherMint
                    ),
                    border = BorderStroke(0.75.dp, CockpitSurfaceBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Speak Range", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${rangeKm.toInt()} km",
                    color = if (isLowRange) CockpitAmber else TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = String.format(Locale.US, "%.1f km/L avg burn", avgKmL),
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Computed from ${String.format(Locale.US, "%.1f", fuelLiters)}L in tank (${fuelPercent.toInt()}% of ${tankCapacity.toInt()}L) at current burn rate (${String.format(Locale.US, "%.1f", burnRateLph)} L/h).",
                color = TextSecondary,
                fontSize = 11.sp
            )

            if (isLowRange) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CockpitAmber.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, CockpitAmber.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CockpitAmber, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Low Range Warning: Autonomy under 50 km. Refuel soon to prevent engine fuel starvation.",
                            color = CockpitAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2x3 Grid of sleek Cockpit Metric Cards
 */
@Composable
private fun FuelMetricsGrid(
    instantKmL: Double,
    instantL100km: Double,
    avgKmL: Double,
    avgL100km: Double,
    tripConsumedL: Double,
    tripCost: Double,
    costPerKm: Double,
    fuelPrice: Double,
    currency: String,
    isCustomPrice: Boolean,
    presetName: String,
    onChangePrice: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Instant Economy & Avg Economy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CockpitMetricCard(
                title = "INSTANT ECONOMY",
                primaryValue = if (instantKmL > 0.1) String.format(Locale.US, "%.1f km/L", instantKmL) else "--.- km/L",
                subValue = if (instantL100km > 0.1) String.format(Locale.US, "%.1f L/100km", instantL100km) else "Idle",
                accentColor = AtherMint,
                modifier = Modifier.weight(1f)
            )

            CockpitMetricCard(
                title = "AVERAGE ECONOMY",
                primaryValue = String.format(Locale.US, "%.1f km/L", avgKmL),
                subValue = String.format(Locale.US, "%.1f L/100km", avgL100km),
                accentColor = CockpitGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Trip Fuel Consumed & Trip Fuel Cost
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CockpitMetricCard(
                title = "TRIP FUEL BURNED",
                primaryValue = String.format(Locale.US, "%.2f L", tripConsumedL),
                subValue = "Active Session",
                accentColor = CockpitAmber,
                modifier = Modifier.weight(1f)
            )

            CockpitMetricCard(
                title = "TRIP FUEL COST",
                primaryValue = String.format(Locale.US, "%.2f %s", tripCost, currency),
                subValue = "Total Session Cost",
                accentColor = AtherMint,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Cost per Km & Regional Fuel Price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CockpitMetricCard(
                title = "COST PER KM",
                primaryValue = String.format(Locale.US, "%.3f %s/km", costPerKm, currency),
                subValue = "Efficiency Rate",
                accentColor = CockpitSteel,
                modifier = Modifier.weight(1f)
            )

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onChangePrice() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REGIONAL PRICE",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Price",
                            tint = AtherMint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%.2f %s/L", fuelPrice, currency),
                        color = AtherMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isCustomPrice) "Manual Override" else presetName,
                        color = TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CockpitMetricCard(
    title: String,
    primaryValue: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = primaryValue,
                color = accentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subValue,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Refueling Diary Item Card
 */
@Composable
private fun RefuelingDiaryItemCard(
    entry: RefuelingEntry,
    currency: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.dateTime,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (entry.locationNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS",
                            tint = AtherMint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = CockpitSteel,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = String.format(Locale.US, "+%.1f L Added", entry.fuelLiters),
                        color = AtherMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f %s (%.2f %s/L)", entry.costEuro, entry.currency.ifBlank { currency }, entry.pricePerLiter, entry.currency.ifBlank { currency }),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${entry.odometerKm.toInt()} km",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tank Level: ${entry.afterRefuelingPercent.toInt()}%",
                        color = CockpitAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (entry.locationNotes.isNotBlank()) {
                Text(
                    text = entry.locationNotes,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Regional Fuel Price Preset & Custom Override Dialog
 */
@Composable
private fun RegionalFuelPriceDialog(
    viewModel: SharedTelemetryViewModel,
    onDismiss: () -> Unit
) {
    val presets = viewModel.regionalFuelPresets
    val selectedPreset by viewModel.selectedFuelPreset.collectAsStateWithLifecycle()
    val isCustomPrice by viewModel.isCustomFuelPrice.collectAsStateWithLifecycle()
    val currentPrice by viewModel.fuelPricePerLiter.collectAsStateWithLifecycle()
    val currentCurrency by viewModel.fuelCurrency.collectAsStateWithLifecycle()

    var manualPriceText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentPrice)) }
    var manualCurrencyText by remember { mutableStateOf(currentCurrency) }
    var selectedTab by remember { mutableStateOf(if (isCustomPrice) 1 else 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Regional Fuel Price & Override",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Segmented tab: Regional Presets vs Manual Override
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CockpitCardElevated)
                        .padding(2.dp)
                ) {
                    Surface(
                        onClick = { selectedTab = 0 },
                        shape = RoundedCornerShape(6.dp),
                        color = if (selectedTab == 0) CockpitCard else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Regional Presets",
                            color = if (selectedTab == 0) AtherMint else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Surface(
                        onClick = { selectedTab = 1 },
                        shape = RoundedCornerShape(6.dp),
                        color = if (selectedTab == 1) CockpitCard else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Manual Override",
                            color = if (selectedTab == 1) AtherMint else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                if (selectedTab == 0) {
                    Text(
                        text = "Select your current regional fuel price to calculate live trip fuel expenditure accurately:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        presets.forEach { preset ->
                            val isSelected = !isCustomPrice && selectedPreset.id == preset.id
                            Surface(
                                onClick = {
                                    viewModel.selectRegionalPricePreset(preset)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AtherMint.copy(alpha = 0.12f) else CockpitCardElevated,
                                border = BorderStroke(1.dp, if (isSelected) AtherMint else CockpitSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = preset.regionName,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = preset.fuelType,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = String.format(Locale.US, "%.2f %s/L", preset.pricePerLiter, preset.currency),
                                            color = AtherMint,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = AtherMint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Enter your exact local gas station price per liter and currency symbol:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = manualPriceText,
                        onValueChange = { manualPriceText = it },
                        label = { Text("Price per Liter") },
                        placeholder = { Text("e.g. 1.78") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = manualCurrencyText,
                        onValueChange = { manualCurrencyText = it },
                        label = { Text("Currency Symbol") },
                        placeholder = { Text("€, ₹, $, £") },
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = {
                        val p = manualPriceText.toDoubleOrNull() ?: currentPrice
                        viewModel.setCustomFuelPriceOverride(p, manualCurrencyText.ifBlank { "€" })
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AtherMint)
                ) {
                    Text("Apply Custom Price", color = CockpitBackground, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = AtherMint)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = CockpitCard
    )
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AtherMint,
    unfocusedBorderColor = CockpitSurfaceBorder,
    focusedLabelColor = AtherMint,
    unfocusedLabelColor = TextMuted,
    cursorColor = AtherMint,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = CockpitCardElevated,
    unfocusedContainerColor = CockpitCardElevated
)
