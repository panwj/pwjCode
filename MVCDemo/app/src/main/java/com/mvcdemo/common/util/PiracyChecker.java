package com.mvcdemo.common.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Base64;

import com.google.android.gms.common.util.ArrayUtils;

import java.io.File;
import java.security.MessageDigest;
import java.util.List;

import com.mvcdemo.common.Constants;


/**
 *  生成彩蛋签名校验key的工具类
 */
public class PiracyChecker {

    public static boolean verifyAppSignature(Context context) {

        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {

                MessageDigest md = MessageDigest.getInstance("SHA");

                md.update(signature.toByteArray());

                final String currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT).trim();

                //compare signatures
                if (Constants.SIGNATURE.trim().equals(currentSignature)) {
                    return true;
                }

            }

        } catch (Exception e) {

          //assumes an issue in checking signature., but we let the caller decide on what to do.

        }

        return false;
    }

    private static final String[] PLAY_STORE_APP_IDS = {
            "com.android.vending",
            "com.google.android.feedback",
    };

    public static boolean verifyInstaller(final Context context) {
        List<String> list = ArrayUtils.toArrayList(PLAY_STORE_APP_IDS);
        final String installer = context.getPackageManager()

                .getInstallerPackageName(context.getPackageName());

        return installer != null && list.contains(installer);
    }

    public static boolean isInEmulator(boolean deepCheck) {
        int ratingCheckEmulator = 0;

        String product = Build.PRODUCT;
        if (product != null && (product.contains("sdk") || product.contains("Andy") ||
                product.contains("ttVM_Hdragon") || product.contains("google_sdk") ||
                product.contains("Droid4X") || product.contains("nox") ||
                product.contains("sdk_x86") || product.contains("sdk_google") ||
                product.contains("vbox86p"))) {
            ratingCheckEmulator++;
        }

        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null && (manufacturer.equalsIgnoreCase("unknown") || manufacturer.equalsIgnoreCase("Genymotion") ||
                manufacturer.contains("Andy") || manufacturer.contains("MIT") ||
                manufacturer.contains("nox") || manufacturer.contains("TiantianVM"))) {
            ratingCheckEmulator++;
        }

        String brand = Build.BRAND;
        if (brand != null && (brand.equalsIgnoreCase("generic") || brand.equalsIgnoreCase("generic_x86") ||
                brand.equalsIgnoreCase("TTVM") || brand.contains("Andy"))) {
            ratingCheckEmulator++;
        }

        String device = Build.DEVICE;
        if (device != null && (device.contains("generic") || device.contains("generic_x86") ||
                device.contains("Andy") || device.contains("ttVM_Hdragon") ||
                device.contains("Droid4X") || device.contains("nox") ||
                device.contains("generic_x86_64") || device.contains("vbox86p"))) {
            ratingCheckEmulator++;
        }

        String model = Build.MODEL;
        if (model != null && (model.equalsIgnoreCase("sdk") || model.equalsIgnoreCase("google_sdk") ||
                model.contains("Droid4X") || model.contains("TiantianVM") ||
                model.contains("Andy") || model.equalsIgnoreCase(
                "Android SDK built for x86_64") ||
                model.equalsIgnoreCase("Android SDK built for x86"))) {
            ratingCheckEmulator++;
        }

        String hardware = Build.HARDWARE;

        if (hardware != null && (hardware.equalsIgnoreCase("goldfish") || hardware.equalsIgnoreCase("vbox86") ||
                hardware.contains("nox") || hardware.contains("ttVM_x86"))) {
            ratingCheckEmulator++;
        }

        String fingerprint = Build.FINGERPRINT;
        if (fingerprint != null && (fingerprint.contains("generic") ||
                fingerprint.contains("generic/sdk/generic") ||
                fingerprint.contains("generic_x86/sdk_x86/generic_x86") ||
                fingerprint.contains("Andy") || fingerprint.contains("ttVM_Hdragon") ||
                fingerprint.contains("generic_x86_64") ||
                fingerprint.contains("generic/google_sdk/generic") ||
                fingerprint.contains("vbox86p") ||
                fingerprint.contains("generic/vbox86p/vbox86p"))) {
            ratingCheckEmulator++;
        }

        if (deepCheck) {
            try {
                String glRenderer = GLES20.glGetString(GLES20.GL_RENDERER);
                    if (glRenderer.contains("Bluestacks") || glRenderer.contains("Translator"))
                        ratingCheckEmulator += 10;
            } catch (Exception e) {
            }

            try {
                File sharedFolder = new File(
                        "${Environment.getExternalStorageDirectory()}${File.separatorChar}windows" +
                                "${File.separatorChar}BstSharedFolder");
                if (sharedFolder.exists())
                    ratingCheckEmulator += 10;
            } catch (Exception e) {
            }
        }

        return ratingCheckEmulator > 3;
    }
}
