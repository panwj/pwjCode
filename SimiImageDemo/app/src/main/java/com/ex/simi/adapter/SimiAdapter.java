package com.ex.simi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ex.simi.R;
import com.ex.simi.entry.Picture;
import com.ex.simi.entry.PictureGroup;

import java.util.ArrayList;
import java.util.List;

public class SimiAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Picture> mList;

    public SimiAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<PictureGroup> list) {
        if (list == null) return;

        List<Picture> temp = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Picture picture = new Picture();
            picture.type = i;
            temp.add(picture);
            temp.addAll(list.get(i).getPicture());
        }
        if (mList == null) mList = new ArrayList<>();
        mList.clear();
        mList.addAll(temp);
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == -1 ? 1 : 4;
                }
            });
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case -1:
                holder = new HolderContent(LayoutInflater.from(mContext).inflate(R.layout.item2, null));
                break;
            default:
                holder = new HolderTitle(LayoutInflater.from(mContext).inflate(R.layout.item1, null));
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HolderTitle) {
            ((HolderTitle) holder).setTitle(mList.get(position));
        } else if (holder instanceof  HolderContent) {
            ((HolderContent) holder).setIcon(mList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    class HolderTitle extends RecyclerView.ViewHolder {

        private TextView title;

        public HolderTitle(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.group);
        }

        public void setTitle(Picture picture) {
            title.setText(String.valueOf(picture.type));
        }
    }

    class HolderContent extends RecyclerView.ViewHolder {

        private ImageView icon;
        public HolderContent(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.image);
        }

        public void setIcon(Picture picture) {
            Glide.with(mContext)
                    .load(picture.path)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(icon);
        }
    }
}
