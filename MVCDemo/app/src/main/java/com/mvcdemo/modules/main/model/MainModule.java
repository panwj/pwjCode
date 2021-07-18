package com.mvcdemo.modules.main.model;

import com.mvcdemo.modules.main.activity.MainActivity;

public class MainModule {

    private MainActivity activity;

    public MainModule(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    public void updateUI() {
        this.activity.updateUI("你点击了tab1");
    }
}
