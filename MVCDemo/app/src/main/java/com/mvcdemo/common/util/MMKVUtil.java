package com.mvcdemo.common.util;

import android.content.Context;

import com.tencent.mmkv.MMKV;

import com.mvcdemo.BuildConfig;


public class MMKVUtil {

    private static volatile MMKVUtil instance = null;
    private MMKV mmkv = null;

    private MMKVUtil() {
    }

    public static MMKVUtil getInstance() {
        if (instance == null) {
            synchronized (MMKVUtil.class) {
                if (instance == null) {
                    instance = new MMKVUtil();
                }
            }
        }
        return instance;
    }

    public void mmkvInit(Context context) {
        MMKV.initialize(context);
    }

    public synchronized boolean saveString(final String key, final String value) {
        getMmkvWithID();
        if (mmkv == null) {
            return false;
        }
        return mmkv.encode(key, value);
    }

    public synchronized String getString(final String key, final String def) {
        getMmkvWithID();
        if (mmkv == null) {
            return def;
        }
        return mmkv.decodeString(key, def);
    }

    public synchronized boolean saveBoolean(final String key, final boolean value) {
        getMmkvWithID();
        if (mmkv == null) {
            return false;
        }
        return mmkv.encode(key, value);
    }

    public synchronized boolean getBoolean(final String key, boolean def) {
        getMmkvWithID();
        if (mmkv == null) {
            return false;
        }
        return mmkv.decodeBool(key, def);
    }

    public synchronized boolean saveInt(final String key, final int value) {
        getMmkvWithID();
        if (mmkv == null) {
            return false;
        }
        return mmkv.encode(key, value);
    }

    public synchronized int getInt(final String key, final int def) {
        getMmkvWithID();
        if (mmkv == null) {
            return def;
        }
        return mmkv.decodeInt(key, def);
    }

    public synchronized boolean saveLong(final String key, final long value) {
        getMmkvWithID();
        if (mmkv == null) {
            return false;
        }
        return mmkv.encode(key, value);
    }

    public synchronized long getLong(final String key, final long def) {
        getMmkvWithID();
        if (mmkv == null) {
            return def;
        }
        return mmkv.decodeLong(key, def);
    }

    public synchronized boolean saveFloat(final String key, final float value) {
        getMmkvWithID();
        if (mmkv == null) {
            return false;
        }
        return mmkv.encode(key, value);
    }

    public synchronized float getFloat(final String key, float def) {
        getMmkvWithID();
        if (mmkv == null) {
            return def;
        }
        return mmkv.decodeFloat(key, def);
    }

    public synchronized void remove(final String key) {
        getMmkvWithID();
        if (mmkv == null) {
            return;
        }
        mmkv.removeValueForKey(key);
    }

    public synchronized void removeAll() {
        getMmkvWithID();
        if (mmkv == null) return;
        mmkv.clearAll();
    }

    private void getMmkvWithID() {
        if (mmkv == null)
            mmkv = MMKV.mmkvWithID(BuildConfig.APPLICATION_ID);
    }
}
