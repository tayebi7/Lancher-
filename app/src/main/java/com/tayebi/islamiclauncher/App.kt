package com.tayebi.islamiclauncher

import android.app.Application
import java.io.File

class App : Application() {
    lateinit var engine: Engine
        private set

    override fun onCreate() {
        super.onCreate()
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                File(filesDir, "crash.txt").writeText(android.util.Log.getStackTraceString(e))
            } catch (_: Throwable) {
            }
            prev?.uncaughtException(t, e)
        }
        try {
            engine = Engine(this)
        } catch (e: Throwable) {
            File(filesDir, "crash.txt").writeText("Engine init failed:\n" + android.util.Log.getStackTraceString(e))
            throw e
        }
    }
}
