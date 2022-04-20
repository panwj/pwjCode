package com.ex.simi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.simi.adapter.SimiAdapter;
import com.ex.simi.cv.ImageCVHistogram;
import com.ex.simi.entry.Picture;
import com.ex.simi.entry.PictureGroup;
import com.ex.simi.normal.ImageHashUtil;
import com.ex.simi.util.Logv;
import com.ex.simi.util.PhotoRepository;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class SimiImageActivity extends AppCompatActivity {

    private RecyclerView mRecycleView;
    private SimiAdapter mAdapter;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simi_image_activity_layout);

        mRecycleView = findViewById(R.id.recycle);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, 4));
        mAdapter = new SimiAdapter(getApplicationContext());
        mRecycleView.setAdapter(mAdapter);

        mHandler = new Handler(getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                simiPicture(SimiImageActivity.this);
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void simiPicture(Context context) {
        Logv.e("simiPicture() start");
        long time = System.currentTimeMillis();

        List<Picture> list = PhotoRepository.getPictures(context);
        for (Picture picture : list) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 8;
            Bitmap bitmap = BitmapFactory.decodeFile(picture.getPath(), options);

//            Bitmap dBitmap = ImageHashUtil.unifiedBitmap(bitmap, ImageHashUtil.WIDTH, ImageHashUtil.HEIGHT);
//            long dFinger = ImageHashUtil.calculateFingerPrintDHash(dBitmap);
//            picture.setDFinger(dFinger);

            Bitmap aBitmap = ImageHashUtil.unifiedBitmap(bitmap, ImageHashUtil.A_SIZE, ImageHashUtil.A_SIZE);
            long aFinger = ImageHashUtil.calculateFingerPrintAHash(aBitmap);
            picture.setAFinger(aFinger);

//            Mat mat = ImageCVHistogram.calculateMatData(picture.getPath());
//            picture.setMat(mat);

//            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
//            if (aBitmap != null && !aBitmap.isRecycled()) aBitmap.recycle();
//            if (dBitmap != null && !dBitmap.isRecycled()) dBitmap.recycle();
        }

        Logv.e("get finger time ---> " + (System.currentTimeMillis() - time) / 1000);

        List<PictureGroup> groups = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Picture picture1 = list.get(i);

            if (!picture1.isUse()) {
                List<Picture> temp = new ArrayList<>();
                temp.add(picture1);

                for (int j = i + 1; j < list.size(); j++) {
                    Picture picture2 = list.get(j);
                    int dDist = ImageHashUtil.hammingDistance(picture1.getDFinger(), picture2.getDFinger(), "dHash");
                    if (!picture2.isUse() && dDist < 5) {
                        temp.add(picture2);
                        picture2.setUse(true);
                    } else if (!picture2.isUse() && ImageHashUtil.hammingDistance(picture1.getAFinger(), picture2.getAFinger(), "aHash") < 3) {
                        temp.add(picture2);
                        picture2.setUse(true);
                    } else if (!picture2.isUse() && ImageCVHistogram.comPareHist(picture1.getMat(), picture2.getMat())) {
                        temp.add(picture2);
                        picture2.setUse(true);
                    }
                }

                PictureGroup group = new PictureGroup();
                group.setPicture(temp);
                groups.add(group);
            }
        }

        Logv.e("simiPicture() end : " + ((System.currentTimeMillis() - time) / 1000) + "   group size = " + groups.size());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.setData(groups);
            }
        });
    }
}
