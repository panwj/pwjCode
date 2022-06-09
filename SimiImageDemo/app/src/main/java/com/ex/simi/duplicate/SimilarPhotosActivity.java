package com.ex.simi.duplicate;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.ex.simi.R;
import com.ex.simi.duplicate.adapter.SimilarContentAdapter;
import com.ex.simi.duplicate.entity.ContentItemBean;
import com.ex.simi.duplicate.entity.DuplicatePhotoGroup;
import com.ex.simi.duplicate.entity.HeaderItemBean;
import com.ex.simi.duplicate.entity.PhotoEntity;
import com.ex.simi.duplicate.event.SimilarUpdateEvent;
import com.ex.simi.duplicate.mvp.SimilarPhotoContract;
import com.ex.simi.duplicate.mvp.SimilarPhotoPresenter;
import com.ex.simi.storage.StorageUtil;
import com.ex.simi.superclass.ExpendNode;
import com.ex.simi.util.DisplayUtil;
import com.ex.simi.util.ExternalStorageUtil;
import com.ex.simi.util.FileUtilDialog;
import com.ex.simi.util.GlobalConsts;
import com.ex.simi.util.PermissionHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class SimilarPhotosActivity extends AppCompatActivity implements SimilarPhotoContract.View, View.OnClickListener
        , CompoundButton.OnCheckedChangeListener, OnItemClickListener, OnItemChildClickListener,
        PermissionHelper.PermissionCallbacks {

    private SimilarPhotoContract.Presenter mPresenter;

    private SimilarContentAdapter mAdapter;
    private List<HeaderItemBean> mHeaderItemList = new ArrayList<>();
    private HashSet<PhotoEntity> mSelectedFileSet = new HashSet<>();
    private HashSet<PhotoEntity> mSelectedBestSet = new HashSet<>();
    private List<DuplicatePhotoGroup> mGroupList = new ArrayList<>();

    private TextView mDeleteTv;
    private View mEmptyView, mBackIv;
    private CheckBox mSelectedAllCb;
    private AlertDialog mLoadingDlg;

    private int mTotalCount = 0;
    private long mTotalDeleteSize;

    private Disposable mGroupDisposable;
    private Disposable mSelectDisposable;
    private Disposable mDeleteDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similar_photos);
        EventBus.getDefault().register(this);
        initViews();
        mPresenter = new SimilarPhotoPresenter(this);
        if (PermissionHelper.hasStoragePermissions(this))
            mPresenter.loadImages();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (mPresenter != null) mPresenter.loadImages();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.SETTINGS_REQUEST_CODE || requestCode == ExternalStorageUtil.MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE) {
            if (PermissionHelper.hasStoragePermissions(this)) {
                if (mPresenter != null) mPresenter.loadImages();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void initViews() {
        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mSelectedAllCb = findViewById(R.id.iv_select_all);
        mSelectedAllCb.setOnCheckedChangeListener(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);
        ((DefaultItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mAdapter = new SimilarContentAdapter(new ArrayList<>());
        mAdapter.addChildClickViewIds(R.id.iv_select); // 先注册需要点击的子控件id（注意，请不要写在convert方法里）
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemChildClickListener(this);
        int mSpaceCount = 4;
        recyclerView.setLayoutManager(new GridLayoutManager(this, mSpaceCount));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new SpaceItemDecoration(DisplayUtil.dip2px(this, 1), mSpaceCount));

        mDeleteTv = findViewById(R.id.tv_delete);
        mDeleteTv.setOnClickListener(this);
        updateDeleteTv(mTotalDeleteSize);
    }

    @Override
    public void showProgress() {
        if (mLoadingDlg == null) mLoadingDlg = SimilarPhotoPresenter.showProgressDialog(this, null);
        if (mLoadingDlg != null) mLoadingDlg.show();
    }

    @Override
    public void showList(int count, long size, List<DuplicatePhotoGroup> groupList) {
        mTotalCount = count;
        if (isFinishing()) return;

        if (groupList == null || groupList.isEmpty()) {
            updateSelectedAllUI(false);
            showEmptyView();
        } else {
            mSelectedFileSet.clear();
            mSelectedBestSet.clear();
            mHeaderItemList.clear();
            mGroupList.clear();
            mGroupList.addAll(groupList);
            if (mGroupDisposable != null && !mGroupDisposable.isDisposed())
                mGroupDisposable.dispose();
            mGroupDisposable = Observable.create(new ObservableOnSubscribe<List<HeaderItemBean>>() {
                @Override
                public void subscribe(@NonNull ObservableEmitter<List<HeaderItemBean>> emitter) throws Exception {
                    List<HeaderItemBean> list = new ArrayList<>();
                    for (DuplicatePhotoGroup group : groupList) {
                        HeaderItemBean headerItemBean = new HeaderItemBean(group);//contentItemBean的初始化在这里
                        headerItemBean.setExpanded(true);
                        headerItemBean.mIsSelected = true;
                        list.add(headerItemBean);
                        mSelectedFileSet.addAll(group.getPhotoInfoListExcludeBest());
                        long noBestPicGroupSize = group.getGroupFileSize() - group.getBestPhoto().size;
                        mTotalDeleteSize += noBestPicGroupSize;
                    }
                    emitter.onNext(list);
                    emitter.onComplete();
                }
            }).subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<HeaderItemBean>>() {
                        @Override
                        public void accept(List<HeaderItemBean> list) throws Exception {
                            mHeaderItemList.clear();
                            mHeaderItemList.addAll(list);
                            mAdapter.replaceData(mHeaderItemList);
                            updateSelectedAllUI(true);
                            updateDeleteTv(mTotalDeleteSize);
                            Log.e("pwj", "mHeaderItemList = " + mHeaderItemList.size() + "  mTotalCount = " + mTotalCount + "  mSelectedBestSet = " + mSelectedBestSet.size());
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                        }
                    });
        }
    }

    @Override
    public void hideProgress() {
        if (mLoadingDlg != null) mLoadingDlg.dismiss();
    }

    @Override
    public void showFailed() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_delete:
                showDeleteConfirmDialog();
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        BaseNode entity = mAdapter.getItem(position);
        if (entity instanceof ContentItemBean) {
            ContentItemBean itemBean = (ContentItemBean) entity;
            handleSubItem(itemBean, position);
        } else if (entity instanceof HeaderItemBean) {
            HeaderItemBean headerItemBean = (HeaderItemBean) entity;
            headerItemBean.mIsSelected = !headerItemBean.mIsSelected;
            List<BaseNode> contentItemBeanList = new ArrayList<>(headerItemBean.getChildNode());
            int i = 0;
            long includeSize = 0;//统计已经计算过的size
            for (BaseNode baseNode : contentItemBeanList) {
                ContentItemBean contentItemBean = (ContentItemBean) baseNode;
                if (contentItemBean.mInfoBean.isChecked) {
                    includeSize += contentItemBean.mInfoBean.size;
                }
                if (contentItemBean.mInfoBean.isBestPhoto) {
                    contentItemBean.mInfoBean.isChecked = false;
                } else {
                    contentItemBean.mInfoBean.isChecked = headerItemBean.mIsSelected;
                }
                if (headerItemBean.isExpanded()) {
                    mAdapter.notifyItemChanged(position + i + 1, contentItemBean);
                }
                handleSelectSingleItem(contentItemBean.mInfoBean);
                i++;
            }

            //先更新子item状态，再更新头部
            if (headerItemBean.mIsSelected) {
                long[] infoArr = SimilarPhotoPresenter.calculateGroupInfo(headerItemBean);
                mTotalDeleteSize += infoArr[1] - includeSize;//减去已经算进总size的item
            } else {
                mTotalDeleteSize -= includeSize;
            }
            mAdapter.notifyItemChanged(position, headerItemBean);
        }
        updateDeleteTv(mTotalDeleteSize);
        updateSelectedAllUI(true);
    }

    void handleSubItem(ContentItemBean itemBean, int position) {
        itemBean.mInfoBean.isChecked = !itemBean.mInfoBean.isChecked;
        HeaderItemBean headerItemBean = itemBean.getHeaderItemBean();
        if (itemBean.mInfoBean.isChecked) {
            mTotalDeleteSize += itemBean.mInfoBean.size;
        } else {
            mTotalDeleteSize -= itemBean.mInfoBean.size;
        }
        mAdapter.notifyItemChanged(position, itemBean);
        List<BaseNode> contentItemBeanList = new ArrayList<>(headerItemBean.getChildNode());
        boolean isSelected = true;
        for (BaseNode baseNode : contentItemBeanList) {
            ContentItemBean contentItemBean = (ContentItemBean) baseNode;
            if (!contentItemBean.mInfoBean.isChecked && !contentItemBean.mInfoBean.isBestPhoto) {
                isSelected = false;
                break;
            }
        }
        headerItemBean.mIsSelected = isSelected;
        int groupPosition = mAdapter.getData().indexOf(headerItemBean);
        mAdapter.notifyItemChanged(groupPosition, headerItemBean);
        handleSelectSingleItem(itemBean.mInfoBean);
    }

    private void handleSelectSingleItem(PhotoEntity infoBean) {
        if (infoBean.isChecked) {
            if (infoBean.isBestPhoto) mSelectedBestSet.add(infoBean);
            mSelectedFileSet.add(infoBean);
        } else {
            mSelectedBestSet.remove(infoBean);
            mSelectedFileSet.remove(infoBean);
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        BaseNode entity = mAdapter.getItem(position);
        if (entity instanceof ContentItemBean) {
            ContentItemBean itemBean = (ContentItemBean) entity;
            ArrayList<PhotoEntity> list = itemBean.getHeaderItemBean().getPhotoGroup().getPhotoInfoList();
            int currentIndex = list.indexOf(itemBean.mInfoBean);
            SimilarPhotosGroupActivity.start(this, currentIndex, list);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!buttonView.isPressed()) return;
        mSelectedFileSet.clear();
        mSelectedBestSet.clear();
        if (mSelectDisposable != null && !mSelectDisposable.isDisposed())
            mSelectDisposable.dispose();
        mSelectDisposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Exception {
                for (MultiItemEntity multiItemEntity : mHeaderItemList) {
                    HeaderItemBean headerItemBean = (HeaderItemBean) multiItemEntity;
                    headerItemBean.mIsSelected = isChecked;
                    List<BaseNode> contentItemBeanList = new ArrayList<>(headerItemBean.getChildNode());
                    for (BaseNode baseNode : contentItemBeanList) {
                        ContentItemBean contentItemBean = (ContentItemBean) baseNode;
                        PhotoEntity info = contentItemBean.mInfoBean;
                        if (info.isBestPhoto) {
                            if (info.isChecked) mTotalDeleteSize -= info.size;//减去未选中的照片size
                            info.isChecked = false;
                        } else {
                            if (!info.isChecked) mTotalDeleteSize += info.size;//加上没有算进总size的item
                            info.isChecked = isChecked;
                        }
                    }
                    if (isChecked)
                        mSelectedFileSet.addAll(headerItemBean.getPhotoGroup().getPhotoInfoListExcludeBest());
                }
                emitter.onNext(0);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        mAdapter.notifyDataSetChanged();
                        if (!isChecked) mTotalDeleteSize = 0;
                        updateDeleteTv(mTotalDeleteSize);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }

    private void updateSelectedAllUI(boolean visibility) {
        boolean isSelectedAll = mSelectedFileSet.size() - mSelectedBestSet.size() == mTotalCount - mHeaderItemList.size();
        if (mSelectedAllCb != null) {
            mSelectedAllCb.setChecked(isSelectedAll);
            mSelectedAllCb.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }

    private void updateDeleteTv(long size) {
        if (size <= 0) {
            mDeleteTv.setText(getResources().getString(R.string.duplicate_photos_btn_delete, ""));
            mDeleteTv.setEnabled(false);
        } else {
            mDeleteTv.setText(getResources().getQuantityString(R.plurals.duplicate_photos_delete_btn_text, mSelectedFileSet.size(), mSelectedFileSet.size(), StorageUtil.convert2Str(size)));
            mDeleteTv.setEnabled(true);
        }
    }

    private void showEmptyView() {
        if (mEmptyView == null) {
            ViewStub stub = findViewById(R.id.stub_empty_view);
            mEmptyView = stub.inflate();
        }
        if (mEmptyView != null) mEmptyView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mGroupList != null) mGroupList.clear();
        if (mSelectedFileSet != null) mSelectedFileSet.clear();
        if (mPresenter != null) mPresenter.destroy();
        if (mLoadingDlg != null) mLoadingDlg.cancel();
        if (mBackIv != null) mBackIv.setOnClickListener(null);
        if (mSelectedAllCb != null) mSelectedAllCb.setOnCheckedChangeListener(null);
        if (mGroupDisposable != null && !mGroupDisposable.isDisposed()) mGroupDisposable.dispose();
        if (mSelectDisposable != null && !mSelectDisposable.isDisposed())
            mSelectDisposable.dispose();
    }

    private void showDeleteConfirmDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.similar_photo_delete_confirm_dialog, null);
        TextView title = view.findViewById(R.id.tv_title);
        TextView content = view.findViewById(R.id.tv_content);
        TextView cancel = view.findViewById(R.id.tv_no);
        TextView ok = view.findViewById(R.id.tv_ok);
        int count = mSelectedFileSet.size();
        title.setText(getResources().getQuantityString(R.plurals.duplicate_photos_delete_title, count, count));
        int contentId = mSelectedBestSet.size() > 0 ? R.string.duplicate_photos_delete_tip_best : R.string.duplicate_photos_delete_tip;
        content.setText(getResources().getString(contentId));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && !isFinishing()) dialog.dismiss();
                deleteSelectedPhotos();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && !isFinishing()) dialog.dismiss();
            }
        });
        if (!isFinishing()) {
            try {
                dialog.show();
            } catch (Exception e) {

            }
        }
    }

    private void deleteSelectedPhotos() {
        if (mPresenter == null || isFinishing()) return;
        List<PhotoEntity> list = new ArrayList<>(mSelectedFileSet);
        for (PhotoEntity photoInfo : list) {
            Iterator<HeaderItemBean> iterator = mHeaderItemList.iterator();
            while (iterator.hasNext()) {
                HeaderItemBean headerItemBean = (HeaderItemBean) iterator.next();
                DuplicatePhotoGroup group = headerItemBean.getPhotoGroup();
                if (photoInfo.groupId == group.getGroupId()) {
                    Iterator<BaseNode> iterator1 = new ArrayList<>(headerItemBean.getChildNode()).iterator();
                    while (iterator1.hasNext()) {
                        ContentItemBean contentItemBean = (ContentItemBean) iterator1.next();
                        if (contentItemBean.mInfoBean.equals(photoInfo)) {
                            removeItem(contentItemBean);
                            group.updatePhotoInfoList(photoInfo);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        List<HeaderItemBean> headerList = new ArrayList<>();
        for (MultiItemEntity multiItemEntity : mHeaderItemList) {
            HeaderItemBean headerItemBean = (HeaderItemBean) multiItemEntity;
            if (headerItemBean.hasSubItem()) {
                if (headerItemBean.getChildNode().size() < 2)
                    headerList.add(headerItemBean);
            } else {
                headerList.add(headerItemBean);
            }
        }
        for (HeaderItemBean headerItemBean : headerList) {
            Iterator<HeaderItemBean> iterator1 = mHeaderItemList.iterator();
            while (iterator1.hasNext()) {
                HeaderItemBean headerItemBean1 = (HeaderItemBean) iterator1.next();
                if (headerItemBean1.getPhotoGroup().getGroupId() == headerItemBean.getPhotoGroup().getGroupId()) {
                    removeItem(headerItemBean1);
                    break;
                }
            }
        }

        if (mAdapter.getItemCount() == 0) {
            updateSelectedAllUI(false);
            showEmptyView();
        }

        AlertDialog deletingDialog = FileUtilDialog.showSimilarPhotosDeletingDialog(SimilarPhotosActivity.this, new FileUtilDialog.DialogListener() {
            @Override
            public void onOK() {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onDismiss() {//Delete completed！
                FileUtilDialog.showFileDeletedDialog(SimilarPhotosActivity.this, list.size(), null);
                mSelectedFileSet.clear();
                mSelectedBestSet.clear();
                mHeaderItemList.removeAll(headerList);
                mTotalCount = mTotalCount - list.size();
                mTotalDeleteSize = 0;
                updateDeleteTv(mTotalDeleteSize);
            }
        });
        mPresenter.deletePhotos(this, deletingDialog, list);
    }

    /**
     * 数据源的remove操作
     */
    private void removeItem(MultiItemEntity item) {
        if (item instanceof ContentItemBean) {
            ContentItemBean contentItemBean = (ContentItemBean) item;
            HeaderItemBean headerItemBean = contentItemBean.getHeaderItemBean();
            mAdapter.nodeRemoveData(headerItemBean, contentItemBean);//3.X后关于父节点是否展开和子节点notify的方法都封装在此了
            int pos = mAdapter.getData().indexOf(headerItemBean);
            ExpendNode.notifyParentNodeChanged(mAdapter, headerItemBean, pos);
        } else if (item instanceof HeaderItemBean) {
            HeaderItemBean headerItemBean = (HeaderItemBean) item;
            mAdapter.remove(headerItemBean);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SimilarUpdateEvent event) {
        PhotoEntity photoInfo = event.getPhotoInfo();
        if (photoInfo != null) {
            for (MultiItemEntity multiItemEntity : mHeaderItemList) {
                HeaderItemBean headerItemBean = (HeaderItemBean) multiItemEntity;
                int groupPos = mAdapter.getItemPosition(headerItemBean);
                if (photoInfo.groupId == headerItemBean.getPhotoGroup().getGroupId()) {
                    if (headerItemBean.getChildNode() != null)
                        for (int i = 0; i < headerItemBean.getChildNode().size(); i++) {
                            ContentItemBean contentItemBean = (ContentItemBean) headerItemBean.getSubItem(i);
                            if (contentItemBean.mInfoBean.equals(photoInfo)) {
                                handleSubItem(contentItemBean, groupPos + i + 1);//该方法中已经更新了mTotalDeleteSize
                            }
                        }
                }
            }
            updateDeleteTv(mTotalDeleteSize);
            updateSelectedAllUI(true);
        }
    }

    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int space;

        public SpaceItemDecoration(int space, int spanCount) {
            this.space = space;
            this.spanCount = spanCount;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            SimilarContentAdapter adapter = (SimilarContentAdapter) parent.getAdapter();
            if (adapter == null || position < 0) {
                return;
            }
            if (adapter.getItemViewType(position) == GlobalConsts.TYPE_CONTENT_PIC) {
                BaseNode infoBean = adapter.getItem(position);
                if (infoBean instanceof ContentItemBean) {
                    ContentItemBean itemBean = (ContentItemBean) infoBean;
                    int groupPos = adapter.getData().indexOf(itemBean.getHeaderItemBean());
                    int newPosition = (position - groupPos - 1);
                    int column = newPosition % spanCount; // view 所在的列(从0开始）
                    outRect.left = column * space / spanCount; // column * (列间距 * (1f / 列数))
                    outRect.right = space - (column + 1) * space / spanCount; // 列间距 - (column + 1) * (列间距 * (1f /列数))
                    if (newPosition < spanCount) {
                        outRect.top = 2 * space; // item top
                    } else {
                        outRect.top = 0;
                    }
                }
                outRect.bottom = space * 2;
            }
        }
    }
}
