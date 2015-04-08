package com.projectspeedracer.thefoodapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

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
        Log.e("PUSH RECEIVED","push notification received..");

        // Custom processing:
        String channel = intent.getStringExtra(KEY_PUSH_CHANNEL); // may be null
        try {
            JSONObject notificationData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA)); // JSON payload
            Log.v(TAG, "Push message received on Channel - "+channel+" -- Data: "+notificationData.toString());

            if (notificationData.getString("userId").equals(ParseUser.getCurrentUser().getObjectId().toString())) {
                // do not show notification to self.
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Call super to show it on Notification bar. Also see getNotification()
        super.onPushReceive(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        // return
        // We don't want to open the notification yet.
//        super.onPushOpen(context, intent);
    }
}