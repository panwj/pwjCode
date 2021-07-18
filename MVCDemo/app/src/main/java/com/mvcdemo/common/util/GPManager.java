package com.mvcdemo.common.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.mvcdemo.BuildConfig;
import com.mvcdemo.R;

/**
 * Created by smy on 17-8-30.
 */

public class GPManager {

    static final String FACEBOOK_LINK ="https://www.facebook.com/Screen-Recorder-701870780020838/";
    static final String PLAY_APP_PREFIX = "https://play.google.com/store/apps/details?id=";

    public static void supportUserFeedback(Context context, String deviceInfo) {

        Intent support = new Intent(Intent.ACTION_SEND);
        support.setType("plain/text");
        support.putExtra(Intent.EXTRA_EMAIL, context.getResources().getStringArray(R.array.config_Email));
        support.putExtra(Intent.EXTRA_CC, "");

        support.putExtra(Intent.EXTRA_SUBJECT, deviceInfo);
        support.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.send_email_tips) + "\n" + FACEBOOK_LINK + "\n\n");
        try {
            context.startActivity(Intent.createChooser(support,
                    context.getResources().getString(R.string.action_title_mail)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context.getApplicationContext(),
                    R.string.email_unavailable, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public static boolean searchAppOnPlay(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(PLAY_APP_PREFIX + BuildConfig.APPLICATION_ID));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean searchAppOnPlay(Context context, String pkg) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(PLAY_APP_PREFIX + pkg));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUrlWithUtmSrc(String packageName, String utmSource) {
        return PLAY_APP_PREFIX + packageName + "&referrer=utm_source%3D" + utmSource + "%26utm_medium%3Dcpi";
    }

    public static void visitAppFacebook(Context context) {
        Uri fansUri = Uri.parse(FACEBOOK_LINK);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(fansUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
