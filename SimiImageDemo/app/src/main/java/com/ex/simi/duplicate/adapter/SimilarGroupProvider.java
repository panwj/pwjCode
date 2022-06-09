package com.ex.simi.duplicate.adapter;


import static com.ex.simi.duplicate.adapter.SimilarContentAdapter.updateHeaderItemSelectIvStatus;
import static com.ex.simi.util.GlobalConsts.TYPE_LEVEL_0;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ex.simi.R;
import com.ex.simi.duplicate.entity.HeaderItemBean;

import org.jetbrains.annotations.NotNull;

/**
 * Created by smy on 20-11-10.
 */

public class SimilarGroupProvider extends BaseNodeProvider {

    @Override
    public int getItemViewType() {
        return TYPE_LEVEL_0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_similarphotos_section_header;
    }

    @Override
    public void convert(@NotNull BaseViewHolder holder, @NotNull BaseNode data) {
        // 数据类型需要自己强转
        final HeaderItemBean headerItemBean = (HeaderItemBean) data;
        updateHeaderItemSelectIvStatus(getContext(), holder, headerItemBean);
    }
}