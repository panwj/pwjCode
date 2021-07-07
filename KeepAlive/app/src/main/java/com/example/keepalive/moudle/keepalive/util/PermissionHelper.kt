package com.example.keepalive.moudle.keepalive.util

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.Size
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.app.AppOpsManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.lang.reflect.Method
import java.util.*

object PermissionHelper {
    private const val TAG = "PermissionHelper"
    const val OVERLAY_PERMISSION_REQ_CODE = 1000
    const val SETTINGS_REQUEST_CODE = 1992

    /**
     * Handle the result of a permission request, should be called from the calling [ ]'s [ActivityCompat.OnRequestPermissionsResultCallback.onRequestPermissionsResult] method.
     *
     *
     * If any permissions were granted or denied, the `object` will receive the appropriate
     * callbacks through [PermissionCallbacks] and methods will be run if appropriate.
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param receivers    an array of objects that implement [PermissionCallbacks].
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        vararg receivers: Any
    ) {
        // Make a collection of granted and denied permissions from the request.
        val granted: MutableList<String> = ArrayList()
        val denied: MutableList<String> = ArrayList()
        for (i in permissions.indices) {
            val perm = permissions[i]
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm)
            } else {
                denied.add(perm)
            }
        }

        // iterate through all receivers
        for (`object` in receivers) {
            // Report granted permissions, if any.
            if (!granted.isEmpty()) {
                if (`object` is PermissionCallbacks) {
                    `object`.onPermissionsGranted(requestCode, granted)
                }
            }

            // Report denied permissions, if any.
            if (!denied.isEmpty()) {
                if (`object` is PermissionCallbacks) {
                    `object`.onPermissionsDenied(requestCode, denied)
                }
            }
        }
    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     [Context].
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        return hasPermissions(context, Arrays.asList(*permissions))
    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     [Context].
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    fun hasPermissions(context: Context, permissions: List<String?>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission!!
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param activity          [Activity].
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    fun somePermissionPermanentlyDenied(
        activity: Activity,
        deniedPermissions: List<String?>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        if (deniedPermissions.size == 0) return false
        for (permission in deniedPermissions) {
            val rationale = activity.shouldShowRequestPermissionRationale(permission!!)
            if (!rationale) return true
        }
        return false
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param fragment          [Fragment].
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    fun somePermissionPermanentlyDenied(
        fragment: Fragment,
        deniedPermissions: List<String?>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        if (deniedPermissions.size == 0) return false
        for (permission in deniedPermissions) {
            val rationale = fragment.shouldShowRequestPermissionRationale(permission!!)
            if (!rationale) return true
        }
        return false
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param fragment          [android.app.Fragment].
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    fun somePermissionPermanentlyDenied(
        fragment: android.app.Fragment,
        deniedPermissions: List<String?>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        if (deniedPermissions.size == 0) return false
        for (permission in deniedPermissions) {
            val rationale = fragment.shouldShowRequestPermissionRationale(permission!!)
            if (!rationale) return true
        }
        return false
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param context           [Context].
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    fun somePermissionPermanentlyDenied(
        context: Context,
        deniedPermissions: List<String?>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        if (deniedPermissions.size == 0) return false
        if (context !is Activity) return false
        for (permission in deniedPermissions) {
            val rationale = context.shouldShowRequestPermissionRationale(permission!!)
            if (!rationale) return true
        }
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun hasOverlayInVivo(context: Context?): Boolean {
        val cursor: Cursor?
        requireNotNull(context) { "context is null" }
        cursor = try {
            context.contentResolver.query(
                Uri.parse("content://com.iqoo.secure.provider.secureprovider/allowfloatwindowapp"),
                null,
                "pkgname = ?",
                arrayOf(context.packageName),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (cursor == null) {
            return hasOverlayInVivo2(context)
        }
        cursor.columnNames
        if (cursor.moveToFirst()) {
            val i = cursor.getInt(cursor.getColumnIndex("currentlmode"))
            cursor.close()
            return i == 0
        }
        cursor.close()
        return hasOverlayInVivo2(context)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun hasOverlayInVivo2(context: Context?): Boolean {
        val cursor: Cursor?
        requireNotNull(context) { "context is null" }
        cursor = try {
            context.contentResolver.query(
                Uri.parse("content://com.vivo.permissionmanager.provider.permission/float_window_apps"),
                null,
                "pkgname = ?",
                arrayOf(context.packageName),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        if (cursor == null) {
            return Settings.canDrawOverlays(context)
        }
        cursor.columnNames
        if (cursor.moveToFirst()) {
            val i = cursor.getInt(cursor.getColumnIndex("currentlmode"))
            cursor.close()
            return i == 0
        }
        cursor.close()
        return Settings.canDrawOverlays(context)
    }

    fun canRequestPackageInstalls(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        var canRequest = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canRequest = context.packageManager.canRequestPackageInstalls()
        }
        return canRequest
    }

    const val GET_UNKNOWN_APP_SOURCES = 10001

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun openInstallUnknownApps(activity: Activity?) {
        if (activity == null) {
            return
        }
        //跳转到打开权限界面
        val packageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        activity.startActivityForResult(packageIntent, GET_UNKNOWN_APP_SOURCES)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun openInstallUnknownApps(fragment: Fragment?) {
        if (fragment == null) {
            return
        }
        //跳转到打开权限界面
        val packageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
        fragment.startActivityForResult(packageIntent, GET_UNKNOWN_APP_SOURCES)
    }

    /**
     * Callback interface to receive the results of `PermissionHelper.requestPermissions()`
     * calls.
     */
    interface PermissionCallbacks : OnRequestPermissionsResultCallback {
        fun onPermissionsGranted(requestCode: Int, perms: List<String>)
        fun onPermissionsDenied(requestCode: Int, perms: List<String>)
    }
}