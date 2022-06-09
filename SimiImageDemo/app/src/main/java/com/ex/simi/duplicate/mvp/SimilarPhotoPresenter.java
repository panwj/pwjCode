package com.ex.simi.duplicate.mvp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.ex.simi.R;
import com.ex.simi.duplicate.entity.ContentItemBean;
import com.ex.simi.duplicate.entity.DuplicatePhotoGroup;
import com.ex.simi.duplicate.entity.HeaderItemBean;
import com.ex.simi.duplicate.entity.PhotoEntity;

import java.util.List;


/**
 * Created by shewenbiao on 18-7-11.
 */

public class SimilarPhotoPresenter implements SimilarPhotoContract.Presenter {

    private SimilarPhotoContract.Model mModel;
    private SimilarPhotoContract.View mView;

    public SimilarPhotoPresenter(SimilarPhotoContract.View view) {
        mView = view;
        mModel = new SimilarPhotoModel(this);
    }

    @Override
    public void loadImages() {
        if (mView != null) {
            mView.showProgress();
        }
        if (mModel != null) {
            mModel.loadImages();
        }
    }

    @Override
    public void loadSuccess(int count, long size, List<DuplicatePhotoGroup> groupList) {
        if (mView != null) {
            mView.hideProgress();
            mView.showList(count, size, groupList);
        }
    }

    @Override
    public void loadFailed() {
        if (mView != null) {
            mView.hideProgress();
            mView.showFailed();
        }
    }

    @Override
    public void destroy() {
        mView = null;
        if (mModel != null) {
            mModel.destroy();
            mModel = null;
        }
    }

    @Override
    public void deletePhotos(Context context, AlertDialog alertDialog, List<PhotoEntity> list) {
        if (mModel != null) {
            mModel.deletePhotos(context, alertDialog, list);
        }
    }

    /**
     * 统计一组中被选中的 object 的个数与file size
     * @param headerItemBean
     * @return
     */
    public static long[] calculateGroupInfo(HeaderItemBean headerItemBean) {
        long[] infoArr = new long[2];
        int groupCount = 0;
        long groupSize = 0;
        for (BaseNode baseNode : headerItemBean.getChildNode()) {
            ContentItemBean contentItemBean = (ContentItemBean) baseNode;
            PhotoEntity info = contentItemBean.mInfoBean;
            if (info.isChecked) {
                groupSize += info.size;
                groupCount++;
            }
        }
        infoArr[0] = groupCount;
        infoArr[1] = groupSize;
        return infoArr;
    }

    public static AlertDialog showProgressDialog(Activity activity, String tip) {
        AlertDialog dialog = null;
        if (activity == null || activity.isFinishing()) return dialog;
        if (TextUtils.isEmpty(tip)) {
            tip = activity.getResources().getString(R.string.loading);
        }
        dialog = new AlertDialog.Builder(activity, R.style.CustomProgressDialog).create();

        View loadView = LayoutInflater.from(activity).inflate(R.layout.dlg_progress_custom_light, null);
        TextView tvTip = loadView.findViewById(R.id.tv_tip);

        tvTip.setText(tip);
        dialog.setView(loadView, 0, 0, 0, 0);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    dialog.dismiss();
                    activity.finish();
                    return true;
                }
                return false;
            }
        });
        if (!activity.isFinishing()) dialog.show();
        return dialog;
    }
}
