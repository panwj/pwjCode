package com.ex.simi.duplicate.mvp;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;


import com.ex.simi.duplicate.entity.DuplicatePhotoGroup;
import com.ex.simi.duplicate.entity.PhotoEntity;

import java.util.List;

/**
 * Created by shewenbiao on 18-7-11.
 */

public interface SimilarPhotoContract {

    interface View {
        void showProgress();

        void showList(int count, long size, List<DuplicatePhotoGroup> groupList);

        void hideProgress();

        void showFailed();
    }

    interface Presenter {
        void loadImages();

        void loadSuccess(int count, long size, List<DuplicatePhotoGroup> groupList);

        void loadFailed();

        void destroy();

        void deletePhotos(Context context, AlertDialog deleteDialog, List<PhotoEntity> list);
    }

    interface Model {
        void loadImages();

        void destroy();

        void deletePhotos(Context context, AlertDialog deleteDialog, List<PhotoEntity> list);
    }
}
