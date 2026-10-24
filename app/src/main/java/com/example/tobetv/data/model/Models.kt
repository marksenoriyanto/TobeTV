package com.example.tobetv.data.model

import com.google.gson.annotations.SerializedName

data class ChannelSource(
    @SerializedName("name") val name: String = "",
    @SerializedName("url") val url: String = ""
)

data class Channel(
    @SerializedName("id") val id: String = "",
    @SerializedName("number") val number: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("category") val category: String = "Nasional",
    @SerializedName(value = "logo_url", alternate = ["logo"]) val logoUrl: String? = null,
    @SerializedName("is_indonesia") val isIndonesia: Boolean = false,
    @SerializedName("sources") val sources: List<ChannelSource> = emptyList(),
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true
) {
    fun getPrimaryStreamUrl(): String? = sources.firstOrNull()?.url
    fun getSourceByIndex(index: Int): ChannelSource? = sources.getOrNull(index)
}

data class VodItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("category") val category: String = "Film",
    @SerializedName("poster_url") val posterUrl: String? = null,
    @SerializedName("stream_url") val streamUrl: String = "",
    @SerializedName("year") val year: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("sort_order") val sortOrder: Int = 0,
    @SerializedName("is_active") val isActive: Boolean = true
)

data class AppUpdate(
    @SerializedName("version_code") val versionCode: Int = 0,
    @SerializedName("version_name") val versionName: String = "",
    @SerializedName("apk_url") val apkUrl: String = "",
    @SerializedName("changelog") val changelog: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true
)
