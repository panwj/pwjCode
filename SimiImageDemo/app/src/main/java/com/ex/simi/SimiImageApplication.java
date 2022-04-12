package com.ex.simi;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.ex.simi.util.Logv;

import org.opencv.android.OpenCVLoader;

public class SimiImageApplication extends Application {

    static {
        System.loadLibrary("opencv_java4");//加载OpenCV动态库
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (OpenCVLoader.initDebug()) {
            Logv.e("init OpenCV Success!!!");
        } else {
            Logv.e("init OpenCV Failed!!!");
        }
    }
}
