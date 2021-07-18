package com.mvcdemo.common.util;


import java.util.HashMap;
import java.util.Map;


/**
 * 判断是否是pro用户工具类
 */
public class PrimeManager {

    public static final String PREF_SUBS_USER_NEW = "pref_new_subs_user";

    public static final String[] PKG_MAINLAND = new String[]{
            "com.eg.android.AlipayGphone",//Alipay
            "com.taobao.taobao",//Taobao
            "com.tencent.mm",//Wechat
            "com.tencent.mobileqq",//QQ
            "com.jingdong.app.mall",//JD
            "com.sankuai.meituan",//MeiTuan
            "me.ele",//Ele
            "com.baidu.searchbox",//Baidu
            "com.sina.weibo",//Weibo
            "com.dianping.v1",//DaZhongDianPing
            "com.zhihu.android"//Zhihu
    };

    public static Map<String, String> S_PKG_MAP = new HashMap<>();

    static {
        for (int i = 0; i < PKG_MAINLAND.length; i++) {
            String pkgName = "";
            switch (i) {
                case 0:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Aplipay");
                    break;
                case 1:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Taobao");
                    break;
                case 2:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Wechat");
                    break;
                case 3:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "QQ");
                    break;
                case 4:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "JD");
                    break;
                case 5:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Meituan");
                    break;
                case 6:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Ele");
                    break;
                case 7:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Baidu");
                    break;
                case 8:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Weibo");
                    break;
                case 9:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Dianping");
                    break;
                case 10:
                    pkgName = PKG_MAINLAND[i];
                    S_PKG_MAP.put(pkgName, "Zhihu");
                    break;
                default:
                    pkgName = "";
                    break;
            }
        }
    }

    public static void saveSubscriptionsUser(boolean isSub) {
        MMKVUtil.getInstance().saveBoolean(PREF_SUBS_USER_NEW, isSub);
    }

    public static boolean isSubscriptionUser() {
        return MMKVUtil.getInstance().getBoolean(PREF_SUBS_USER_NEW, false);
    }
}
