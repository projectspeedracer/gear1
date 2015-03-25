package com.projectspeedracer.thefoodapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by avkadam on 3/24/15.
 */
public class CustomParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    // Documentation on :
    // https://www.parse.com/docs/android/api/com/parse/ParsePushBroadcastReceiver.html
    private final String TAG = "CustomPushReceiver";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        // Call super to show it on Notification bar. Also see getNotification()
        super.onPushReceive(context, intent);

        // Custom processing:
        String channel = intent.getStringExtra(KEY_PUSH_CHANNEL); // may be null
        try {
            JSONObject notificationData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA)); // JSON payload
            Log.v(TAG, "Push message revceived on Channel - "+channel+" -- Data: "+notificationData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}