package com.ex.simi.duplicate.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.ex.simi.superclass.ExpendNode;
import com.ex.simi.util.GlobalConsts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smy on 20-08-10.
 */

public class HeaderItemBean extends ExpendNode implements MultiItemEntity {
    private DuplicatePhotoGroup mPhotoGroup;
    public boolean mIsSelected;
    private int mGroupSelectCount;

    public HeaderItemBean(DuplicatePhotoGroup photoGroup) {
        this.mPhotoGroup = photoGroup;
        if (photoGroup == null || photoGroup.getPhotoInfoList() == null || photoGroup.getPhotoInfoList().isEmpty()) {
        } else {
            List<BaseNode> contentList = new ArrayList<>();
            for (PhotoEntity info : mPhotoGroup.getPhotoInfoList()) {
                ContentItemBean itemBean = new ContentItemBean(this, info);
                contentList.add(itemBean);
            }
            setSubItems(contentList);
        }
    }

    @Override
    public int getItemType() {
        return GlobalConsts.TYPE_LEVEL_0;
    }

    //getter and setter

    public DuplicatePhotoGroup getPhotoGroup() {
        return mPhotoGroup;
    }

    public HeaderItemBean setPhotoGroup(DuplicatePhotoGroup photoGroup) {
        this.mPhotoGroup = photoGroup;
        return this;
    }

    public int getGroupSelectCount() {
        return mGroupSelectCount;
    }

    public void setGroupSelectCount(int mGroupSelectCount) {
        this.mGroupSelectCount = mGroupSelectCount;
    }
}
