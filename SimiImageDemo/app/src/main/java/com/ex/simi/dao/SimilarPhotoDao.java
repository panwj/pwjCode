package com.ex.simi.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.ex.simi.entry.PhotoEntity;

import java.util.List;

@Dao
public interface SimilarPhotoDao {

    @Query("SELECT * FROM similar_photo_table ORDER BY takeDate DESC")
    public List<PhotoEntity> getPhotoDesc();

    @Query("SELECT * FROM similar_photo_table")
    public List<PhotoEntity> getPhoto();

    @Query("SELECT strftime('%Y-%m-%d', time, 'unixepoch') FROM similar_photo_table GROUP BY strftime('%Y-%m-%d', time, 'unixepoch') ORDER BY time DESC")
    public List<String> getPhotoGroupByTime();

    @Query("SELECT * FROM similar_photo_table WHERE strftime('%Y-%m-%d', time, 'unixepoch') = :formatTime")
    public List<PhotoEntity> getPhotoByFormatTime(String formatTime);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertPhoto(PhotoEntity... pictures);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertPhoto(List<PhotoEntity> list);

    @Query("SELECT max(id) FROM similar_photo_table")
    public long getMaxPhotoId();

    @Query("DELETE FROM similar_photo_table WHERE id = :id")
    public void deletePhotoById(long id);

    @Delete
    public void deletePhotos(PhotoEntity... photos);

    @Delete
    public void deletePhotos(List<PhotoEntity> photos);

    @Query("DELETE FROM similar_photo_table")
    public void deleteAll();
}
