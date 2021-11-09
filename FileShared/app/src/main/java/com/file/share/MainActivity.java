package com.file.share;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends ConnectionsActivity implements View.OnClickListener, DeviceAdapter.OnItemClickListener {

    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;
    private static final long ONT_TIME = 60 * 1000;
    private static final String SERVICE_ID = FileSharedApplication.getApplication().getPackageName() + ".SERVICE_ID";
    private String mName;
    private State mCurState = State.UNKNOWN;

    private final MyHandle mHandle = new MyHandle(this);
    static class MyHandle extends Handler {

        private WeakReference<MainActivity> weakReference;

        public MyHandle(MainActivity activity) {
            weakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            if (activity == null || activity.isFinishing()) return;
            switch (msg.what) {
                case MSG_START:
                    activity.setState(State.SEARCHING);
                    activity.mHandle.sendEmptyMessageDelayed(MSG_STOP, ONT_TIME);
                    break;
                case MSG_STOP:
                    activity.setState(State.UNKNOWN);
                    activity.mHandle.sendEmptyMessageDelayed(MSG_START, ONT_TIME);
                    break;
            }
        }
    }

    private ProgressBar mProgressBar;
    protected TextView mDeviceName;
    private TextView mPermissionTv;
    protected Button mStartBtn, mStopBtn;
    private RecyclerView mFoundDeviceRv;
    private DeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connections_activity_layout);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
        editDeviceName();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandle.removeMessages(MSG_START);
        mHandle.removeMessages(MSG_STOP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updatePermissionUI((PermissionHelper.hasStoragePermissions(this) && PermissionHelper.hasPermissions(this, PermissionHelper.REQUIRED_PERMISSIONS)) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case PermissionHelper.MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE:
                updatePermissionUI(View.GONE);
                break;
            case PermissionHelper.NORMAL_PERMISSION_CODE:
                if (!PermissionHelper.hasStoragePermissions(this))
                    PermissionHelper.requestStoragePermissions(this);
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case PermissionHelper.MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE:
            case PermissionHelper.NORMAL_PERMISSION_CODE:
                updatePermissionUI(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
//                mHandle.sendEmptyMessage(MSG_START);
                setState(State.SEARCHING);
                break;
            case R.id.btn_stop:
                setState(State.UNKNOWN);
                break;
        }
    }

    @Override
    public void onItemClick(Endpoint endpoint, int position) {
        if (endpoint == null) return;
        if (endpoint.getState() == ConnectionState.CONNECTED) {
            disconnect(endpoint);
        } else if (endpoint.getState() == ConnectionState.UNKNOWN) {
            connectToEndpoint(endpoint);
        }
    }

    @Override
    protected void onAdvertisingStarted() {
    }

    @Override
    protected void onAdvertisingFailed() {
    }

    @Override
    protected void onDiscoveryStarted() {
        setProgressBarState(View.VISIBLE);
    }

    @Override
    protected void onDiscoveryFailed() {
        setProgressBarState(View.INVISIBLE);
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        updateData();
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        updateData();
        new AlertDialog.Builder(this)
                .setTitle("Accept connection to " + connectionInfo.getEndpointName())
                .setMessage("Confirm the code matches on both devices: " + connectionInfo.getAuthenticationDigits())
                .setPositiveButton(
                        "Accept",
                        (DialogInterface dialog, int which) ->
                                // The user confirmed, so we can accept the connection.
                                 acceptConnection(endpoint))
                .setNegativeButton(
                        android.R.string.cancel,
                        (DialogInterface dialog, int which) ->
                                // The user canceled, so we should reject the connection.
                                rejectConnection(endpoint))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        updateData();
    }

    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        updateData();
    }

    @Override
    protected void onConnectionFailed(Endpoint endpoint) {
//        updateData();
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
    }

    @Override
    protected String getName() {
        return mName;
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    private void initView() {
        mDeviceName = findViewById(R.id.tv_device);
        mPermissionTv = findViewById(R.id.tv_permission);
        mProgressBar = findViewById(R.id.progress_circular);

        mStartBtn = findViewById(R.id.btn_start);
        mStopBtn = findViewById(R.id.btn_stop);
        mStartBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);

        mFoundDeviceRv = findViewById(R.id.found_devices);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mFoundDeviceRv.setLayoutManager(layoutManager);
        mAdapter = new DeviceAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mFoundDeviceRv.setAdapter(mAdapter);
    }

    protected void requestPermissions() {
        if (!PermissionHelper.hasPermissions(this, PermissionHelper.REQUIRED_PERMISSIONS)) {
            updatePermissionUI(View.VISIBLE);
            PermissionHelper.requestPermissions(this, PermissionHelper.NORMAL_PERMISSION_CODE, PermissionHelper.REQUIRED_PERMISSIONS);
        } else if (!PermissionHelper.hasStoragePermissions(this)) {
            updatePermissionUI(View.VISIBLE);
            PermissionHelper.requestStoragePermissions(this);
        } else {
            updatePermissionUI(View.GONE);
        }
    }

    protected void updatePermissionUI(int visibility) {
        if (mPermissionTv != null) mPermissionTv.setVisibility(visibility);
    }

    private void setProgressBarState(int state) {
        if (mProgressBar != null) mProgressBar.setVisibility(state);
    }

    private void updateData() {
        if (mAdapter != null) mAdapter.setData(new ArrayList<>(getAllEndpoints()));
    }

    /** 自定义device名称 */
    private void editDeviceName() {
        if (!TextUtils.isEmpty(mName)) return;
        mName = generateRandomName();
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("FileShard")
                .setMessage("Device name is " + mName)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mDeviceName.setText("Device name : " + mName);
                    }
                }).show();

    }

    private String generateRandomName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name;
    }

    private void setState(State state) {
        if (mCurState == state) {
            logV("状态一致，无需处理！！！");
            return;
        }
        State old = mCurState;
        mCurState = state;
        onStateChanged(old, mCurState);
    }

    private void onStateChanged(State oldState, State curState) {
        switch (curState) {
            case SEARCHING:
                startAdvertising();
                startDiscovering();
                break;
            case CONNECTED:
                break;
            case UNKNOWN:
                stopAdvertising();
                stopDiscovering();
                setProgressBarState(View.INVISIBLE);
                break;
            default:
                break;
        }
    }
}