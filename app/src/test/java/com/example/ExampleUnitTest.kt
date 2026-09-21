package com.example

import com.example.model.TelemetryTick
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun parseWebSocketTickWithMlPayload() {
        val json = """
        {
            "row_index": 12,
            "total_rows": 500,
            "elapsed_seconds": 1.2,
            "playback_percent": 2.4,
            "data": {
                "rpm": 2450.0,
                "vss": 65.0,
                "coolant_temp": 88.0,
                "throttle_pos": 22.5,
                "maf": 14.8,
                "map_kpa": 101.3,
                "intake_air_temp": 28.0,
                "ambient_temp": 25.0,
                "pedal_d": 21.0,
                "pedal_e": 21.2
            },
            "ml": {
                "driver_behaviour": {
                    "label": "Economical",
                    "confidence": 0.94,
                    "tts_message": "Smooth throttle input maintained."
                },
                "health": {
                    "status": "Normal",
                    "is_anomaly": false,
                    "anomaly_score": 0.00012,
                    "triggered_features": []
                },
                "fuel": {
                    "fcr_gs": 1.02,
                    "mileage_kmpl": 17.8,
                    "vss_kmph": 65.0,
                    "tier": 1,
                    "method": "maf"
                }
            }
        }
        """.trimIndent()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(TelemetryTick::class.java)
        val tick = adapter.fromJson(json)

        assertNotNull(tick)
        assertEquals(12, tick?.rowIndex)
        assertEquals(2450.0, tick?.data?.rpm ?: 0.0, 0.01)
        assertNotNull(tick?.ml)
        assertEquals("Economical", tick?.ml?.driverBehaviour?.label)
        assertEquals(0.94, tick?.ml?.driverBehaviour?.confidence ?: 0.0, 0.01)
        assertEquals(false, tick?.ml?.health?.isAnomaly)
        assertEquals("maf", tick?.ml?.fuel?.method)
        assertEquals(17.8, tick?.ml?.fuel?.mileageKmpl ?: 0.0, 0.01)
    }
}
