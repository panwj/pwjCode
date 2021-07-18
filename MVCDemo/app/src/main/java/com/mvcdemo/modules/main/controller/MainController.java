package com.mvcdemo.modules.main.controller;

import com.mvcdemo.modules.main.activity.MainActivity;
import com.mvcdemo.modules.main.model.MainModule;

public class MainController {

    private MainModule mainModule;
    private MainActivity activity;

    public MainController(MainActivity activity) {
        this.activity = activity;
    }

    public void updateUI() {
        mainModule = new MainModule(activity);
        mainModule.updateUI();
    }

}
