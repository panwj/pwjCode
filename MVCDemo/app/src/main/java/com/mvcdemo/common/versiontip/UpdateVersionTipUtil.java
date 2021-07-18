package com.mvcdemo.common.versiontip;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import com.mvcdemo.R;
import com.mvcdemo.common.Constants;
import com.mvcdemo.common.firebase.config.FBRemoteConfig;
import com.mvcdemo.common.util.AppUtil;
import com.mvcdemo.common.util.GPManager;
import com.mvcdemo.common.util.MMKVUtil;
import com.mvcdemo.common.versiontip.bean.NewVersionInfo;
import com.mvcdemo.common.versiontip.event.ExitAppEvent;

public class UpdateVersionTipUtil {

    public static final String PREF_INT_PREV_APP_VERSION_CODE_IN_STOR = "int_prev_app_version_code";
    public static final String PREF_INT_APP_VERSION_CODE_IN_STORE = "int_versioncode";
    public static final String PREF_STR_APP_VERSION_NAME_IN_STORE = "str_versionname";
    public static final String PREF_STR_APP_WHAT_NEW_CONTENT = "str_whatsnew";
    private static final String KEY_INT_APP_COMPULSORY_UPGRADING = "int_compulsory_upgrading";

    public static NewVersionInfo getNewVersionInStore() {
        String versionInfo = "";
        NewVersionInfo newVersionInfo = null;
        try {
            FBRemoteConfig remoteConfig = FBRemoteConfig.getInstance();
            FirebaseRemoteConfig firebaseRemoteConfig = remoteConfig.getRemoteConfigRef();
            versionInfo = firebaseRemoteConfig.getString(remoteConfig.JSON_APP_VERSION_CODE_IN_STORE);
//            versionInfo = "{\"int_versioncode\": 151,   \"str_versionname\": \"1.5.0\",   \"str_whatsnew\": \"1. One tap to remove junk files & free up your storage space;2.Easily hide unwanted notifications;3.Performance improved.\",   \"int_compulsory_upgrading\": \"0\" }";
            if (!TextUtils.isEmpty(versionInfo)) {

                JSONObject jsonObject = new JSONObject(versionInfo);
                int code = jsonObject.getInt(PREF_INT_APP_VERSION_CODE_IN_STORE);
                String versionName = jsonObject.getString(PREF_STR_APP_VERSION_NAME_IN_STORE);
                String whatnew = jsonObject.getString(PREF_STR_APP_WHAT_NEW_CONTENT);
                int isCompulsory = jsonObject.getInt(KEY_INT_APP_COMPULSORY_UPGRADING);

                newVersionInfo = new NewVersionInfo();
                newVersionInfo.setVersionCode(code);
                newVersionInfo.setVersionName(versionName);
                newVersionInfo.setWhatsNew(whatnew);
                newVersionInfo.setCompulsoryUpgrading(isCompulsory);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newVersionInfo;
    }

    public static boolean hasNewVersionInStore(Context context) {
        boolean hasNewVersion = false;

        try {
            NewVersionInfo newVersionInfo = getNewVersionInStore();
            int versionCode = 0;
            int curAppVersionCode = AppUtil.getVersionCode(context);
            if (newVersionInfo != null) {
                versionCode = newVersionInfo.getVersionCode();
            } else {
                versionCode = curAppVersionCode;
            }

            if (versionCode > curAppVersionCode) {
                hasNewVersion = true;
                int prevVersionCode = MMKVUtil.getInstance().getInt(PREF_INT_PREV_APP_VERSION_CODE_IN_STOR, curAppVersionCode);
                if (versionCode > prevVersionCode || newVersionInfo.isCompulsoryUpgrading()) {
                    MMKVUtil.getInstance().saveBoolean(Constants.PREF_IS_SHOW_UPDATE_VERSION_DIALOG, true);
                    MMKVUtil.getInstance().saveInt(PREF_INT_PREV_APP_VERSION_CODE_IN_STOR, versionCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            hasNewVersion = false;
        }
        return hasNewVersion;
    }

    public static void showUpdatedVersionDialog(final Activity activity) {

        if (activity == null) return;
        Context context = activity.getApplicationContext();
        final NewVersionInfo newVersionInfo = getNewVersionInStore();
        if (MMKVUtil.getInstance().getBoolean(Constants.PREF_IS_SHOW_UPDATE_VERSION_DIALOG, true)) {

            if (newVersionInfo != null) {
                View view = LayoutInflater.from(context).inflate(R.layout.update_new_version_dialog, null);
                TextView versionTitle = (TextView) view.findViewById(R.id.update_version_title);
                TextView whatsNewTv = (TextView) view.findViewById(R.id.update_version_content);
                TextView closeTv = (TextView) view.findViewById(R.id.update_version_close);
                TextView updateNow = (TextView) view.findViewById(R.id.update_version_ok);

                versionTitle.setText(context.getString(R.string.what_new_title, newVersionInfo.getVersionName()));
                String versionInfo = "";
                if (!TextUtils.isEmpty(newVersionInfo.getWhatsNew())) {
                    String[] str = newVersionInfo.getWhatsNew().split(";");
                    for (int i = 0; i < str.length; i++) {
                        versionInfo = versionInfo + str[i] + "\n";
                    }

                }
                whatsNewTv.setText(versionInfo);
                final AlertDialog dialog = new AlertDialog
                        .Builder(activity)
                        .setView(view)
                        .create();
                dialog.setContentView(view);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                closeTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (newVersionInfo.isCompulsoryUpgrading()) {
                            EventBus.getDefault().post(new ExitAppEvent());
                        }
                    }
                });

                updateNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        GPManager.searchAppOnPlay(context, context.getPackageName());
                        if (newVersionInfo.isCompulsoryUpgrading()) {
                            EventBus.getDefault().post(new ExitAppEvent());
                        }
                    }
                });

                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
                            return true;
                        return false;
                    }
                });

                MMKVUtil.getInstance().saveBoolean(Constants.PREF_IS_SHOW_UPDATE_VERSION_DIALOG, false);
                dialog.show();
            }
        }
    }
}
