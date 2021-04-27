package com.example.keepalive.moudle.keepalive.job

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.keepalive.moudle.keepalive.Constants
import com.example.keepalive.moudle.keepalive.util.PermissionHelper

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class KeepLiveJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        Log.e("pwj", "job service --> onStartJob()")
        KeepLiveService.start(this)
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.e("pwj", "job service --> onStopJob()")
        return false
    }

    companion object {
        fun start(context: Context) {
            try {
                val builder = JobInfo.Builder(
                    10,
                    ComponentName(
                        context.packageName,
                        KeepLiveJobService::class.java.name
                    )
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //7.0以上最小间隔是15分钟
                    builder.setPeriodic(Constants.ONE_HOUR)
                    //                                    setPeriodic：设置时间间隔，单位毫秒。该方法不能和
//                                    setMinimumLatency、setOverrideDeadline这两个同时调用，
//                                    否则会报错“java.lang.IllegalArgumentException:
//                                    Can't call setMinimumLatency() on a periodic job”，
//                                    或者报错“java.lang.IllegalArgumentException:
//                                    Can't call setOverrideDeadline() on a periodic job”。
                } else {
                    //每隔1分钟执行一次job
                    builder.setPeriodic(Constants.FIVE_MINUTE)
                }
                if (PermissionHelper.hasPermissions(
                        context,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED
                    )
                ) else {
//                                    setPersisted：重启后是否还要继续执行，此时需要声明权限RECEIVE_BOOT_COMPLETED，
//                                    否则会报错“java.lang.IllegalArgumentException:
//                                    Error: requested job be persisted without holding RECEIVE_BOOT_COMPLETED permission.”
//                                    而且RECEIVE_BOOT_COMPLETED需要在安装的时候就要声明，如果一开始没声明，
//                                    而在升级时才声明，那么依然会报权限不足的错误。
                    builder.setPersisted(true)
                }
                val jobScheduler =
                    context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                jobScheduler?.schedule(builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}