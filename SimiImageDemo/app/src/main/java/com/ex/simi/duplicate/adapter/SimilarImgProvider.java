package com.ex.simi.duplicate.adapter;


import static com.ex.simi.util.GlobalConsts.TYPE_CONTENT_PIC;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ex.simi.R;
import com.ex.simi.duplicate.entity.ContentItemBean;
import com.ex.simi.duplicate.entity.PhotoEntity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by smy on 20-11-10.
 */

public class SimilarImgProvider extends BaseNodeProvider {

    @Override
    public int getItemViewType() {
        return TYPE_CONTENT_PIC;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_similarphotos_section_content;
    }

    @Override
    public void convert(@NotNull BaseViewHolder holder, @NotNull BaseNode data) {
        // 数据类型需要自己强转
        final ContentItemBean itemBean = (ContentItemBean) data;
        PhotoEntity photoInfoBean = itemBean.mInfoBean;
        ImageView pictureIv = holder.getView(R.id.iv_picture);
        try {
            Glide.with(getContext())
                    .load(itemBean.mInfoBean.path)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.img_picture)
                            .error(R.drawable.img_picture).centerCrop())
                    .into(pictureIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimilarContentAdapter.updateSubItemSelectIvStatus(holder, photoInfoBean);
    }
}
