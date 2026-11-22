package com.tayebi.islamiclauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tayebi.islamiclauncher.ui.LauncherApp

class MainActivity : ComponentActivity() {
    private lateinit var engine: Engine
    private var locCb: ((Boolean) -> Unit)? = null
    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        locCb?.invoke(it)
        locCb = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            engine = (application as App).engine
        } catch (t: Throwable) {
            CrashActivity.start(this, "Engine init failed:\n" + android.util.Log.getStackTraceString(t))
            finish()
            return
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideBars()
        intent.getStringExtra("screen")?.let { n -> Screen.values().firstOrNull { it.name.equals(n, true) }?.let { engine.go(it) } }
        setContent { LauncherApp(engine, this) }
    }

    override fun onResume() {
        super.onResume()
        if (::engine.isInitialized) hideBars()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Pressing HOME on the remote returns to the launcher home screen.
        if (::engine.isInitialized && intent.hasCategory(Intent.CATEGORY_HOME)) engine.go(Screen.Home)
        if (::engine.isInitialized) intent.getStringExtra("screen")?.let { n -> Screen.values().firstOrNull { it.name.equals(n, true) }?.let { engine.go(it) } }
    }

    private fun hideBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun ensureLocationPermission(cb: (Boolean) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            cb(true)
        } else {
            locCb = cb
            permLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
}
