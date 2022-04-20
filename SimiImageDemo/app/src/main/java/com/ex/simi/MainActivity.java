package com.ex.simi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ex.simi.cv.ImageCVHistogram;
import com.ex.simi.entry.Photo;
import com.ex.simi.normal.ImageAHash;
import com.ex.simi.normal.ImageHashUtil;
import com.ex.simi.normal.ImagePHash;
import com.ex.simi.util.Logv;
import com.ex.simi.util.PermissionHelper;
import com.ex.simi.util.PhotoRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionHelper.PermissionCallbacks {

    static {
        System.loadLibrary("opencv_java4");//加载OpenCV动态库
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (PermissionHelper.hasStoragePermissions(this)) {
            initData();
        } else {
            PermissionHelper.requestStoragePermissions(this);
        }
        try {
            /**
             * 0.7802265070164313
             * 0010110111000011011011001001101100100010010100111
             * 3.1047429034190697
             * 0010011111001011001111100101100110010010111111001
             */
//            int a = new ImagePHash().isSmailPic(
//                    "/storage/emulated/0/Pictures/图片/BC214994-CFCE-451C-8789-89C3CB5BB154_4_5005_c.jpeg"
//                    , "/storage/emulated/0/Pictures/图片/207BC41F-ED8A-4394-A13B-8EA9D36BA6AD_1_105_c.jpeg");
//            Logv.e("pHash a = " + a);
//
//            List<Photo> list = new ArrayList<>();
//            Photo photo1 = new Photo();
//            photo1.setPath("/storage/emulated/0/Pictures/图片/BC214994-CFCE-451C-8789-89C3CB5BB154_4_5005_c.jpeg");
//
//            Photo photo2 = new Photo();
//            photo2.setPath("/storage/emulated/0/Pictures/图片/207BC41F-ED8A-4394-A13B-8EA9D36BA6AD_1_105_c.jpeg");
//            list.add(photo1);
//            list.add(photo2);
//            ImageAHash.find(this, list);
//            new ImageCVHistogram().testHistogramMatch(this);

            ImageHashUtil.test(
                    "/storage/emulated/0/DCIM/Camera/IMG_20220407_113739.jpg"
                    , "/storage/emulated/0/DCIM/Camera/IMG_20220407_113734.jpg");

            ImageHashUtil.test(
                    "/storage/emulated/0/Pictures/图片/DF037B73-3492-49B1-8010-EACCD4BC9945_1_105_c.jpeg"
                    , "/storage/emulated/0/Pictures/图片/77DDC9F4-013B-4D7A-8C48-B1BDC4AB5D53.jpeg");
        } catch (Exception e) {
            e.printStackTrace();
            Logv.e("exception : " + e.toString());
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
            Intent intent = new Intent(MainActivity.this, SimiImageActivity.class);
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
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

            return convertView;
        }
    }
}