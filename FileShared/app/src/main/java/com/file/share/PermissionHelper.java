package com.file.share;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionHelper {

    public static final int MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE = 1001;
    public static final int NORMAL_PERMISSION_CODE = 1002;

    public static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
    private static final String[] STORAGE_PERMISSIONS =
            new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

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
            Log.e("demo", "Should never be requesting permissions on API < 23!");
        } else {
            ActivityCompat.requestPermissions(host, perms, requestCode);
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

    public static boolean hasStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return hasPermissions(context, STORAGE_PERMISSIONS);
        }
        return isExternalStorageManager();
    }

    private static boolean isExternalStorageManager() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager();
    }

    public static void requestStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            requestPermissions(activity, MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE, STORAGE_PERMISSIONS);
            return;
        }
        requestStorageExternalManagerPermission(activity, MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE);
    }

    private static void requestStorageExternalManagerPermission(Context context, int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
