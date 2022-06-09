package com.ex.simi.duplicate.event;


import com.ex.simi.duplicate.entity.DuplicatePhotoGroup;

public class SimilarSingleCompletedEvent {
    private DuplicatePhotoGroup mDuplicatePhotoGroup;

    public DuplicatePhotoGroup getDuplicatePhotoGroup() {
        return mDuplicatePhotoGroup;
    }

    public void setDuplicatePhotoGroup(DuplicatePhotoGroup mDuplicatePhotoGroup) {
        this.mDuplicatePhotoGroup = mDuplicatePhotoGroup;
    }
}
