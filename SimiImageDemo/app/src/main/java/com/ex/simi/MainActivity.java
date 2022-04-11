package com.ex.simi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ex.simi.entry.Photo;
import com.ex.simi.util.PermissionHelper;
import com.ex.simi.util.PhotoRepository;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionHelper.PermissionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (PermissionHelper.hasStoragePermissions(this)) {
            initData();
        } else {
            PermissionHelper.requestStoragePermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions,grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        initData();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private void initData() {
        final List<Photo> photos = PhotoRepository.getPhoto(this);
        GridView gridView = (GridView) findViewById(R.id.grid);
        gridView.setAdapter(new Adapter(photos));
    }

    public void find(View view) {
        if (PermissionHelper.hasStoragePermissions(this)) {
            Intent intent = new Intent(MainActivity.this, GroupActivity.class);
            startActivity(intent);
        }
    }

    private class Adapter extends BaseAdapter {
        List<Photo> photos;

        public Adapter(List<Photo> photos) {
            this.photos = photos;
        }

        @Override
        public int getCount() {
            return photos != null ? photos.size() : 0;
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
            Photo photo = photos.get(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_image, parent, false);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.image);

            Glide.with(MainActivity.this)
                    .load(photo.getPath())
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

            return convertView;
        }
    }
}