package com.example.tobetv.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableIntStateOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.tobetv.data.model.Channel
import com.example.tobetv.player.TvPlayerManager
import com.example.tobetv.ui.components.TvCard
import com.example.tobetv.ui.components.VideoPlayerView
import com.example.tobetv.ui.theme.TobeAccentLight
import com.example.tobetv.ui.theme.TobeAccentPrimary
import com.example.tobetv.ui.theme.TobeBgDeep
import com.example.tobetv.ui.theme.TobeGlassStroke
import com.example.tobetv.ui.theme.TobeLive
import com.example.tobetv.ui.theme.TobeSurfaceBright
import com.example.tobetv.ui.theme.TobeSurfaceCard
import com.example.tobetv.ui.theme.TobeTextMuted
import com.example.tobetv.ui.theme.TobeTextPrimary
import com.example.tobetv.ui.theme.TobeTextSecondary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun FullscreenPlayerScreen(
    channel: Channel,
    channels: List<Channel>,
    playerManager: TvPlayerManager,
    onBack: () -> Unit
) {
    var showOsd by remember { mutableStateOf(true) }
    var userInteracted by remember { mutableStateOf(0) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FILL) }
    val playbackState by playerManager.playbackState.collectAsState()

    BackHandler {
        onBack()
    }

    // Auto-hide OSD after 4 seconds
    LaunchedEffect(showOsd, userInteracted) {
        if (showOsd) {
            delay(4000)
            showOsd = false
        }
    }

    val currentChannel = playbackState.currentChannel ?: channel
    val currentChannelIndex = channels.indexOfFirst { it.id == currentChannel.id }

    fun nextChannel() {
        if (channels.isNotEmpty() && currentChannelIndex != -1) {
            val next = channels[(currentChannelIndex + 1) % channels.size]
            playerManager.playChannel(next, 0)
            showOsd = true
            userInteracted++
        }
    }

    fun prevChannel() {
        if (channels.isNotEmpty() && currentChannelIndex != -1) {
            val prevIndex = if (currentChannelIndex - 1 < 0) channels.size - 1 else currentChannelIndex - 1
            val prev = channels[prevIndex]
            playerManager.playChannel(prev, 0)
            showOsd = true
            userInteracted++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showOsd = !showOsd
                userInteracted++
            }
    ) {
        // Video View Fullscreen
        VideoPlayerView(
            player = playerManager.exoPlayer,
            modifier = Modifier.fillMaxSize(),
            resizeMode = resizeMode
        )

        // Loading Buffering Indicator
        if (playbackState.isBuffering) {
            CircularProgressIndicator(
                color = TobeAccentPrimary,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.Center)
            )
        }

        // Error message overlay
        playbackState.errorMessage?.let { errorMsg ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(TobeSurfaceCard.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMsg,
                        color = TobeLive,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mencoba server cadangan atau tekan KEMBALI",
                        color = TobeTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // OSD OVERLAY (Stitch 10-foot spec)
        AnimatedVisibility(
            visible = showOsd,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                TobeBgDeep.copy(alpha = 0.85f),
                                Color.Transparent,
                                Color.Transparent,
                                TobeBgDeep.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .padding(horizontal = 48.dp, vertical = 32.dp)
            ) {
                // Top OSD Bar: Channel info
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Number
                        Box(
                            modifier = Modifier
                                .background(TobeAccentPrimary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentChannel.number,
                                color = TobeBgDeep,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Name
                        Text(
                            text = currentChannel.name,
                            color = TobeTextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Live Badge
                        Box(
                            modifier = Modifier
                                .background(TobeLive, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "● LIVE",
                                color = TobeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Failover Badge
                        if (playbackState.autoSwitchedBackup) {
                            Box(
                                modifier = Modifier
                                    .background(TobeSurfaceBright, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Auto-Backup Aktif",
                                    color = TobeAccentLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Kategori: ${currentChannel.category} • Sumber: ${playbackState.currentSource?.name ?: "Default"}",
                        color = TobeTextSecondary,
                        fontSize = 14.sp
                    )
                }

                // Bottom OSD Bar: Controls & Remote Hints
                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    // Server switchers (Live TV only)
                    if (!playbackState.isVod && currentChannel.sources.size > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Ganti Server Siaran:",
                                color = TobeTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            currentChannel.sources.forEachIndexed { idx, src ->
                                val isSelected = playbackState.currentSourceIndex == idx
                                TvCard(
                                    onClick = {
                                        playerManager.switchSource(idx)
                                        userInteracted++
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    focusedBorderColor = TobeAccentPrimary,
                                    unfocusedBorderColor = if (isSelected) TobeAccentPrimary else TobeGlassStroke
                                ) { isFocused ->
                                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                        Text(
                                            text = src.name,
                                            color = if (isFocused || isSelected) TobeAccentPrimary else TobeTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Navigation / Player Controls & Aspect Ratio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (playbackState.isVod) {
                                // VOD Controls: Rewind, Play/Pause, Forward
                                TvCard(
                                    onClick = {
                                        playerManager.seekBackward(15_000)
                                        userInteracted++
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) { isFocused ->
                                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "⏪ -15 Detik",
                                            color = if (isFocused) TobeBgDeep else TobeTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                TvCard(
                                    onClick = {
                                        playerManager.togglePlayPause()
                                        userInteracted++
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    focusedBorderColor = TobeAccentPrimary,
                                    unfocusedBorderColor = TobeAccentPrimary.copy(alpha = 0.5f)
                                ) { isFocused ->
                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                        Text(
                                            text = if (playbackState.isPlaying) "⏸ Jeda" else "▶ Putar",
                                            color = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                TvCard(
                                    onClick = {
                                        playerManager.seekForward(15_000)
                                        userInteracted++
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) { isFocused ->
                                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "⏩ +15 Detik",
                                            color = if (isFocused) TobeBgDeep else TobeTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                // Live TV Channel switcher buttons (Next / Prev)
                                TvCard(
                                    onClick = { prevChannel() },
                                    shape = RoundedCornerShape(8.dp)
                                ) { isFocused ->
                                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "◀ Saluran Sebelumnya",
                                            color = if (isFocused) TobeBgDeep else TobeTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                TvCard(
                                    onClick = { nextChannel() },
                                    shape = RoundedCornerShape(8.dp)
                                ) { isFocused ->
                                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "Saluran Berikutnya ▶",
                                            color = if (isFocused) TobeBgDeep else TobeTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // Aspect Ratio Toggle
                            val aspectLabel = when (resizeMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Layar Penuh (Stretch 16:9)"
                                AspectRatioFrameLayout.RESIZE_MODE_FIT -> "Rasio Asli (Fit)"
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Zoom (Potong Tepi)"
                                else -> "Format Layar"
                            }
                            TvCard(
                                onClick = {
                                    resizeMode = when (resizeMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        else -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    }
                                    userInteracted++
                                },
                                shape = RoundedCornerShape(8.dp),
                                focusedBorderColor = TobeAccentPrimary,
                                unfocusedBorderColor = TobeAccentPrimary.copy(alpha = 0.5f)
                            ) { isFocused ->
                                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "⛶ $aspectLabel",
                                        color = if (isFocused) TobeBgDeep else TobeAccentLight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (playbackState.isVod) "Tekan [OK] untuk OSD • [KEMBALI] ke Daftar Film" else "Tekan [OK] untuk sembunyikan OSD • [KEMBALI] ke Daftar",
                            color = TobeTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
