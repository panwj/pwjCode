package com.mvcdemo.common.firebase.config;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import com.mvcdemo.BuildConfig;
import com.mvcdemo.R;


/**
 * firebase remoteConfig 工具类
 * 需要定义一个xml放默认值
 */
public class FBRemoteConfig {

    private static final String TAG = "FBRemoteConfig";

    // get app version code config
    public static final String JSON_APP_VERSION_CODE_IN_STORE = "json_app_version_in_store";

    //recommend app
    public static final String CONFIG_KEY_JSON_RECOMMEND_APPS_SETTING = "json_recommend_apps_settings";

    private static final long CACHE_EXPIRATION = 1 * 3600; // 1 HOUR

    private static FBRemoteConfig sInstance = null;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public static FBRemoteConfig getInstance() {
        if (sInstance == null) {
            sInstance = new FBRemoteConfig();
        }
        return sInstance;
    }

    private FBRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        if (mFirebaseRemoteConfig != null) {
            mFirebaseRemoteConfig.setConfigSettings(configSettings);
            // set default values, edit res/xml/firebase_remote_config_defaults.xml
            mFirebaseRemoteConfig.setDefaults(R.xml.firebase_remote_config_defaults);
        }

        fetchConfig();
    }

    /**
     * Fetch remote config from server.
     */
    public void fetchConfig() {

        Log.d(TAG, "fetchConfig()");

        if (mFirebaseRemoteConfig == null) {
            return;
        }
        // cacheExpirationSeconds is set to CACHE_EXPIRATION here, indicating that any previously
        // fetched and cached config would be considered expired because it would have been fetched
        // more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
        // throttling is in progress. The default expiration duration is 43200 (12 hours).
        mFirebaseRemoteConfig.fetch(CACHE_EXPIRATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Remote Config Fetch Succeeded");
                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Log.d(TAG, "Remote Config Fetch failed");
                        }
                    }
                });
    }

    public FirebaseRemoteConfig getRemoteConfigRef() {
        return mFirebaseRemoteConfig;
    }
}
