package com.example.keepalive.moudle.keepalive.job

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.keepalive.KeepAliveApplication
import com.example.keepalive.MainActivity
import com.example.keepalive.R
import com.example.keepalive.moudle.keepalive.Constants
import com.example.keepalive.moudle.keepalive.util.NotificationUtil
import com.example.keepalive.moudle.keepalive.util.Util


class KeepLiveService : Service() {

    val NOTIFICATION_ID = 0x10
    val NOTIFICATION_ID_O = 0x11

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("pwj", "live service --> onCreate()");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("pwj", "live service --> onStartCommand()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            try {
                stopForeground(true)
            } catch (e: java.lang.Exception) {
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //API 18以上，发送Notification并将其置为前台后，启动InnerService
            val builder = Notification.Builder(this)
            builder.setSmallIcon(R.mipmap.ic_launcher)
            startForeground(NOTIFICATION_ID, builder.build())
            try {
                startService(Intent(this, InnerService::class.java))
            } catch (e: java.lang.Exception) {
            }
        } else {
            //API 18以下，直接发送Notification并将其置为前台
            startForeground(NOTIFICATION_ID, Notification())
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("pwj", "live service --> onDestroy()");
        try {
            stopForeground(true)
        } catch (e: java.lang.Exception) {
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            start(KeepAliveApplication.app as Context)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun startForeground() {
        var notificationUtil = NotificationUtil(this)
        notificationUtil.createNotificationChannel(
            Constants.NOTIFICATION_APP_CHANNEL_ID,
            Constants.NOTIFICATION_APP_CHANNEL,
            NotificationManager.IMPORTANCE_LOW
        )
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_APP_CHANNEL_ID)
        builder.setContentTitle(resources.getString(R.string.app_is_running))
            .setContentText(resources.getString(R.string.tap_for_more))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
        val notification = builder.build()
        startForeground(NOTIFICATION_ID_O, notification)
    }

    companion object {
        fun start(context: Context) {
            val isRunning: Boolean = Util.isServiceRunning(context, KeepLiveService::class.java)
            Log.d("pwj", "live service isRunning = " + isRunning);
            if (!isRunning) {
                val intent = Intent(context, KeepLiveService::class.java)
                try {
                    context.startService(intent)
                } catch (e: java.lang.Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    }
                }
            }
        }
    }

    inner class InnerService : Service() {

        override fun onBind(p0: Intent?): IBinder? {
            TODO("Not yet implemented")
        }

        override fun onCreate() {
            super.onCreate()
            Log.d("pwj", "inner live service --> onCreate()");
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            Log.d("pwj", "inner live service --> onStartCommand()");
            try {
                var builder = Notification.Builder(this)
                builder.setSmallIcon(R.mipmap.ic_launcher)
                startForeground(NOTIFICATION_ID, builder.build())

                Handler().postDelayed(Runnable {
                    stopForeground(true)
                    var manager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (manager != null) manager.cancel(NOTIFICATION_ID)
                    stopSelf()
                }, 100)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return START_STICKY
        }

        override fun onDestroy() {
            super.onDestroy()
            Log.d("pwj", "inner live service --> onDestroy()");
        }
    }
}