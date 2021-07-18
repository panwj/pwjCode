package com.mvcdemo.modules.setting;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.mvcdemo.R;
import com.mvcdemo.modules.base.BaseActivity;

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
