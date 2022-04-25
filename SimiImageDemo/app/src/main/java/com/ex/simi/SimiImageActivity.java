package com.ex.simi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.simi.adapter.SimiAdapter;
import com.ex.simi.cv.ImageCVHistogram;
import com.ex.simi.dao.PictureDaoManager;
import com.ex.simi.entry.Picture;
import com.ex.simi.entry.PictureGroup;
import com.ex.simi.normal.ImageHashUtil;
import com.ex.simi.util.Logv;
import com.ex.simi.util.PhotoRepository;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimiImageActivity extends AppCompatActivity {

    private RecyclerView mRecycleView;
    private SimiAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mTimeView;
    private Handler mHandler;
    private int sampleSize;
    private boolean dHash, aHash, opencv, desc, pro1, pro2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simi_image_activity_layout);

        mRecycleView = findViewById(R.id.recycle);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, 4));
        mAdapter = new SimiAdapter(getApplicationContext());
        mRecycleView.setAdapter(mAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences(getApplicationContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        sampleSize = sharedPreferences.getInt("sampleSize", 8);
        dHash = sharedPreferences.getBoolean("dHash", true);
        aHash = sharedPreferences.getBoolean("aHash", true);
        desc = sharedPreferences.getBoolean("desc", true);
        opencv = sharedPreferences.getBoolean("opencv", false);
        pro1 = sharedPreferences.getBoolean("pro1", false);
        pro2 = sharedPreferences.getBoolean("pro2", false);

        mProgressBar = findViewById(R.id.progress_circular);
        updateProgressBar(View.VISIBLE);
        mTimeView = findViewById(R.id.tv_info);
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

    private void updateProgressBar(int visibility) {
        if (mProgressBar != null) mProgressBar.setVisibility(visibility);
    }

    private void simiPicture(Context context) {
        Logv.e("simiPicture() start");
        long time = System.currentTimeMillis();

        /**
         * 增量更新系统数据库数据
         */
        long id = PictureDaoManager.getDatabase(context).getPictureDao().getMaxPictureId();
        Logv.e("id = " + id);
        List<Picture> listSys = PhotoRepository.getPictures(context, id);

        Logv.e("sampleSize = " + sampleSize + "   dHash = " + dHash + "  aHash = " + aHash + "  opencv = " + opencv + " desc = " + desc + " pro2 = " + pro2);

        for (Picture picture : listSys) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            options.inMutable = true;
            options.inSampleSize = sampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(picture.path, options);

            Bitmap dBitmap = ImageHashUtil.unifiedBitmap(bitmap, ImageHashUtil.WIDTH, ImageHashUtil.HEIGHT);
            long dFinger = ImageHashUtil.calculateFingerPrintDHash(dBitmap);
            picture.d_finger = dFinger;

            Bitmap aBitmap = ImageHashUtil.unifiedBitmap(bitmap, ImageHashUtil.A_SIZE, ImageHashUtil.A_SIZE);
            long aFinger = ImageHashUtil.calculateFingerPrintAHash(aBitmap);
            picture.a_finger = aFinger;

            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            if (aBitmap != null && !aBitmap.isRecycled()) aBitmap.recycle();
            if (dBitmap != null && !dBitmap.isRecycled()) dBitmap.recycle();
        }
        Logv.e("system list size : " + listSys.size());

        PictureDaoManager.getDatabase(context).getPictureDao().insertPicture(listSys);
        List<Picture> list = desc ? PictureDaoManager.getDatabase(context).getPictureDao().getPictureDesc()
                : PictureDaoManager.getDatabase(context).getPictureDao().getPicture();

        if (opencv) {
            for (Picture picture : list) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inMutable = true;
                options.inSampleSize = sampleSize;
                Bitmap bitmap = BitmapFactory.decodeFile(picture.path, options);

                Bitmap cvBitmap = ImageHashUtil.unifiedBitmap(bitmap, 64, 64);
                Mat[] mats = ImageCVHistogram.calculateHistData(cvBitmap);
                picture.mats = mats;

                if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
                if (cvBitmap != null && !cvBitmap.isRecycled()) cvBitmap.recycle();
            }
        }

        Logv.e("get finger time ---> " + (System.currentTimeMillis() - time) / 1000 + "    list.size() = " + list.size());

        List<PictureGroup> groups;
        if (pro2) {
            Logv.e("-----------> pro2");
            groups = compareCVFinger(arrangementGroupsList1(compareFinger(list, aHash, dHash, opencv, 2, 2)));
        } else {
            Logv.e("-----------> normal");
            groups = compareFinger(list, aHash, dHash, opencv, 2, 2);
        }
        arrangementGroupsList(groups);
        Logv.e("simiPicture() end : " + ((System.currentTimeMillis() - time) / 1000) + "   group size = " + groups.size());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isDestroyed() || isFinishing()) return;
                updateProgressBar(View.GONE);
                mTimeView.setText("扫描完成耗时 ： " + ((System.currentTimeMillis() - time) / 1000) + " 秒, " + groups.size() + " 组");
                mAdapter.setData(groups);
            }
        });
    }

    private List<PictureGroup> compareFinger(List<Picture> list, boolean aHash, boolean dHash, boolean opencv, int aDist, int dDist) {
        List<PictureGroup> groups = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Picture picture1 = list.get(i);

            if (!picture1.isUse) {
                List<Picture> temp = new ArrayList<>();
                temp.add(picture1);

                for (int j = i + 1; j < list.size(); j++) {
                    Picture picture2 = list.get(j);
                    if (dHash && !picture2.isUse && ImageHashUtil.hammingDistance(picture1.d_finger, picture2.d_finger, "dHash") < dDist) {
                        temp.add(picture2);
                        picture2.isUse = true;
                    } else if (aHash && !picture2.isUse && ImageHashUtil.hammingDistance(picture1.a_finger, picture2.a_finger, "aHash") < aDist) {
                        temp.add(picture2);
                        picture2.isUse = true;
                    } else if (opencv && !picture2.isUse
                            && ImageCVHistogram.comPareHist(picture1.mats[0], picture2.mats[0])
                            && ImageCVHistogram.comPareHist(picture1.mats[1], picture2.mats[1])
                            && ImageCVHistogram.comPareHist(picture1.mats[2], picture2.mats[2])) {
                        temp.add(picture2);
                        picture2.isUse = true;
                    }
                }

                PictureGroup group = new PictureGroup();
                group.setPicture(temp);
                groups.add(group);
            }
        }
        return groups;
    }

    private List<PictureGroup> compareCVFinger(List<PictureGroup> groups) {
        List<PictureGroup> tempGroups = new ArrayList<>();

        for (PictureGroup pictureGroup : groups) {
            if (pictureGroup.getPicture().size() <= 2) {
                tempGroups.add(pictureGroup);
                continue;
            }

            List<Picture> list = pictureGroup.getPicture();
            for (int i = 0; i < list.size(); i++) {
                Picture picture1 = list.get(i);

                if (!picture1.isUse) {
                    List<Picture> temp = new ArrayList<>();
                    temp.add(picture1);

                    for (int j = i + 1; j < list.size(); j++) {
                        Picture picture2 = list.get(j);
                        if (!picture2.isUse
                                && ImageCVHistogram.comPareHist(picture1.mats[0], picture2.mats[0])
                                && ImageCVHistogram.comPareHist(picture1.mats[1], picture2.mats[1])
                                && ImageCVHistogram.comPareHist(picture1.mats[2], picture2.mats[2])) {
                            temp.add(picture2);
                            picture2.isUse = true;
                        }
                    }

                    PictureGroup group = new PictureGroup();
                    group.setPicture(temp);
                    tempGroups.add(group);
                }
            }
        }
        return tempGroups;
    }

    private List<PictureGroup> arrangementGroupsList1(List<PictureGroup> groups) {
        for (PictureGroup group : groups) {
            for (Picture picture : group.getPicture()) {
                picture.isUse = false;
            }
        }
        return groups;
    }

    private List<Picture> arrangementGroupsList(List<PictureGroup> groups) {
        List<Picture> temp = new ArrayList<>();
        for (PictureGroup group : groups) {
            for (Picture picture : group.getPicture()) {
                picture.isUse = false;
                temp.add(picture);
            }
        }
        Logv.e("arrangementGroupsList() temp : " + temp.size());
        return temp;
    }
}
