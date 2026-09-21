package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfileItem
import com.example.model.VehicleProfile
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CockpitAmber
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.CockpitSteel
import com.example.ui.theme.CockpitSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SharedTelemetryViewModel

enum class OnboardingOption {
    ADD_NEW_USER,
    EXISTING_USER
}

@Composable
fun UserOnboardingScreen(
    viewModel: SharedTelemetryViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val userProfiles by viewModel.userProfiles.collectAsState()
    val selectedUserId by viewModel.selectedUserId.collectAsState()

    var selectedOption by remember { mutableStateOf(OnboardingOption.EXISTING_USER) }
    var newUserName by remember { mutableStateOf("") }
    var newUserVehicle by remember { mutableStateOf("Seat Leon 1.6 FSI") }
    var activeChosenUser by remember(selectedUserId, userProfiles) {
        mutableStateOf(userProfiles.find { it.id == selectedUserId } ?: userProfiles.firstOrNull())
    }

    // Two-page registration wizard state variables
    var registrationStep by remember { mutableIntStateOf(1) } // 1: Profile & Engine, 2: Dynamics & Tank

    // Page 1 Inputs (Matching Screenshot 1)
    var profileNameInput by remember { mutableStateOf("pratham") }
    var fuelSupplyInput by remember { mutableStateOf("gasoline") }
    var startStopEquippedInput by remember { mutableStateOf(false) }
    var displacementCcInput by remember { mutableStateOf("1700") }
    var maxPowerHpInput by remember { mutableStateOf("89.0") }
    var odometerKmInput by remember { mutableStateOf("59477.00") }
    var isActiveProfileInput by remember { mutableStateOf(true) }

    // Page 2 Inputs (Matching Screenshot 2)
    var totalWeightKgInput by remember { mutableStateOf("1250.0") }
    var consumptionL100kmInput by remember { mutableStateOf("6.5") }
    var correctiveConsumptionInput by remember { mutableStateOf("1.0") }
    var tankCapacityLitersInput by remember { mutableStateOf("45.0") }
    var fuelLeftPercentInput by remember { mutableStateOf("24.00") }
    var vinInput by remember { mutableStateOf("WVWZZZ3CZWE098124") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Branding & Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(CockpitSteel.copy(alpha = 0.25f), CockpitCardElevated)
                    )
                )
                .border(1.5.dp, CockpitSteel, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "AutoVue Logo",
                tint = CockpitSteel,
                modifier = Modifier.size(38.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AutoVue",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CockpitSteel.copy(alpha = 0.18f))
                        .border(1.dp, CockpitSteel.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OBD-II",
                        color = CockpitSteel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Vehicle Telemetry & Diagnostics",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Select a profile option to get started",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Two Main Options: Add New User or Existing User
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Option 1: Existing User
            val isExistingSelected = selectedOption == OnboardingOption.EXISTING_USER
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedOption = OnboardingOption.EXISTING_USER }
                    .testTag("existing_user_option_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExistingSelected) CockpitCardElevated else CockpitCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isExistingSelected) 2.dp else 1.dp,
                    color = if (isExistingSelected) CockpitSteel else CardBorder
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isExistingSelected) CockpitSteel.copy(alpha = 0.2f)
                                else CockpitBackground
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Existing User",
                            tint = if (isExistingSelected) CockpitSteel else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Existing User",
                        color = if (isExistingSelected) TextPrimary else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Continue with saved driver",
                        color = TextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }

            // Option 2: Add New User
            val isNewUserSelected = selectedOption == OnboardingOption.ADD_NEW_USER
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedOption = OnboardingOption.ADD_NEW_USER }
                    .testTag("add_user_option_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNewUserSelected) CockpitCardElevated else CockpitCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isNewUserSelected) 2.dp else 1.dp,
                    color = if (isNewUserSelected) CockpitSteel else CardBorder
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isNewUserSelected) CockpitSteel.copy(alpha = 0.2f)
                                else CockpitBackground
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add New User",
                            tint = if (isNewUserSelected) CockpitSteel else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Add New User",
                        color = if (isNewUserSelected) TextPrimary else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Create new driver profile",
                        color = TextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // Section Content based on selected option
        AnimatedContent(
            targetState = selectedOption,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "OnboardingContent"
        ) { option ->
            when (option) {
                OnboardingOption.EXISTING_USER -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Select an existing profile:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // List existing profiles
                        userProfiles.forEach { profile ->
                            val isChosen = activeChosenUser?.id == profile.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        activeChosenUser = profile
                                    }
                                    .testTag("existing_user_item_${profile.name.lowercase()}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isChosen) CockpitCardElevated else CockpitCard
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChosen) CockpitSteel else CardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isChosen) CockpitSteel.copy(alpha = 0.25f)
                                                    else CockpitSurfaceBorder
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = if (isChosen) CockpitSteel else TextMuted,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = profile.name,
                                                    color = TextPrimary,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (profile.isDefault) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(CockpitGreen.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "DEFAULT",
                                                            color = CockpitGreen,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = profile.vehicleModel,
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    // Checkmark
                                    if (isChosen) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = CockpitSteel,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Continue Button
                        Button(
                            onClick = {
                                activeChosenUser?.let {
                                    viewModel.selectUser(it)
                                }
                                onNavigateToDashboard()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("existing_user_continue_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CockpitSteel),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Continue as ${activeChosenUser?.name ?: "Driver"}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                OnboardingOption.ADD_NEW_USER -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Step Header & Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (registrationStep == 1) "Profile & Engine Setup" else "Dynamics & Fuel Setup",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Page $registrationStep of 2",
                                        color = CockpitSteel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(26.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(CockpitSteel)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(26.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (registrationStep == 2) CockpitSteel else CockpitSurfaceBorder)
                                    )
                                }
                            }

                            if (registrationStep == 1) {
                                // ====== PAGE 1: Profile & Engine Setup (Screenshot 1) ======

                                // 1. Profile name*
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = profileNameInput,
                                        onValueChange = { if (it.length <= 8) profileNameInput = it },
                                        label = { Text("Profile name*", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("pratham", color = TextMuted, fontSize = 13.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("profile_name_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "enter profile name (up to 8 characters).",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 2. Fuel supply*
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Fuel supply*", color = TextMuted, fontSize = 12.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("gasoline", "diesel", "hybrid", "electric", "lpg").forEach { type ->
                                            val isFuelChosen = fuelSupplyInput.equals(type, ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isFuelChosen) CockpitSteel.copy(alpha = 0.25f) else CockpitCardElevated)
                                                    .border(
                                                        1.dp,
                                                        if (isFuelChosen) CockpitSteel else CockpitSurfaceBorder,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { fuelSupplyInput = type }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = type,
                                                    color = if (isFuelChosen) TextPrimary else TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isFuelChosen) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "select the engine type.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 3. Start and stop*
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Start and stop*", color = TextMuted, fontSize = 12.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        listOf(false to "No", true to "Yes").forEach { (equipped, label) ->
                                            val isSelected = startStopEquippedInput == equipped
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) CockpitSteel.copy(alpha = 0.25f) else CockpitCardElevated)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) CockpitSteel else CockpitSurfaceBorder,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { startStopEquippedInput = equipped }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) TextPrimary else TextSecondary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Does your engine equip with start-stop system.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 4. Displacement (cc)*
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = displacementCcInput,
                                        onValueChange = { displacementCcInput = it },
                                        label = { Text("Displacement (cc)*", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("1700", color = TextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("displacement_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "enter the engine displacement in cubic centimeters (liters x 1000). For example, a 2 liters engine you have to enter 2000.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 5. Max power (HP)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = maxPowerHpInput,
                                        onValueChange = { maxPowerHpInput = it },
                                        label = { Text("Max power (HP)", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("89.0", color = TextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("max_power_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "you can insert the engine peak power.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 6. Odometer (km)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = odometerKmInput,
                                        onValueChange = { odometerKmInput = it },
                                        label = { Text("Odometer (km)", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("59477.00", color = TextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("odometer_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "enter the distance shown by the odometer (odometer) located on the dashboard of the car.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 7. Active Checkbox
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { isActiveProfileInput = !isActiveProfileInput }
                                    ) {
                                        Checkbox(
                                            checked = isActiveProfileInput,
                                            onCheckedChange = { isActiveProfileInput = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = CockpitSteel,
                                                checkmarkColor = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Active",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "all movement data will be associated with the active profile.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Continue to Page 2 Button
                                val canProceed = profileNameInput.trim().isNotEmpty()
                                Button(
                                    onClick = { registrationStep = 2 },
                                    enabled = canProceed,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("onboarding_next_page_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CockpitSteel,
                                        disabledContainerColor = CockpitSteel.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Next: Dynamics & Fuel (Page 2)",
                                        color = if (canProceed) TextPrimary else TextMuted,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = if (canProceed) TextPrimary else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                            } else {
                                // ====== PAGE 2: Dynamics, Consumption & Tank Specs (Screenshot 2) ======

                                // 1. Total weight (KG)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = totalWeightKgInput,
                                        onValueChange = { totalWeightKgInput = it },
                                        label = { Text("Total weight (KG)", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("1250.0", color = TextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("weight_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "If you get wrong power and torque data, you can insert the total weight for better calculations: empty weight + weight of the fuel and driver of all the masses on board. Weight in kg = lbs * 2.20462",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 2. Consump. (L/100km)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = consumptionL100kmInput,
                                        onValueChange = { consumptionL100kmInput = it },
                                        label = { Text("Consump. (L/100km)", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("6.5", color = TextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("consumption_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "you can enter the average fuel consumption (under standard conditions).",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 3. Corrective consumption with Lightning Bolt icon
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = correctiveConsumptionInput,
                                        onValueChange = { correctiveConsumptionInput = it },
                                        label = { Text("Corrective consumption", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("1.0", color = TextMuted, fontSize = 13.sp) },
                                        trailingIcon = {
                                            IconButton(onClick = { correctiveConsumptionInput = "1.0" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Bolt,
                                                    contentDescription = "Estimate divider",
                                                    tint = CockpitAmber,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("corrective_consumption_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "Divider corrective to fix consumption. For example, if Smart Control calculates 60 mpg of consumption but your car has 30 mpg, you must set 2.0 value (60/30). Push the button in the shape of lightning to estimate the divider according to the refuelings or the expected consumption set to the car profile.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 4. Tank capacity (l)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = tankCapacityLitersInput,
                                        onValueChange = { tankCapacityLitersInput = it },
                                        label = { Text("Tank capacity (l)", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("45.0", color = TextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("tank_capacity_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "you can insert the tank capacity.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 5. Fuel left (%)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = fuelLeftPercentInput,
                                        onValueChange = { fuelLeftPercentInput = it },
                                        label = { Text("Fuel left (%)", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("24.00", color = TextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("fuel_left_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "you can insert the current fuel percentage in your tank (100% full).",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 6. VIN
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = vinInput,
                                        onValueChange = { vinInput = it },
                                        label = { Text("VIN", color = TextMuted, fontSize = 12.sp) },
                                        placeholder = { Text("WVWZZZ3CZWE098124", color = TextMuted, fontSize = 13.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("vin_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CockpitSurfaceBorder
                                        )
                                    )
                                    Text(
                                        text = "you can insert the Vehicle Identification Number (VIN).",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Actions: Back to Page 1 & Save Profile (Screenshot 2 Save Floppy icon)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { registrationStep = 1 },
                                        modifier = Modifier
                                            .weight(0.38f)
                                            .height(50.dp),
                                        border = BorderStroke(1.dp, CockpitSurfaceBorder),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Page 1", color = TextSecondary, fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val newProfile = VehicleProfile(
                                                profileName = profileNameInput.trim().ifEmpty { "Driver" },
                                                fuelSupply = fuelSupplyInput,
                                                startAndStopEquipped = startStopEquippedInput,
                                                displacementCc = displacementCcInput.toIntOrNull() ?: 1700,
                                                maxPowerHp = maxPowerHpInput.toDoubleOrNull() ?: 89.0,
                                                odometerKm = odometerKmInput.toDoubleOrNull() ?: 59477.0,
                                                totalWeightKg = totalWeightKgInput.toDoubleOrNull() ?: 1250.0,
                                                consumptionL100km = consumptionL100kmInput.toDoubleOrNull() ?: 6.5,
                                                correctiveConsumption = correctiveConsumptionInput.toDoubleOrNull() ?: 1.0,
                                                tankCapacityLiters = tankCapacityLitersInput.toDoubleOrNull() ?: 45.0,
                                                fuelLeftPercent = fuelLeftPercentInput.toDoubleOrNull() ?: 24.0,
                                                vin = vinInput.trim().ifEmpty { "WVWZZZ3CZWE098124" },
                                                isActive = isActiveProfileInput
                                            )
                                            viewModel.registerNewUserWithProfile(newProfile)
                                            onNavigateToDashboard()
                                        },
                                        modifier = Modifier
                                            .weight(0.62f)
                                            .height(50.dp)
                                            .testTag("save_user_profile_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = CockpitSteel),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = "Save",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Save Profile",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subtle Guest / Quick access fallback
        Text(
            text = "Skip to Dashboard as Guest",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable { onNavigateToDashboard() }
                .padding(8.dp)
        )
    }
}
