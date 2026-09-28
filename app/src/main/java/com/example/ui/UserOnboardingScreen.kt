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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
    EXISTING_USER,
    ADD_NEW_USER
}

/**
 * Initial Onboarding / Driver Selection Screen.
 * Uses clean, warm luxury automotive styling consistent across all application tabs.
 */
@Composable
fun UserOnboardingScreen(
    viewModel: SharedTelemetryViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val vehicleProfile by viewModel.vehicleProfile.collectAsState()

    var selectedOption by remember { mutableStateOf(OnboardingOption.EXISTING_USER) }
    var registrationStep by remember { mutableIntStateOf(1) } // 1: Profile & Engine, 2: Dynamics & Tank

    val userProfiles = remember {
        listOf(
            UserProfileItem("1", "Pratham", "Volkswagen Passat 2.0 TDI", isDefault = true),
            UserProfileItem("2", "Test Driver", "BMW 320d F30")
        )
    }

    var activeChosenUser by remember { mutableStateOf<UserProfileItem?>(userProfiles.firstOrNull()) }

    // Page 1 Inputs
    var profileNameInput by remember { mutableStateOf("pratham") }
    var fuelSupplyInput by remember { mutableStateOf("gasoline") }
    var startStopEquippedInput by remember { mutableStateOf(false) }
    var displacementCcInput by remember { mutableStateOf("1700") }
    var maxPowerHpInput by remember { mutableStateOf("89.0") }
    var odometerKmInput by remember { mutableStateOf("59477.00") }
    var isActiveProfileInput by remember { mutableStateOf(true) }

    // Page 2 Inputs
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
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Hero Header Card (Consistent Cockpit Card styling)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CockpitCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Emblem Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CockpitCardElevated)
                        .border(1.dp, CardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "AutoVue Telemetry Logo",
                        tint = CockpitSteel,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Brand & Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AutoVue",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CockpitSteel)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "OBD-II COCKPIT",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Text(
                        text = "Intelligent Vehicle Telemetry & Diagnostics Platform",
                        color = CockpitSteel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Select driver profile to initialize vehicle cockpit",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Two Main Selector Cards: Existing Driver vs New Driver
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Option 1: Existing Driver Card
            val isExistingSelected = selectedOption == OnboardingOption.EXISTING_USER
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedOption = OnboardingOption.EXISTING_USER }
                    .testTag("existing_user_option_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExistingSelected) CockpitCardElevated else CockpitCard
                ),
                border = BorderStroke(
                    width = if (isExistingSelected) 1.5.dp else 1.dp,
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isExistingSelected) CockpitCard else CockpitCardElevated)
                            .border(
                                1.dp,
                                if (isExistingSelected) CockpitSteel else CardBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Existing User",
                            tint = if (isExistingSelected) CockpitSteel else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "Existing Driver",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (isExistingSelected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Continue with saved vehicle",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }

            // Option 2: Add New Driver Card
            val isNewUserSelected = selectedOption == OnboardingOption.ADD_NEW_USER
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedOption = OnboardingOption.ADD_NEW_USER }
                    .testTag("add_user_option_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNewUserSelected) CockpitCardElevated else CockpitCard
                ),
                border = BorderStroke(
                    width = if (isNewUserSelected) 1.5.dp else 1.dp,
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isNewUserSelected) CockpitCard else CockpitCardElevated)
                            .border(
                                1.dp,
                                if (isNewUserSelected) CockpitSteel else CardBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add New Driver",
                            tint = if (isNewUserSelected) CockpitSteel else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "New Driver",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (isNewUserSelected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Create telemetry profile",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 16.dp)
                                    .background(CockpitSteel, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = "SELECT DRIVER PROFILE",
                                color = CockpitSteel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // List existing profiles
                        userProfiles.forEach { profile ->
                            val isChosen = activeChosenUser?.id == profile.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeChosenUser = profile }
                                    .testTag("existing_user_item_${profile.name.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isChosen) CockpitCardElevated else CockpitCard
                                ),
                                border = BorderStroke(
                                    width = if (isChosen) 1.5.dp else 1.dp,
                                    color = if (isChosen) CockpitSteel else CardBorder
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
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(CockpitCard)
                                                .border(1.dp, CardBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = if (isChosen) CockpitSteel else TextMuted,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                                            .background(CockpitCardElevated)
                                                            .border(1.dp, CardBorder, RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "DEFAULT",
                                                            color = CockpitSteel,
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

                                    // Selection Checkmark
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

                        // Launch Cockpit Button in clean CockpitSteel
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
                                .shadow(4.dp, RoundedCornerShape(12.dp))
                                .testTag("existing_user_continue_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CockpitSteel,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Launch Cockpit as ${activeChosenUser?.name ?: "Driver"}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                OnboardingOption.ADD_NEW_USER -> {
                    // Registration Card with standard Cockpit Card styling
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Step Header & Progress Indicator
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
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(28.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(CockpitSteel)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(28.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (registrationStep == 2) CockpitSteel else CardBorder)
                                    )
                                }
                            }

                            if (registrationStep == 1) {
                                // ====== PAGE 1: Profile & Engine Setup ======

                                // 1. Profile name*
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = profileNameInput,
                                        onValueChange = { if (it.length <= 8) profileNameInput = it },
                                        label = { Text("Profile name*", color = TextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("pratham", color = TextMuted, fontSize = 13.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("profile_name_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
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
                                    Text("Fuel supply*", color = TextSecondary, fontSize = 12.sp)
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
                                                    .background(if (isFuelChosen) CockpitSteel else CockpitCardElevated)
                                                    .border(
                                                        1.dp,
                                                        if (isFuelChosen) CockpitSteel else CardBorder,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { fuelSupplyInput = type }
                                                    .padding(vertical = 9.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = type,
                                                    color = if (isFuelChosen) Color.White else TextSecondary,
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
                                    Text("Start and stop*", color = TextSecondary, fontSize = 12.sp)
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
                                                    .background(if (isSelected) CockpitSteel else CockpitCardElevated)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) CockpitSteel else CardBorder,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { startStopEquippedInput = equipped }
                                                    .padding(vertical = 9.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) Color.White else TextSecondary,
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
                                        label = { Text("Displacement (cc)*", color = TextSecondary, fontSize = 12.sp) },
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
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
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
                                        label = { Text("Max power (HP)", color = TextSecondary, fontSize = 12.sp) },
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
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
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
                                        label = { Text("Odometer (km)", color = TextSecondary, fontSize = 12.sp) },
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
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
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
                                                checkmarkColor = Color.White,
                                                uncheckedColor = CardBorder
                                            )
                                        )
                                        Text(
                                            text = "Active Profile",
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
                                        .shadow(4.dp, RoundedCornerShape(12.dp))
                                        .testTag("onboarding_next_page_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CockpitSteel,
                                        disabledContainerColor = CockpitSteel.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Next: Dynamics & Fuel (Page 2)",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                            } else {
                                // ====== PAGE 2: Dynamics, Consumption & Tank Specs ======

                                // 1. Total weight (KG)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = totalWeightKgInput,
                                        onValueChange = { totalWeightKgInput = it },
                                        label = { Text("Total weight (KG)", color = TextSecondary, fontSize = 12.sp) },
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
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
                                        )
                                    )
                                    Text(
                                        text = "If you get wrong power and torque data, you can insert the total weight for better calculations: empty weight + weight of the fuel and driver of all the masses on board. Weight in kg = lbs * 2.20462",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 2. Reference consumption (liters/100km)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = consumptionL100kmInput,
                                        onValueChange = { consumptionL100kmInput = it },
                                        label = { Text("Reference consumption (liters/100km)", color = TextSecondary, fontSize = 12.sp) },
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
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
                                        )
                                    )
                                    Text(
                                        text = "insert the estimated average consumption in liters per 100km. If you prefer, you can use the values specified by the manufacturer.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 3. Corrective consumption factor
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = correctiveConsumptionInput,
                                        onValueChange = { correctiveConsumptionInput = it },
                                        label = { Text("Corrective consumption factor", color = TextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("1.0", color = TextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("corrective_factor_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
                                        )
                                    )
                                    Text(
                                        text = "if the instant consumption value doesn't match the trip computer, you can insert a correction factor. For example, if instant consumption value is 10% lower than trip computer, the factor is 1.10. Default is 1.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 4. Tank capacity (liters)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = tankCapacityLitersInput,
                                        onValueChange = { tankCapacityLitersInput = it },
                                        label = { Text("Tank capacity (liters)", color = TextSecondary, fontSize = 12.sp) },
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
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
                                        )
                                    )
                                    Text(
                                        text = "insert the tank capacity in liters. 1 gallon = 3.785 liters, 1 UK gallon = 4.546 liters.",
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
                                        label = { Text("Fuel left (%)", color = TextSecondary, fontSize = 12.sp) },
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
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
                                        )
                                    )
                                    Text(
                                        text = "it's the fuel level in the tank in percentage. 100% full, 50% half.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 6. VIN / Chassis Number
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = vinInput,
                                        onValueChange = { vinInput = it },
                                        label = { Text("VIN (Vehicle Identification Number)", color = TextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("WVWZZZ3CZWE098124", color = TextMuted, fontSize = 13.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("vin_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CockpitSteel,
                                            unfocusedBorderColor = CardBorder,
                                            focusedContainerColor = CockpitCard,
                                            unfocusedContainerColor = CockpitCard,
                                            cursorColor = CockpitSteel
                                        )
                                    )
                                    Text(
                                        text = "optional vehicle chassis identification code.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Wizard Navigation Buttons (Back & Complete Registration)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { registrationStep = 1 },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, CardBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = TextPrimary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Back", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = {
                                            val newProfile = VehicleProfile(
                                                profileName = profileNameInput.ifBlank { "pratham" },
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
                                                vin = vinInput.ifBlank { "WVWZZZ3CZWE098124" },
                                                isActive = isActiveProfileInput
                                            )
                                            viewModel.registerNewUserWithProfile(newProfile)
                                            onNavigateToDashboard()
                                        },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(50.dp)
                                            .shadow(4.dp, RoundedCornerShape(12.dp))
                                            .testTag("save_and_launch_cockpit_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CockpitSteel,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Save & Launch",
                                            color = Color.White,
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
    }
}
