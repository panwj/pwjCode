package com.ex.simi;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.ex.simi.util.Logv;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class SimiImageApplication extends Application {

    private static SimiImageApplication mInstance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private LoaderCallbackInterface mLoaderCallback = new LoaderCallbackInterface() {
        @Override
        public void onManagerConnected(int status) {
            Logv.e("init OpenCV onManagerConnected() : " + status);
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Logv.e("init OpenCV --> SUCCESS");
                    break;
                case LoaderCallbackInterface.INIT_FAILED:
                    Logv.e("init OpenCV --> INIT_FAILED");
                    break;
                case LoaderCallbackInterface.MARKET_ERROR:
                    Logv.e("init OpenCV --> MARKET_ERROR");
                    break;
                case LoaderCallbackInterface.INSTALL_CANCELED:
                    Logv.e("init OpenCV --> INSTALL_CANCELED");
                    break;
                case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION:
                    Logv.e("init OpenCV --> INCOMPATIBLE_MANAGER_VERSION");
                    break;
            }
        }

        @Override
        public void onPackageInstall(int operation, InstallCallbackInterface callback) {
            Logv.e("init OpenCV onPackageInstall() : " + operation + "  " + callback.getPackageName());
        }
    };

    public static SimiImageApplication getApp() {
        return mInstance;
    }
}
