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
import com.example.keepalive.BuildConfig
import java.lang.reflect.Method
import java.util.*

object PermissionHelper {
    private const val TAG = "PermissionHelper"
    const val OVERLAY_PERMISSION_REQ_CODE = 1000
    const val SETTINGS_REQUEST_CODE = 1992

    /**
     * Request a set of permissions, showing a rationale if the system requests it.
     *
     * @param host        requesting context.
     * @param requestCode request code to track this request, must be &lt; 256.
     * @param perms       a set of permissions to be requested.
     * @see Manifest.permission
     */
    fun requestPermissions(
        host: Activity, requestCode: Int, @Size(min = 1) vararg perms: String
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Should never be requesting permissions on API < 23!")
        } else {
            ActivityCompat.requestPermissions(host, perms, requestCode)
        }
    }

    /**
     * Request permissions from a Support Fragment with standard OK/Cancel buttons.
     *
     * @see .requestPermissions
     */
    fun requestPermissions(
        host: Fragment, requestCode: Int, @Size(min = 1) vararg perms: String
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Should never be requesting permissions on API < 23!")
        } else {
            host.requestPermissions(perms, requestCode)
        }
    }

    /**
     * Request permissions from a standard Fragment with standard OK/Cancel buttons.
     *
     * @see .requestPermissions
     */
    fun requestPermissions(
        host: android.app.Fragment, requestCode: Int, @Size(min = 1) vararg perms: String
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Should never be requesting permissions on API < 23!")
        } else {
            host.requestPermissions(perms, requestCode)
        }
    }

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

    fun hasPermission3(context: Context?, vararg permissions: String?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        for (permission in permissions) {
            val op = AppOpsManagerCompat.permissionToOp(permission!!)
            if (TextUtils.isEmpty(op)) continue
            var result =
                AppOpsManagerCompat.noteProxyOp(context!!, op!!, BuildConfig.APPLICATION_ID)
            if (result == AppOpsManagerCompat.MODE_IGNORED) return false
            result = ContextCompat.checkSelfPermission(context, permission)
            if (result != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
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

    fun openNotify(context: Context) {
        // TODO Auto-generated method stub
        val currentApiVersion = Build.VERSION.SDK_INT
        try {
            val service = context.getSystemService("statusbar")
            val statusbarManager = Class
                .forName("android.app.StatusBarManager")
            var expand: Method? = null
            if (service != null) {
                expand = if (currentApiVersion <= 16) {
                    statusbarManager.getMethod("expand")
                } else {
                    statusbarManager
                        .getMethod("expandNotificationsPanel")
                }
                expand.isAccessible = true
                expand.invoke(service)
            }
        } catch (e: Exception) {
            Log.d("sss", "openNotify e = $e")
        }
    }

    /**
     * 判断 悬浮窗口权限是否打开
     * 由于android未提供直接跳转到悬浮窗设置页的api，此方法使用反射去查找相关函数进行跳转
     * 部分第三方ROM可能不适用
     *
     * @param context
     * @return true 允许  false禁止
     */
    fun isAppOps(context: Context): Boolean {
        try {
            val `object` = context.getSystemService(Context.APP_OPS_SERVICE) ?: return false
            val localClass: Class<*> = `object`.javaClass
            val arrayOfClass: Array<Class<*>?> = arrayOfNulls(3)
            arrayOfClass.set(0, Integer.TYPE)
            arrayOfClass.set(1, Integer.TYPE)
            arrayOfClass.set(2, String::class.java)

            val method = localClass.getMethod("checkOp", *arrayOfClass) ?: return false
            val arrayOfObject1 = arrayOfNulls<Any>(3)
            arrayOfObject1[0] = Integer.valueOf(24)
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid())
            arrayOfObject1[2] = BuildConfig.APPLICATION_ID
            val m = (method.invoke(`object`, *arrayOfObject1) as Int).toInt()
            return m == AppOpsManager.MODE_ALLOWED
        } catch (ex: Exception) {
        }
        return false
    }

    /**
     * 打开应用详情页
     *
     * @param activity
     * @return true if it's open success.
     */
    fun openAppSettings(activity: Activity): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            intent.data = uri
            activity.startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 打开应用详情页
     *
     * @param fragment
     * @return true if it's open success.
     */
    fun openAppSettings(fragment: android.app.Fragment): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            intent.data = uri
            fragment.startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 打开应用详情页
     *
     * @param fragment
     * @return true if it's open success.
     */
    fun openAppSettings(fragment: Fragment): Boolean {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            intent.data = uri
            fragment.startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 打开悬浮窗设置页
     * 部分第三方ROM无法直接跳转可使用[.openAppSettings]跳到应用详情页
     *
     * @param context
     * @return true if it's open successful.
     */
    fun openOpsSettings(context: Activity) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            context.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            openAppSettings(context)
        }
    }

    /**
     * Drop notification bar
     *
     * @param context
     */
    fun collapseStatusBar(context: Context) {
        try {
            val statusBarManager = context.getSystemService("statusbar")
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

    /**
     * haspermission2  ex:xiaomi
     *
     * @param context
     * @return
     */
    fun hasPermission2(context: Context, vararg permissions: String?): Boolean {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            //int outgoingOp = appOpsManager.checkOp(AppOpsManager.OPSTR_READ_CELL_BROADCASTS, Binder.getCallingUid(), context.getPackageName());
            //Log.d("sss", "outgoingOp = "  outgoingOp);
            //if (outgoingOp != AppOpsManager.MODE_ALLOWED) {
            //    return false;
            //}
            for (permission in permissions) {
                val op = convertPermissionToOP(permission)
                if (TextUtils.isEmpty(op)) continue
                val phoneStateOp = appOpsManager.checkOp(
                    op,
                    Binder.getCallingUid(), BuildConfig.APPLICATION_ID
                )
                if (phoneStateOp != AppOpsManager.MODE_ALLOWED) return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("sss", "exception = $e")
        }
        return true
    }

    fun convertPermissionToOP(permission: String?): String {
        return when (permission) {
            Manifest.permission.CALL_PHONE -> AppOpsManager.OPSTR_CALL_PHONE
            Manifest.permission.READ_PHONE_STATE -> AppOpsManager.OPSTR_READ_PHONE_STATE
            Manifest.permission.SEND_SMS -> AppOpsManager.OPSTR_SEND_SMS
            Manifest.permission.RECEIVE_SMS -> AppOpsManager.OPSTR_RECEIVE_SMS
            Manifest.permission.READ_SMS -> AppOpsManager.OPSTR_READ_SMS
            Manifest.permission.RECEIVE_WAP_PUSH -> AppOpsManager.OPSTR_RECEIVE_WAP_PUSH
            Manifest.permission.RECEIVE_MMS -> AppOpsManager.OPSTR_RECEIVE_MMS
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE
            Manifest.permission.READ_EXTERNAL_STORAGE -> AppOpsManager.OPSTR_READ_EXTERNAL_STORAGE
            Manifest.permission.READ_CONTACTS -> AppOpsManager.OPSTR_READ_CONTACTS
            Manifest.permission.WRITE_CONTACTS -> AppOpsManager.OPSTR_WRITE_CONTACTS
            Manifest.permission.ACCESS_FINE_LOCATION -> AppOpsManager.OPSTR_FINE_LOCATION
            Manifest.permission.ACCESS_COARSE_LOCATION -> AppOpsManager.OPSTR_COARSE_LOCATION
            else -> ""
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        val canDrawOverlays: Boolean
        canDrawOverlays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!"vivo".equalsIgnoreCase(Build.BRAND)) {
            Settings.canDrawOverlays(context)
            //            } else {
//                canDrawOverlays = hasOverlayInVivo(context);
//            }
        } else {
            hasOverlayBellow23(context)
        }
        return canDrawOverlays
    }

    private fun hasOverlayBellow23(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            true
        } else try {
            var checkOp = 0
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val systemService = context.getSystemService("appops") ?: return true
                checkOp = systemService.javaClass
                    .getMethod("checkOp", *arrayOf(Integer.TYPE, Integer.TYPE, String::class.java))
                    .invoke(
                        systemService,
                        *arrayOf<Any>(24, Binder.getCallingUid(), BuildConfig.APPLICATION_ID)
                    ) as Int
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                val appOpsManager =
                    context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                if (appOpsManager != null) {
                    checkOp = appOpsManager.javaClass
                        .getMethod(
                            "checkOp",
                            *arrayOf(Integer.TYPE, Integer.TYPE, String::class.java)
                        )
                        .invoke(
                            appOpsManager,
                            *arrayOf(24, Binder.getCallingUid(), context.packageName)
                        ) as Int
                }
            } else {
                val appOpsManager =
                    context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                if (appOpsManager != null) {
                    checkOp = appOpsManager.checkOp(
                        "android:system_alert_window",
                        Binder.getCallingUid(),
                        context.packageName
                    )
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) checkOp == AppOpsManager.MODE_ALLOWED else checkOp == 0
        } catch (e: Exception) {
            true
        }
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