package com.example.tobetv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.tobetv.data.model.Channel
import com.example.tobetv.data.repository.ChannelRepository
import com.example.tobetv.data.repository.UpdateRepository
import com.example.tobetv.player.TvPlayerManager
import com.example.tobetv.ui.launcher.MainLauncherScreen
import com.example.tobetv.ui.livetv.LiveTvBrowserScreen
import com.example.tobetv.ui.player.FullscreenPlayerScreen
import com.example.tobetv.ui.settings.SettingsScreen
import com.example.tobetv.ui.theme.TobeBgDeep
import com.example.tobetv.ui.theme.TobeTVTheme
import com.example.tobetv.ui.vod.VodCatalogScreen
import kotlinx.coroutines.launch

sealed class Screen {
    object Launcher : Screen()
    object LiveTv : Screen()
    data class Fullscreen(
        val channel: Channel,
        val returnScreen: Screen = Screen.LiveTv
    ) : Screen()
    object Vod : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var channelRepository: ChannelRepository
    private lateinit var updateRepository: UpdateRepository
    private lateinit var playerManager: TvPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channelRepository = ChannelRepository(applicationContext)
        updateRepository = UpdateRepository(applicationContext)
        playerManager = TvPlayerManager(applicationContext)

        // Asynchronous background sync from Supabase (does not block cold start!)
        lifecycleScope.launch {
            channelRepository.refreshFromSupabase()
        }

        setContent {
            TobeTVTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Launcher) }
                val channels by channelRepository.channels.collectAsState()
                val vodItems by channelRepository.vodItems.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TobeBgDeep)
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Launcher -> {
                            MainLauncherScreen(
                                onNavigateToLiveTv = {
                                    currentScreen = Screen.LiveTv
                                },
                                onNavigateToVod = {
                                    currentScreen = Screen.Vod
                                },
                                onNavigateToSettings = {
                                    currentScreen = Screen.Settings
                                },
                                totalChannels = channels.size
                            )
                        }

                        is Screen.LiveTv -> {
                            BackHandler {
                                playerManager.stop()
                                currentScreen = Screen.Launcher
                            }
                            LiveTvBrowserScreen(
                                channels = channels,
                                playerManager = playerManager,
                                onOpenFullscreen = { channel ->
                                    currentScreen = Screen.Fullscreen(channel, returnScreen = Screen.LiveTv)
                                },
                                onBack = {
                                    playerManager.stop()
                                    currentScreen = Screen.Launcher
                                }
                            )
                        }

                        is Screen.Fullscreen -> {
                            FullscreenPlayerScreen(
                                channel = screen.channel,
                                channels = channels,
                                playerManager = playerManager,
                                onBack = {
                                    if (screen.returnScreen == Screen.Vod) {
                                        playerManager.stop()
                                    }
                                    currentScreen = screen.returnScreen
                                }
                            )
                        }

                        is Screen.Vod -> {
                            BackHandler {
                                currentScreen = Screen.Launcher
                            }
                            VodCatalogScreen(
                                items = vodItems,
                                onPlayVod = { vodItem ->
                                    playerManager.playDirectUrl(vodItem.streamUrl, vodItem.title)
                                    currentScreen = Screen.Fullscreen(
                                        channel = Channel(
                                            id = vodItem.id,
                                            name = vodItem.title,
                                            category = vodItem.category
                                        ),
                                        returnScreen = Screen.Vod
                                    )
                                },
                                onBack = {
                                    currentScreen = Screen.Launcher
                                }
                            )
                        }

                        is Screen.Settings -> {
                            BackHandler {
                                currentScreen = Screen.Launcher
                            }
                            SettingsScreen(
                                channelRepository = channelRepository,
                                updateRepository = updateRepository,
                                onBack = {
                                    currentScreen = Screen.Launcher
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (::playerManager.isInitialized) {
            playerManager.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::playerManager.isInitialized) {
            playerManager.release()
        }
    }
}
