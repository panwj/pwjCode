package com.ex.simi;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ex.simi.cv.ImageCVHistogram;
import com.ex.simi.entry.Group;
import com.ex.simi.entry.Photo;
import com.ex.simi.normal.ImageAHash;
import com.ex.simi.util.PhotoRepository;

import java.util.ArrayList;
import java.util.List;


public class GroupActivity extends AppCompatActivity {

    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        final List<Photo> photos = PhotoRepository.getPhoto(this);
        final ListView listView = (ListView) findViewById(R.id.list);

        mHandler = new Handler(getMainLooper());

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Group> groups = ImageAHash.find(GroupActivity.this, photos);

//                final List<Group> groups = new ImageCVHistogram().find(GroupActivity.this, photos);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<Photo> list1 = new ArrayList<>();
                        for (Group group : groups) {
                            Photo photo = new Photo();
                            photo.setName("组别");
                            list1.add(photo);
                            list1.addAll(group.getPhotos());
                        }
                        listView.setAdapter(new Adapter(list1));
                    }
                });
            }
        }).start();
    }

    private class Adapter extends BaseAdapter {

        private List<Photo> groups;

        public Adapter(List<Photo> groups) {
            this.groups = groups;
        }

        @Override
        public int getCount() {
            return groups == null ? 0 : groups.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_group, parent, false);
            }

            TextView name = (TextView) convertView.findViewById(R.id.name);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);

            Photo photo = groups.get(position);
            if (TextUtils.equals("组别", photo.getName())) {
                name.setText(photo.getName());
                name.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
            } else {
                name.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
                Glide.with(GroupActivity.this)
                        .load(photo.getPath())
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(icon);
            }

            return convertView;
        }
    }


}
