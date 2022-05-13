package com.ex.simi.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ex.simi.entry.PhotoEntity;

@Database(entities = {PhotoEntity.class}, version = 1)
public abstract class PictureDaoManager extends RoomDatabase {
    public abstract SimilarPhotoDao getPictureDao();

    private final static String DB_NAME = "picture_db_room";
    private static PictureDaoManager INSTANCE;

    public static PictureDaoManager getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (PictureDaoManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), PictureDaoManager.class, DB_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
