package com.ex.simi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
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
import com.ex.simi.entry.DuplicatePhotoGroup;
import com.ex.simi.entry.PhotoEntity;
import com.ex.simi.normal.ImageHashUtil;
import com.ex.simi.util.Logv;
import com.ex.simi.util.PhotoRepository;

import org.opencv.core.Mat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimiImageActivity extends AppCompatActivity {

    private static final long TIME_ = 24 * 60 * 60;

    private RecyclerView mRecycleView;
    private SimiAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mTimeView;
    private Handler mHandler;
    private int sampleSize, similarCount, simiGroup, groupCount;
    private boolean dHash, aHash, opencv, desc, pro1, pro2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simi_image_activity_layout);

        mRecycleView = findViewById(R.id.recycle);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, 3));
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
                calculationSimilarPhoto();
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

    private void calculationSimilarPhoto() {
        Logv.e("calculationSimilarPhoto() start");
        long time = System.currentTimeMillis();

        PhotoRepository.updateLocalSimilarDB(getApplicationContext());

        Logv.e("更新数据库 ---> " + (System.currentTimeMillis() - time) / 1000);

        LinkedHashMap<PhotoEntity, List<PhotoEntity>> linkedHashMap = new LinkedHashMap<>();

        List<String> groupTime = PictureDaoManager.getDatabase(getApplicationContext()).getPictureDao().getPhotoGroupByTime();
        for (String formatTime : groupTime) {
            List<PhotoEntity> list = PictureDaoManager.getDatabase(getApplicationContext()).getPictureDao().getPhotoByFormatTime(formatTime);
            if (/*list.size() > 1*/true) linkedHashMap.put(list.get(0), list);
        }
        Logv.e("第一次分组 ---> " + (System.currentTimeMillis() - time) / 1000 + "    groupTime ： " + groupTime.size());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<DuplicatePhotoGroup> groupList = new ArrayList<>();

        long similarSize = 0;
        for (Map.Entry key : linkedHashMap.entrySet()) {
            List<PhotoEntity> tempGroup = linkedHashMap.get(key.getKey());
            for (int i = 0; i < tempGroup.size(); i++) {
                PhotoEntity picture1 = tempGroup.get(i);

                if (!picture1.isUse) {
                    ArrayList<PhotoEntity> temp = new ArrayList<>();
                    temp.add(picture1);

                    for (int j = i + 1; j < tempGroup.size(); j++) {
                        PhotoEntity picture2 = tempGroup.get(j);
                        if (!picture2.isUse && ImageHashUtil.hammingDistance(picture1.d_finger, picture2.d_finger) < 2) {
                            temp.add(picture2);
                            picture2.isUse = true;
                        } else if (!picture2.isUse && ImageHashUtil.hammingDistance(picture1.a_finger, picture2.a_finger) < 2) {
                            temp.add(picture2);
                            picture2.isUse = true;
                        }
                    }

                    if (temp.size() >= 2) {
                        long groupFileSize = 0;
                        PhotoEntity bestPhoto = temp.get(0);
                        for (PhotoEntity info : temp) {
                            info.isChecked = true;
                            info.isBestPhoto = false;
                            if (info.size > bestPhoto.size) {
                                bestPhoto = info;
                            }
                            groupFileSize = groupFileSize + info.size;
                        }
                        bestPhoto.isBestPhoto = true;
                        bestPhoto.isChecked = false;

                        similarCount = similarCount + temp.size();
                        simiGroup = simiGroup + 1;
                        similarSize = similarSize + groupFileSize;

                        DuplicatePhotoGroup duplicatePhotoGroup = new DuplicatePhotoGroup();
                        duplicatePhotoGroup.setPhotoInfoList(temp);
                        duplicatePhotoGroup.setGroupFileSize(groupFileSize);
                        duplicatePhotoGroup.setTimeName(simpleDateFormat.format(new Date(temp.get(0).time)));
                        groupList.add(duplicatePhotoGroup);

                    }

                    groupCount = groupCount + 1;
                }
            }
        }
        long lastTime = (System.currentTimeMillis() - time) / 1000;
        Logv.e("完成时间 ---> " + lastTime);
        int count = PictureDaoManager.getDatabase(getApplicationContext()).getPictureDao().getPhoto().size();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isDestroyed() || isFinishing()) return;
                updateProgressBar(View.GONE);
                mTimeView.setText("扫描耗时 ：" + lastTime + " 秒, " + groupList.size() + "/" + groupCount + "组，" + similarCount + "/" + count + "张");
                mAdapter.setData(groupList);
            }
        });

    }
}
