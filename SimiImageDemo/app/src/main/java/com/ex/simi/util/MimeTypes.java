package com.ex.simi.util;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class MimeTypes {

    private static final long BIG_FILE = 10 * 1024 * 1024;
    public static final HashMap<String, String> MIME_TYPES = new HashMap<>();

    static {
        /*
         * ================= MIME TYPES ====================
        */
        MIME_TYPES.put("asm", "text/x-asm");
        MIME_TYPES.put("def", "text/plain");
        MIME_TYPES.put("in", "text/plain");
        MIME_TYPES.put("rc", "text/plain");
        MIME_TYPES.put("list", "text/plain");
        MIME_TYPES.put("log", "text/plain");
        MIME_TYPES.put("pl", "text/plain");
        MIME_TYPES.put("prop", "text/plain");
        MIME_TYPES.put("properties", "text/plain");
        MIME_TYPES.put("rc", "text/plain");

        MIME_TYPES.put("epub", "application/epub+zip");
        MIME_TYPES.put("ibooks", "application/x-ibooks+zip");

        MIME_TYPES.put("ifb", "text/calendar");
        MIME_TYPES.put("eml", "message/rfc822");
        MIME_TYPES.put("msg", "application/vnd.ms-outlook");

        MIME_TYPES.put("ace", "application/x-ace-compressed");
        MIME_TYPES.put("bz", "application/x-bzip");
        MIME_TYPES.put("bz2", "application/x-bzip2");
        MIME_TYPES.put("cab", "application/vnd.ms-cab-compressed");
        MIME_TYPES.put("gz", "application/x-gzip");
        MIME_TYPES.put("lrf", "application/octet-stream");
        MIME_TYPES.put("jar", "application/java-archive");
        MIME_TYPES.put("xz", "application/x-xz");
        MIME_TYPES.put("Z", "application/x-compress");

        MIME_TYPES.put("bat", "application/x-msdownload");
        MIME_TYPES.put("ksh", "text/plain");
        MIME_TYPES.put("sh", "application/x-sh");

        MIME_TYPES.put("db", "application/octet-stream");
        MIME_TYPES.put("db3", "application/octet-stream");

        MIME_TYPES.put("otf", "x-font-otf");
        MIME_TYPES.put("ttf", "x-font-ttf");
        MIME_TYPES.put("psf", "x-font-linux-psf");

        MIME_TYPES.put("cgm", "image/cgm");
        MIME_TYPES.put("btif", "image/prs.btif");
        MIME_TYPES.put("dwg", "image/vnd.dwg");
        MIME_TYPES.put("dxf", "image/vnd.dxf");
        MIME_TYPES.put("fbs", "image/vnd.fastbidsheet");
        MIME_TYPES.put("fpx", "image/vnd.fpx");
        MIME_TYPES.put("fst", "image/vnd.fst");
        MIME_TYPES.put("mdi", "image/vnd.ms-mdi");
        MIME_TYPES.put("npx", "image/vnd.net-fpx");
        MIME_TYPES.put("xif", "image/vnd.xiff");
        MIME_TYPES.put("pct", "image/x-pict");
        MIME_TYPES.put("pic", "image/x-pict");
        MIME_TYPES.put("bmp", "image/bmp");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");

        MIME_TYPES.put("adp", "audio/adpcm");
        MIME_TYPES.put("au", "audio/basic");
        MIME_TYPES.put("snd", "audio/basic");
        MIME_TYPES.put("m2a", "audio/mpeg");
        MIME_TYPES.put("m3a", "audio/mpeg");
        MIME_TYPES.put("oga", "audio/ogg");
        MIME_TYPES.put("spx", "audio/ogg");
        MIME_TYPES.put("aac", "audio/x-aac");
        MIME_TYPES.put("mka", "audio/x-matroska");
        MIME_TYPES.put("m3u", "audio/x-mpegurl");
        MIME_TYPES.put("m4a", "audio/mp4a-latm");
        MIME_TYPES.put("m4b", "audio/mp4a-latm");
        MIME_TYPES.put("m4p", "audio/mp4a-latm");
        MIME_TYPES.put("mp2", "audio/x-mpeg");
        MIME_TYPES.put("mp3", "audio/x-mpeg");
        MIME_TYPES.put("mpga", "audio/mpeg");
        MIME_TYPES.put("ogg", "audio/ogg");
        MIME_TYPES.put("rmvb", "audio/x-pn-realaudio");
        MIME_TYPES.put("wav", "audio/x-wav");
        MIME_TYPES.put("wma", "audio/x-ms-wma");
        MIME_TYPES.put("wmv", "audio/x-ms-wmv");

        MIME_TYPES.put("jpgv", "video/jpeg");
        MIME_TYPES.put("jpgm", "video/jpm");
        MIME_TYPES.put("jpm", "video/jpm");
        MIME_TYPES.put("mj2", "video/mj2");
        MIME_TYPES.put("mjp2", "video/mj2");
        MIME_TYPES.put("mpa", "video/mpeg");
        MIME_TYPES.put("ogv", "video/ogg");
        MIME_TYPES.put("flv", "video/x-flv");
        MIME_TYPES.put("mkv", "video/x-matroska");
        MIME_TYPES.put("3gp", "video/3gpp");
        MIME_TYPES.put("asf", "video/x-ms-asf");
        MIME_TYPES.put("avi", "video/x-msvideo");
        MIME_TYPES.put("m4u", "video/vnd.mpegurl");
        MIME_TYPES.put("m4v", "video/x-m4v");
        MIME_TYPES.put("mov", "video/quicktime");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("mpe", "video/mpeg");
        MIME_TYPES.put("mpeg", "video/mpeg");
        MIME_TYPES.put("mpg", "video/mpeg");
        MIME_TYPES.put("mpg4", "video/mp4");

        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("csv", "text/csv");

        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("tgz", "application/x-gtar");
        MIME_TYPES.put("tar", "application/x-gzip");


    }

    public static String getMimeType(File file) {
        if (file.isDirectory()) {
            return null;
        }

        String type = null;
        final String extension = FileUtil.getFileExtension(file.getName());

        if (!TextUtils.isEmpty(extension)) {
            final String extensionLowerCase = extension.toLowerCase(Locale.getDefault());
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extensionLowerCase);
            if (type == null) {
                type = MIME_TYPES.get(extensionLowerCase);
            }
        }

        if (type == null)
            type = "*/*";
        return type;
    }

    private static boolean mimeTypeMatch(String mime, String input) {
        return Pattern.matches(mime.replace("*", ".*"), input);
    }

    public static boolean isPicture(File f) {
        final String mime = getMimeType(f);
        return mime != null && mimeTypeMatch("image/*", mime);
    }

    public static boolean isVideo(File f) {
        final String mime = getMimeType(f);
        return mime != null && mimeTypeMatch("video/*", mime);
    }

    public static boolean isDoc(File f){
        final String mime = getMimeType(f);
        return mime != null && (mime.equals("text/plain")
                || mime.equals("application/pdf")
                || mime.equals("application/msword")
                || mime.equals("application/vnd.ms-excel"));
    }

    public static boolean isApk(File f) {
        String name = FileUtil.getFileName(f);
        return name.toLowerCase().endsWith(".apk");
    }

    public static boolean isZip(File f) {
        String name = FileUtil.getFileName(f);
        return name.toLowerCase().endsWith(".zip");
    }

    public static boolean isMusic(File f) {
        final String REGEX = "(.*/)*.+\\.(mp3|m4a|ogg|wav|aac)$";
        return f.getName().matches(REGEX);
    }

    public static boolean isBigFile(File f) {
        return f.length() > BIG_FILE;
    }

    public static boolean isTempFile(File f) {
        String name = FileUtil.getFileName(f);
        return name.toLowerCase().endsWith(".tmp") || name.toLowerCase().endsWith(".temp");
    }

    public static boolean isLog(File f) {
        String name = FileUtil.getFileName(f);
        return name.toLowerCase().endsWith(".log");
    }
}
