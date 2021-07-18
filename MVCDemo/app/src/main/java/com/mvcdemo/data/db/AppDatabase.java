package com.mvcdemo.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.mvcdemo.data.entity.User;
import com.mvcdemo.data.dao.UserDao;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}

