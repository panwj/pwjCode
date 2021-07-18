package com.mvcdemo.common.util;

import android.util.Log;

import com.mvcdemo.BuildConfig;


public class Logger {

    private static final String TAG = "MCVDemo";
    private static final boolean open = BuildConfig.DEBUG;

    public static void e(String string) {
        if (open) Log.e(TAG, string);
    }

    public static void d(String string) {
        if (open) Log.d(TAG, string);
    }

    public static void e(String tag, String string) {
        if (open) Log.e(tag, string);
    }

    public static void d(String tag, String string) {
        if (open) Log.d(tag, string);
    }
}
