package com.example.keepalive.moudle.keepalive.util

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build.VERSION_CODES

class NotificationUtil(context: Context) : ContextWrapper(context) {
    private var mNotificationManager: NotificationManager? = null
    private val mContext: Context

    @TargetApi(VERSION_CODES.O)
    fun createNotificationChannel(channelId: String?, channelName: String?, importance: Int) {
        val channel = NotificationChannel(channelId, channelName, importance)
        notificationManager!!.createNotificationChannel(channel)
    }

    val notificationManager: NotificationManager?
        get() {
            if (mNotificationManager == null) {
                mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return mNotificationManager
        }

    companion object {
        private const val TAG = "NotificationUtil"
    }

    init {
        mContext = context.applicationContext
    }
}