package com.example.model

data class NotificationSettings(
    // ====== General & Trip Messages (Screenshots 5 & 6) ======
    val generateStartTripMessage: Boolean = true,
    val generateEndTripMessage: Boolean = true,
    val generateConsumptionMessage: Boolean = true,
    val generateStyleDrivingMessage: Boolean = true,
    val generateLackOfAutonomyAlert: Boolean = true, // Low fuel range
    
    // Engine Operating & Low Temp Messages
    val generateEngineOperatingTempMessage: Boolean = true,
    val warningTextEngineOperatingTemp: String = "It has been reached the engine operating temperature",
    val generateLowEngineTempMessage: Boolean = false,
    val warningTextLowEngineTemp: String = "You are insane, you can't push a cold engine",
    
    // Limits & Sliders (Screenshot 5)
    val timeOutDisplayMessageSec: Int = 3,          // Range: 1 - 10 sec
    val overspeedingMessageOverKmh: Int = 130,      // Range: 50 - 220 km/h
    val overspeedingAlertText: String = "Slow down. You are always the usual speeder",
    val overspeedingAlertTimeOutSec: Int = 60,      // Range: 10 - 300 sec
    val drivingDurationMessageMin: Int = 30,        // Range: 15 - 180 min
    
    // Performance & Driving Dynamics Messages (Screenshots 1 & 2)
    val generateMaxPowerMessage: Boolean = false,
    val generateMaxTorqueMessage: Boolean = false,
    val generateOverspeedingAlert: Boolean = true,
    val generateTime1000mMessage: Boolean = false,
    val generateTime400mMessage: Boolean = false,
    val generateBraking100To0Message: Boolean = false,
    val generateBraking50To0Message: Boolean = false,
    val generateAccel0To50Message: Boolean = false,
    val generateAccel0To100Message: Boolean = true,
    val generatePickup40To70Message: Boolean = true,
    val generatePickup60To100Message: Boolean = true,
    val generatePickup80To120Message: Boolean = true,
    val generatePickup90To130Message: Boolean = false,

    // Vocal Messages / Text-To-Speech (Screenshots 3 & 4)
    val startTripVocalMessage: Boolean = false,
    val endTripVocalMessage: Boolean = false,
    val consumptionVocalMessage: Boolean = true,
    val styleDrivingVocalMessage: Boolean = true,
    val lackOfAutonomyVocalAlert: Boolean = true,
    val engineOperatingTempVocalMessage: Boolean = true,
    val lowEngineTempVocalAlert: Boolean = false,
    val overspeedingVocalAlert: Boolean = true,
    val acceleration0To100VocalMessage: Boolean = true,
    val pickup40To70VocalMessage: Boolean = false,
    val pickup60To100VocalMessage: Boolean = false,
    val pickup80To120VocalMessage: Boolean = true
)
