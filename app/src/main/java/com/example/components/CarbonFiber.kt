package com.example.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDarkBackground
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonWeaveDark
import com.example.ui.theme.CarbonWeaveHighlight
import com.example.ui.theme.CarbonWeaveLight
import com.example.ui.theme.LamboCarbonBlack
import com.example.ui.theme.LamboYellow
import com.example.ui.theme.LamboYellowBright

/**
 * Draws a realistic 2x2 Twill Carbon Fiber Weave with clear-coat gloss overlay.
 */
fun Modifier.carbonFiberPattern(
    baseDark: Color = CarbonWeaveDark,
    weaveLight: Color = CarbonWeaveLight,
    weaveHighlight: Color = CarbonWeaveHighlight,
    tileUnitDp: Float = 12f,
    overlayAlpha: Float = 0.08f
): Modifier = this.drawBehind {
    val tileSize = tileUnitDp.dp.toPx()
    val half = tileSize / 2f

    // 1. Solid dark base
    drawRect(color = CarbonDarkBackground)

    val cols = (size.width / tileSize).toInt() + 2
    val rows = (size.height / tileSize).toInt() + 2

    // 2. 2x2 Twill interlocking fiber weave
    for (c in 0 until cols) {
        for (r in 0 until rows) {
            val x = c * tileSize
            val y = r * tileSize

            val isEvenBlock = (c + r) % 2 == 0

            if (isEvenBlock) {
                // Top-left to bottom-right diagonal fiber
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(baseDark, weaveLight, weaveHighlight, baseDark),
                        start = Offset(x, y),
                        end = Offset(x + half, y + half)
                    ),
                    topLeft = Offset(x, y),
                    size = Size(half, half)
                )
                // Bottom-right quadrant diagonal fiber
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(weaveHighlight, weaveLight, baseDark),
                        start = Offset(x + half, y + half),
                        end = Offset(x + tileSize, y + tileSize)
                    ),
                    topLeft = Offset(x + half, y + half),
                    size = Size(half, half)
                )
            } else {
                // Top-right to bottom-left diagonal fiber
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(weaveLight, baseDark, weaveHighlight),
                        start = Offset(x + half, y),
                        end = Offset(x, y + half)
                    ),
                    topLeft = Offset(x, y),
                    size = Size(half, half)
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(baseDark, weaveHighlight, weaveLight),
                        start = Offset(x + tileSize, y + half),
                        end = Offset(x + half, y + tileSize)
                    ),
                    topLeft = Offset(x + half, y + half),
                    size = Size(half, half)
                )
            }
        }
    }

    // 3. Clear-coat gloss specular gradient for authentic automotive lacquer effect
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = overlayAlpha),
                Color.Transparent,
                Color.White.copy(alpha = overlayAlpha * 0.4f),
                Color.Black.copy(alpha = 0.35f)
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    )
}

/**
 * A container styled as an automotive Carbon Fiber Panel with Lamborghini Urus Yellow edge accents.
 */
@Composable
fun CarbonFiberPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    border: BorderStroke? = BorderStroke(1.dp, CarbonBorder),
    hasYellowAccent: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val finalBorder = if (hasYellowAccent) {
        BorderStroke(1.5.dp, LamboYellow)
    } else {
        border ?: BorderStroke(1.dp, CarbonBorder)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .carbonFiberPattern()
            .border(finalBorder, shape)
    ) {
        content()
    }
}

/**
 * Lamborghini Urus inspired Giallo Auge racing stripe element with yellow & carbon black accents.
 */
@Composable
fun UrusRacingStripe(
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
    ) {
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .background(LamboYellow)
        )
        Box(
            modifier = Modifier
                .weight(0.1f)
                .fillMaxHeight()
                .background(LamboCarbonBlack)
        )
        Box(
            modifier = Modifier
                .weight(0.2f)
                .fillMaxHeight()
                .background(LamboYellowBright)
        )
    }
}
