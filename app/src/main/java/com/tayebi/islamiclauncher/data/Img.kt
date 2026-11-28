package com.tayebi.islamiclauncher.data

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** Tiny remote image loader (logos, news pictures) with an in-memory cache. Failures just return null. */
object Img {
    private val cache = LruCache<String, ImageBitmap>(120)
    private val failed = HashSet<String>()

    suspend fun load(url: String, maxSide: Int = 400): ImageBitmap? {
        if (url.isBlank() || !url.startsWith("http")) return null
        cache.get(url)?.let { return it }
        if (synchronized(failed) { url in failed }) return null
        return withContext(Dispatchers.IO) {
            try {
                val c = URL(url.replace("http://", "https://")).openConnection() as HttpURLConnection
                c.connectTimeout = 8000; c.readTimeout = 10000; c.instanceFollowRedirects = true
                c.setRequestProperty("User-Agent", "IslamicSmartTVLauncher/1.0")
                val bytes = try {
                    if (c.responseCode !in 200..299) throw java.io.IOException("HTTP ${c.responseCode}")
                    c.inputStream.use { it.readBytes() }
                } finally { c.disconnect() }
                val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, o)
                var sample = 1
                while (o.outWidth / (sample * 2) >= maxSide && o.outHeight / (sample * 2) >= maxSide) sample *= 2
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
                    ?: throw java.io.IOException("decode")
                bmp.asImageBitmap().also { cache.put(url, it) }
            } catch (t: Throwable) {
                synchronized(failed) { failed.add(url) }
                null
            }
        }
    }
}
