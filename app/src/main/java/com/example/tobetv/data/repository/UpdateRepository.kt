package com.example.tobetv.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.tobetv.BuildConfig
import com.example.tobetv.data.model.AppUpdate
import com.example.tobetv.data.network.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateRepository(private val context: Context) {

    suspend fun checkForUpdate(): Result<AppUpdate?> = withContext(Dispatchers.IO) {
        val result = SupabaseClient.checkLatestUpdate()
        result.map { update ->
            if (update != null && update.versionCode > BuildConfig.VERSION_CODE) {
                update
            } else {
                null
            }
        }
    }

    suspend fun downloadAndInstallApk(
        apkUrl: String,
        onProgress: (Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            var currentUrl = apkUrl
            var connection: HttpURLConnection
            var redirectCount = 0
            while (true) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                val status = connection.responseCode
                if (status in 300..399 && redirectCount < 5) {
                    val location = connection.getHeaderField("Location")
                    if (!location.isNullOrEmpty()) {
                        currentUrl = location
                        redirectCount++
                        connection.disconnect()
                        continue
                    }
                }
                break
            }

            val fileLength = connection.contentLength
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val apkFile = File(cacheDir, "update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val data = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            val progress = (total * 100 / fileLength).toInt()
                            onProgress(progress)
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                }
            }

            installApk(apkFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun installApk(apkFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
