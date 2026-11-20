package com.tayebi.islamiclauncher

import android.app.Application
import android.content.Intent
import android.os.Process
import kotlin.system.exitProcess

class App : Application() {
    /** Created lazily by MainActivity so a failure shows an error screen instead of a silent crash. */
    val engine: Engine by lazy { Engine(this) }

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                CrashActivity.start(this, android.util.Log.getStackTraceString(e))
            } catch (_: Throwable) {
            }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }
}
