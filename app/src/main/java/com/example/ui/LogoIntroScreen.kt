package com.example.ui

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.CockpitSteel
import kotlinx.coroutines.delay

/**
 * Startup Intro Video Screen.
 * Plays the user's Car_launches_for_app_intro video when the app is opened (cold start).
 * Never triggered on bottom tab switching or app reload.
 */
@Composable
fun LogoIntroScreen(
    onIntroComplete: () -> Unit
) {
    val context = LocalContext.current
    var hasCompleted by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    val completeIntro = {
        if (!hasCompleted) {
            hasCompleted = true
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
            onIntroComplete()
        }
    }

    // Safety timeout: auto-advance after 12 seconds in case playback or codec stalls
    LaunchedEffect(Unit) {
        delay(12000L)
        completeIntro()
    }

    // Find the car_launches_intro raw resource
    val rawResId = remember {
        val id = context.resources.getIdentifier("car_launches_intro", "raw", context.packageName)
        if (id != 0) id else R.raw.car_launches_intro
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { completeIntro() }
    ) {
        // Video Player
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    videoViewRef = this
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val uri = Uri.parse("android.resource://${ctx.packageName}/$rawResId")
                    setVideoURI(uri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        try {
                            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                        } catch (_: Exception) {}
                        start()
                    }
                    setOnCompletionListener {
                        completeIntro()
                    }
                    setOnErrorListener { _, _, _ ->
                        completeIntro()
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top-Right Skip Intro Button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E232E).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF3B4354), RoundedCornerShape(20.dp))
                .clickable { completeIntro() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "SKIP",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Skip Intro",
                    tint = CockpitSteel,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
