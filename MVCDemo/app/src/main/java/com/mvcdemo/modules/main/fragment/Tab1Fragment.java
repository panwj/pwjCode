package com.mvcdemo.modules.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import com.mvcdemo.MvcApplication;
import com.mvcdemo.R;
import com.mvcdemo.common.google.billing.BillingActivity;
import com.mvcdemo.common.util.AppUtil;
import com.mvcdemo.common.util.ClickUtils;
import com.mvcdemo.data.entity.User;
import com.mvcdemo.modules.base.BaseFragment;

public class Tab1Fragment extends BaseFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        initView(view);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tab1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView(View view) {
        view.findViewById(R.id.billing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BillingActivity.class));
            }
        });

        TextView textView = view.findViewById(R.id.tv_1);
        ClickUtils.handleClickListener(new Handler(), textView, 5000, null, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        User user = new User();
                        user.firstName = "张";
                        user.lastName = "三";
                        MvcApplication.getMvcApplication().getAppDB().userDao().insertAll(new User());
                    }
                }).start();
                Snackbar.make(textView, AppUtil.getSurprise(), Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
