package com.mvcdemo.common.util;

import android.content.Context;
import com.mvcdemo.common.Constants;


/**
 * 版本变更工具类
 */
public class VersionChecker {
    private static final String PREF_KEY_FIRST_INSTALL_VERSION_CODE = "VersionChecker_first_install_version_code";
    public static final String PREF_KEY_PREV_VERSION_CODE = "VersionChecker_prev_version_code";
    private static final String PREF_KEY_CURRENT_VERSION_CODE = "VersionChecker_current_version_code";
    public static final String PREF_KEY_SHOW_WHATS_NEW = "VersionChecker_show_whats_new";
    private static final int UNINIT_VERSION_CODE = -1;

    public static void updateVersionChecker(Context ctx) {

        // this package version code
        int versionCode = AppUtil.getVersionCode(ctx);

        // version code get from prefs
        int currentCode = MMKVUtil.getInstance().getInt(PREF_KEY_CURRENT_VERSION_CODE, UNINIT_VERSION_CODE);

        if (currentCode == UNINIT_VERSION_CODE) {
            // version info not init yet. It is first install

            MMKVUtil.getInstance().saveInt(PREF_KEY_CURRENT_VERSION_CODE, versionCode);
            MMKVUtil.getInstance().saveInt(PREF_KEY_FIRST_INSTALL_VERSION_CODE, versionCode);
            MMKVUtil.getInstance().saveInt(PREF_KEY_PREV_VERSION_CODE, versionCode);
            return;
        }

        if (versionCode != currentCode) {
            // update from previous version , so update version info
            MMKVUtil.getInstance().saveInt(PREF_KEY_PREV_VERSION_CODE, currentCode);
            MMKVUtil.getInstance().saveInt(PREF_KEY_CURRENT_VERSION_CODE, versionCode);
            // for judge is show release notes
            MMKVUtil.getInstance().saveBoolean(PREF_KEY_SHOW_WHATS_NEW, true);
            MMKVUtil.getInstance().saveBoolean(Constants.PREF_SHOW_UPDATE_SUMMARY, true);
            return;
        }

        // versionCode == currentCode
        // version info has been updated, just return
    }

    public static int getFirstInstallVersion() {
        return MMKVUtil.getInstance().getInt(PREF_KEY_FIRST_INSTALL_VERSION_CODE, UNINIT_VERSION_CODE);
    }

    public static int getPrevInstallVersion() {
        return MMKVUtil.getInstance().getInt(PREF_KEY_PREV_VERSION_CODE, UNINIT_VERSION_CODE);
    }

    public static int getCurInstallVersion() {
        return MMKVUtil.getInstance().getInt(PREF_KEY_CURRENT_VERSION_CODE, UNINIT_VERSION_CODE);
    }

    public static boolean isShowWhatsNew() {
        return MMKVUtil.getInstance().getBoolean(PREF_KEY_SHOW_WHATS_NEW, false);
    }
}