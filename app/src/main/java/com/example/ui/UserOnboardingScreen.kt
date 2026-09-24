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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.CarbonFiberPanel
import com.example.components.UrusRacingStripe
import com.example.components.carbonFiberPattern
import com.example.model.UserProfileItem
import com.example.model.VehicleProfile
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDarkBackground
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CarbonTextMuted
import com.example.ui.theme.CarbonTextPrimary
import com.example.ui.theme.CarbonTextSecondary
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitGreen
import com.example.ui.theme.LamboCarbonBlack
import com.example.ui.theme.LamboYellow
import com.example.ui.theme.LamboYellowBright
import com.example.ui.theme.LamboYellowDark
import com.example.ui.theme.LamboYellowGlow
import com.example.ui.theme.LamboYellowSubtle
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
    var activeChosenUser by remember(selectedUserId, userProfiles) {
        mutableStateOf(userProfiles.find { it.id == selectedUserId } ?: userProfiles.firstOrNull())
    }

    // Two-page registration wizard state variables
    var registrationStep by remember { mutableIntStateOf(1) } // 1: Profile & Engine, 2: Dynamics & Tank

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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Carbon Fibre Hero Header with Lamborghini Urus Yellow Racing Accents
        CarbonFiberPanel(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, LamboYellow),
            hasYellowAccent = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Lamborghini Urus Giallo Auge Top Accent Stripe
                UrusRacingStripe(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(2.dp)),
                    thickness = 3.dp
                )

                // Carbon + Urus Yellow Tachometer/Speed Emblem
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    LamboYellow.copy(alpha = 0.28f),
                                    CarbonDarkBackground
                                )
                            )
                        )
                        .border(2.dp, LamboYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "AutoVue Telemetry Logo",
                        tint = LamboYellowBright,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Brand & Telemetry Badge
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
                            color = CarbonTextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LamboYellow)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "GIALLO OBD-II",
                                color = LamboCarbonBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Text(
                        text = "Super-SUV Telemetry & High Performance Diagnostics",
                        color = LamboYellowBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Select driver profile to initialize vehicle cockpit",
                        color = CarbonTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Two Main Selector Cards with Carbon Fibre Weave & Urus Yellow Active Highlighting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Option 1: Existing User Card (Carbon Fibre)
            val isExistingSelected = selectedOption == OnboardingOption.EXISTING_USER
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .carbonFiberPattern()
                    .border(
                        BorderStroke(
                            width = if (isExistingSelected) 2.dp else 1.dp,
                            color = if (isExistingSelected) LamboYellow else CarbonBorder
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { selectedOption = OnboardingOption.EXISTING_USER }
                    .testTag("existing_user_option_card")
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isExistingSelected) LamboYellow.copy(alpha = 0.22f)
                                else CarbonDarkBackground
                            )
                            .border(
                                1.dp,
                                if (isExistingSelected) LamboYellow else CarbonBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Existing User",
                            tint = if (isExistingSelected) LamboYellowBright else CarbonTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Existing Driver",
                        color = if (isExistingSelected) Color.White else CarbonTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Continue with saved vehicle",
                        color = if (isExistingSelected) LamboYellowBright.copy(alpha = 0.9f) else CarbonTextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp
                    )
                }
            }

            // Option 2: Add New User Card (Carbon Fibre)
            val isNewUserSelected = selectedOption == OnboardingOption.ADD_NEW_USER
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .carbonFiberPattern()
                    .border(
                        BorderStroke(
                            width = if (isNewUserSelected) 2.dp else 1.dp,
                            color = if (isNewUserSelected) LamboYellow else CarbonBorder
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { selectedOption = OnboardingOption.ADD_NEW_USER }
                    .testTag("add_user_option_card")
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isNewUserSelected) LamboYellow.copy(alpha = 0.22f)
                                else CarbonDarkBackground
                            )
                            .border(
                                1.dp,
                                if (isNewUserSelected) LamboYellow else CarbonBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add New Driver",
                            tint = if (isNewUserSelected) LamboYellowBright else CarbonTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "New Driver",
                        color = if (isNewUserSelected) Color.White else CarbonTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Create telemetry profile",
                        color = if (isNewUserSelected) LamboYellowBright.copy(alpha = 0.9f) else CarbonTextMuted,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp, 16.dp)
                                    .background(LamboYellow, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = "SELECT DRIVER PROFILE",
                                color = LamboYellowDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        // List existing profiles in Carbon Fibre Cards
                        userProfiles.forEach { profile ->
                            val isChosen = activeChosenUser?.id == profile.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .carbonFiberPattern()
                                    .border(
                                        BorderStroke(
                                            if (isChosen) 1.5.dp else 1.dp,
                                            if (isChosen) LamboYellow else CarbonBorder
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        activeChosenUser = profile
                                    }
                                    .testTag("existing_user_item_${profile.name.lowercase()}")
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                .background(
                                                    if (isChosen) LamboYellow.copy(alpha = 0.22f)
                                                    else CarbonDarkBackground
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isChosen) LamboYellow else CarbonBorder,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = if (isChosen) LamboYellowBright else CarbonTextMuted,
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
                                                    color = CarbonTextPrimary,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (profile.isDefault) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(LamboYellow.copy(alpha = 0.2f))
                                                            .border(1.dp, LamboYellow.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "DEFAULT",
                                                            color = LamboYellowBright,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = profile.vehicleModel,
                                                color = CarbonTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    // Urus Yellow Selection Checkmark
                                    if (isChosen) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = LamboYellowBright,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Continue as Selected Driver Button in Lamborghini Urus Yellow with bold black text
                        Button(
                            onClick = {
                                activeChosenUser?.let {
                                    viewModel.selectUser(it)
                                }
                                onNavigateToDashboard()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(6.dp, RoundedCornerShape(12.dp))
                                .testTag("existing_user_continue_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LamboYellow,
                                contentColor = LamboCarbonBlack
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Launch Cockpit as ${activeChosenUser?.name ?: "Driver"}",
                                color = LamboCarbonBlack,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = LamboCarbonBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                OnboardingOption.ADD_NEW_USER -> {
                    // Registration Card with Carbon Fibre Weave and Urus Yellow Styling
                    CarbonFiberPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CarbonBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Step Header & Urus Yellow Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (registrationStep == 1) "Profile & Engine Setup" else "Dynamics & Fuel Setup",
                                        color = CarbonTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Page $registrationStep of 2",
                                        color = LamboYellowBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(28.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(LamboYellow)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .height(5.dp)
                                            .width(28.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (registrationStep == 2) LamboYellow else CarbonBorder)
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
                                        label = { Text("Profile name*", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("pratham", color = CarbonTextMuted, fontSize = 13.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("profile_name_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "enter profile name (up to 8 characters).",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 2. Fuel supply*
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Fuel supply*", color = CarbonTextSecondary, fontSize = 12.sp)
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
                                                    .background(if (isFuelChosen) LamboYellow else CarbonDarkBackground)
                                                    .border(
                                                        1.dp,
                                                        if (isFuelChosen) LamboYellowBright else CarbonBorder,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { fuelSupplyInput = type }
                                                    .padding(vertical = 9.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = type,
                                                    color = if (isFuelChosen) LamboCarbonBlack else CarbonTextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isFuelChosen) FontWeight.Black else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "select the engine type.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 3. Start and stop*
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Start and stop*", color = CarbonTextSecondary, fontSize = 12.sp)
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
                                                    .background(if (isSelected) LamboYellow else CarbonDarkBackground)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) LamboYellowBright else CarbonBorder,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { startStopEquippedInput = equipped }
                                                    .padding(vertical = 9.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) LamboCarbonBlack else CarbonTextSecondary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Does your engine equip with start-stop system.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 4. Displacement (cc)*
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = displacementCcInput,
                                        onValueChange = { displacementCcInput = it },
                                        label = { Text("Displacement (cc)*", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("1700", color = CarbonTextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("displacement_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "enter the engine displacement in cubic centimeters (liters x 1000). For example, a 2 liters engine you have to enter 2000.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 5. Max power (HP)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = maxPowerHpInput,
                                        onValueChange = { maxPowerHpInput = it },
                                        label = { Text("Max power (HP)", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("89.0", color = CarbonTextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("max_power_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "you can insert the engine peak power.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 6. Odometer (km)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = odometerKmInput,
                                        onValueChange = { odometerKmInput = it },
                                        label = { Text("Odometer (km)", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("59477.00", color = CarbonTextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("odometer_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "enter the distance shown by the odometer (odometer) located on the dashboard of the car.",
                                        color = CarbonTextMuted,
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
                                                checkedColor = LamboYellow,
                                                checkmarkColor = LamboCarbonBlack,
                                                uncheckedColor = CarbonBorder
                                            )
                                        )
                                        Text(
                                            text = "Active Profile",
                                            color = CarbonTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "all movement data will be associated with the active profile.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Continue to Page 2 Button in Urus Yellow
                                val canProceed = profileNameInput.trim().isNotEmpty()
                                Button(
                                    onClick = { registrationStep = 2 },
                                    enabled = canProceed,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .shadow(6.dp, RoundedCornerShape(12.dp))
                                        .testTag("onboarding_next_page_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LamboYellow,
                                        disabledContainerColor = LamboYellow.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Next: Dynamics & Fuel (Page 2)",
                                        color = if (canProceed) LamboCarbonBlack else CarbonTextMuted,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = if (canProceed) LamboCarbonBlack else CarbonTextMuted,
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
                                        label = { Text("Total weight (KG)", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("1250.0", color = CarbonTextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("weight_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "If you get wrong power and torque data, you can insert the total weight for better calculations: empty weight + weight of the fuel and driver of all the masses on board. Weight in kg = lbs * 2.20462",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 2. Reference consumption (liters/100km)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = consumptionL100kmInput,
                                        onValueChange = { consumptionL100kmInput = it },
                                        label = { Text("Reference consumption (liters/100km)", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("6.5", color = CarbonTextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("consumption_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "insert the estimated average consumption in liters per 100km. If you prefer, you can use the values specified by the manufacturer.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 3. Corrective consumption factor
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = correctiveConsumptionInput,
                                        onValueChange = { correctiveConsumptionInput = it },
                                        label = { Text("Corrective consumption factor", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("1.0", color = CarbonTextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("corrective_factor_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "if the instant consumption value doesn't match the trip computer, you can insert a correction factor. For example, if instant consumption value is 10% lower than trip computer, the factor is 1.10. Default is 1.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 4. Tank capacity (liters)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = tankCapacityLitersInput,
                                        onValueChange = { tankCapacityLitersInput = it },
                                        label = { Text("Tank capacity (liters)", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("45.0", color = CarbonTextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("tank_capacity_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "insert the tank capacity in liters. 1 gallon = 3.785 liters, 1 UK gallon = 4.546 liters.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 5. Fuel left (%)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = fuelLeftPercentInput,
                                        onValueChange = { fuelLeftPercentInput = it },
                                        label = { Text("Fuel left (%)", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("24.00", color = CarbonTextMuted, fontSize = 13.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("fuel_left_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "it's the fuel level in the tank in percentage. 100% full, 50% half.",
                                        color = CarbonTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }

                                // 6. VIN / Chassis Number
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedTextField(
                                        value = vinInput,
                                        onValueChange = { vinInput = it },
                                        label = { Text("VIN (Vehicle Identification Number)", color = CarbonTextSecondary, fontSize = 12.sp) },
                                        placeholder = { Text("WVWZZZ3CZWE098124", color = CarbonTextMuted, fontSize = 13.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("vin_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = CarbonTextPrimary,
                                            unfocusedTextColor = CarbonTextPrimary,
                                            focusedBorderColor = LamboYellow,
                                            unfocusedBorderColor = CarbonBorder,
                                            focusedContainerColor = CarbonDarkBackground,
                                            unfocusedContainerColor = CarbonDarkBackground
                                        )
                                    )
                                    Text(
                                        text = "optional vehicle chassis identification code.",
                                        color = CarbonTextMuted,
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
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, CarbonBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = CarbonTextPrimary
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
                                            .height(52.dp)
                                            .shadow(6.dp, RoundedCornerShape(12.dp))
                                            .testTag("save_and_launch_cockpit_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LamboYellow,
                                            contentColor = LamboCarbonBlack
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = null,
                                            tint = LamboCarbonBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Save & Launch",
                                            color = LamboCarbonBlack,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
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
