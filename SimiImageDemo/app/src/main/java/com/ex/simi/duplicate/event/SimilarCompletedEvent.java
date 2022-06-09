package com.ex.simi.duplicate.event;


import com.ex.simi.duplicate.entity.DuplicatePhotoGroup;

import java.util.List;

public class SimilarCompletedEvent {
    private List<DuplicatePhotoGroup> mList;
    private int count;
    private long size;

    public List<DuplicatePhotoGroup> getList() {
        return mList;
    }

    public void setList(List<DuplicatePhotoGroup> mList) {
        this.mList = mList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
