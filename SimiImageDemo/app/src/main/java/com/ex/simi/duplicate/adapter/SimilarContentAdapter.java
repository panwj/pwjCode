package com.ex.simi.duplicate.adapter;


import static com.ex.simi.util.GlobalConsts.TYPE_CONTENT;
import static com.ex.simi.util.GlobalConsts.TYPE_CONTENT_PIC;
import static com.ex.simi.util.GlobalConsts.TYPE_LEVEL_0;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ex.simi.R;
import com.ex.simi.duplicate.entity.ContentItemBean;
import com.ex.simi.duplicate.entity.HeaderItemBean;
import com.ex.simi.duplicate.entity.PhotoEntity;
import com.ex.simi.superclass.BaseNodeAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by smy on 20-8-10.
 */

public class SimilarContentAdapter extends BaseNodeAdapter {

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == TYPE_LEVEL_0 || getItemViewType(position) == TYPE_CONTENT
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public SimilarContentAdapter(List<BaseNode> data) {
        super(data);
        addNodeProvider(new SimilarGroupProvider());
        addNodeProvider(new SimilarImgProvider());
    }

    /**
     * 这里和Provider里的逻辑不能共存
     */
//    @Override
//    protected void convert(@NonNull final BaseViewHolder holder, final BaseNode item) {
//        switch (holder.getItemViewType()) {
//            case TYPE_LEVEL_0:
//                break;
//            case TYPE_CONTENT_PIC:
//                break;
//            default:
//                break;
//        }
//    }
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            for (Object object : payloads) {
                if (object instanceof ContentItemBean) {
                    ContentItemBean itemBean = (ContentItemBean) object;
                    PhotoEntity photoInfoBean = itemBean.mInfoBean;
                    updateSubItemSelectIvStatus(holder, photoInfoBean);
                } else if (object instanceof HeaderItemBean) {
                    HeaderItemBean itemBean = (HeaderItemBean) object;
                    updateHeaderItemSelectIvStatus(getContext(), holder, itemBean);
                }
            }
        }
    }

    public static void updateSubItemSelectIvStatus(@NonNull BaseViewHolder holder,PhotoEntity photoInfo) {
        int selectIvId;
        View bgView = holder.findView(R.id.bg_select);
        View bgUnView = holder.findView(R.id.bg_unselect);
        if (photoInfo.isChecked) {
            selectIvId = R.drawable.ic_selected;
            bgView.setVisibility(View.VISIBLE);
            bgUnView.setVisibility(View.GONE);
        } else {
            selectIvId = R.drawable.ic_unselected;
            bgView.setVisibility(View.GONE);
            bgUnView.setVisibility(View.VISIBLE);
        }
        holder.setImageResource(R.id.iv_select, selectIvId);
        holder.setVisible(R.id.iv_best_pic, photoInfo.isBestPhoto);
    }

    public static void updateHeaderItemSelectIvStatus(Context context, @NonNull BaseViewHolder holder, HeaderItemBean itemBean) {
        int selectIvId;
        if (itemBean.hasSubItem()) {
            holder.setText(R.id.tv_count, context.getResources().getString(R.string.duplicate_photos_group_counts, itemBean.getPhotoGroup().getChildCount()));
        }
        if (itemBean.mIsSelected) {
            selectIvId = R.drawable.ic_selected;
        } else {
            selectIvId = R.drawable.ic_unselected;
        }
        holder.setImageResource(R.id.iv_select, selectIvId);
    }

    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> list, int i) {
        BaseNode node = list.get(i);
        if (node instanceof HeaderItemBean) {
            return TYPE_LEVEL_0;
        } else if (node instanceof ContentItemBean) {
            return TYPE_CONTENT_PIC;
        }
        return -1;
    }
}
