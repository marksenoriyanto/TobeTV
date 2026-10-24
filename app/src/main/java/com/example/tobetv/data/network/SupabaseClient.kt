package com.example.tobetv.data.network

import com.example.tobetv.BuildConfig
import com.example.tobetv.data.model.AppUpdate
import com.example.tobetv.data.model.Channel
import com.example.tobetv.data.model.VodItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object SupabaseClient {
    private val gson = Gson()
    private val baseUrl: String get() = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val anonKey: String get() = BuildConfig.SUPABASE_ANON_KEY

    private fun openConnection(endpoint: String): HttpURLConnection {
        val fullUrl = if (endpoint.startsWith("http")) endpoint else "$baseUrl/rest/v1/$endpoint"
        val url = URL(fullUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 6000
        conn.readTimeout = 8000
        conn.setRequestProperty("apikey", anonKey)
        conn.setRequestProperty("Authorization", "Bearer $anonKey")
        conn.setRequestProperty("Accept", "application/json")
        return conn
    }

    suspend fun fetchChannels(): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            if (baseUrl.isEmpty() || anonKey.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Supabase config is empty"))
            }
            val conn = openConnection("channels?select=*&order=sort_order.asc")
            conn.requestMethod = "GET"

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                val type = object : TypeToken<List<Channel>>() {}.type
                val channels: List<Channel> = gson.fromJson(response, type)
                Result.success(channels)
            } else {
                Result.failure(Exception("HTTP Error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchVodContent(): Result<List<VodItem>> = withContext(Dispatchers.IO) {
        try {
            if (baseUrl.isEmpty() || anonKey.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Supabase config is empty"))
            }
            val conn = openConnection("vod_content?select=*&order=sort_order.asc")
            conn.requestMethod = "GET"

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                val type = object : TypeToken<List<VodItem>>() {}.type
                val items: List<VodItem> = gson.fromJson(response, type)
                Result.success(items)
            } else {
                Result.failure(Exception("HTTP Error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkLatestUpdate(): Result<AppUpdate?> = withContext(Dispatchers.IO) {
        try {
            if (baseUrl.isEmpty() || anonKey.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Supabase config is empty"))
            }
            val conn = openConnection("app_updates?select=*&order=version_code.desc&limit=1")
            conn.requestMethod = "GET"

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                val type = object : TypeToken<List<AppUpdate>>() {}.type
                val updates: List<AppUpdate> = gson.fromJson(response, type)
                Result.success(updates.firstOrNull())
            } else {
                Result.failure(Exception("HTTP Error: ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
