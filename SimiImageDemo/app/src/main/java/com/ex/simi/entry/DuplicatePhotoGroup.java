package com.ex.simi.entry;

import java.io.Serializable;
import java.util.ArrayList;

public class DuplicatePhotoGroup implements Serializable {

    private ArrayList<PhotoEntity> mPhotoInfoList = new ArrayList<>();
    private String mTimeName;
    private int mGroupId;
    private long mGroupFileSize;

    public ArrayList<PhotoEntity> getPhotoInfoList() {
        return mPhotoInfoList;
    }

    public void setPhotoInfoList(ArrayList<PhotoEntity> photoInfoList) {
        mPhotoInfoList = photoInfoList;
    }

    public int getChildSize() {
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

    @Override
    public String toString() {
        return "ImageInfoGroup{" +
                "mImageInfoList=" + mPhotoInfoList + '}';
    }
}
