package com.example.tobetv.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.tobetv.data.model.Channel
import com.example.tobetv.data.model.ChannelSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class TvPlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isVod: Boolean = false,
    val currentChannel: Channel? = null,
    val currentSource: ChannelSource? = null,
    val currentSourceIndex: Int = 0,
    val totalSources: Int = 0,
    val errorMessage: String? = null,
    val autoSwitchedBackup: Boolean = false
)

@OptIn(UnstableApi::class)
class TvPlayerManager(private val context: Context) {

    // Tuned LoadControl for Low-Spec Android TV:
    // Min buffer: 4s, Max buffer: 10s, Back buffer: 0s (Saves up to 70% RAM!)
    private val lowMemoryLoadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs = */ 4_000,
            /* maxBufferMs = */ 10_000,
            /* bufferForPlaybackMs = */ 1_500,
            /* bufferForPlaybackAfterRebufferMs = */ 2_500
        )
        .setBackBuffer(0, false)
        .build()

    // OkHttp Client configured for reliable IPTV streaming
    // Fixes "unexpected end of stream on com.android.okhttp" and CDN connection drops
    // Automatically injects required Referer and User-Agent per CDN provider
    private val okHttpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            val request = chain.request()
            val host = request.url.host.lowercase()
            val builder = request.newBuilder()

            when {
                host.contains("dens.tv") -> {
                    builder.header("User-Agent", "DENSGO/3.00.00 (Linux;Android 15.0.0;) ExoPlayerLib/2.19.1")
                    builder.header("Referer", "https://www.dens.tv/")
                    builder.header("Origin", "https://www.dens.tv")
                }
                host.contains("r-plus") || host.contains("rcti") || host.contains("workers.dev") || host.contains("malingtv") -> {
                    builder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    builder.header("Referer", "https://www.rctiplus.com/")
                    builder.header("Origin", "https://www.rctiplus.com")
                }
                else -> {
                    builder.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                }
            }
            builder.header("Accept", "*/*")
            builder.header("Connection", "keep-alive")
            chain.proceed(builder.build())
        }
        .build()

    private val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)

    private val defaultDataSourceFactory = DefaultDataSource.Factory(context, okHttpDataSourceFactory)

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(defaultDataSourceFactory))
        .setLoadControl(lowMemoryLoadControl)
        .build()

    private val _playbackState = MutableStateFlow(TvPlaybackState())
    val playbackState: StateFlow<TvPlaybackState> = _playbackState.asStateFlow()

    private var activeChannel: Channel? = null
    private var activeSourceIndex = 0

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.value = _playbackState.value.copy(
                            isBuffering = true,
                            errorMessage = null
                        )
                    }
                    Player.STATE_READY -> {
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = exoPlayer.playWhenReady,
                            isBuffering = false,
                            errorMessage = null
                        )
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            isBuffering = false
                        )
                    }
                    Player.STATE_IDLE -> {
                        _playbackState.value = _playbackState.value.copy(
                            isBuffering = false
                        )
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlayerError(error: PlaybackException) {
                val channel = activeChannel
                if (channel != null && channel.sources.size > 1 && (activeSourceIndex + 1) < channel.sources.size) {
                    // AUTO FAILOVER: Switch to Backup Source seamlessly!
                    val nextIndex = activeSourceIndex + 1
                    playSource(channel, nextIndex, autoSwitched = true)
                } else {
                    _playbackState.value = _playbackState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = "Siaran bermasalah: ${error.errorCodeName}"
                    )
                }
            }
        })
    }

    fun playChannel(channel: Channel, sourceIndex: Int = 0) {
        activeChannel = channel
        activeSourceIndex = sourceIndex
        playSource(channel, sourceIndex, autoSwitched = false)
    }

    fun switchSource(index: Int) {
        val channel = activeChannel ?: return
        if (index in channel.sources.indices) {
            activeSourceIndex = index
            playSource(channel, index, autoSwitched = false)
        }
    }

    fun playDirectUrl(url: String, title: String) {
        activeChannel = null
        activeSourceIndex = 0
        _playbackState.value = TvPlaybackState(
            currentChannel = Channel(name = title, category = "Film"),
            currentSource = ChannelSource(name = "VOD", url = url),
            currentSourceIndex = 0,
            totalSources = 1,
            isBuffering = true,
            isVod = true
        )
        val mimeType: String? = when {
            url.contains(".mpd", ignoreCase = true) -> MimeTypes.APPLICATION_MPD
            url.contains(".m3u8", ignoreCase = true) -> MimeTypes.APPLICATION_M3U8
            url.contains(".mp4", ignoreCase = true) || url.contains("pixeldrain", ignoreCase = true) || url.contains("download", ignoreCase = true) -> MimeTypes.APPLICATION_MP4
            else -> null
        }
        val builder = MediaItem.Builder().setUri(Uri.parse(url))
        if (mimeType != null) {
            builder.setMimeType(mimeType)
        }
        val mediaItem = builder.build()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(mediaItem, true)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun playSource(channel: Channel, index: Int, autoSwitched: Boolean) {
        val source = channel.getSourceByIndex(index)
        if (source == null || source.url.isEmpty()) {
            _playbackState.value = _playbackState.value.copy(
                errorMessage = "Sumber siaran tidak tersedia"
            )
            return
        }

        _playbackState.value = TvPlaybackState(
            currentChannel = channel,
            currentSource = source,
            currentSourceIndex = index,
            totalSources = channel.sources.size,
            isBuffering = true,
            isVod = false,
            autoSwitchedBackup = autoSwitched
        )

        val mimeType: String? = when {
            source.url.contains(".mpd", ignoreCase = true) -> MimeTypes.APPLICATION_MPD
            source.url.contains(".m3u8", ignoreCase = true) -> MimeTypes.APPLICATION_M3U8
            source.url.contains(".mp4", ignoreCase = true) || source.url.contains("pixeldrain", ignoreCase = true) || source.url.contains("download", ignoreCase = true) -> MimeTypes.APPLICATION_MP4
            else -> null
        }
        val builder = MediaItem.Builder().setUri(Uri.parse(source.url))
        if (mimeType != null) {
            builder.setMimeType(mimeType)
        }
        val mediaItem = builder.build()
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(mediaItem, true)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun seekForward(ms: Long = 10_000) {
        val target = (exoPlayer.currentPosition + ms).coerceAtMost(if (exoPlayer.duration > 0) exoPlayer.duration else Long.MAX_VALUE)
        exoPlayer.seekTo(target)
    }

    fun seekBackward(ms: Long = 10_000) {
        val target = (exoPlayer.currentPosition - ms).coerceAtLeast(0)
        exoPlayer.seekTo(target)
    }

    fun togglePlayPause() {
        exoPlayer.playWhenReady = !exoPlayer.playWhenReady
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _playbackState.value = TvPlaybackState()
    }

    fun release() {
        exoPlayer.release()
    }
}
