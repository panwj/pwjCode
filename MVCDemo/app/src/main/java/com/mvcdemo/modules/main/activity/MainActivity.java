package com.mvcdemo.modules.main.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.mvcdemo.R;
import com.mvcdemo.common.firebase.push.PushData;
import com.mvcdemo.common.firebase.push.PushUtils;
import com.mvcdemo.common.google.installreferrer.InstallReferrerTool;
import com.mvcdemo.common.util.MMKVUtil;
import com.mvcdemo.common.util.VersionChecker;
import com.mvcdemo.common.versiontip.UpdateVersionTipUtil;
import com.mvcdemo.common.versiontip.event.ExitAppEvent;
import com.mvcdemo.modules.base.BaseActivity;
import com.mvcdemo.modules.main.controller.MainController;

public class MainActivity extends BaseActivity {

    private NavController mNavController;
    private NavController.OnDestinationChangedListener mListener;
    private FloatingActionButton floatingActionButton;
    private MainController mainController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerEventBus();

        mainController = new MainController(this);
        init();

        //install referrer
        InstallReferrerTool.getInstance().fetchReferrer(this);

        //for upgrade tip
        boolean hasNewVersion = UpdateVersionTipUtil.hasNewVersionInStore(getApplicationContext());
        if (hasNewVersion) {
            UpdateVersionTipUtil.showUpdatedVersionDialog(this);
        }

        //what's new
        if (VersionChecker.isShowWhatsNew()) {
            MMKVUtil.getInstance().saveBoolean(VersionChecker.PREF_KEY_SHOW_WHATS_NEW, false);
            //todo 显示版本变更的dialog
//            showUpdatedVersionSummary();
            Toast.makeText(this, "版本升级了", Toast.LENGTH_SHORT).show();
        }

        Intent intent = getIntent();
        PushData pushData = null;
        if (intent != null) {
            pushData = intent.getParcelableExtra("push_data");
        }
        if (pushData != null) {
            PushUtils.launcherPushPage(getApplicationContext(), pushData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterEventBus();
        if (mNavController != null) mNavController.removeOnDestinationChangedListener(mListener);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ExitAppEvent(ExitAppEvent exitAppEvent) {
        finish();
    }

    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);
        navView.setItemTextColor(getResources().getColorStateList(R.drawable.navigation_tab_title_background));

//        mNavHostFragment = (MainHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mListener = new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                switch (destination.getId()) {
                    case R.id.navigation_tab1:
                        break;
                    case R.id.navigation_tab2:
                        break;
                    case R.id.navigation_tab3:
                        break;
                }
            }
        };
        mNavController.addOnDestinationChangedListener(mListener);
//        mNavController.navigate(R.id.navigation_sender);
        NavigationUI.setupWithNavController(navView, mNavController);
        NavigationUI.setupActionBarWithNavController(this, mNavController);

        setRedDoc(navView);

        floatingActionButton = findViewById(R.id.floating);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainController.updateUI();
            }
        });
    }

    private void setRedDoc(BottomNavigationView navigationView) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigationView.getChildAt(0);
    }

    public void updateUI(String string) {
        Snackbar.make(floatingActionButton, string, Snackbar.LENGTH_LONG).show();
    }

}