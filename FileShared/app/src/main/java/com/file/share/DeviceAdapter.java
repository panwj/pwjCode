package com.file.share;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private List<Endpoint> mData;

    public DeviceAdapter() {
        mData = new ArrayList<>();
    }

    public void setData(List<Endpoint> map) {
        if (map == null) return;
        mData.clear();
        mData.addAll(map);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_info_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.ViewHolder holder, int position) {
        holder.updateData(mData.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) onItemClickListener.onItemClick(mData.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mId, mName, mState;
        private ProgressBar mProgressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mId = itemView.findViewById(R.id.tv_id);
            mName = itemView.findViewById(R.id.tv_name);
            mState = itemView.findViewById(R.id.tv_state);
            mProgressBar = itemView.findViewById(R.id.progress_bar);
        }

        public void updateData(Endpoint endpoint) {
            mId.setText(endpoint.getId());
            mName.setText(endpoint.getName());

            ConnectionState state = endpoint.getState();
            mState.setText(state.name());

            if (state == ConnectionState.CONNECTED) {
                mProgressBar.setVisibility(View.INVISIBLE);
            } else if (state == ConnectionState.PENDING) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else if (state == ConnectionState.UNKNOWN) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Endpoint endpoint, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
