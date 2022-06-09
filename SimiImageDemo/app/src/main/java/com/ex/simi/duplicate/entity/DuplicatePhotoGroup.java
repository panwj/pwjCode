package com.ex.simi.duplicate.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by shewenbiao on 18-7-11.
 */

public class DuplicatePhotoGroup implements Serializable {

    private int mGroupId;
    private long mGroupFileSize;
    private String mTimeName;
    private ArrayList<PhotoEntity> mPhotoInfoList = new ArrayList<>();

    public ArrayList<PhotoEntity> getPhotoInfoList() {
        return mPhotoInfoList;
    }

    public ArrayList<PhotoEntity> getPhotoInfoListExcludeBest() {
        ArrayList<PhotoEntity> list = new ArrayList<>(mPhotoInfoList);
        list.remove(0);
        return list;
    }

    public void updatePhotoInfoList(PhotoEntity photoEntity) {
        if (photoEntity == null || mPhotoInfoList == null || mPhotoInfoList.size() <= 2) return;
        mPhotoInfoList.remove(photoEntity);
        getBestPhoto();
    }

    public void setPhotoInfoList(ArrayList<PhotoEntity> photoInfoList) {
        mPhotoInfoList = photoInfoList;
    }

    public int getChildCount() {
        return mPhotoInfoList.size();
    }

    public String getTimeName() {
        return mTimeName;
    }

    public void setTimeName(String timeName) {
        mTimeName = timeName;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    public long getGroupFileSize() {
        return mGroupFileSize;
    }

    public void setGroupFileSize(long groupFileSize) {
        this.mGroupFileSize = groupFileSize;
    }

    public PhotoEntity getBestPhoto() {
        PhotoEntity photoEntity = mPhotoInfoList.get(0);
        photoEntity.isBestPhoto = true;
        photoEntity.isChecked = false;
        return photoEntity;
    }

    @Override
    public String toString() {
        return "ImageInfoGroup{" +
                "mImageInfoList=" + mPhotoInfoList + '}';
    }
}
