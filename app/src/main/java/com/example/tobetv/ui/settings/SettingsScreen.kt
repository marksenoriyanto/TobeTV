package com.example.tobetv.ui.settings

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tobetv.BuildConfig
import com.example.tobetv.data.model.AppUpdate
import com.example.tobetv.data.repository.ChannelRepository
import com.example.tobetv.data.repository.UpdateRepository
import com.example.tobetv.ui.components.TvCard
import com.example.tobetv.ui.theme.TobeAccentPrimary
import com.example.tobetv.ui.theme.TobeBgDeep
import com.example.tobetv.ui.theme.TobeLive
import com.example.tobetv.ui.theme.TobeSurfaceBright
import com.example.tobetv.ui.theme.TobeSurfaceCard
import com.example.tobetv.ui.theme.TobeTextMuted
import com.example.tobetv.ui.theme.TobeTextPrimary
import com.example.tobetv.ui.theme.TobeTextSecondary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    channelRepository: ChannelRepository,
    updateRepository: UpdateRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateResultText by remember { mutableStateOf<String?>(null) }
    var latestUpdate by remember { mutableStateOf<AppUpdate?>(null) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }

    var isSyncingChannels by remember { mutableStateOf(false) }
    var syncResultText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TobeBgDeep)
            .padding(horizontal = 48.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvCard(
                    onClick = onBack,
                    shape = RoundedCornerShape(10.dp)
                ) { isFocused ->
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kembali",
                            color = if (isFocused) TobeBgDeep else TobeTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = "Pengaturan & Info Sistem",
                    color = TobeTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: App Version & In-App Updater
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(TobeSurfaceCard, RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "PEMBARUAN APLIKASI (OTA)",
                        color = TobeAccentPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Versi Saat Ini: v${BuildConfig.VERSION_NAME} (Kode: ${BuildConfig.VERSION_CODE})",
                        color = TobeTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Kamu bisa mengecek dan memasang pembaruan langsung dari TV tanpa harus membuka browser atau memakai USB.",
                        color = TobeTextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Button: Cek Pembaruan
                    TvCard(
                        onClick = {
                            if (!isCheckingUpdate && !isDownloading) {
                                isCheckingUpdate = true
                                updateResultText = null
                                latestUpdate = null
                                coroutineScope.launch {
                                    val result = updateRepository.checkForUpdate()
                                    isCheckingUpdate = false
                                    result.onSuccess { update ->
                                        if (update != null) {
                                            latestUpdate = update
                                            updateResultText = "Pembaruan v${update.versionName} tersedia!"
                                        } else {
                                            updateResultText = "Aplikasi kamu sudah versi terbaru!"
                                        }
                                    }.onFailure {
                                        updateResultText = "Gagal mengecek pembaruan: periksa internet"
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { isFocused ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = null,
                                    tint = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isCheckingUpdate) "Mengecek Server..." else "Cek Pembaruan",
                                color = if (isFocused) TobeBgDeep else TobeTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Result message
                    updateResultText?.let { msg ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = msg,
                            color = if (latestUpdate != null) TobeAccentPrimary else TobeTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Download & Install Card if update is available
                    latestUpdate?.let { update ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TobeSurfaceBright, RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Rilis Baru: v${update.versionName}",
                                    color = TobeAccentPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                update.changelog?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it,
                                        color = TobeTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (isDownloading) {
                                    Column {
                                        LinearProgressIndicator(
                                            progress = { downloadProgress / 100f },
                                            color = TobeAccentPrimary,
                                            trackColor = TobeSurfaceCard,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Mengunduh APK: $downloadProgress%",
                                            color = TobeTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    TvCard(
                                        onClick = {
                                            isDownloading = true
                                            coroutineScope.launch {
                                                updateRepository.downloadAndInstallApk(update.apkUrl) { progress ->
                                                    downloadProgress = progress
                                                }
                                                isDownloading = false
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) { isFocused ->
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Unduh & Pasang Pembaruan",
                                                color = if (isFocused) TobeBgDeep else TobeTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Column: Database Sync & Hardware Profile
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(TobeSurfaceCard, RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = "SYNC DATABASE & STATUS PERANGKAT",
                        color = TobeAccentPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TobeLive, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Status Cloud: Terhubung (Aktif)",
                            color = TobeTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TobeAccentPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mode Player: Low-RAM Buffer (Hemat Memori TV)",
                            color = TobeTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Button: Sync Channels
                    TvCard(
                        onClick = {
                            if (!isSyncingChannels) {
                                isSyncingChannels = true
                                syncResultText = null
                                coroutineScope.launch {
                                    val ok = channelRepository.refreshFromSupabase()
                                    isSyncingChannels = false
                                    syncResultText = if (ok) {
                                        "Berhasil memperbarui daftar channel dari Supabase!"
                                    } else {
                                        "Data lokal sudah sesuai dengan server."
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { isFocused ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSyncingChannels) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = if (isFocused) TobeBgDeep else TobeAccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isSyncingChannels) "Menyinkronkan..." else "Perbarui Link Channel Sekarang",
                                color = if (isFocused) TobeBgDeep else TobeTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    syncResultText?.let { msg ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = msg,
                            color = TobeTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
