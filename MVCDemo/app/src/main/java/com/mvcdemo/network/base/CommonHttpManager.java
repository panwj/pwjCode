package com.mvcdemo.network.base;

import android.content.Context;
import android.util.Log;

import com.yanzhenjie.nohttp.InitializationConfig;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.OkHttpNetworkExecutor;
import com.yanzhenjie.nohttp.cache.DiskCacheStore;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;

import com.mvcdemo.BuildConfig;


public class CommonHttpManager {

    private static final int MAX_THREADS_COUNT = 10;

    private static volatile CommonHttpManager sInstance = null;

    private RequestQueue mRequestQueue = null;

    public static CommonHttpManager getInstance() {
        if (sInstance == null) {
            synchronized (CommonHttpManager.class) {
                if (sInstance == null) {
                    sInstance = new CommonHttpManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * Initialize NoHttp, should invoke on {@link android.app.Application#onCreate()}.
     */
    public static void initialize(Context context) {
        try {
            NoHttp.initialize(InitializationConfig.newBuilder(context)
                            .connectionTimeout(30 * 1000)
                            .readTimeout(30 * 1000)
                            .cacheStore(new DiskCacheStore(context))
                            .networkExecutor(new OkHttpNetworkExecutor())
                            .retry(3)
                            .build());
            Logger.setDebug(BuildConfig.DEBUG);
            Logger.setTag("CommonHttpManager");
        } catch (Exception e) {
            Log.e("CommonHttpManager", e.toString());
        }
    }

    private CommonHttpManager() {
        mRequestQueue = NoHttp.newRequestQueue(MAX_THREADS_COUNT);
    }


    /**
     * Cancel request by sign
     * @param sign the sign
     */
    public void cancelBySign(Object sign) {
        if (mRequestQueue != null) mRequestQueue.cancelBySign(sign);
    }

    /**
     * Cancel all request
     */
    public void cancelAll() {
        if (mRequestQueue != null) mRequestQueue.cancelAll();
    }

    /**
     * Add a request to queue
     *
     * @param what      request id
     * @param request   the request
     * @param listener  callback listener
     */
    protected  <T> void add(int what, Request<T> request, OnResponseListener listener) {
        mRequestQueue.add(what, request, listener);
    }

    /**
     * called when exit app to release cpu。
     */
    public void stop() {
        mRequestQueue.stop();
    }
}
