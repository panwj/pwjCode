/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mvcdemo.common.google.billing;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.FeatureType;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Handles all the interactions with Play Store (via Billing library), maintains connection to
 * it through BillingClient and caches temporary states/data if needed
 */
public class BillingManager implements PurchasesUpdatedListener {
    // Default value of mBillingClientResponseCode until BillingManager was not yeat initialized
    public static final int BILLING_MANAGER_NOT_INITIALIZED = -1;

    private static final String TAG = "Billing";

    private static final String BASE_64_ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsod+nxfTqW63zqtDirg9yzqgyLgyWCQ7HRT0O+yBHBfuoSvSqdBmvpecyGebb41tciWkdC6b8v30AWhdW3lg/LKEk/hGHagK6ENcLzD2EHyCZJXymSY+Y6/Wm4hFcKwAJ7yfoldlPhfldXaOlJjNz8oPsIjDtfGBVF9j8VoRBME68BXmy7wkktpMD1FDd+MpLEzxwazCenhkRh0HHerls7Qzl0VkWV43uoMmYDLVSvjI37e0d/KtnQo/nWheBY8zaG1WCoaLWggZTv0QEqIao6MvlzopAQH1rDe+RCOAPMJB4BNX2xa8grzqt9VWB5Jmak0D0cPRacpQk4+RTzDAzQIDAQAB";
    /**
     * A reference to BillingClient
     **/
    private BillingClient mBillingClient;

    /**
     * True if billing service is connected now.
     */
    private boolean mIsServiceConnected;

    private BillingUpdatesListener mBillingUpdatesListener;

    private Activity mActivity;

    private List<Purchase> mPurchases = new ArrayList<>();

    private Set<String> mTokensToBeConsumed;

    private int mBillingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;


    /**
     * Listener to the updates that happen when purchases list was updated or consumption of the
     * item was finished
     */
    public interface BillingUpdatesListener {
        void onBillingClientSetupFinished();

        void onConsumeFinished(String sku, @BillingClient.BillingResponseCode int result);

        void onPurchasesUpdated(List<Purchase> purchases);

        void onBillingError(@BillingClient.BillingResponseCode int result);
    }

    /**
     * Listener for the Billing client state to become connected
     */
    public interface ServiceConnectedListener {
        void onServiceConnected(@BillingClient.BillingResponseCode int resultCode);
    }

    public BillingManager(Activity activity, final BillingUpdatesListener updatesListener) {
        Log.d(TAG, "Creating Billing client.");
        mActivity = activity;
        mBillingUpdatesListener = updatesListener;
        mBillingClient = BillingClient.newBuilder(mActivity).enablePendingPurchases().setListener(this).build();

        Log.d(TAG, "Starting setup.");

        // Start setup. This is asynchronous and the specified listener will be called
        // once setup completes.
        // It also starts to report all the new purchases through onPurchasesUpdated() callback.
        startServiceConnection(new Runnable() {
            @Override
            public void run() {
                // Notifying the listener that billing client is ready
                if (mBillingUpdatesListener != null)
                    mBillingUpdatesListener.onBillingClientSetupFinished();
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                queryPurchases();
            }
        });
    }

    /**
     * Handle a callback that purchases were updated from the Billing library
     */
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int resultCode = billingResult.getResponseCode();
        if (resultCode == BillingClient.BillingResponseCode.OK) {
            if (purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
                if (mBillingUpdatesListener != null)
                    mBillingUpdatesListener.onPurchasesUpdated(mPurchases);
            }
        } else if (resultCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
            if (mBillingUpdatesListener != null) mBillingUpdatesListener.onBillingError(resultCode);
        } else {
            Log.d(TAG, "onPurchasesUpdated() got unknown resultCode: " + resultCode);
            if (mBillingUpdatesListener != null) mBillingUpdatesListener.onBillingError(resultCode);
        }
    }

    /**
     * Start a purchase flow
     */
    public void initiatePurchaseFlow(final String skuId, ArrayList<String> skuList, final @SkuType String billingType) {
        initiatePurchaseFlow(skuId, skuList, null, null, billingType);
    }

    /**
     * Start a purchase or subscription replace flow
     */
    public void initiatePurchaseFlow(final String skuId, ArrayList<String> skuList, String oldSku, String oldPurchaseToken,
                                     final @SkuType String billingType) {
        Runnable purchaseFlowRequest = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Launching in-app purchase flow. Replace old SKU? " + (oldSku != null));
                if (mActivity == null) return;
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(billingType);
                mBillingClient.querySkuDetailsAsync(params.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                             List<SkuDetails> skuDetailsList) {
                                if (mActivity == null) return;
                                // Process the result.
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                                        && skuDetailsList != null) {
                                    SkuDetails skuDetails = null;
                                    for (SkuDetails details : skuDetailsList) {
                                        String sku = details.getSku();
                                        if (sku.equals(skuId)) {
                                            skuDetails = details;
                                        }
                                    }
                                    if (skuDetails != null) {
                                        Log.d(TAG, "Launching in-app purchase flow. Replace old SKU? " + (oldSku != null));
                                        BillingFlowParams.Builder purchaseParamsBuilder = BillingFlowParams.newBuilder()
                                                .setSkuDetails(skuDetails);
                                        if (oldSku != null)
                                            purchaseParamsBuilder.setOldSku(oldSku, oldPurchaseToken);
                                        mBillingClient.launchBillingFlow(mActivity, purchaseParamsBuilder.build());
                                    }
                                } else {
                                    Log.d(TAG, "initiatePurchaseFlow  error : " + billingResult.getResponseCode());
                                    if (mBillingUpdatesListener != null)
                                        mBillingUpdatesListener.onBillingError(billingResult.getResponseCode());
                                }
                                // Process the result.
                            }
                        });
            }
        };

        executeServiceRequest(purchaseFlowRequest);
    }

    public Context getContext() {
        return mActivity;
    }

    /**
     * Clear the resources
     */
    public void destroy() {
        Log.d(TAG, "Destroying the manager.");

        if (mBillingClient != null && mBillingClient.isReady()) {
            mBillingClient.endConnection();
            mBillingClient = null;
        }
        mBillingUpdatesListener = null;
        mActivity = null;
    }

    public void querySkuDetailsAsync(@SkuType final String itemType, final List<String> skuList,
                                     final SkuDetailsResponseListener listener) {
        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable queryRequest = new Runnable() {
            @Override
            public void run() {
                if (mActivity == null) return;
                // Query the purchase async
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(itemType);
                mBillingClient.querySkuDetailsAsync(params.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                                if (listener != null) {
                                    listener.onSkuDetailsResponse(billingResult, skuDetailsList);
                                }
                            }

                        });
            }
        };

        executeServiceRequest(queryRequest);
    }

    public void consumeAsync(String sku, final String purchaseToken) {
        // If we've already scheduled to consume this token - no action is needed (this could happen
        // if you received the token when querying purchases inside onReceive() and later from
        // onActivityResult()
        if (mTokensToBeConsumed == null) {
            mTokensToBeConsumed = new HashSet<>();
        } else if (mTokensToBeConsumed.contains(purchaseToken)) {
            Log.d(TAG, "Token was already scheduled to be consumed - skipping...");
            return;
        }
        mTokensToBeConsumed.add(purchaseToken);

        // Generating Consume Response listener
        final ConsumeResponseListener onConsumeListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                try {
                    if (mPurchases != null) {
                        for (Purchase purchase : mPurchases) {
                            if (TextUtils.equals(purchaseToken, purchase.getPurchaseToken())) {
                                mPurchases.remove(purchase);
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {

                }
                // If billing service was disconnected, we try to reconnect 1 time
                // (feel free to introduce your retry policy here).
                if (mBillingUpdatesListener != null)
                    mBillingUpdatesListener.onConsumeFinished(sku, billingResult.getResponseCode());
            }
        };

        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable consumeRequest = new Runnable() {
            @Override
            public void run() {
                if (mBillingClient != null) {
                    // Consume the purchase async
                    ConsumeParams consumeParams =
                            ConsumeParams.newBuilder()
                                    .setPurchaseToken(purchaseToken)
                                    .build();
                    mBillingClient.consumeAsync(consumeParams, onConsumeListener);
                }
            }
        };

        executeServiceRequest(consumeRequest);
    }

    /**
     * Returns the value Billing client response code or BILLING_MANAGER_NOT_INITIALIZED if the
     * clien connection response was not received yet.
     */
    public int getBillingClientResponseCode() {
        return mBillingClientResponseCode;
    }

    /**
     * Handles the purchase
     * <p>Note: Notice that for each purchase, we check if signature is valid on the client.
     * It's recommended to move this check into your backend.
     * See {@link screen.recorder.common.billing.util.Security#verifyPurchase(String, String, String)}
     * </p>
     *
     * @param purchase Purchase to be handled
     */
    private void handlePurchase(Purchase purchase) {
        if (purchase == null) {
            return;
        }
        acknowledgePurchase(purchase);
        if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
            Log.d(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
            return;
        }
        Log.d(TAG, "Got a verified purchase: " + purchase);

        if (mPurchases == null)
            mPurchases = new ArrayList<>();
        mPurchases.add(purchase);
    }


    public void acknowledgePurchase(Purchase purchase) {
        if (purchase == null) {
            return;
        }
        if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
            Log.d(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
            return;
        }
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase if it hasn't already been acknowledged.
            String sku = purchase.getSku();
            if (!purchase.isAcknowledged() && !TextUtils.isEmpty(sku)) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                if (mBillingClient != null) {
                    mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                            Log.d(TAG, "onAcknowledgePurchaseResponse() billingResult : " + billingResult);
                        }
                    });
                }
            }
        }
    }


    /**
     * Handle a result from querying of purchases and report an updated list to the listener
     */
    private void onQueryPurchasesFinished(PurchasesResult result) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (mBillingClient == null || result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Billing client was null or result code (" + result.getResponseCode()
                    + ") was bad - quitting");
            if (mBillingUpdatesListener != null)
                mBillingUpdatesListener.onBillingError(result.getResponseCode());
            return;
        }

        Log.d(TAG, "Query inventory was successful.");

        // Update the UI and purchases inventory with new list of purchases
        if (mPurchases != null) mPurchases.clear();
        onPurchasesUpdated(result.getBillingResult(), result.getPurchasesList());
    }

    /**
     * Checks if subscriptions are supported for current client
     * <p>Note: This method does not automatically retry for RESULT_SERVICE_DISCONNECTED.
     * It is only used in unit tests and after queryPurchases execution, which already has
     * a retry-mechanism implemented.
     * </p>
     */
    public boolean areSubscriptionsSupported() {
        int responseCode = mBillingClient.isFeatureSupported(FeatureType.SUBSCRIPTIONS).getResponseCode();
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "areSubscriptionsSupported() got an error response: " + responseCode);
        }
        return responseCode == BillingClient.BillingResponseCode.OK;
    }

    /**
     * Query purchases across various use cases and deliver the result in a formalized way through
     * a listener
     */
    public void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                if (mBillingClient == null) {
                    if (mBillingUpdatesListener != null)
                        mBillingUpdatesListener.onBillingError(BillingClient.BillingResponseCode.ERROR);
                    return;
                }
                PurchasesResult purchasesResult = mBillingClient.queryPurchases(SkuType.INAPP);
                long time = System.currentTimeMillis();
                Log.d(TAG, "Querying purchases elapsed time: " + (System.currentTimeMillis() - time)
                        + "ms");
                // If there are subscriptions supported, we add subscription rows as well
                if (areSubscriptionsSupported()) {
                    PurchasesResult subscriptionResult
                            = mBillingClient.queryPurchases(SkuType.SUBS);

                    Log.d(TAG, "Querying purchases and subscriptions elapsed time: "
                            + (System.currentTimeMillis() - time) + "ms");
                    if (subscriptionResult.getPurchasesList() != null) {
                        Log.d(TAG, "Querying subscriptions result code: "
                                + subscriptionResult.getResponseCode()
                                + " res: " + subscriptionResult.getPurchasesList().size());

                        if (subscriptionResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (purchasesResult.getPurchasesList() != null) {
                                purchasesResult.getPurchasesList().addAll(
                                        subscriptionResult.getPurchasesList());
                            }
                        } else {
                            Log.e(TAG, "Got an error response trying to query subscription purchases");
                        }
                    }
                } else if (purchasesResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Skipped subscription purchases query since they are not supported");
                } else {
                    Log.d(TAG, "queryPurchases() got an error response code: "
                            + purchasesResult.getResponseCode());
                }
                onQueryPurchasesFinished(purchasesResult);
            }
        };

        executeServiceRequest(queryToExecute);
    }

    public void startServiceConnection(final Runnable executeOnSuccess) {
        mBillingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.d(TAG, "Setup finished. Response code: " + billingResult.getResponseCode());
                int billingResponseCode = billingResult.getResponseCode();
                if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
                    mIsServiceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                } else {
                    if (mBillingUpdatesListener != null)
                        mBillingUpdatesListener.onBillingError(billingResponseCode);
                }
                mBillingClientResponseCode = billingResponseCode;
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected()...");
                mIsServiceConnected = false;
                if (mBillingUpdatesListener != null)
                    mBillingUpdatesListener.onBillingError(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED);
            }
        });
    }

    private void executeServiceRequest(Runnable runnable) {
        if (mIsServiceConnected) {
            runnable.run();
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable);
        }
    }

    /* BASE_64_ENCODED_PUBLIC_KEY should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        String publicKey = BASE_64_ENCODED_PUBLIC_KEY;
        if (publicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please update your app's public key at: "
                    + "BASE_64_ENCODED_PUBLIC_KEY");
        }

        try {
            return Security.verifyPurchase(publicKey, signedData, signature);
        } catch (IOException e) {
            Log.e(TAG, "Got an exception trying to validate a purchase: " + e);
            return false;
        }
    }
}

