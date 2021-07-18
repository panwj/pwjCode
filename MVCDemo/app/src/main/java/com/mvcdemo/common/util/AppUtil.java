package com.mvcdemo.common.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import com.mvcdemo.common.Constants;

public class AppUtil {

    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public static String getSurprise() {
        return new String(Base64.decode(Constants.SAY_HELLO.getBytes(), Base64.DEFAULT));
    }
}
