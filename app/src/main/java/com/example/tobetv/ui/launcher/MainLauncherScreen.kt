package com.example.tobetv.ui.launcher

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tobetv.ui.components.TvCard
import com.example.tobetv.ui.theme.TobeAccentPrimary
import com.example.tobetv.ui.theme.TobeBgDeep
import com.example.tobetv.ui.theme.TobeLive
import com.example.tobetv.ui.theme.TobeSurfaceBright
import com.example.tobetv.ui.theme.TobeSurfaceCard
import com.example.tobetv.ui.theme.TobeTextMuted
import com.example.tobetv.ui.theme.TobeTextPrimary
import com.example.tobetv.ui.theme.TobeTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainLauncherScreen(
    onNavigateToLiveTv: () -> Unit,
    onNavigateToVod: () -> Unit,
    onNavigateToSettings: () -> Unit,
    totalChannels: Int = 21
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TobeBgDeep)
            .padding(horizontal = 48.dp, vertical = 36.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Logo, Date, Clock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(TobeAccentPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "TOBE TV",
                        color = TobeAccentPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .background(TobeSurfaceBright, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Playbox Hub",
                            color = TobeTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentTime,
                        color = TobeTextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentDate,
                        color = TobeTextMuted,
                        fontSize = 14.sp
                    )
                }
            }

            // 3 Large Launcher Cards (Stitch Playbox Concept)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                LauncherCard(
                    modifier = Modifier.weight(1f),
                    title = "Siaran TV",
                    subtitle = "$totalChannels Saluran Indonesia & TV Luar",
                    badge = "LIVE",
                    badgeColor = TobeLive,
                    icon = Icons.Default.LiveTv,
                    accentColor = TobeAccentPrimary,
                    onClick = onNavigateToLiveTv
                )

                LauncherCard(
                    modifier = Modifier.weight(1f),
                    title = "Film & Bioskop",
                    subtitle = "Katalog Film & Serial Pilihan",
                    badge = "VOD",
                    badgeColor = TobeAccentPrimary,
                    icon = Icons.Default.Movie,
                    accentColor = TobeAccentPrimary,
                    onClick = onNavigateToVod
                )

                LauncherCard(
                    modifier = Modifier.weight(1f),
                    title = "Pengaturan",
                    subtitle = "Cek Pembaruan & Info Sistem",
                    badge = "SISTEM",
                    badgeColor = TobeSurfaceBright,
                    icon = Icons.Default.Settings,
                    accentColor = TobeAccentPrimary,
                    onClick = onNavigateToSettings
                )
            }

            // Bottom D-Pad Remote Guide Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gunakan tombol panah remote [▲ ▼ ◀ ▶] lalu tekan [OK] untuk memilih",
                    color = TobeTextMuted,
                    fontSize = 14.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(TobeLive, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Auto-Failover Backup Siaran Aktif",
                        color = TobeTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LauncherCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    TvCard(
        modifier = modifier.height(280.dp),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp)
    ) { isFocused ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            if (isFocused) accentColor else TobeSurfaceBright,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isFocused) TobeBgDeep else TobeTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(badgeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        color = TobeTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    color = if (isFocused) accentColor else TobeTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    color = TobeTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
