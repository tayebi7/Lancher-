package com.tayebi.islamiclauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

/** Discovers the apps that are really installed on the device. */
class AppsRepo(private val ctx: Context) {
    fun queryAll(): List<AppInfo> {
        val pm = ctx.packageManager
        val found = LinkedHashMap<String, ResolveInfo>()
        for (cat in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val i = Intent(Intent.ACTION_MAIN).addCategory(cat)
            for (ri in pm.queryIntentActivities(i, 0)) {
                val p = ri.activityInfo.packageName
                if (p != ctx.packageName) found.putIfAbsent(p, ri)
            }
        }
        return found.values.map { ri ->
            AppInfo(
                ri.activityInfo.packageName,
                ri.loadLabel(pm).toString(),
                runCatching { ri.loadIcon(pm).toBitmap(160, 160).asImageBitmap() }.getOrNull(),
                runCatching { ri.activityInfo.loadBanner(pm)?.toBitmap(320, 180)?.asImageBitmap() }.getOrNull()
            )
        }.sortedBy { it.label.lowercase() }
    }

    companion object {
        /** Preferred default favourites, matched against what is actually installed. */
        val DEFAULTS = listOf(
            listOf("com.google.android.youtube.tv", "com.google.android.youtube"),
            listOf("com.google.android.youtube.tvkids", "com.google.android.youtube.kids"),
            listOf("net.mbc.shahid", "com.mbc.shahid"),
            listOf("com.disney.disneyplus")
        )
        /** Never suggested automatically (the user can still add them by hand). */
        val EXCLUDED = setOf(
            "com.netflix.ninja", "com.netflix.mediaclient", "com.android.vending",
            "com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient", "com.amazon.avod", "com.amazon.firetv.youtube"
        )
        val EXTRAS = listOf(
            "org.xbmc.kodi",
            "org.videolan.vlc", "com.spotify.tv.android", "com.google.android.youtube.tvmusic"
        )
    }
}
