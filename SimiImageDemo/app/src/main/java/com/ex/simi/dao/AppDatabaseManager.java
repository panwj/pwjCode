package com.ex.simi.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ex.simi.duplicate.db.SimilarPhotoDao;
import com.ex.simi.duplicate.entity.PhotoEntity;

/**
 * Created by smy on 20-12-26.
 */

@Database(entities = {PhotoEntity.class}, version = 1)
public abstract class AppDatabaseManager extends RoomDatabase {

    public abstract SimilarPhotoDao getSimilarPhotoDao();

    private final static String DB_NAME = "room_file_security";
    private static AppDatabaseManager INSTANCE;

    public static AppDatabaseManager getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabaseManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabaseManager.class, DB_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}