package com.ex.simi.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ex.simi.entry.Picture;

import java.util.List;

@Dao
public interface PictureDao {

    @Query("SELECT * FROM picture_table ORDER BY takeDate DESC")
    public List<Picture> getPictureDesc();

    @Query("SELECT * FROM picture_table")
    public List<Picture> getPicture();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertPicture(Picture... pictures);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertPicture(List<Picture> list);

    @Query("SELECT max(id) FROM picture_table")
    public long getMaxPictureId();

    @Query("DELETE FROM picture_table")
    public void deleteAll();
}
