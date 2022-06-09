package com.ex.simi.duplicate.event;


import com.ex.simi.duplicate.entity.PhotoEntity;

/**
 * Created by shewenbiao on 18-7-17.
 */

public class SimilarUpdateEvent {
    private PhotoEntity mPhotoInfo;
    public SimilarUpdateEvent(PhotoEntity photoInfo) {
        mPhotoInfo = photoInfo;
    }

    public PhotoEntity getPhotoInfo() {
        return mPhotoInfo;
    }
}
