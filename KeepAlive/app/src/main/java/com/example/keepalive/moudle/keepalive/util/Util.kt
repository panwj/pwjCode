package com.example.keepalive.moudle.keepalive.util

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Process
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.reflect.Method
import java.util.*

/**
 * Created by shewenbiao on 18-4-10.
 */
object Util {
    fun getCountryName(context: Context): String? {
        var countryName: String? = ""
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (telephonyManager != null) {
            val localeCountry = telephonyManager.networkCountryIso
            if (localeCountry != null) {
                val locale = Locale("", localeCountry)
                countryName = locale.displayCountry
            }
        }
        if (TextUtils.isEmpty(countryName)) {
            countryName = Locale.getDefault().displayCountry
        }
        return countryName
    }

    fun showInput(context: Context, view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm != null) {
                    view.requestFocus()
                    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
                }
            }
        })
    }

    fun hideInput(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     *
     * 收起通知栏
     * @param context
     */
    fun collapseStatusBar(context: Context) {
        try {
            val statusBarManager = context.getSystemService("statusbar") ?: return
            val collapse: Method
            collapse = if (Build.VERSION.SDK_INT <= 16) {
                statusBarManager.javaClass.getMethod("collapse")
            } else {
                statusBarManager.javaClass.getMethod("collapsePanels")
            }
            collapse.invoke(statusBarManager)
        } catch (localException: Exception) {
            localException.printStackTrace()
        }
    }

    fun isServiceRunning(context: Context, cls: Class<*>?): Boolean {
        val componentName = ComponentName(context, cls!!)
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var isRunning = false
        var runningServiceInfoList: List<ActivityManager.RunningServiceInfo>? = null
        if (activityManager != null) {
            runningServiceInfoList = activityManager.getRunningServices(Int.MAX_VALUE)
        }
        if (runningServiceInfoList == null) {
            return false
        }
        for (info in runningServiceInfoList) {
            if (info.service == componentName) {
                if (info.pid == Process.myPid()) {
                    isRunning = true
                }
            }
        }
        return isRunning
    }

    /**
     * get current process name
     * @param context
     * @return
     */
    fun getCurrentProcessName(context: Context): String {
        val runningAppProcesses: List<RunningAppProcessInfo>
        try {
            val bufferedReader = BufferedReader(FileReader(File("/proc/" + Process.myPid() + "/cmdline")))
            val trim = bufferedReader.readLine().trim { it <= ' ' }
            bufferedReader.close()
            return trim
        } catch (e: Exception) {
            e.printStackTrace()
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    ?: return ""
            runningAppProcesses = activityManager.runningAppProcesses
            if (runningAppProcesses == null) {
                return ""
            }
            val myPid = Process.myPid()
            for (runningAppProcessInfo in runningAppProcesses) {
                if (runningAppProcessInfo.pid == myPid) {
                    return runningAppProcessInfo.processName
                }
            }
        }
        return ""
    }

    fun isActivityDestroyed(activity: Activity?): Boolean {
        return activity == null || activity.isFinishing ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed
    }

    fun setWebViewPath(context: Context) {
        //java.lang.RuntimeException: Using WebView from more than one process at once with the same data directory is not supported
        //android 9.0开始不支持同时使用多个进程中具有相同数据目录的WebView, 针对这个问题，谷歌也给出了解决方案：在初始化的时候，需要为其它进程webView设置目录
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getCurrentProcessName(context)
            if (context.packageName != processName) { //判断不等于默认进程名称
                WebView.setDataDirectorySuffix(processName)
            }
        }
    }
}