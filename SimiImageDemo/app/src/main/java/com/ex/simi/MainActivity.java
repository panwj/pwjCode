package com.ex.simi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ex.simi.dao.AppDatabaseManager;
import com.ex.simi.duplicate.SimilarPhotosActivity;
import com.ex.simi.duplicate.entity.PhotoEntity;
import com.ex.simi.duplicate.util.PhotoRepository;
import com.ex.simi.util.PermissionHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionHelper.PermissionCallbacks {

    static {
        System.loadLibrary("opencv_java4");//加载OpenCV动态库
    }

    private TextView mCountTv, mNewTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCountTv = findViewById(R.id.tv_count);
        mNewTv = findViewById(R.id.tv_new);
        if (PermissionHelper.hasStoragePermissions(this)) {
            initData();
        } else {
            PermissionHelper.requestStoragePermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        initData();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                List<Long> mCountList = PhotoRepository.getPicturesId(getApplicationContext());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCountTv.setText("总计：" + mCountList.size() + "   time : " + (System.currentTimeMillis() - time));
                    }
                });

                long id = AppDatabaseManager.getDatabase(getApplicationContext()).getSimilarPhotoDao().getMaxPhotoId();
                List<PhotoEntity> list = PhotoRepository.getPictures(getApplicationContext(), id);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNewTv.setText("新增：" + list.size() + "   time : " + (System.currentTimeMillis() - time));
                    }
                });
            }
        }).start();
    }

    public void find(View view) {
        if (PermissionHelper.hasStoragePermissions(this)) {
            Intent intent = new Intent(MainActivity.this, SimilarPhotosActivity.class);
            startActivity(intent);
        }
    }
}