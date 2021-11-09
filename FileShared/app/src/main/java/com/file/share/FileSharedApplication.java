package com.file.share;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class FileSharedApplication extends MultiDexApplication {

    private static FileSharedApplication mInstance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        mInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Application getApplication() {
        return mInstance;
    }
}
