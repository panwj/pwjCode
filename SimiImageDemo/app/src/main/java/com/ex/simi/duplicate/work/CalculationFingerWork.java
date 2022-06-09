package com.ex.simi.duplicate.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ex.simi.BuildConfig;
import com.ex.simi.duplicate.util.PhotoRepository;


public class CalculationFingerWork extends Worker {

    private static final String TAG = "similar_image";
    public static final String WORK_TAG = "finger_tag";

    public CalculationFingerWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (BuildConfig.LOG_DEBUG) Log.e(TAG, "CalculationFingerWork() start");
        long time = System.currentTimeMillis();
        PhotoRepository.updateLocalSimilarDB(getApplicationContext());
        if (BuildConfig.LOG_DEBUG)
            Log.e(TAG, "CalculationFingerWork() end : " + ((System.currentTimeMillis() - time) / 1000));
        return Result.success();
    }
}
