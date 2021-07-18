package com.mvcdemo.common.util;

public class MAD5Util {

    /**
     * Get the MD5 digest.
     *
     * @param source The source bytes.
     * @return The MD5 digest.
     */
    public static String getMD5(byte[] source) {
        String s = null;

        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            md.update(source);
            byte tmp[] = md.digest();
            s = byteArrayToHexString(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * Convert a byte array to a hex string.
     *
     * @param bytes The byte array to be converted.
     * @return The hex string.
     */
    public static String byteArrayToHexString(byte bytes[]) {
        StringBuffer hex_string = new StringBuffer(bytes.length * 2);
        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                int byte_val = bytes[i];
                if (byte_val < 0) {
                    byte_val += 256;
                }
                String hex_code = Integer.toHexString(byte_val);
                if (hex_code.length() % 2 == 1) {
                    hex_code = "0" + hex_code;
                }
                hex_string.append(hex_code);
            }

        }
        return hex_string.toString();
    }
}
