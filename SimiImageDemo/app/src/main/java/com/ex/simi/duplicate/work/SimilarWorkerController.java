package com.ex.simi.duplicate.work;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


import com.ex.simi.duplicate.entity.DuplicatePhotoGroup;
import com.ex.simi.duplicate.event.SimilarCompletedEvent;
import com.ex.simi.duplicate.event.SimilarSingleCompletedEvent;
import com.ex.simi.duplicate.event.SimilarStartEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class SimilarWorkerController {

    public static final String SIMILAR_WORK_TAG = "similar_work_tag";
    private static volatile SimilarWorkerController mInstance;
    private OnSimilarScanListener mOnSimilarScanListener;

    private SimilarWorkerController() {

    }

    public static SimilarWorkerController getInstance() {
        if (mInstance == null) {
            synchronized (SimilarWorkerController.class) {
                if (mInstance == null)
                    mInstance = new SimilarWorkerController();
            }
        }
        return mInstance;
    }

    public void doSimilarWork(Context context) {
        registerEvent();

        WorkManager workManager = WorkManager.getInstance(context);
        OneTimeWorkRequest fingerWork = new OneTimeWorkRequest.Builder(CalculationFingerWork.class)
                .addTag(CalculationFingerWork.WORK_TAG)
                .build();
        OneTimeWorkRequest similarWork = new OneTimeWorkRequest.Builder(CalculationSimilarPhotoWork.class)
                .addTag(CalculationSimilarPhotoWork.WORK_TAG)
                .build();
        workManager.beginUniqueWork(SIMILAR_WORK_TAG, ExistingWorkPolicy.KEEP, fingerWork)
                .then(similarWork)
                .enqueue();
    }

    public void setSimilarScanListener(OnSimilarScanListener listener) {
        this.mOnSimilarScanListener = listener;
    }

    public void cancelSimilarWork(Context context) {
        unregisterEvent();

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(CalculationSimilarPhotoWork.WORK_TAG);
    }

    private void registerEvent() {
        if (EventBus.getDefault().isRegistered(this)) return;
        EventBus.getDefault().register(this);
    }

    private void unregisterEvent() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SimilarStartEvent event) {
        if (mOnSimilarScanListener != null) mOnSimilarScanListener.onSimilarScanStarted();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SimilarSingleCompletedEvent event) {
        if (mOnSimilarScanListener != null)
            mOnSimilarScanListener.onSimilarSingleCompleted(event.getDuplicatePhotoGroup());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SimilarCompletedEvent event) {
        if (mOnSimilarScanListener != null)
            mOnSimilarScanListener.onSimilarScanAllCompleted(event.getCount(), event.getSize(), event.getList());
    }

    public interface OnSimilarScanListener {
        void onSimilarScanStarted();

        void onSimilarSingleCompleted(DuplicatePhotoGroup group);

        void onSimilarScanAllCompleted(int count, long size, List<DuplicatePhotoGroup> list);
    }
}
