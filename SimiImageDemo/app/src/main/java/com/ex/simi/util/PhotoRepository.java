package com.ex.simi.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.ex.simi.entry.Photo;
import com.ex.simi.entry.Picture;

import java.io.File;
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

    public static List<Photo> getPhoto(Context context) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " desc";
        Cursor cursor = contentResolver.query(uri, STORE_IMAGES, null, null, sortOrder);

        List<Photo> result = new ArrayList<>();

        while (cursor != null && cursor.moveToNext()) {
            Photo photo = new Photo();

            photo.setId(cursor.getLong(0));
            photo.setPath(cursor.getString(1));
            photo.setName(cursor.getString(2));
            photo.setMimetype(cursor.getString(3));
            photo.setSize(cursor.getLong(4));

            if (TextUtils.isEmpty(photo.getPath()) || !new File(photo.getPath()).exists()) continue;

            result.add(photo);
        }

        if (cursor != null) cursor.close();

        Logv.e("getPhoto: size = " + result.size());

        return result;
    }

    public static List<Picture> getPictures(Context context, long id) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " desc";
        String selection = MediaStore.Images.Media._ID + ">?";
        String[] selectionArgs = new String[]{"" + id};
        Cursor cursor = contentResolver.query(uri, STORE_IMAGES, selection, selectionArgs, null);

        List<Picture> result = new ArrayList<>();

        while (cursor != null && cursor.moveToNext()) {
//            if (result.size() > 50) break;
            Picture picture = new Picture();

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

        Logv.e("getPictures: size = " + result.size());

        return result;
    }

}
