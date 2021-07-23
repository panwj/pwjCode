package com.mvcdemo.common.event;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * 实现打点的关键类
 */
public class EventLogger {

    public static void logEvent(Context context, String trackerName) {
        logEvent(context, trackerName, null);
    }

    public static void logEvent(Context context, String trackerName, Bundle bundle) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();
        logFirebaseEvent(appContext, trackerName, bundle);
    }

    // TODO need config app in firebase console
    private static void logFirebaseEvent(Context context, String trackerName, Bundle bundle) {
        try {
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
            firebaseAnalytics.logEvent(trackerName, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}