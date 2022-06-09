package com.ex.simi.duplicate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ex.simi.R;
import com.ex.simi.duplicate.entity.PhotoEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shewenbiao on 18-7-17.
 */

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<PhotoRecyclerAdapter.MyViewHolder> {

    private List<PhotoEntity> mPhotoInfoList;
    private Context mContext;
    private int mPreIndex;
    private OnItemClickListener mOnItemClickListener;

    public PhotoRecyclerAdapter(Context context) {
        mContext = context;
        mPhotoInfoList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_dulicate_photos_group, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        PhotoEntity photoInfo = mPhotoInfoList.get(position);
        if (photoInfo.isChecked) {
            holder.mSelectIv.setVisibility(View.VISIBLE);
        } else {
            holder.mSelectIv.setVisibility(View.GONE);
        }
        if (mPreIndex == position) {
            holder.mSelectedBgView.setVisibility(View.VISIBLE);
        } else {
            holder.mSelectedBgView.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.getAdapterPosition());
                }
            }
        });
        try {
            Glide.with(mContext).load(new File(photoInfo.path)).into(holder.mPhotoIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoInfoList == null ? 0 : mPhotoInfoList.size();
    }

    public void setDatas(List<PhotoEntity> list) {
        if (list != null) {
            mPhotoInfoList.clear();
            mPhotoInfoList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void setPreIndex(int preIndex) {
        mPreIndex = preIndex;
    }

    public int getPreIndex() {
        return mPreIndex;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView mPhotoIv;
        ImageView mSelectIv;
        View mSelectedBgView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mPhotoIv = itemView.findViewById(R.id.iv_photo);
            mSelectIv = itemView.findViewById(R.id.iv_select);
            mSelectedBgView = itemView.findViewById(R.id.bg_select);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
