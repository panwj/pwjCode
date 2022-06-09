package com.ex.simi.duplicate.entity;


import static com.ex.simi.util.GlobalConsts.TYPE_CONTENT_PIC;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.ex.simi.superclass.ExpendNode;

/**
 * Created by smy on 20-08-10.
 */

public class ContentItemBean extends ExpendNode implements MultiItemEntity {

    public PhotoEntity mInfoBean;
    private HeaderItemBean mHeaderItemBean;

    public ContentItemBean(HeaderItemBean headerItemBean, PhotoEntity photoInfoBean) {
        this.mInfoBean = photoInfoBean;
        this.mHeaderItemBean = headerItemBean;
    }

    @Override
    public int getItemType() {
        return TYPE_CONTENT_PIC;
    }

    public void setHeaderItemBean(HeaderItemBean headerItemBean) {
        mHeaderItemBean = headerItemBean;
    }

    public HeaderItemBean getHeaderItemBean() {
        return mHeaderItemBean;
    }
}