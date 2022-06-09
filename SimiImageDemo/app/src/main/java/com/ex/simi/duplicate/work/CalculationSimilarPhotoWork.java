package com.ex.simi.duplicate.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.ex.simi.BuildConfig;
import com.ex.simi.dao.AppDatabaseManager;
import com.ex.simi.duplicate.entity.DuplicatePhotoGroup;
import com.ex.simi.duplicate.entity.PhotoEntity;
import com.ex.simi.duplicate.event.SimilarCompletedEvent;
import com.ex.simi.duplicate.event.SimilarSingleCompletedEvent;
import com.ex.simi.duplicate.util.ImageHashUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CalculationSimilarPhotoWork extends Worker {

    private static final String TAG = "similar_image";
    public static final String WORK_TAG = "similar_tag";

    public CalculationSimilarPhotoWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (BuildConfig.LOG_DEBUG) Log.e(TAG, "CalculationSimilarPhotoWork() start");
        long time = System.currentTimeMillis();
        calculationSimilarPhoto();
        if (BuildConfig.LOG_DEBUG)
            Log.e(TAG, "CalculationSimilarPhotoWork() end : " + ((System.currentTimeMillis() - time) / 1000));
        return Result.success();
    }

    private void calculationSimilarPhoto() {
        if (BuildConfig.LOG_DEBUG) Log.e(TAG, "calculationSimilarPhoto() start");
        long time = System.currentTimeMillis();

        LinkedHashMap<PhotoEntity, List<PhotoEntity>> linkedHashMap = new LinkedHashMap<>();

        List<String> groupTime = AppDatabaseManager.getDatabase(getApplicationContext()).getSimilarPhotoDao().getPhotoGroupByTime();
        for (String formatTime : groupTime) {
            List<PhotoEntity> list = AppDatabaseManager.getDatabase(getApplicationContext()).getSimilarPhotoDao().getPhotoByFormatTime(formatTime);
            if (list.size() > 1) linkedHashMap.put(list.get(0), list);
        }

        if (BuildConfig.LOG_DEBUG) Log.e(TAG, "第一次分组（按天分组）耗时 : "
                + ((System.currentTimeMillis() - time) / 1000)
                + "  查询到的分组：" + groupTime.size()
                + "  最终分组结果：" + linkedHashMap.size());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<DuplicatePhotoGroup> groupList = new ArrayList<>();
        int similarCount = 0;
        long similarSize = 0;
        int groupId = 0;
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
                        Collections.sort(temp, Collections.reverseOrder());
                        long groupFileSize = 0;
                        for (PhotoEntity info : temp) {
                            info.groupId = groupId;
                            groupFileSize = groupFileSize + info.size;
                        }

                        PhotoEntity bestPhoto = temp.get(0);
                        bestPhoto.isBestPhoto = true;
                        bestPhoto.isChecked = false;

                        similarCount = similarCount + temp.size();
                        similarSize = similarSize + groupFileSize;

                        DuplicatePhotoGroup duplicatePhotoGroup = new DuplicatePhotoGroup();
                        duplicatePhotoGroup.setGroupId(groupId);
                        duplicatePhotoGroup.setPhotoInfoList(temp);
                        duplicatePhotoGroup.setGroupFileSize(groupFileSize);
                        duplicatePhotoGroup.setTimeName(simpleDateFormat.format(new Date(temp.get(0).time)));
                        groupList.add(duplicatePhotoGroup);
                        groupId++;

                        SimilarSingleCompletedEvent event = new SimilarSingleCompletedEvent();
                        event.setDuplicatePhotoGroup(duplicatePhotoGroup);
                        EventBus.getDefault().post(event);
                    }
                }
            }
        }
        if (BuildConfig.LOG_DEBUG)
            Log.e(TAG, "最终结果   相似照片组： " + groupList.size() + "   相似照片数： " + similarCount);

        SimilarCompletedEvent event = new SimilarCompletedEvent();
        event.setList(groupList);
        event.setCount(similarCount);
        event.setSize(similarSize);
        EventBus.getDefault().post(event);
    }
}
