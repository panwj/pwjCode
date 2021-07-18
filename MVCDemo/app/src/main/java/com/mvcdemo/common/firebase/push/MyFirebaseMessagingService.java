/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mvcdemo.common.firebase.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import com.mvcdemo.R;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FBMessagingService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud LocalConv.
     */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        if (remoteMessage == null) {
            return;
        }

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "data : " + remoteMessage.getData().toString());
            Map<String, String> dataMap = remoteMessage.getData();
            PushData pushData = new PushData();
            pushData.mImageLink = dataMap.get(PushData.DATA_IMAGE_LINK);
            pushData.mTitle = dataMap.get(PushData.DATA_TITLE);
            pushData.mContents = dataMap.get(PushData.DATA_CONTENTS);
            pushData.mThemeName = dataMap.get(PushData.DATA_THEME_NAME);
            pushData.mPackageName = dataMap.get(PushData.DATA_PACKAGE_NAME);
            pushData.mVersionCode = dataMap.get(PushData.DATA_APP_VERSION_CODE);
            pushData.mVersionName = dataMap.get(PushData.DATA_APP_VERSION_NAME);
            pushData.mUpdateLink = dataMap.get(PushData.DATA_UPDATE_LINK);
            Log.d(TAG, "push data : " + pushData.toString());

            PushUtils.launcherPushPage(MyFirebaseMessagingService.this, pushData);
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "notifitycation " + remoteMessage.getNotification().toString());
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(TAG, "onDeletedMessages() ");
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d(TAG, "onMessageSent() " + s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        Log.d(TAG, "onSendError() s = " + s);
    }

    private void sendNotification(String messageBody) {
        /**
         * todo 需要判断是否添加通知渠道
         */
        Intent intent = new Intent(this, PushWindowActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                /*.setLargeIcon()*/
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
