package com.mvcdemo.common.google.installreferrer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import com.mvcdemo.common.event.EventLogger;
import com.mvcdemo.common.util.PiracyChecker;

/* *
 * 2019-12-16 11:50 from HHD
 * 测试之后发现，这个API的默认channel是organic
 * */
public class InstallReferrerTool {

    //region >>> singleton
    /*
     * 2019-12-16 10:07
     * */
    /////////////////////////////////////↓↓↓ --> init <-- ↓↓↓/////////////////////////////////////
    private InstallReferrerTool() {
    }

    private static class InstanceHolder {
        private static final InstallReferrerTool instance = new InstallReferrerTool();
    }

    public static InstallReferrerTool getInstance() {
        return InstanceHolder.instance;
    }
    /////////////////////////////////////↑↑↑ --> init <-- ↑↑↑/////////////////////////////////////
    //endregion <<< singleton

    //region >>>
    /*
     * 2019-12-16 10:08
     * */
    /////////////////////////////////////↓↓↓↓↓↓↓↓↓/////////////////////////////////////
    InstallReferrerClient mReferrerClient;
    SharedPreferences mSharedPreferences;

    public static final String PREFS_AUDIENCES_TYPE = "com.install.referrer.audiences.type.version2";

    public static final String UTM_SOURCE = "utm_source";
    public static final String UTM_MEDIUM = "utm_medium";
    public static final String UTM_CAMPAIGN = "utm_campaign";
    public static final String CAMPAIGN_ID = "campaignid";
    public static final String CAMPAIGN_TYPE = "campaigntype";

    public static final int AUDIENCES_TYPE_MY_GP_INSTALL_ORGANIC = 10005;
    public static final int AUDIENCES_TYPE_MY_GP_INSTALL_THIRD_PART = 10006;
    public static final int AUDIENCES_TYPE_MY_NGP_INSTALL = 10007;
    public static final int AUDIENCES_TYPE_CRACK_GP_INSTALL = 10008;
    public static final int AUDIENCES_TYPE_CRACK_NGP_INSTALL = 10009;

    // -1 设备上从没有安装过
    private static final int FETCH_AUDIENCES_STATE_NONE = -1;
    // 0 已经获取过渠道
    private static final int FETCH_AUDIENCES_STATE_SUCCESS = 0;

    private static final String INSTALL_ALL = "install_all";
    private static final String INSTALL_SUM = "install_sum";
    private static final String KEY_RELEASE_CHANNEL = "release_channel";
    private static final String KEY_PACKAGE_NAME = "package_name";
    public static final String MY_GP_INSTALL_ORGANIC = "my_gp_install_organic";
    public static final String MY_GP_INSTALL_THIRD_PART = "my_gp_install_third_part";
    private static final String MY_NGP_INSTALL = "my_ngp_install";
    private static final String CRACK_GP_INSTALL = "crack_gp_install";
    private static final String CRACK_NGP_INSTALL = "crack_ngp_install";
    private static final String PREF_HAS_FETCHED_REFERRER = "has_fetched_referrer_version2";
    private static final String PREF_HAS_ALL_INSTALL_RECORDED = "has_all_install_recorded";


    public int getAudiencesType(Context context) {
        int channel = AUDIENCES_TYPE_MY_GP_INSTALL_ORGANIC;
        if (mSharedPreferences != null) {
            channel = mSharedPreferences.getInt(PREFS_AUDIENCES_TYPE, AUDIENCES_TYPE_MY_GP_INSTALL_ORGANIC);
        }
        return channel;
    }

    public void fetchReferrer(final Context context) {
        if (context == null) {
            return;
        }

        mSharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);

        if (!mSharedPreferences.getBoolean(PREF_HAS_ALL_INSTALL_RECORDED, false)) {
            EventLogger.logEvent(context, INSTALL_ALL);
            mSharedPreferences.edit().putBoolean(PREF_HAS_ALL_INSTALL_RECORDED, true).commit();
        }

        int hasFetchedReferrer = mSharedPreferences.getInt(PREF_HAS_FETCHED_REFERRER, FETCH_AUDIENCES_STATE_NONE);
        if (hasFetchedReferrer == FETCH_AUDIENCES_STATE_SUCCESS) {
            return;
        }

        if (PiracyChecker.verifyAppSignature(context)) {
            //应用没被二次签名打包
            if (PiracyChecker.verifyInstaller(context)) {
                //应用没被二次签名打包且发布在play store上
                try {
                    mReferrerClient = InstallReferrerClient.newBuilder(context).build();
                    mReferrerClient.startConnection(new InstallReferrerStateListener() {
                        @Override
                        public void onInstallReferrerSetupFinished(int responseCode) {
                            switch (responseCode) {
                                case InstallReferrerClient.InstallReferrerResponse.OK:
                                    // Connection established.
                                    ReferrerDetails response = null;
                                    String referrerUrl = "";
                                    long referrerClickTimestampSeconds = -1;
                                    try {
                                        response = mReferrerClient.getInstallReferrer();
                                        referrerUrl = response.getInstallReferrer();
                                        referrerClickTimestampSeconds = response.getReferrerClickTimestampSeconds();
                                    } catch (Exception e) {
                                        referrerUrl = "";
                                    } finally {
                                        parseReferrerUrl(context, referrerUrl, referrerClickTimestampSeconds);
                                        mReferrerClient.endConnection();
                                    }
                                    break;
                                case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                                    noReferrerTrack(context, "feature_not_supported");
                                    break;
                                case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                                    noReferrerTrack(context, "service_unavailable");
                                    break;
                            }
                        }

                        @Override
                        public void onInstallReferrerServiceDisconnected() {
                            // Try to restart the connection on the next request to
                            // Google Play by calling the startConnection() method.
                        }
                    });
                } catch (Exception e) {
                    EventLogger.logEvent(context, "startConnection_error");
                    logChannelEvent(context, MY_GP_INSTALL_THIRD_PART, null);
                }
            } else {
                //应用没被二次签名打包且发布在其他渠道
                logChannelEvent(context, MY_NGP_INSTALL, null);
                mSharedPreferences.edit().putInt(PREFS_AUDIENCES_TYPE, AUDIENCES_TYPE_MY_NGP_INSTALL).commit();
                mSharedPreferences.edit().putInt(PREF_HAS_FETCHED_REFERRER, FETCH_AUDIENCES_STATE_SUCCESS).commit();
            }
        } else {
            //应用被二次签名打包
            if (PiracyChecker.verifyInstaller(context)) {
                //应用被二次签名打包且发布在play store上
                logChannelEvent(context, CRACK_GP_INSTALL, context.getPackageName());
                mSharedPreferences.edit().putInt(PREFS_AUDIENCES_TYPE, AUDIENCES_TYPE_CRACK_GP_INSTALL).commit();
            } else {
                //应用被二次签名打包且发布在其他渠道上
                logChannelEvent(context, CRACK_NGP_INSTALL, context.getPackageName());
                mSharedPreferences.edit().putInt(PREFS_AUDIENCES_TYPE, AUDIENCES_TYPE_CRACK_NGP_INSTALL).commit();
            }
            mSharedPreferences.edit().putInt(PREF_HAS_FETCHED_REFERRER, FETCH_AUDIENCES_STATE_SUCCESS).commit();
        }
    }

    private void logChannelEvent(Context context, String channel, String packageName) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_RELEASE_CHANNEL, channel);
        if (!TextUtils.isEmpty(packageName)) {
            bundle.putString(KEY_PACKAGE_NAME, packageName);
        }
        EventLogger.logEvent(context, INSTALL_SUM, bundle);
    }

    private void parseReferrerUrl(Context context, String referrer, long timeStamp) {
        if (TextUtils.isEmpty(referrer)) {
            noReferrerTrack(context, null);
            return;
        }

        Bundle bundle = buildReferrerBundle(context, referrer);
        if (bundle == null) {
            noReferrerTrack(context, null);
            return;
        }
        bundle.putString("bundle", bundle2string(bundle));
        bundle.putString("timeStamp", timeStamp + "");
        EventLogger.logEvent(context, "install_with_referrer", bundle);

        trackChannels(context, bundle);

//        logReferrer(referrer);
        mSharedPreferences.edit().putInt(PREF_HAS_FETCHED_REFERRER, FETCH_AUDIENCES_STATE_SUCCESS).commit();
    }

    private void noReferrerTrack(Context context, String msg) {

        logChannelEvent(context, MY_GP_INSTALL_THIRD_PART, null);

        if (!TextUtils.isEmpty(msg)) {
            Bundle bundle = new Bundle();
            bundle.putString("msg", msg);
            EventLogger.logEvent(context, "install_no_referrer", bundle);
        } else {
            EventLogger.logEvent(context, "install_no_referrer");
        }

        //因为防止install_sum数据重复，所以这个地方也记录为FETCH_AUDIENCES_STATE_SUCCESS    author: SheWenBiao
        mSharedPreferences.edit().putInt(PREF_HAS_FETCHED_REFERRER, FETCH_AUDIENCES_STATE_SUCCESS).commit();
    }

    private Bundle buildReferrerBundle(Context context, String referrer) {
        Bundle bundle = null;
        String[] referrerArray = referrer.split("&");
        if (referrerArray != null && referrerArray.length > 0) {
            bundle = new Bundle();
            for (String ref : referrerArray) {
                if (!TextUtils.isEmpty(ref)) {
                    String[] utmArray = ref.split("=");
                    if (utmArray != null && utmArray.length == 2) {
                        bundle.putString(utmArray[0], utmArray[1]);
                    }
                }
            }
        }

        return bundle;
    }

    private String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string += " " + key + " => " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }

//    private void logReferrer(String referrer) {
//        if (BuildConfig.DEBUG)
//            return;
//
//        String packageName = BuildConfig.APPLICATION_ID;
//        CommonAuthAsyncHttpClient asyncHttpClient = CommonAuthAsyncHttpClient.getCommonHttpsClient();
//
//        RequestParams params = new RequestParams();
//        params.put("packagename", packageName);
//        params.put("referrer", referrer);
//
//        final String submitUrl = "https://ads.cocomobi.com/packageLog/installreferrer.php";
//        asyncHttpClient.post(submitUrl, params, new AsyncHttpResponseHandler() {
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) { }
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) { }
//        });
//    }
    /////////////////////////////////////↑↑↑↑↑↑↑↑↑/////////////////////////////////////

    //region >>> 渠道划分
    /*
     * 2019-12-16 10:53
     * */
    /////////////////////////////////////↓↓↓↓↓↓↓↓↓/////////////////////////////////////
    private void trackChannels(Context context, Bundle bundle) {
        // record audiences type
        String utm_source = bundle.getString(UTM_SOURCE);
        String utm_medium = bundle.getString(UTM_MEDIUM);
//        String campaign_id = bundle.getString(CAMPAIGN_ID);
//        String campaign_type = bundle.getString(CAMPAIGN_TYPE);

        if (isGpOrganic(utm_source, utm_medium)) {
            logChannelEvent(context, MY_GP_INSTALL_ORGANIC, null);
            mSharedPreferences.edit().putInt(PREFS_AUDIENCES_TYPE, AUDIENCES_TYPE_MY_GP_INSTALL_ORGANIC).commit();
        } else {
            logChannelEvent(context, MY_GP_INSTALL_THIRD_PART, null);
            mSharedPreferences.edit().putInt(PREFS_AUDIENCES_TYPE, AUDIENCES_TYPE_MY_GP_INSTALL_THIRD_PART).commit();
        }
    }

    // utm_source = google-play, utm_medium = organic
    private boolean isGpOrganic(String utm_source, String utm_medium) {
        if (TextUtils.equals(utm_source, "google-play") && TextUtils.equals(utm_medium, "organic")) {
            return true;
        }

        return false;
    }

    // Install referrer API 中没有campaign字段
    private boolean isAdWordsCampaign(String campaign_id, String campaign_type) {
        if (!TextUtils.isEmpty(campaign_id) && !TextUtils.isEmpty(campaign_type)) {
            return true;
        }

        return false;
    }

    // utm_source = appcross, utm_medium = recommend
    private boolean isAppCross(String utm_source, String utm_medium) {
        if (TextUtils.equals(utm_source, "appcross") && TextUtils.equals(utm_medium, "recommend")) {
            return true;
        }

        return false;
    }
    /////////////////////////////////////↑↑↑↑↑↑↑↑↑/////////////////////////////////////
}
