package com.example.tobetv.ui.livetv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

@Composable
fun LiveTvBrowserScreen(
    channels: List<Channel>,
    playerManager: TvPlayerManager,
    onOpenFullscreen: (Channel) -> Unit,
    onBack: () -> Unit
) {
    val categories = remember(channels) {
        listOf("Semua") + channels.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val filteredChannels = remember(channels, selectedCategory) {
        if (selectedCategory == "Semua") channels else channels.filter { it.category == selectedCategory }
    }

    var selectedChannel by remember {
        mutableStateOf(filteredChannels.firstOrNull())
    }

    LaunchedEffect(filteredChannels) {
        if (selectedChannel == null || filteredChannels.none { it.id == selectedChannel?.id }) {
            selectedChannel = filteredChannels.firstOrNull()
        }
    }

    val playbackState by playerManager.playbackState.collectAsState()

    // When channel changes, start preview playback
    LaunchedEffect(selectedChannel) {
        selectedChannel?.let { channel ->
            if (playbackState.currentChannel?.id != channel.id) {
                playerManager.playChannel(channel, sourceIndex = 0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TobeBgDeep)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KOLOM 1: Kategori
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .fillMaxHeight()
                    .background(TobeSurfaceCard, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "KATEGORI",
                    color = TobeTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        TvCard(
                            onClick = {
                                selectedCategory = category
                                selectedChannel = filteredChannels.firstOrNull()
                            },
                            focusedBorderColor = TobeAccentPrimary,
                            unfocusedBorderColor = if (isSelected) TobeAccentPrimary else TobeGlassStroke,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isFocused || isSelected) TobeAccentPrimary else TobeTextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // KOLOM 2: Daftar Saluran
            Column(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .background(TobeSurfaceCard, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SALURAN TV",
                        color = TobeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredChannels.size} Saluran",
                        color = TobeTextSecondary,
                        fontSize = 12.sp
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredChannels) { channel ->
                        val isCurrent = selectedChannel?.id == channel.id
                        TvCard(
                            onClick = {
                                selectedChannel = channel
                                onOpenFullscreen(channel)
                            },
                            onFocus = {
                                selectedChannel = channel
                            },
                            focusedBorderColor = TobeAccentPrimary,
                            unfocusedBorderColor = if (isCurrent) TobeAccentPrimary else TobeGlassStroke,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) { isFocused ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Channel Number Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isFocused) TobeAccentPrimary else TobeSurfaceBright,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = channel.number,
                                        color = if (isFocused) TobeBgDeep else TobeTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Logo / Icon
                                if (!channel.logoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = channel.logoUrl,
                                        contentDescription = channel.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = channel.name,
                                        color = if (isFocused) TobeAccentPrimary else TobeTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = channel.category,
                                        color = TobeTextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                // Multi-source Backup Indicator
                                if (channel.sources.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .background(TobeSurfaceBright, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${channel.sources.size} Server",
                                            color = TobeAccentLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // KOLOM 3: Live Preview & Kontrol
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Preview Screen Player Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TobeSurfaceCard)
                        .border(1.dp, TobeGlassStroke, RoundedCornerShape(20.dp))
                ) {
                    VideoPlayerView(
                        player = playerManager.exoPlayer,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Left Live Badge
                    Box(
                        modifier = Modifier
                            .padding(14.dp)
                            .align(Alignment.TopStart)
                            .background(TobeLive, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "● LIVE PREVIEW",
                            color = TobeTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Buffering Indicator
                    if (playbackState.isBuffering) {
                        CircularProgressIndicator(
                            color = TobeAccentPrimary,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Error Message
                    playbackState.errorMessage?.let { errorMsg ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(TobeSurfaceCard.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = errorMsg,
                                color = TobeLive,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detail Panel & Server Switcher
                selectedChannel?.let { channel ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TobeSurfaceCard, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${channel.number} • ${channel.name}",
                                    color = TobeTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Kategori: ${channel.category}",
                                    color = TobeTextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            // Fullscreen Button
                            TvCard(
                                onClick = { onOpenFullscreen(channel) },
                                shape = RoundedCornerShape(10.dp)
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = null,
                                        tint = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Layar Penuh (OK)",
                                        color = if (isFocused) TobeBgDeep else TobeTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Server Selection Row
                        if (channel.sources.size > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Pilih Server:",
                                    color = TobeTextMuted,
                                    fontSize = 12.sp
                                )
                                channel.sources.forEachIndexed { index, source ->
                                    val isSelected = playbackState.currentSourceIndex == index
                                    TvCard(
                                        onClick = { playerManager.switchSource(index) },
                                        shape = RoundedCornerShape(8.dp),
                                        focusedBorderColor = TobeAccentPrimary,
                                        unfocusedBorderColor = if (isSelected) TobeAccentPrimary else TobeGlassStroke
                                    ) { isFocused ->
                                        Box(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = source.name,
                                                color = if (isFocused || isSelected) TobeAccentPrimary else TobeTextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
}
