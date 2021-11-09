package com.file.share.util;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DeviceUtil {

    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getResolution(Context context) {
        return getScreenHeight(context) + "x" + getScreenWidth(context);
    }

    public static int getScreenWidth(Context context) {
        int mScreenWidth = 0;
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
            mScreenWidth = dm.widthPixels;
        }
        return mScreenWidth;
    }

    public static int getScreenHeight(Context context) {
        int mScreenHeight = 0;
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                wm.getDefaultDisplay().getRealMetrics(dm);
            } else {
                wm.getDefaultDisplay().getMetrics(dm);
            }
            mScreenHeight = dm.heightPixels;
        }
        return mScreenHeight;
    }

    public static boolean hasMultitouch(Context context) {
        try {
            return context.getPackageManager().hasSystemFeature("android.hardware.touchscreen.multitouch");
        } catch (Exception e) {
            return false;
        }
    }
}
