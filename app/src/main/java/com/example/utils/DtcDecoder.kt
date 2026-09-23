package com.example.utils

import com.example.model.DtcCode

object DtcDecoder {
    private val knownCodes = mapOf(
        "P0300" to DtcCode(
            code = "P0300",
            system = "Powertrain / Ignition",
            description = "Random / Multiple Cylinder Misfire Detected",
            mode = "Mode 03",
            severity = "CRITICAL",
            plainMeaning = "Engine cylinders are not firing fuel evenly, causing rough idling and power loss.",
            plainAction = "Avoid high acceleration. Inspect spark plugs, ignition coils, and fuel injectors."
        ),
        "P0171" to DtcCode(
            code = "P0171",
            system = "Powertrain / Fuel Trim",
            description = "Fuel System Too Lean (Bank 1)",
            mode = "Mode 03",
            severity = "Attention",
            plainMeaning = "Engine receives slightly more air than optimal fuel ratio target.",
            plainAction = "Inspect mass air flow (MAF) sensor and vacuum hoses for leaks."
        ),
        "P0172" to DtcCode(
            code = "P0172",
            system = "Powertrain / Fuel Trim",
            description = "Fuel System Too Rich (Bank 1)",
            mode = "Mode 03",
            severity = "Attention",
            plainMeaning = "Engine is burning excessive fuel compared to the intake air volume.",
            plainAction = "Check oxygen sensor readings, air filter cleanliness, and fuel pressure regulator."
        ),
        "P0420" to DtcCode(
            code = "P0420",
            system = "Emissions / Catalytic Converter",
            description = "Catalyst System Efficiency Below Threshold (Bank 1)",
            mode = "Mode 07",
            severity = "Low Risk",
            plainMeaning = "Catalytic converter is filtering exhaust gases below factory efficiency limits.",
            plainAction = "Safe to drive short-term. Schedule catalytic converter and O2 sensor inspection."
        ),
        "P0113" to DtcCode(
            code = "P0113",
            system = "Powertrain / Air Intake",
            description = "Intake Air Temperature Sensor 1 Circuit High",
            mode = "Mode 03",
            severity = "Attention",
            plainMeaning = "Intake air temperature sensor is reporting unusually high electrical resistance.",
            plainAction = "Verify IAT sensor wiring harness and plug connection."
        ),
        "P0128" to DtcCode(
            code = "P0128",
            system = "Cooling System / Engine",
            description = "Coolant Thermostat Below Regulating Temperature",
            mode = "Mode 03",
            severity = "Attention",
            plainMeaning = "Engine takes longer than normal to reach ideal operating temperature (85-95°C).",
            plainAction = "Check coolant level and thermostat valve operation."
        ),
        "P0500" to DtcCode(
            code = "P0500",
            system = "Vehicle Speed / Transmission",
            description = "Vehicle Speed Sensor 'A' Malfunction",
            mode = "Mode 03",
            severity = "CRITICAL",
            plainMeaning = "ECU lost accurate pulse data from the vehicle transmission output speed sensor.",
            plainAction = "Drive with caution. Speedometer and cruise control may report incorrect speeds."
        ),
        "U0100" to DtcCode(
            code = "U0100",
            system = "Network / CAN Bus",
            description = "Lost Communication With ECM / PCM 'A'",
            mode = "Mode 03",
            severity = "CRITICAL",
            plainMeaning = "Data communication between engine control module and vehicle gateway was interrupted.",
            plainAction = "Check 12V battery terminal voltage and main CAN bus harness integrity."
        )
    )

    fun decode(rawCode: String): DtcCode {
        val clean = rawCode.trim().uppercase()
        knownCodes[clean]?.let { return it }

        // Generic fallback with intelligent subsystem deduction based on OBD-II standard prefix
        val system = when {
            clean.startsWith("P01") -> "Fuel & Air Metering"
            clean.startsWith("P02") -> "Fuel Injector Circuit"
            clean.startsWith("P03") -> "Ignition System / Misfire"
            clean.startsWith("P04") -> "Auxiliary Emissions Controls"
            clean.startsWith("P05") -> "Vehicle Speed & Idle Control"
            clean.startsWith("P06") -> "Computer Output Circuits"
            clean.startsWith("P07") || clean.startsWith("P08") -> "Transmission System"
            clean.startsWith("B") -> "Body Electronics"
            clean.startsWith("C") -> "Chassis / Braking (ABS/ESP)"
            clean.startsWith("U") -> "Network / Bus Communication"
            else -> "Powertrain / General ECU"
        }

        val severity = if (clean.startsWith("U") || clean.contains("30") || clean.contains("50")) {
            "CRITICAL"
        } else {
            "Attention"
        }

        return DtcCode(
            code = clean,
            system = system,
            description = "Diagnostic Trouble Code $clean logged by ECU",
            mode = "Mode 03",
            severity = severity,
            plainMeaning = "Vehicle controller detected sensor variance or subsystem fault matching standard code $clean.",
            plainAction = "Run detailed diagnostic verification or scan associated sensor channels."
        )
    }
}
