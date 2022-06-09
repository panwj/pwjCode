package com.ex.simi.duplicate.mvp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Process;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;


import com.ex.simi.SimiImageApplication;
import com.ex.simi.dao.AppDatabaseManager;
import com.ex.simi.duplicate.entity.DuplicatePhotoGroup;
import com.ex.simi.duplicate.entity.PhotoEntity;
import com.ex.simi.duplicate.work.SimilarWorkerController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shewenbiao on 18-7-11.
 */

public class SimilarPhotoModel implements SimilarPhotoContract.Model, SimilarWorkerController.OnSimilarScanListener {

    private SimilarPhotoPresenter mPresenter;

    public SimilarPhotoModel(SimilarPhotoPresenter presenter) {
        mPresenter = presenter;
        SimilarWorkerController.getInstance().setSimilarScanListener(this);
    }

    @Override
    public void loadImages() {
        SimilarWorkerController.getInstance().doSimilarWork(SimiImageApplication.getApp());
    }

    @Override
    public void destroy() {
    }

    @Override
    public void onSimilarScanStarted() {

    }

    @Override
    public void onSimilarSingleCompleted(DuplicatePhotoGroup group) {

    }

    @Override
    public void onSimilarScanAllCompleted(int count, long size, List<DuplicatePhotoGroup> list) {
        if (mPresenter != null) {
            mPresenter.loadSuccess(count, size, list);
        }
    }

    @Override
    public void deletePhotos(Context context, AlertDialog deleteDialog, final List<PhotoEntity> list) {
        new Thread(new DeletePhotosRunnable(context, deleteDialog, list)).start();
    }

    private class DeletePhotosRunnable implements Runnable {

        private List<PhotoEntity> mList = new ArrayList<>();
        private final Context mContext;
        private final ContentResolver mContentResolver;
        private final AlertDialog mDeleteDialog;

        public DeletePhotosRunnable(Context context, AlertDialog deleteDialog, List<PhotoEntity> list) {
            mList.clear();
            if (list != null) mList.addAll(list);
            mContext = context;
            mContentResolver = context.getContentResolver();
            this.mDeleteDialog = deleteDialog;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if (mList == null || mList.isEmpty()) return;

            for (PhotoEntity photoInfo : mList) {
                try {
                    synchronized (mContentResolver) {
                        mContentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "_id = " + photoInfo.id, null);
                    }
                    File file = new File(photoInfo.path);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            AppDatabaseManager.getDatabase(mContext).getSimilarPhotoDao().deletePhotos(mList);
            ((Activity) mContext).runOnUiThread(new Runnable() {//传进来的是activity所以可以强转
                @Override
                public void run() {
                    if (mDeleteDialog != null) mDeleteDialog.dismiss();
                }
            });
        }
    }
}
