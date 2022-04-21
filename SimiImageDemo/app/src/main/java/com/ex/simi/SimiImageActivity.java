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
import java.util.List;

public class SimiImageActivity extends AppCompatActivity {

    private RecyclerView mRecycleView;
    private SimiAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mTimeView;
    private Handler mHandler;
    private int sampleSize;
    private boolean dHash, aHash, desc;

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

        Logv.e("sampleSize = " + sampleSize + "   dHash = " + dHash + "  aHash = " + aHash + "   " + desc);

        for (Picture picture : listSys) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
//            options.inMutable = true;
            options.inSampleSize = sampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(picture.path, options);

            Bitmap dBitmap = ImageHashUtil.unifiedBitmap(bitmap, ImageHashUtil.WIDTH, ImageHashUtil.HEIGHT);
            long dFinger = ImageHashUtil.calculateFingerPrintDHash(dBitmap);
            picture.d_finger = dFinger;

            Bitmap aBitmap = ImageHashUtil.unifiedBitmap(bitmap, ImageHashUtil.A_SIZE, ImageHashUtil.A_SIZE);
            long aFinger = ImageHashUtil.calculateFingerPrintAHash(aBitmap);
            picture.a_finger = aFinger;

//            Bitmap cvBitmap = ImageHashUtil.unifiedBitmap(bitmap, 64, 64);
//            Mat mat = ImageCVHistogram.calculateMatData(aBitmap);
//            picture.setMat(mat);

            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            if (aBitmap != null && !aBitmap.isRecycled()) aBitmap.recycle();
            if (dBitmap != null && !dBitmap.isRecycled()) dBitmap.recycle();
        }
        Logv.e("system list size : " + listSys.size());

        PictureDaoManager.getDatabase(context).getPictureDao().insertPicture(listSys);
        List<Picture> list = desc ? PictureDaoManager.getDatabase(context).getPictureDao().getPictureDesc()
                : PictureDaoManager.getDatabase(context).getPictureDao().getPicture();

        Logv.e("get finger time ---> " + (System.currentTimeMillis() - time) / 1000);

        List<PictureGroup> groups = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Picture picture1 = list.get(i);

            if (!picture1.isUse) {
                List<Picture> temp = new ArrayList<>();
                temp.add(picture1);

                for (int j = i + 1; j < list.size(); j++) {
                    Picture picture2 = list.get(j);
                     if (dHash && !picture2.isUse && ImageHashUtil.hammingDistance(picture1.d_finger, picture2.d_finger, "dHash") < 5) {
                        temp.add(picture2);
                        picture2.isUse = true;
                    } else if (aHash && !picture2.isUse && ImageHashUtil.hammingDistance(picture1.a_finger, picture2.a_finger, "aHash") < 3) {
                        temp.add(picture2);
                         picture2.isUse = true;
                    } else if (!picture2.isUse && ImageCVHistogram.comPareHist(picture1.mat, picture2.mat)) {
                        temp.add(picture2);
                         picture2.isUse = true;
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
                if (isDestroyed() || isFinishing()) return;
                updateProgressBar(View.GONE);
                mTimeView.setText("扫描完成耗时 ： " + ((System.currentTimeMillis() - time) / 1000) + " 秒, " + groups.size() + " 组");
                mAdapter.setData(groups);
            }
        });
    }
}
