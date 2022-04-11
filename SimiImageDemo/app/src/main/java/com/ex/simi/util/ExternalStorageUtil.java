package com.ex.simi.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.fragment.app.Fragment;

public class ExternalStorageUtil {

    public static final int MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE = 1001;

    // Checks if a volume containing external storage is available
    // for read and write.
    public static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
    }

    // Checks if a volume containing external storage is available to at least read.
    public static boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ||
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY;
    }

    public static boolean isExternalStorageManager() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager();
    }

    public static void requestStorageExternalManagerPermission(Activity activity, int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void requestStorageExternalManagerPermission(Fragment fragment, int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 非activity or fragment 申请 onActivityResult（）不回调
     */
    public static void requestStorageExternalManagerPermission(Context context, int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void getStorageAvailableBytes(Context context) {
        /*// App needs 10 MB within internal storage.
        private static final long NUM_BYTES_NEEDED_FOR_MY_APP = 1024 * 1024 * 10L;

        StorageManager storageManager =
                getApplicationContext().getSystemService(StorageManager.class);
        UUID appSpecificInternalDirUuid = storageManager.getUuidForPath(getFilesDir());
        long availableBytes =
                storageManager.getAllocatableBytes(appSpecificInternalDirUuid);
        if (availableBytes >= NUM_BYTES_NEEDED_FOR_MY_APP) {
            storageManager.allocateBytes(
                    appSpecificInternalDirUuid, NUM_BYTES_NEEDED_FOR_MY_APP);
        } else {
            Intent storageIntent = new Intent();
            storageIntent.setAction(ACTION_MANAGE_STORAGE);
            // Display prompt to user, requesting that they choose files to remove.
        }*/
    }
}