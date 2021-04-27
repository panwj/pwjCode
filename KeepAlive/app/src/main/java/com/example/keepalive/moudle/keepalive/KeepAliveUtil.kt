package com.example.keepalive.moudle.keepalive

import android.content.Context
import android.os.Build
import com.example.keepalive.moudle.keepalive.job.KeepLiveJobService
import com.example.keepalive.moudle.keepalive.job.KeepLiveService

class KeepAliveUtil {

    companion object {
        fun keepAlive(context: Context) {
            KeepLiveService.start(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                KeepLiveJobService.start(context)
            }
        }
    }
}