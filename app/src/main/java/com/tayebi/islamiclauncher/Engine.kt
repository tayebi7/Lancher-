package com.tayebi.islamiclauncher

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.tayebi.islamiclauncher.data.AppInfo
import com.tayebi.islamiclauncher.data.AppsRepo
import com.tayebi.islamiclauncher.data.DayTimes
import com.tayebi.islamiclauncher.data.Geo
import com.tayebi.islamiclauncher.data.Place
import com.tayebi.islamiclauncher.data.PrayerRepo
import com.tayebi.islamiclauncher.data.Settings
import com.tayebi.islamiclauncher.data.Weather
import com.tayebi.islamiclauncher.data.WeatherRepo
import com.tayebi.islamiclauncher.i18n.tr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.coroutines.resume

enum class Screen { Home, Apps, Favorites, Prayer, Quran, Hadith, Settings }
enum class CityTarget { Prayer, Weather }

sealed interface Dlg {
    data class City(val target: CityTarget) : Dlg
    object Method : Dlg
    object School : Dlg
    object Tune : Dlg
    object Lang : Dlg
    object Bg : Dlg
    object BgUrl : Dlg
    object HijriAdj : Dlg
    object Volume : Dlg
    object AdhanUrl : Dlg
    object Developer : Dlg
    object Reciter : Dlg
    object PickFav : Dlg
    data class AppMenu(val app: AppInfo, val inFavs: Boolean) : Dlg
}

/** Application-wide state holder. Lives as long as the process, so prayer alerts keep working in the background. */
class Engine(val app: Application) {
    val s = Settings(app)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + kotlinx.coroutines.CoroutineExceptionHandler { _, ex -> android.util.Log.e("Engine", "background error", ex) })
    val audio = AudioController(app, s)
    private val prayerRepo = PrayerRepo(s.sp)
    private val weatherRepo = WeatherRepo(s.sp)
    private val appsRepo = AppsRepo(app)
    val q = QuranState(this)
    val h = HadithState(this)

    var screen by mutableStateOf(Screen.Home)
    var dlg by mutableStateOf<Dlg?>(null)
    var toast by mutableStateOf<String?>(null)
    var nowMs by mutableLongStateOf(System.currentTimeMillis())
    var online by mutableStateOf(true)

    var today by mutableStateOf<DayTimes?>(null)
    var tomorrow by mutableStateOf<DayTimes?>(null)
    var prayerFailed by mutableStateOf(false)

    var weather by mutableStateOf<Weather?>(null)
    var weatherFailed by mutableStateOf(false)

    var installed by mutableStateOf<List<AppInfo>>(emptyList())
    var adhanBanner by mutableStateOf<String?>(null)
    var customBg by mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)

    private var lastPrayerAttempt = 0L
    private var lastWeatherAttempt = 0L
    private var lastAdhanKey = ""
    private var lastAdhanStart = 0L
    private val prayerKeys = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
    private var prayerJob: Job? = null
    private var weatherJob: Job? = null

    init {
        audio.onError = { toast(t(it)) }
        loadCustomBg()
        watchNetwork()
        watchPackages()
        loadApps()
        startTicker()
    }

    fun t(key: String) = tr(key, s.lang.value)

    fun toast(msg: String) {
        toast = msg
        scope.launch {
            delay(3500)
            if (toast == msg) toast = null
        }
    }

    // ---------- custom background ----------
    private val bgFile get() = java.io.File(app.filesDir, "bg_custom.img")

    private fun decodeBg(): androidx.compose.ui.graphics.ImageBitmap? = try {
        val o = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(bgFile.path, o)
        var sample = 1
        while (o.outWidth / (sample * 2) >= 1920) sample *= 2
        val bmp = android.graphics.BitmapFactory.decodeFile(bgFile.path, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample })
        bmp?.asImageBitmap()
    } catch (ex: Throwable) {
        null
    }

    fun loadCustomBg() {
        if (!bgFile.exists()) return
        scope.launch { customBg = withContext(Dispatchers.IO) { decodeBg() } }
    }

    fun setBgFromUrl(url: String) {
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    com.tayebi.islamiclauncher.data.Http.download(url, bgFile)
                    decodeBg()
                } catch (ex: Exception) {
                    null
                }
            }
            if (ok != null) {
                customBg = ok
                s.bg.set(8)
            } else toast(t("bg_failed"))
        }
    }

    // ---------- launcher (Home app) status ----------
    fun isDefaultHome(): Boolean = try {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        app.packageManager.resolveActivity(i, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName == app.packageName
    } catch (ex: Exception) {
        false
    }

    /** Android does not let an app silently become/stop being the Home app; open the system chooser/settings. */
    fun openHomeChooser() {
        toast(t("launcher_hint"))
        val flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            app.startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS).addFlags(flags))
        } catch (ex: Exception) {
            try {
                // With no default set, pressing Home shows the "choose launcher" dialog.
                app.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(flags))
            } catch (_: Exception) {
                try {
                    app.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(flags))
                } catch (_: Exception) {
                }
            }
        }
    }

    // ---------- navigation ----------
    fun go(sc: Screen) {
        screen = sc
        if (sc == Screen.Quran) q.loadChapters()
    }

    fun back() {
        when {
            screen == Screen.Quran && q.open > 0 -> q.close()
            screen == Screen.Hadith && h.view != HView.Books -> h.back()
            else -> screen = Screen.Home
        }
    }

    // ---------- places ----------
    fun prayerPlace(): Place = Place.parse(s.prayerPlace.value)
    fun weatherPlace(): Place = if (s.weatherSame.value) prayerPlace() else Place.parse(s.weatherPlace.value)
    private fun zone(): ZoneId = runCatching { ZoneId.of(prayerPlace().tz) }.getOrDefault(ZoneId.systemDefault())

    fun pickPlace(target: CityTarget, p: Place) {
        if (target == CityTarget.Prayer) {
            s.prayerPlace.set(p.toJson())
            refreshPrayer()
            if (s.weatherSame.value) refreshWeather()
        } else {
            s.weatherSame.set(false)
            s.weatherPlace.set(p.toJson())
            refreshWeather()
        }
    }

    fun setWeatherSame(v: Boolean) {
        s.weatherSame.set(v)
        refreshWeather()
    }

    // ---------- prayer ----------
    fun refreshPrayer() {
        prayerJob?.cancel()
        prayerJob = scope.launch {
            lastPrayerAttempt = System.currentTimeMillis()
            val p = prayerPlace()
            val d = ZonedDateTime.now(zone()).toLocalDate()
            val m = s.method.value
            val sc = s.school.value
            val tune = s.tuneList()
            val a = prayerRepo.get(p, d, m, sc, tune)
            val b = prayerRepo.get(p, d.plusDays(1), m, sc, tune)
            if (a != null) {
                today = a
                tomorrow = b
                prayerFailed = false
            } else {
                prayerFailed = true
            }
        }
    }

    fun prayerUpdatedMs(): Long = prayerRepo.lastUpdated()

    /** Index (0..5) and epoch-ms of the next prayer (Sunrise is skipped – it is not a prayer). */
    fun nextPrayer(): Pair<Int, Long>? {
        val t = today ?: return null
        val z = zone()
        for (i in intArrayOf(0, 2, 3, 4, 5)) {
            val ms = ZonedDateTime.of(t.date, t.t[i], z).toInstant().toEpochMilli()
            if (ms > nowMs) return i to ms
        }
        val tm = tomorrow ?: return null
        return 0 to ZonedDateTime.of(tm.date, tm.t[0], z).toInstant().toEpochMilli()
    }

    fun testAdhan() = audio.playAdhan()

    // ---------- weather ----------
    fun refreshWeather() {
        weatherJob?.cancel()
        val p = weatherPlace()
        weather = weatherRepo.cached(p)
        weatherJob = scope.launch {
            lastWeatherAttempt = System.currentTimeMillis()
            try {
                weather = weatherRepo.fetch(p)
                weatherFailed = false
            } catch (ex: Exception) {
                weatherFailed = true
            }
        }
    }

    fun refreshAll() {
        refreshPrayer()
        refreshWeather()
    }

    // ---------- apps & favorites ----------
    fun loadApps() {
        scope.launch {
            installed = withContext(Dispatchers.IO) { appsRepo.queryAll() }
            ensureDefaultFavs()
        }
    }

    private fun ensureDefaultFavs() {
        if (s.favorites.value != Settings.UNSET || installed.isEmpty()) return
        val out = ArrayList<String>()
        for (group in AppsRepo.DEFAULTS.take(4)) {
            group.firstOrNull { p -> installed.any { it.pkg == p } }?.let { out.add(it) }
        }
        installed.firstOrNull { it.label.contains("iptv", true) && it.pkg !in out }?.let { out.add(it.pkg) }
        AppsRepo.DEFAULTS.last().firstOrNull { p -> installed.any { it.pkg == p } }?.let { out.add(it) }
        s.favorites.set(out.joinToString("\n"))
    }

    fun favPkgs(): List<String> {
        val v = s.favorites.value
        return if (v == Settings.UNSET) emptyList() else v.split("\n").filter { it.isNotBlank() }
    }

    fun favApps(): List<AppInfo> = favPkgs().mapNotNull { p -> installed.firstOrNull { it.pkg == p } }

    private fun saveFavs(l: List<String>) = s.favorites.set(l.joinToString("\n"))

    fun toggleFav(pkg: String) {
        val l = favPkgs()
        saveFavs(if (pkg in l) l - pkg else l + pkg)
    }

    fun moveFav(pkg: String, delta: Int) {
        val l = favPkgs().toMutableList()
        val i = l.indexOf(pkg)
        val j = i + delta
        if (i < 0 || j < 0 || j >= l.size) return
        l.removeAt(i)
        l.add(j, pkg)
        saveFavs(l)
    }

    fun launch(a: AppInfo) {
        val pm = app.packageManager
        val i = pm.getLeanbackLaunchIntentForPackage(a.pkg) ?: pm.getLaunchIntentForPackage(a.pkg)
        if (i == null) {
            toast(a.label)
            return
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            app.startActivity(i)
        } catch (ex: Exception) {
            toast(a.label)
        }
    }

    // ---------- background watchers ----------
    private fun watchNetwork() {
        val cm = app.getSystemService(ConnectivityManager::class.java) ?: return
        online = try {
            cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (_: Throwable) {
            true
        }
        try {
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    scope.launch {
                        online = true
                        refreshAll()
                    }
                }

                override fun onLost(network: Network) {
                    scope.launch { online = false }
                }
            })
        } catch (_: Exception) {
        }
    }

    private fun watchPackages() {
        val f = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(app, object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) = loadApps()
        }, f, ContextCompat.RECEIVER_EXPORTED)
    }

    private fun startTicker() {
        scope.launch {
            refreshAll()
            while (true) {
                val n = System.currentTimeMillis()
                nowMs = n
                try {
                    housekeeping(n)
                } catch (_: Throwable) {
                }
                delay(1000 - (n % 1000) + 5)
            }
        }
    }

    private fun housekeeping(n: Long) {
        audio.poll()
        val t = today
        val day = ZonedDateTime.ofInstant(Instant.ofEpochMilli(n), zone()).toLocalDate()
        if ((t == null || t.date != day) && n - lastPrayerAttempt > 30_000) refreshPrayer()
        val wGap = if (weatherFailed || weather == null) 120_000L else 30 * 60_000L
        if (n - lastWeatherAttempt > wGap) refreshWeather()
        checkAdhan(n)
    }


    private fun checkAdhan(n: Long) {
        if (adhanBanner != null && !audio.adhanPlaying && lastAdhanStart > 0 && n - lastAdhanStart > 5_000) {
            adhanBanner = null
        }
        if (!s.adhanOn.value) return
        val t = today ?: return
        val z = zone()
        for (i in intArrayOf(0, 2, 3, 4, 5)) {
            val ms = ZonedDateTime.of(t.date, t.t[i], z).toInstant().toEpochMilli()
            val diff = n - ms
            if (diff in 0..3_000) {
                val key = "${t.date}-$i"
                if (key != lastAdhanKey) {
                    lastAdhanKey = key
                    lastAdhanStart = n
                    adhanBanner = String.format(java.util.Locale.US, t("now_prayer"), t(prayerKeys[i]))
                    audio.playAdhan()
                }
            }
        }
    }

    fun stopAdhan() {
        audio.stopAdhan()
        adhanBanner = null
    }

    // ---------- location ----------
    @SuppressLint("MissingPermission")
    suspend fun locate(): Place? {
        val lm = app.getSystemService(LocationManager::class.java) ?: return null
        var loc: Location? = null
        try {
            for (p in lm.getProviders(true)) {
                val l = lm.getLastKnownLocation(p)
                if (l != null && (loc == null || l.time > loc.time)) loc = l
            }
            if (loc == null) {
                val provider = lm.getProviders(true).firstOrNull()
                if (provider != null) {
                    loc = withTimeoutOrNull(15_000) {
                        suspendCancellableCoroutine<Location?> { cont ->
                            val li = object : LocationListener {
                                override fun onLocationChanged(l: Location) {
                                    if (cont.isActive) cont.resume(l)
                                }

                                override fun onProviderEnabled(p: String) {}
                                override fun onProviderDisabled(p: String) {}

                                @Deprecated("Deprecated in Java")
                                override fun onStatusChanged(p: String?, status: Int, extras: Bundle?) {
                                }
                            }
                            @Suppress("DEPRECATION")
                            lm.requestSingleUpdate(provider, li, Looper.getMainLooper())
                            cont.invokeOnCancellation { lm.removeUpdates(li) }
                        }
                    }
                }
            }
        } catch (ex: SecurityException) {
            return null
        }
        val l = loc ?: return null
        return Geo.fromCoords(l.latitude, l.longitude, s.lang.value)
    }
}
