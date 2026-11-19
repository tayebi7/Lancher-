package com.tayebi.islamiclauncher

import android.app.Application

class App : Application() {
    lateinit var engine: Engine
        private set

    override fun onCreate() {
        super.onCreate()
        engine = Engine(this)
    }
}
