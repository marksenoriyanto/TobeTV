package com.example.tobetv.data.repository

import android.content.Context
import com.example.tobetv.data.model.Channel
import com.example.tobetv.data.model.VodItem
import com.example.tobetv.data.network.SupabaseClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStreamReader

class ChannelRepository(private val context: Context) {
    private val gson = Gson()
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _vodItems = MutableStateFlow<List<VodItem>>(emptyList())
    val vodItems: StateFlow<List<VodItem>> = _vodItems.asStateFlow()

    init {
        loadChannelsFromLocalAssets()
        loadDefaultVodContent()
    }

    private fun loadChannelsFromLocalAssets() {
        try {
            context.assets.open("channels.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<Channel>>() {}.type
                val localChannels: List<Channel> = gson.fromJson(reader, type)
                if (localChannels.isNotEmpty()) {
                    _channels.value = localChannels
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDefaultVodContent() {
        try {
            context.assets.open("vod.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<VodItem>>() {}.type
                val localVod: List<VodItem> = gson.fromJson(reader, type)
                if (localVod.isNotEmpty()) {
                    _vodItems.value = localVod
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback default sample videos if vod.json is unavailable
        _vodItems.value = listOf(
            VodItem(
                id = "vod_1",
                title = "Tears of Steel",
                category = "Sci-Fi",
                posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                year = "2024",
                duration = "12 Menit",
                description = "Kisah pejuang masa depan dalam dunia distopia yang berusaha menyelamatkan bumi."
            )
        )
    }

    suspend fun refreshFromSupabase(): Boolean {
        var anyUpdated = false
        val channelsResult = SupabaseClient.fetchChannels()
        channelsResult.onSuccess { remoteChannels ->
            if (remoteChannels.isNotEmpty()) {
                _channels.value = remoteChannels
                anyUpdated = true
            }
        }

        val vodResult = SupabaseClient.fetchVodContent()
        vodResult.onSuccess { remoteVod ->
            if (remoteVod.isNotEmpty()) {
                _vodItems.value = remoteVod
                anyUpdated = true
            }
        }
        return anyUpdated
    }
}
