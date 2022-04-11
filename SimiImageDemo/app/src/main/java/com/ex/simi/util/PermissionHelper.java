package com.ex.simi.util;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.ex.simi.BuildConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionHelper {

    private static final String TAG = "PermissionHelper";

    public static final int OVERLAY_PERMISSION_REQ_CODE = 1000;
    public static final int SETTINGS_REQUEST_CODE = 1992;
    public static final int RC_STORAGE = 1001;
    public static final String[] PERM_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * Callback interface to receive the results of {@code PermissionHelper.requestPermissions()}
     * calls.
     */
    public interface PermissionCallbacks extends ActivityCompat.OnRequestPermissionsResultCallback {

        void onPermissionsGranted(int requestCode, @NonNull List<String> perms);

        void onPermissionsDenied(int requestCode, @NonNull List<String> perms);
    }

    /**
     * Request a set of permissions, showing a rationale if the system requests it.
     *
     * @param host        requesting context.
     * @param requestCode request code to track this request, must be &lt; 256.
     * @param perms       a set of permissions to be requested.
     * @see Manifest.permission
     */
    public static void requestPermissions(
            @NonNull Activity host, int requestCode, @Size(min = 1) @NonNull String... perms) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Should never be requesting permissions on API < 23!");
        } else {
            ActivityCompat.requestPermissions(host, perms, requestCode);
        }
    }

    /**
     * Request permissions from a Support Fragment with standard OK/Cancel buttons.
     *
     * @see #requestPermissions(Activity, int, String...)
     */
    public static void requestPermissions(
            @NonNull Fragment host, int requestCode, @Size(min = 1) @NonNull String... perms) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Should never be requesting permissions on API < 23!");
        } else {
            host.requestPermissions(perms, requestCode);
        }
    }

    /**
     * Request permissions from a standard Fragment with standard OK/Cancel buttons.
     *
     * @see #requestPermissions(Activity, int, String...)
     */
    public static void requestPermissions(
            @NonNull android.app.Fragment host, int requestCode, @Size(min = 1) @NonNull String... perms) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Should never be requesting permissions on API < 23!");
        } else {
            host.requestPermissions(perms, requestCode);
        }
    }

    /**
     * Handle the result of a permission request, should be called from the calling {@link
     * Activity}'s {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int,
     * String[], int[])} method.
     * <p>
     * If any permissions were granted or denied, the {@code object} will receive the appropriate
     * callbacks through {@link PermissionCallbacks} and methods will be run if appropriate.
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @param receivers    an array of objects that implement {@link PermissionCallbacks}.
     */
    public static void onRequestPermissionsResult(int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grantResults,
                                                  @NonNull Object... receivers) {
        // Make a collection of granted and denied permissions from the request.
        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        // iterate through all receivers
        for (Object object : receivers) {
            // Report granted permissions, if any.
            if (!granted.isEmpty()) {
                if (object instanceof PermissionCallbacks) {
                    ((PermissionCallbacks) object).onPermissionsGranted(requestCode, granted);
                }
            }

            // Report denied permissions, if any.
            if (!denied.isEmpty()) {
                if (object instanceof PermissionCallbacks) {
                    ((PermissionCallbacks) object).onPermissionsDenied(requestCode, denied);
                }
            }
        }
    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasPermissions(@NonNull Context context, @NonNull String... permissions) {
        return hasPermissions(context, Arrays.asList(permissions));
    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context     {@link Context}.
     * @param permissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean hasPermissions(@NonNull Context context, @NonNull List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param activity          {@link Activity}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull Activity activity, @NonNull List<String> deniedPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;

        if (deniedPermissions.size() == 0) return false;

        for (String permission : deniedPermissions) {
            boolean rationale = activity.shouldShowRequestPermissionRationale(permission);
            if (!rationale) return true;
        }
        return false;
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param fragment          {@link Fragment}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean somePermissionPermanentlyDenied(
            @NonNull Fragment fragment,
            @NonNull List<String> deniedPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;

        if (deniedPermissions.size() == 0) return false;

        for (String permission : deniedPermissions) {
            boolean rationale = fragment.shouldShowRequestPermissionRationale(permission);
            if (!rationale) return true;
        }
        return false;
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param fragment          {@link android.app.Fragment}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean somePermissionPermanentlyDenied(
            @NonNull android.app.Fragment fragment,
            @NonNull List<String> deniedPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;

        if (deniedPermissions.size() == 0) return false;

        for (String permission : deniedPermissions) {
            boolean rationale = fragment.shouldShowRequestPermissionRationale(permission);
            if (!rationale) return true;
        }
        return false;
    }

    /**
     * Some privileges permanently disabled, may need to set up in the execute.
     *
     * @param context           {@link Context}.
     * @param deniedPermissions one or more permissions.
     * @return true, other wise is false.
     */
    public static boolean somePermissionPermanentlyDenied(
            @NonNull Context context,
            @NonNull List<String> deniedPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false;

        if (deniedPermissions.size() == 0) return false;

        if (!(context instanceof Activity)) return false;

        for (String permission : deniedPermissions) {
            boolean rationale = ((Activity) context).shouldShowRequestPermissionRationale(permission);
            if (!rationale) return true;
        }
        return false;
    }

    /**
     * 判断 悬浮窗口权限是否打开
     * 由于android未提供直接跳转到悬浮窗设置页的api，此方法使用反射去查找相关函数进行跳转
     * 部分第三方ROM可能不适用
     *
     * @param context
     * @return true 允许  false禁止
     */
    public static boolean isAppOps(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = Integer.valueOf(24);
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = BuildConfig.APPLICATION_ID;
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

    /**
     * 打开应用详情页
     *
     * @param activity
     * @return true if it's open success.
     */
    public static boolean openAppSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            intent.setData(uri);
            activity.startActivityForResult(intent, SETTINGS_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 打开应用详情页
     *
     * @param fragment
     * @return true if it's open success.
     */
    public static boolean openAppSettings(android.app.Fragment fragment) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            intent.setData(uri);
            fragment.startActivityForResult(intent, SETTINGS_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 打开应用详情页
     *
     * @param fragment
     * @return true if it's open success.
     */
    public static boolean openAppSettings(Fragment fragment) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            intent.setData(uri);
            fragment.startActivityForResult(intent, SETTINGS_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 打开悬浮窗设置页
     * 部分第三方ROM无法直接跳转可使用{@link #openAppSettings(Activity)}跳到应用详情页
     *
     * @param context
     * @return true if it's open successful.
     */
    public static void openOpsSettings(Activity context) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            context.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            openAppSettings(context);
        }
    }

    /**
     * haspermission2  ex:xiaomi
     *
     * @param context
     * @return
     */
    public static boolean hasPermission2(Context context, String... permissions) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            //int outgoingOp = appOpsManager.checkOp(AppOpsManager.OPSTR_READ_CELL_BROADCASTS, Binder.getCallingUid(), context.getPackageName());
            //Log.d("sss", "outgoingOp = "  outgoingOp);
            //if (outgoingOp != AppOpsManager.MODE_ALLOWED) {
            //    return false;
            //}
            for (String permission : permissions) {
                String op = convertPermissionToOP(permission);
                if (TextUtils.isEmpty(op)) continue;
                int phoneStateOp = appOpsManager.checkOp(op,
                        Binder.getCallingUid(), BuildConfig.APPLICATION_ID);
                if (phoneStateOp != AppOpsManager.MODE_ALLOWED) return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("sss", "exception = " + e.toString());
        }
        return true;
    }

    public static String convertPermissionToOP(String permission) {
        switch (permission) {
            case Manifest.permission.CALL_PHONE:
                return AppOpsManager.OPSTR_CALL_PHONE;
            case Manifest.permission.READ_PHONE_STATE:
                return AppOpsManager.OPSTR_READ_PHONE_STATE;
            case Manifest.permission.SEND_SMS:
                return AppOpsManager.OPSTR_SEND_SMS;
            case Manifest.permission.RECEIVE_SMS:
                return AppOpsManager.OPSTR_RECEIVE_SMS;
            case Manifest.permission.READ_SMS:
                return AppOpsManager.OPSTR_READ_SMS;
            case Manifest.permission.RECEIVE_WAP_PUSH:
                return AppOpsManager.OPSTR_RECEIVE_WAP_PUSH;
            case Manifest.permission.RECEIVE_MMS:
                return AppOpsManager.OPSTR_RECEIVE_MMS;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return AppOpsManager.OPSTR_READ_EXTERNAL_STORAGE;
            case Manifest.permission.READ_CONTACTS:
                return AppOpsManager.OPSTR_READ_CONTACTS;
            case Manifest.permission.WRITE_CONTACTS:
                return AppOpsManager.OPSTR_WRITE_CONTACTS;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return AppOpsManager.OPSTR_FINE_LOCATION;
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return AppOpsManager.OPSTR_COARSE_LOCATION;
            default:
                return "";
        }
    }

    /**
     * 适配 target 30,判断是否有存储权限
     */
    public static boolean hasStoragePermissions(Context context) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ExternalStorageUtil.isExternalStorageManager())
                || (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && PermissionHelper.hasPermissions(context, PERM_STORAGE));
    }

    /**
     * 适配 target 30,在activity中申请storage权限
     *
     * @param activity 发起activity
     */
    public static void requestStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ExternalStorageUtil.requestStorageExternalManagerPermission(activity, ExternalStorageUtil.MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE);
        } else {
            PermissionHelper.requestPermissions(activity, RC_STORAGE, PERM_STORAGE);
        }
    }

    /**
     * 适配 target 30,在fragment中申请storage权限
     *
     * @param fragment 发起fragment
     */
    public static void requestStoragePermissions(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ExternalStorageUtil.requestStorageExternalManagerPermission(fragment, ExternalStorageUtil.MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE);
        } else {
            PermissionHelper.requestPermissions(fragment, RC_STORAGE, PERM_STORAGE);
        }
    }
}
