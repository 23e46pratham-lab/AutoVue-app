package com.example.model

data class NotificationSettings(
    // ====== General & Trip Messages ======
    val generateStartTripMessage: Boolean = true,
    val generateEndTripMessage: Boolean = true,
    val generateConsumptionMessage: Boolean = true,
    val generateStyleDrivingMessage: Boolean = true, // Visual alert when driving behaviour is aggressive
    val generateLackOfAutonomyAlert: Boolean = true, // Low fuel range
    val generateDrivingDurationAlert: Boolean = true,
    
    // Safety & Engine Critical Messages
    val generateHighRpmAlert: Boolean = true,
    val generateCoolantOverheatAlert: Boolean = true,
    val generateAnomalyAlert: Boolean = true,

    // Engine Operating & Low Temp Messages
    val generateEngineOperatingTempMessage: Boolean = true,
    val warningTextEngineOperatingTemp: String = "It has been reached the engine operating temperature",
    val generateLowEngineTempMessage: Boolean = false,
    val warningTextLowEngineTemp: String = "You are insane, you can't push a cold engine",
    
    // Limits & Sliders
    val timeOutDisplayMessageSec: Int = 3,          // Range: 1 - 10 sec
    val overspeedingMessageOverKmh: Int = 130,      // Range: 50 - 220 km/h
    val overspeedingAlertText: String = "Slow down. You are always the usual speeder",
    val overspeedingAlertTimeOutSec: Int = 60,      // Range: 10 - 300 sec
    val drivingDurationMessageMin: Int = 30,        // Range: 15 - 180 min
    val fuelRangeAutonomyThresholdKm: Int = 50,     // Range: 20 - 120 km
    val lowFuelWarningThresholdPercent: Int = 15,   // Range: 5 - 30 %
    val periodicRangeVoiceAnnouncement: Boolean = false, // Vocal update every 50 km
    
    // Performance & Driving Dynamics Messages
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
    val generateVehicleRangeAlert: Boolean = true,

    // Vocal Messages / Text-To-Speech
    // Note: styleDrivingVocalMessage plays voice warning ONLY when behaviour is aggressive (never for normal/eco/great driving)
    val startTripVocalMessage: Boolean = false,
    val endTripVocalMessage: Boolean = false,
    val consumptionVocalMessage: Boolean = false,
    val styleDrivingVocalMessage: Boolean = true, // Aggressive driving vocal alert ONLY
    val lackOfAutonomyVocalAlert: Boolean = true,
    val vehicleRangeVocalAlert: Boolean = true, // Vocal announcement of estimated driving range based on tank capacity and burn rate
    val drivingDurationVocalAlert: Boolean = false,
    val engineOperatingTempVocalMessage: Boolean = true,
    val lowEngineTempVocalAlert: Boolean = false,
    val overspeedingVocalAlert: Boolean = true,
    val highRpmVocalAlert: Boolean = true,
    val coolantOverheatVocalAlert: Boolean = true,
    val anomalyVocalAlert: Boolean = true,
    val acceleration0To100VocalMessage: Boolean = true,
    val accel0To50VocalMessage: Boolean = false,
    val pickup40To70VocalMessage: Boolean = false,
    val pickup60To100VocalMessage: Boolean = false,
    val pickup80To120VocalMessage: Boolean = true,
    val pickup90To130VocalMessage: Boolean = false,
    val brakingVocalAlert: Boolean = false,
    val maxPowerVocalMessage: Boolean = false
)
