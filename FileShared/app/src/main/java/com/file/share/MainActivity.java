package com.file.share;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.SimpleArrayMap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends ConnectionsActivity implements View.OnClickListener, DeviceAdapter.OnItemClickListener {

    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;
    private static final long ONT_TIME = 60 * 1000;
    private static final String SERVICE_ID = FileSharedApplication.getApplication().getPackageName() + ".SERVICE_ID";
    private static final int READ_REQUEST_CODE = 42;
    private static final String ENDPOINT_ID_EXTRA = FileSharedApplication.getApplication().getPackageName() + ".EndpointId";
    private final SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, Payload> completedFilePayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();

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
    protected Button mStartBtn, mStopBtn, mShareBtn;
    private RecyclerView mFoundDeviceRv;
    private DeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connections_activity_layout);
        initView();
        int
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
        disconnectFromAllEndpoints();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PermissionHelper.MANAGE_ALL_FILES_ACCESS_PERMISSION_REQUEST_CODE)
            updatePermissionUI((PermissionHelper.hasStoragePermissions(this) && PermissionHelper.hasPermissions(this, PermissionHelper.REQUIRED_PERMISSIONS)) ? View.GONE : View.VISIBLE);

        if (requestCode == READ_REQUEST_CODE
                && resultCode == Activity.RESULT_OK
                && resultData != null) {
            String endpointId = resultData.getStringExtra(ENDPOINT_ID_EXTRA);
            logV("onActivityResult() ENDPOINT_ID_EXTRA = " + endpointId);

            // The URI of the file selected by the user.
            Uri uri = resultData.getData();

            Payload filePayload;
            try {
                // Open the ParcelFileDescriptor for this URI with read access.
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                filePayload = Payload.fromFile(pfd);
            } catch (FileNotFoundException e) {
                logV("File not found : " + e.toString());
                return;
            }

            // Construct a simple message mapping the ID of the file payload to the desired filename.
            String filenameMessage = filePayload.getId() + ":" + uri.getLastPathSegment();

            // Send the filename message as a bytes payload.
            Payload filenameBytesPayload =
                    Payload.fromBytes(filenameMessage.getBytes(StandardCharsets.UTF_8));
//            Nearby.getConnectionsClient(this).sendPayload(endpointId, filenameBytesPayload);
            send(filenameBytesPayload);

            // Finally, send the file payload.
//            Nearby.getConnectionsClient(this).sendPayload(endpointId, filePayload);
            send(filePayload);
        }
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
            case R.id.btn_share:
                showImageChooser("");
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
        updateData();
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if (payload.getType() == Payload.Type.BYTES) {
            String payloadFilenameMessage = new String(payload.asBytes(), StandardCharsets.UTF_8);
            long payloadId = addPayloadFilename(payloadFilenameMessage);
            processFilePayload(payloadId);
        } else if (payload.getType() == Payload.Type.FILE) {
            // Add this to our tracking map, so that we can retrieve the payload later.
            incomingFilePayloads.put(payload.getId(), payload);
            processFilePayload(payload.getId());
        }
    }

    @Override
    public void onReceivePayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
        if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
            long payloadId = update.getPayloadId();
            Payload payload = incomingFilePayloads.remove(payloadId);
            if (payload == null) return;
            completedFilePayloads.put(payloadId, payload);
            if (payload.getType() == Payload.Type.FILE) {
                processFilePayload(payloadId);
            }
        }
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
        mShareBtn = findViewById(R.id.btn_share);
        mStartBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
        mShareBtn.setOnClickListener(this);

        mFoundDeviceRv = findViewById(R.id.found_devices);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mFoundDeviceRv.setLayoutManager(layoutManager);
        mAdapter = new DeviceAdapter();
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

    /**
     * Fires an intent to spin up the file chooser UI and select an image for sending to endpointId.
     */
    private void showImageChooser(String endpointId) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(ENDPOINT_ID_EXTRA, endpointId);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /**
     * Extracts the payloadId and filename from the message and stores it in the
     * filePayloadFilenames map. The format is payloadId:filename.
     */
    private long addPayloadFilename(String payloadFilenameMessage) {
        String[] parts = payloadFilenameMessage.split(":");
        long payloadId = Long.parseLong(parts[0]);
        String filename = parts[1];
        filePayloadFilenames.put(payloadId, filename);
        return payloadId;
    }

    private void processFilePayload(long payloadId) {
        // BYTES and FILE could be received in any order, so we call when either the BYTES or the FILE
        // payload is completely received. The file payload is considered complete only when both have
        // been received.
        Payload filePayload = completedFilePayloads.get(payloadId);
        String filename = filePayloadFilenames.get(payloadId);
        logV("processFilePayload() filename : " + filename + "  " + filePayload);
        if (filePayload != null && filename != null) {
            completedFilePayloads.remove(payloadId);
            filePayloadFilenames.remove(payloadId);

            // Get the received file (which will be in the Downloads folder)
            // Because of https://developer.android.com/preview/privacy/scoped-storage, we are not
            // allowed to access filepaths from another process directly. Instead, we must open the
            // uri using our ContentResolver.
            Uri uri = filePayload.asFile().asUri();
            try {
                // Copy the file to a new location.
                InputStream in = getContentResolver().openInputStream(uri);
                File file = new File(getExternalCacheDir(), filename);
                logV("processFilePayload() file : " + file.getAbsolutePath());
                copyStream(in, new FileOutputStream(file));
            } catch (IOException e) {
                // Log the error.
                logV("processFilePayload() exception : " + e.toString());
            } finally {
                // Delete the original file.
                getContentResolver().delete(uri, null, null);
            }
        }
    }

    // add removed tag back to fix b/183037922
    private void processFilePayload2(long payloadId) {
        // BYTES and FILE could be received in any order, so we call when either the BYTES or the FILE
        // payload is completely received. The file payload is considered complete only when both have
        // been received.
        Payload filePayload = completedFilePayloads.get(payloadId);
        String filename = filePayloadFilenames.get(payloadId);
        logV("processFilePayload2() filename = " + filename);
        if (filePayload != null && filename != null) {
            completedFilePayloads.remove(payloadId);
            filePayloadFilenames.remove(payloadId);

            // Get the received file (which will be in the Downloads folder)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Because of https://developer.android.com/preview/privacy/scoped-storage, we are not
                // allowed to access filepaths from another process directly. Instead, we must open the
                // uri using our ContentResolver.
                Uri uri = filePayload.asFile().asUri();
                try {
                    // Copy the file to a new location.
                    InputStream in = getContentResolver().openInputStream(uri);
                    copyStream(in, new FileOutputStream(new File(getCacheDir(), filename)));
                } catch (IOException e) {
                    // Log the error.
                    logV("processFilePayload2() exception = " + e.toString());
                } finally {
                    // Delete the original file.
                    getContentResolver().delete(uri, null, null);
                }
            } else {
                File payloadFile = filePayload.asFile().asJavaFile();

                // Rename the file.
                File file = new File(payloadFile.getParentFile(), filename);
                logV("processFilePayload2() file: " + file.getAbsolutePath());
                payloadFile.renameTo(file);
            }
        }
    }

    /** Copies a stream from one location to another. */
    private void copyStream(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            Toast.makeText(this, "接收成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            logV("copyStream() exception : " + e.toString());
            Toast.makeText(this, "接收失败", Toast.LENGTH_SHORT).show();
        } finally {
            in.close();
            out.close();
        }
    }

}