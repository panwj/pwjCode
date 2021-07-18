package com.mvcdemo.common.google.billing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.mvcdemo.R;


public class RecyAdapter extends RecyclerView.Adapter<RecyAdapter.ViewHolder> {

    private Context context;
    private List<SubItem> datas;

    public RecyAdapter(Context context, List<SubItem> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sub_item_horizon, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int newPos = position % datas.size();

        SubItem item = datas.get(newPos);
        holder.bgTv.setImageResource(item.drawableId);
        holder.desTv.setText(item.strId);

        holder.itemView.setTag(position);


    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bgTv;
        TextView desTv;

        public ViewHolder(View itemView) {
            super(itemView);
            bgTv = itemView.findViewById(R.id.iv_bg);
            desTv = itemView.findViewById(R.id.tv_desc);
        }
    }

}
