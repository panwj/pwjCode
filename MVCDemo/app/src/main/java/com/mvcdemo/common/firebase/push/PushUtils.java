package com.mvcdemo.common.firebase.push;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;


/**
 * Created by panwenjuan on 16-11-18.
 */
public class PushUtils {

    public static int getPackageVersion(Context context, String packageName) {
        int versionCode = 0;
        if (packageName == null) {
            return versionCode;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pi = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            versionCode = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static boolean isInstalled(Context context, String packagename) {
        boolean isInstall = false;
        if (packagename == null) {
            return isInstall;
        }
        PackageManager packageManager = context.getPackageManager();
        String packageName = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packagename, PackageManager.GET_UNINSTALLED_PACKAGES);
            packageName = applicationInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
            packageName = null;
        }
        if (!TextUtils.isEmpty(packageName)) {
            isInstall = true;
        }
        return isInstall;
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void launcherPushPage(Context context, PushData pushData) {

        if (TextUtils.isEmpty(pushData.mPackageName) || TextUtils.isEmpty(pushData.mUpdateLink)) {
            return;
        }

        if (TextUtils.isEmpty(pushData.mVersionCode) || pushData.mVersionCode == "") {
            pushData.mVersionCode = "0";
        }

        if (TextUtils.equals(PushData.CUR_APP_PACKAGE_NAME, pushData.mPackageName)) {
            //for update to new version app
            int curVersionCode = getPackageVersion(context, pushData.mPackageName);
            int pushCode = Integer.parseInt(pushData.mVersionCode);
            if (pushCode > curVersionCode) {
                Intent intent = new Intent();
                intent.setClass(context, PushWindowActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PushData.PUSH_DATA, pushData);
                context.startActivity(intent);
            }
        } else {
            //for recommend app
            if (!isInstalled(context, pushData.mPackageName)) {

                // recommend other app
                Intent intent = new Intent();
                intent.setClass(context, PushWindowActivity.class);
                intent.putExtra(PushData.PUSH_DATA, pushData);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
}
