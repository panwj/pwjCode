package com.file.share;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class ConnectionsActivity extends AppCompatActivity implements PermissionHelper.PermissionCallbacks {

    private static final String TAG = "pwj";

    /** Our handler to Nearby Connections. */
    private ConnectionsClient mConnectionsClient;

    /** The devices we've discovered near us. */
    private final Map<String, Endpoint> mDiscoveredEndpoints = new HashMap<>();

    /**
     * The devices we have pending connections to. They will stay pending until we call {@link
     * #acceptConnection(Endpoint)} or {@link #rejectConnection(Endpoint)}.
     */
    private final Map<String, Endpoint> mPendingConnections = new HashMap<>();

    /**
     * The devices we are currently connected to. For advertisers, this may be large. For discoverers,
     * there will only be one entry in this map.
     */
    private final Map<String, Endpoint> mEstablishedConnections = new HashMap<>();

    private final Map<String, Endpoint> mAllEndpoints = new HashMap<>();

    /**
     * True if we are asking a discovered device to connect to us. While we ask, we cannot ask another
     * device.
     */
    private boolean mIsConnecting = false;

    /** True if we are discovering. */
    private boolean mIsDiscovering = false;

    /** True if we are advertising. */
    private boolean mIsAdvertising = false;

    /** Callbacks for connections to other devices. */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            /**
             * 接收到连接请求
             */
            logD(String.format(
                            "onConnectionInitiated(endpointId = %s, endpointName = %s, isIncomingConnection = %s)",
                            endpointId, connectionInfo.getEndpointName(), "" + connectionInfo.isIncomingConnection()));
            Endpoint endpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());
            endpoint.setState(ConnectionState.PENDING);
            mPendingConnections.put(endpointId, endpoint);
            mAllEndpoints.put(endpointId, endpoint);
            ConnectionsActivity.this.onConnectionInitiated(endpoint, connectionInfo);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            logV(String.format("onConnectionResult(endpointId = %s, result = %s)", endpointId, result));
            /**
             * 连接结果
             */
            mIsConnecting = false;
            Endpoint endpoint = mPendingConnections.remove(endpointId);
            if (!result.getStatus().isSuccess()) {
                logV(String.format(
                                "Connection failed. Received status %s.",
                                ConnectionsActivity.toString(result.getStatus())));
                endpoint.setState(ConnectionState.UNKNOWN);
                mAllEndpoints.put(endpointId, endpoint);
                onConnectionFailed(endpoint);
                return;
            }
            endpoint.setState(ConnectionState.CONNECTED);
            connectedToEndpoint(endpoint);
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            if (!mEstablishedConnections.containsKey(endpointId)) {
                logV("Unexpected disconnection from endpoint " + endpointId);
                return;
            }
            disconnectedFromEndpoint(mEstablishedConnections.get(endpointId));
        }
    };

    /** Callbacks for discovery to other devices. */
    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
            logD(String.format(
                            "onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                            endpointId, info.getServiceId(), info.getEndpointName()));

            if (getServiceId().equals(info.getServiceId())) {
                Endpoint endpoint = new Endpoint(endpointId, info.getEndpointName());
                endpoint.setState(ConnectionState.UNKNOWN);
                mDiscoveredEndpoints.put(endpointId, endpoint);
                mAllEndpoints.put(endpointId, endpoint);
                onEndpointDiscovered(endpoint);
            }
        }

        @Override
        public void onEndpointLost(@NonNull String endpointId) {
            logD(String.format("onEndpointLost(endpointId=%s)", endpointId));
            mAllEndpoints.remove(endpointId);
            onEndpointDiscovered(mDiscoveredEndpoints.remove(endpointId));
        }
    };

    /** Callbacks for payloads (bytes of data) sent from another device to us. */
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            logD(String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
            onReceive(mEstablishedConnections.get(endpointId), payload);
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            logD(String.format(
                            "onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, payloadTransferUpdate));
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectionsClient = Nearby.getConnectionsClient(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Sets the device to advertising mode. It will broadcast to other devices in discovery mode.
     * Either {@link #onAdvertisingStarted()} or {@link #onAdvertisingFailed()} will be called once
     * we've found out if we successfully entered this mode.
     */
    protected void startAdvertising() {
        mIsAdvertising = true;
        String localEndpointName = getName();
        AdvertisingOptions.Builder advertisingOptions = new AdvertisingOptions.Builder()
                .setStrategy(Strategy.P2P_CLUSTER);

        mConnectionsClient.startAdvertising(localEndpointName, getServiceId(), mConnectionLifecycleCallback, advertisingOptions.build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        logV("startAdvertising() endpoint " + localEndpointName);
                        onAdvertisingStarted();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIsAdvertising = false;
                        logV("startAdvertising() failed. msg: " + e.toString());
                        onAdvertisingFailed();
                    }
                });

    }

    /** Stops advertising. */
    protected void stopAdvertising() {
        mIsAdvertising = false;
        mConnectionsClient.stopAdvertising();
    }

    /** Returns {@code true} if currently advertising. */
    protected boolean isAdvertising() {
        return mIsAdvertising;
    }

    /** Called when advertising successfully starts. Override this method to act on the event. */
    protected void onAdvertisingStarted() {}

    /** Called when advertising fails to start. Override this method to act on the event. */
    protected void onAdvertisingFailed() {}

    /**
     * Called when a pending connection with a remote endpoint is created. Use {@link ConnectionInfo}
     * for metadata about the connection (like incoming vs outgoing, or the authentication token). If
     * we want to continue with the connection, call {@link #acceptConnection(Endpoint)}. Otherwise,
     * call {@link #rejectConnection(Endpoint)}.
     */
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {}

    /** Accepts a connection request. */
    protected void acceptConnection(final Endpoint endpoint) {
        mConnectionsClient
                .acceptConnection(endpoint.getId(), mPayloadCallback)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logV("acceptConnection() failed. msg: " + e.toString());
                            }
                        });
    }

    /** Rejects a connection request. */
    protected void rejectConnection(Endpoint endpoint) {
        mConnectionsClient
                .rejectConnection(endpoint.getId())
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logV("rejectConnection() failed. msg: " + e.toString());
                            }
                        });
    }

    /**
     * Sets the device to discovery mode. It will now listen for devices in advertising mode. Either
     * {@link #onDiscoveryStarted()} or {@link #onDiscoveryFailed()} will be called once we've found
     * out if we successfully entered this mode.
     */
    protected void startDiscovering() {
        mIsDiscovering = true;
        mDiscoveredEndpoints.clear();
        DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder()
                .setStrategy(Strategy.P2P_CLUSTER);
        mConnectionsClient.startDiscovery(getServiceId(), mEndpointDiscoveryCallback, discoveryOptions.build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        logV("startDiscovery()");
                        onDiscoveryStarted();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIsDiscovering = false;
                        logV("startDiscovering() failed. msg: " + e.toString());
                        onDiscoveryFailed();
                    }
                });
    }

    /** Stops discovery. */
    protected void stopDiscovering() {
        mIsDiscovering = false;
        mConnectionsClient.stopDiscovery();
    }

    /** Returns {@code true} if currently discovering. */
    protected boolean isDiscovering() {
        return mIsDiscovering;
    }

    /** Called when discovery successfully starts. Override this method to act on the event. */
    protected void onDiscoveryStarted() {}

    /** Called when discovery fails to start. Override this method to act on the event. */
    protected void onDiscoveryFailed() {}

    /**
     * Called when a remote endpoint is discovered. To connect to the device, call {@link
     * #connectToEndpoint(Endpoint)}.
     */
    protected void onEndpointDiscovered(Endpoint endpoint) {}

    /** Disconnects from the given endpoint. */
    protected void disconnect(Endpoint endpoint) {
        mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
        mEstablishedConnections.remove(endpoint.getId());
        mAllEndpoints.remove(endpoint.getId());
    }

    /** Disconnects from all currently connected endpoints. */
    protected void disconnectFromAllEndpoints() {
        for (Endpoint endpoint : mEstablishedConnections.values()) {
            mConnectionsClient.disconnectFromEndpoint(endpoint.getId());
        }
        mEstablishedConnections.clear();
        mAllEndpoints.clear();
    }

    /** Resets and clears all state in Nearby Connections. */
    protected void stopAllEndpoints() {
        mConnectionsClient.stopAllEndpoints();
        mIsAdvertising = false;
        mIsDiscovering = false;
        mIsConnecting = false;
        mDiscoveredEndpoints.clear();
        mPendingConnections.clear();
        mEstablishedConnections.clear();
        mAllEndpoints.clear();
    }

    /**
     * Sends a connection request to the endpoint. Either {@link #onConnectionInitiated(Endpoint,
     * ConnectionInfo)} or {@link #onConnectionFailed(Endpoint)} will be called once we've found out
     * if we successfully reached the device.
     */
    protected void connectToEndpoint(final Endpoint endpoint) {
        logV("Sending a connection request to endpoint " + endpoint);
        // Mark ourselves as connecting so we don't connect multiple times
        mIsConnecting = true;

        // Ask to connect
        mConnectionsClient
                .requestConnection(getName(), endpoint.getId(), mConnectionLifecycleCallback)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logV("requestConnection() failed. msg: " + e.toString());
                                mIsConnecting = false;
                                onConnectionFailed(endpoint);
                            }
                        });
    }

    /** Returns {@code true} if we're currently attempting to connect to another device. */
    protected final boolean isConnecting() {
        return mIsConnecting;
    }

    private void connectedToEndpoint(Endpoint endpoint) {
        logD(String.format("connectedToEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.put(endpoint.getId(), endpoint);
        mAllEndpoints.put(endpoint.getId(), endpoint);
        onEndpointConnected(endpoint);
    }

    private void disconnectedFromEndpoint(Endpoint endpoint) {
        logD(String.format("disconnectedFromEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.remove(endpoint.getId());
        mAllEndpoints.remove(endpoint.getId());
        onEndpointDisconnected(endpoint);
    }

    /**
     * Called when a connection with this endpoint has failed. Override this method to act on the
     * event.
     */
    protected void onConnectionFailed(Endpoint endpoint) {}

    /** Called when someone has connected to us. Override this method to act on the event. */
    protected void onEndpointConnected(Endpoint endpoint) {}

    /** Called when someone has disconnected. Override this method to act on the event. */
    protected void onEndpointDisconnected(Endpoint endpoint) {}

    /** Returns a list of currently connected endpoints. */
    protected Set<Endpoint> getDiscoveredEndpoints() {
        return new HashSet<>(mDiscoveredEndpoints.values());
    }

    /** Returns a list of currently connected endpoints. */
    protected Set<Endpoint> getConnectedEndpoints() {
        return new HashSet<>(mEstablishedConnections.values());
    }

    protected Set<Endpoint> getAllEndpoints() {
        return new HashSet<>(mAllEndpoints.values());
    }

    /**
     * Sends a {@link Payload} to all currently connected endpoints.
     *
     * @param payload The data you want to send.
     */
    protected void send(Payload payload) {
        send(payload, mEstablishedConnections.keySet());
    }

    private void send(Payload payload, Set<String> endpoints) {
        mConnectionsClient
                .sendPayload(new ArrayList<>(endpoints), payload)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logV("sendPayload() failed. msg: " + e.toString());
                            }
                        });
    }

    /**
     * Someone connected to us has sent us data. Override this method to act on the event.
     *
     * @param endpoint The sender.
     * @param payload The data.
     */
    protected void onReceive(Endpoint endpoint, Payload payload) {}

    protected abstract String getName();

    protected abstract String getServiceId();

    /**
     * Transforms a {@link Status} into a English-readable message for logging.
     *
     * @param status The current status
     * @return A readable String. eg. [404]File not found.
     */
    private static String toString(Status status) {
        return String.format(
                Locale.US,
                "[%d]%s",
                status.getStatusCode(),
                status.getStatusMessage() != null
                        ? status.getStatusMessage()
                        : ConnectionsStatusCodes.getStatusCodeString(status.getStatusCode()));
    }

    @CallSuper
    protected void logV(String msg) {
        Log.v(TAG, msg);
    }

    @CallSuper
    protected void logD(String msg) {
        Log.d(TAG, msg);
    }
}
