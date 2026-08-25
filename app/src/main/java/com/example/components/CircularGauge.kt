package com.example.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Indigo500

@Composable
fun CircularGauge(
    title: String,
    value: Float,
    maxValue: Float,
    unit: String,
    modifier: Modifier = Modifier,
    color: Color = Indigo500
) {
    AnalogGauge(
        title = title,
        value = value,
        maxValue = maxValue,
        unit = unit,
        modifier = modifier,
        gaugeColor = color
    )
}

