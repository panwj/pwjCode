package com.mvcdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import com.mvcdemo.common.util.MMKVUtil;
import com.mvcdemo.common.util.VersionChecker;
import com.mvcdemo.data.db.AppDatabase;
import com.mvcdemo.network.base.CommonHttpManager;


public class MvcApplication extends MultiDexApplication implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    private static MvcApplication mvcApplication;
    private boolean isAppForeground;
    private AppDatabase appDB;

    public static MvcApplication getMvcApplication() {
        return mvcApplication;
    }

    public AppDatabase getAppDB(){
        return appDB;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mvcApplication = this;
        appDB = Room.databaseBuilder(this, AppDatabase.class,"mvc_db")
                .addMigrations()
                .build();
        MMKVUtil.getInstance().mmkvInit(this);
        VersionChecker.updateVersionChecker(this);
        CommonHttpManager.initialize(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        isAppForeground = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        isAppForeground = true;
    }

    public boolean isAppForeground() {
        return isAppForeground;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
