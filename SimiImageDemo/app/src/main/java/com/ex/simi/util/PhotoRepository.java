package com.ex.simi.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;


import com.ex.simi.dao.PictureDaoManager;
import com.ex.simi.entry.PhotoEntity;
import com.ex.simi.normal.ImageHashUtil;

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

    private static final String[] STORE_IMAGES = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_TAKEN,
    };

    public static void updateLocalSimilarDB(Context context) {
        long id = PictureDaoManager.getDatabase(context).getPictureDao().getMaxPhotoId();
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
        PictureDaoManager.getDatabase(context).getPictureDao().insertPhoto(sysList);
    }

    private static List<PhotoEntity> getPictures(Context context, long id) {
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


    public static List<PhotoEntity> getPhoto(Context context) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " desc";
        Cursor cursor = contentResolver.query(uri, STORE_IMAGES, null, null, sortOrder);

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

            result.add(picture);
        }

        if (cursor != null) cursor.close();

        Logv.e("getPhoto: size = " + result.size());

        return result;
    }

}
