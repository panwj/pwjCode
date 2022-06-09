package com.ex.simi.duplicate;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ex.simi.duplicate.entity.PhotoEntity;
import com.ex.simi.photoview.OnPhotoTapListener;
import com.ex.simi.photoview.PhotoView;

import java.io.File;

/**
 * Created by shewenbiao on 18-7-17.
 */

public class PhotoPagerFragment extends Fragment {

    private static final String BUNDLE_PHOTO_INFO = "bundle_photo_info";

    private PhotoEntity mPhotoInfo;
    private OnPhotoClickListener mOnPhotoClickListener;

    public static PhotoPagerFragment newInstance(PhotoEntity photoInfo) {
        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_PHOTO_INFO, photoInfo);
        PhotoPagerFragment fragment = new PhotoPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static PhotoPagerFragment newInstance() {
        Bundle args = new Bundle();
        PhotoPagerFragment fragment = new PhotoPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPhotoInfo = bundle.getParcelable(BUNDLE_PHOTO_INFO);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            PhotoView photoView = new PhotoView(activity);
            photoView.setFitsSystemWindows(true);
            photoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            photoView.setOnPhotoTapListener(new PhotoTapListenerImpl());
            if (mPhotoInfo != null) {
                Glide.with(activity).load(new File(mPhotoInfo.path))
                        .apply(new RequestOptions()
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                        .into(photoView);
                //DiskCacheStrategy.AUTOMATIC 它会尝试对本地和远程图片使用最佳的策略。
                // 当你加载远程数据（比如，从URL下载）时，AUTOMATIC%20策略仅会存储未被你的加载过程修改过(比如，变换，裁剪–译者注)的原始数据，因为下载远程数据相比调整磁盘上已经存在的数据要昂贵得多。
                // 对于本地数据，AUTOMATIC%20策略则会仅存储变换过的缩略图，因为即使你需要再次生成另一个尺寸或类型的图片，取回原始数据也很容易。默认使用这种缓存策略
            }
            return photoView;
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    private class PhotoTapListenerImpl implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            if (mOnPhotoClickListener != null) {
                mOnPhotoClickListener.onPhotoClick();
            }
        }
    }

    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        mOnPhotoClickListener = onPhotoClickListener;
    }

    public interface OnPhotoClickListener {
        void onPhotoClick();
    }

    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }
}