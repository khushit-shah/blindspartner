package com.khushitshah.blindspartner.libs.notification;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.HashMap;

@SuppressLint("Registered")
public class NotificationListener extends android.service.notification.NotificationListenerService {
    Context context;
    String[] notificationKeys;
    HashMap<String, String> notificationMap = new HashMap<>();
    NotificationCallback callback;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public void onListenerConnected() {
        Log.d("notify", "listenerConnected");
//        getAllCurrentVisibleNotification();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("notify", "startCommand");
        getAllCurrentVisibleNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    private void getAllCurrentVisibleNotification() {
        StringBuilder toSpeakText = new StringBuilder();

        StatusBarNotification[] activeNotifications = getActiveNotifications();
        Log.d("notify", "statusbarnotification size is" + activeNotifications.length);
        notificationKeys = new String[activeNotifications.length];
        int count = 0;
        for (StatusBarNotification notif : activeNotifications) {

            String pack = notif.getPackageName();
            String ticker = "";
            if (notif.getNotification().tickerText != null) {
                ticker = notif.getNotification().tickerText.toString();
            }
            Bundle extras = notif.getNotification().extras;
            String title;
            title = (extras.get("android.title") instanceof SpannableString) ? null : (String) extras.get("android.title");
            CharSequence text = extras.getCharSequence("android.text");

            Log.i("notifyPackage", pack);
            Log.i("notifyTicker", ticker);
            Log.i("notifyTitle", title == null ? "null" : title);
            Log.i("notifyText", text == null ? "null" : text.toString());

            String key = notif.getKey();
            notificationKeys[count] = key;

            String appName = pack.substring(pack.lastIndexOf('.') >= 0 ? pack.lastIndexOf('.') + 1 : 0);
            String text1 = ", With text " + (text == null ? "" : text.toString());
            if (count == 0)
                toSpeakText.append("By ").append(appName).append(", ").append(title).append(", ").append(text.toString());
            else
                toSpeakText.append("###By ").append(appName).append(", ").append(title).append(", ").append(text.toString());


            count++;
        }
        Log.d("notify", String.valueOf(toSpeakText));
        sendMessageToActivity(toSpeakText.toString());
    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("NotificationParsed");
        // You can also include some extra data.
        intent.putExtra("toSpeak", msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
