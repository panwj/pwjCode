package com.example.keepalive

import android.app.Application
import com.example.keepalive.moudle.keepalive.KeepAliveUtil

class KeepAliveApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        app = this
        KeepAliveUtil.keepAlive(this)
    }

    init {
    }

    companion object {
        var app: KeepAliveApplication? = null
            private set
    }
}