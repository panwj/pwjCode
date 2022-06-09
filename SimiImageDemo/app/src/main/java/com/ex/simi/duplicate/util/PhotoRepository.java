package com.ex.simi.duplicate.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.ex.simi.BuildConfig;
import com.ex.simi.dao.AppDatabaseManager;
import com.ex.simi.duplicate.entity.PhotoEntity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by gavin on 2017/3/27.
 */

public class PhotoRepository {

    private static final String TAG = "similar_image";

    private static final String[] STORE_IMAGES = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_TAKEN,
    };

    public static void updateLocalSimilarDB(Context context) {
        long id = AppDatabaseManager.getDatabase(context).getSimilarPhotoDao().getMaxPhotoId();
        List<PhotoEntity> sysList = getPictures(context, id);
        for (PhotoEntity picture : sysList) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 8;
            Bitmap bitmap = BitmapFactory.decodeFile(picture.path, options);

            Bitmap dBitmap = ImageHashUtil.unifiedBitmap(bitmap, ImageHashUtil.WIDTH, ImageHashUtil.HEIGHT);
            long dFinger = ImageHashUtil.calculateFingerPrintDHash(dBitmap);
            picture.d_finger = dFinger;

            Bitmap aBitmap = ImageHashUtil.unifiedBitmap(bitmap, ImageHashUtil.A_SIZE, ImageHashUtil.A_SIZE);
            long aFinger = ImageHashUtil.calculateFingerPrintAHash(aBitmap);
            picture.a_finger = aFinger;

            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            if (aBitmap != null && !aBitmap.isRecycled()) aBitmap.recycle();
            if (dBitmap != null && !dBitmap.isRecycled()) dBitmap.recycle();
        }
        AppDatabaseManager.getDatabase(context).getSimilarPhotoDao().insertPhoto(sysList);
        updateLocalDBDirtyData(context);
    }

    public static List<PhotoEntity> getPictures(Context context, long id) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " desc";
        String selection = MediaStore.Images.Media._ID + ">?";
        String[] selectionArgs = new String[]{"" + id};
        Cursor cursor = contentResolver.query(uri, STORE_IMAGES, selection, selectionArgs, sortOrder);

        List<PhotoEntity> result = new ArrayList<>();

        while (cursor != null && cursor.moveToNext()) {

            PhotoEntity picture = new PhotoEntity();

            picture.id = cursor.getLong(0);
            picture.path = cursor.getString(1);
            picture.name = cursor.getString(2);
            picture.mimetype = cursor.getString(3);
            picture.size = cursor.getLong(4);
            picture.takeDate = cursor.getLong(5);

            if (TextUtils.isEmpty(picture.path) || !new File(picture.path).exists()) continue;
            // TODO: 2022/5/13 需要确认该时间，如果不合适需要换成 takeDate
            picture.time = (long) (getTimeByPath(picture.path) / 1000.0d);
            if (picture.time == 0) picture.time = picture.takeDate / 1000;

            result.add(picture);
        }

        if (cursor != null) cursor.close();
        if (BuildConfig.LOG_DEBUG) Log.e(TAG, "getPictures() size : " + result.size());

        return result;
    }

    private static long getTimeByPath(String path) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            String attribute = new ExifInterface(path).getAttribute("DateTime");
            if (attribute != null) {
                return simpleDateFormat.parse(attribute).getTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e2) {
            e2.printStackTrace();
        }
        return 0;
    }

    private static void updateLocalDBDirtyData(Context context) {
        try {
            List<Long> sysList = getPicturesId(context);
            List<Long> localList = AppDatabaseManager.getDatabase(context).getSimilarPhotoDao().getPhotosId();
            localList.removeAll(sysList);
            if (BuildConfig.LOG_DEBUG)
                Log.e(TAG, "updateLocalDBDirtyData() size : " + localList.size());
            if (localList.size() > 0)
                AppDatabaseManager.getDatabase(context).getSimilarPhotoDao().deletePhotoById(localList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Long> getPicturesId(Context context) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA}, null, null, null);

        List<Long> result = new ArrayList<>();

        while (cursor != null && cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String path = cursor.getString(1);
            if (TextUtils.isEmpty(path) || !new File(path).exists()) continue;
            result.add(id);
        }

        if (cursor != null) cursor.close();
        if (BuildConfig.LOG_DEBUG) Log.e(TAG, "getPicturesId() size : " + result.size());

        return result;
    }

}
