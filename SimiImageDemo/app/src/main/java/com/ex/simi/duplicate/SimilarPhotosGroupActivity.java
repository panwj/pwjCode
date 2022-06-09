package com.ex.simi.duplicate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import com.ex.simi.R;
import com.ex.simi.duplicate.entity.PhotoEntity;
import com.ex.simi.duplicate.event.SimilarUpdateEvent;
import com.ex.simi.storage.StorageUtil;
import com.ex.simi.util.PhotoManagerViewPager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class SimilarPhotosGroupActivity extends AppCompatActivity implements View.OnClickListener
        , PhotoRecyclerAdapter.OnItemClickListener, ViewPager.OnPageChangeListener, PhotoPagerFragment.OnPhotoClickListener {

    private static final String EXTRA_PHOTO_LIST = "extra_photo_list";
    private static final String EXTRA_CURRENT_INDEX = "extra_current_index";

    private RecyclerView mRecyclerView;
    private PhotoManagerViewPager mViewPager;
    private View mTopBarView;
    private TextView mTitleTv;
    private TextView mSizeTv;
    private ImageView mSelectedIv;
    private PhotoRecyclerAdapter mRecyclerAdapter;
    private PhotoViewPagerAdapter mViewPagerAdapter;

    private List<PhotoEntity> mPhotoInfoList = new ArrayList<>();
    private int mCurrentIndex;
    private boolean mIsAnimationOut;
    private AnimatorSet mAnimatorSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similar_photos_group);
        initViews();
        Intent intent = getIntent();
        if (intent != null) {
            mPhotoInfoList = intent.getParcelableArrayListExtra(EXTRA_PHOTO_LIST);
            mCurrentIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0);
        }
        if (mPhotoInfoList != null) {
            List<Fragment> list = new ArrayList<>();
            for (PhotoEntity info : mPhotoInfoList) {
                PhotoPagerFragment fragment = PhotoPagerFragment.newInstance(info);
                fragment.setOnPhotoClickListener(this);
                list.add(fragment);
            }
            mViewPagerAdapter = new PhotoViewPagerAdapter(getSupportFragmentManager(), list);
            mViewPager.setAdapter(mViewPagerAdapter);
            mViewPager.addOnPageChangeListener(this);
            mViewPager.setCurrentItem(mCurrentIndex);
            mRecyclerAdapter.setDatas(mPhotoInfoList);
        }
        handleData(mCurrentIndex);
    }

    public void initViews() {
        ImageView backIv = findViewById(R.id.iv_back);
        backIv.setOnClickListener(this);

        mTopBarView = findViewById(R.id.top_bar);
        mViewPager = findViewById(R.id.viewpager);
        mTitleTv = findViewById(R.id.tv_title);
        mSizeTv = findViewById(R.id.tv_size);
        mSelectedIv = findViewById(R.id.iv_select);
        mSelectedIv.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setItemAnimator(null);
        mRecyclerAdapter = new PhotoRecyclerAdapter(this);
        mRecyclerAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mRecyclerAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhotoInfoList != null) {
            mPhotoInfoList.clear();
        }
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(this);
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet.removeAllListeners();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_select:
                PhotoEntity photoInfo = mPhotoInfoList.get(mCurrentIndex);
                if (photoInfo.isChecked) {
                    photoInfo.isChecked = false;
                    mSelectedIv.setImageResource(R.drawable.ic_photo_unselected_big);
                } else {
                    photoInfo.isChecked = true;
                    mSelectedIv.setImageResource(R.drawable.ic_photo_selected_big);
                }
                mRecyclerAdapter.notifyItemChanged(mCurrentIndex);
                EventBus.getDefault().post(new SimilarUpdateEvent(photoInfo));
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        mViewPager.setCurrentItem(position);
    }

    private void handleData(int currentIndex) {
        mCurrentIndex = currentIndex;
        mTitleTv.setText(getResources().getString(R.string.duplicate_photos_group_title, currentIndex + 1, mPhotoInfoList.size()));
        mSizeTv.setText(StorageUtil.convert2Str(mPhotoInfoList.get(currentIndex).size));
        int preIndex = mRecyclerAdapter.getPreIndex();
        mRecyclerAdapter.setPreIndex(currentIndex);
        mRecyclerAdapter.notifyItemChanged(currentIndex);
        mRecyclerAdapter.notifyItemChanged(preIndex);
        mRecyclerView.scrollToPosition(currentIndex);
        mViewPager.setCurrentItem(currentIndex);
        PhotoEntity info = mPhotoInfoList.get(currentIndex);
        if (info.isChecked) {
            mSelectedIv.setImageResource(R.drawable.ic_photo_selected_big);
        } else {
            mSelectedIv.setImageResource(R.drawable.ic_photo_unselected_big);
        }
    }

    public static void start(Context context, int currentIndex, ArrayList<PhotoEntity> list) {
        Intent starter = new Intent(context, SimilarPhotosGroupActivity.class);
        starter.putParcelableArrayListExtra(EXTRA_PHOTO_LIST, list);
        starter.putExtra(EXTRA_CURRENT_INDEX, currentIndex);
        context.startActivity(starter);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        handleData(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPhotoClick() {
        mIsAnimationOut = !mIsAnimationOut;
        PropertyValuesHolder ofFloat;
        PropertyValuesHolder ofFloat2;
        ObjectAnimator ofPropertyValuesHolder;
        PropertyValuesHolder ofFloat3;
        ObjectAnimator ofPropertyValuesHolder2;
        PropertyValuesHolder ofFloat4;
        ObjectAnimator ofPropertyValuesHolder3;
        if (mIsAnimationOut) {
            ofFloat = PropertyValuesHolder.ofFloat("translationY", 0.0f, (float) (-mTopBarView.getHeight()));
            ofFloat2 = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
            ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(mTopBarView, ofFloat, ofFloat2);
            ofFloat2 = PropertyValuesHolder.ofFloat("translationY", 0.0f, (float) mRecyclerView.getHeight());
            ofFloat3 = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
            ofPropertyValuesHolder2 = ObjectAnimator.ofPropertyValuesHolder(mRecyclerView, ofFloat2, ofFloat3);
            ofFloat3 = PropertyValuesHolder.ofFloat("translationY", 0.0f, (float) mSelectedIv.getHeight());
            ofFloat4 = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
            ofPropertyValuesHolder3 = ObjectAnimator.ofPropertyValuesHolder(mSelectedIv, ofFloat3, ofFloat4);
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(ofPropertyValuesHolder, ofPropertyValuesHolder2, ofPropertyValuesHolder3);
            mAnimatorSet.setDuration(300);
            ofPropertyValuesHolder3.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mSelectedIv.setVisibility(View.INVISIBLE);
                }
            });
            mAnimatorSet.start();
            return;
        }
        ofFloat = PropertyValuesHolder.ofFloat("translationY", (float) (-mTopBarView.getHeight()), 0.0f);
        ofFloat2 = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(mTopBarView, ofFloat, ofFloat2);
        ofFloat2 = PropertyValuesHolder.ofFloat("translationY", (float) mRecyclerView.getHeight(), 0.0f);
        ofFloat3 = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        ofPropertyValuesHolder2 = ObjectAnimator.ofPropertyValuesHolder(mRecyclerView, ofFloat2, ofFloat3);
        ofFloat3 = PropertyValuesHolder.ofFloat("translationY", (float) mSelectedIv.getHeight(), 0.0f);
        ofFloat4 = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
        ofPropertyValuesHolder3 = ObjectAnimator.ofPropertyValuesHolder(mSelectedIv, ofFloat3, ofFloat4);
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(ofPropertyValuesHolder, ofPropertyValuesHolder2, ofPropertyValuesHolder3);
        mAnimatorSet.setDuration(300);
        ofPropertyValuesHolder3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mSelectedIv.setVisibility(View.VISIBLE);
            }
        });
        mAnimatorSet.start();
    }
}
