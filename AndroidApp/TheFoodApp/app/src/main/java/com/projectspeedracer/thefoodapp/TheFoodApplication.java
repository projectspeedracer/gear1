package com.projectspeedracer.thefoodapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;

/**
 * Created by avkadam on 3/24/15.
 */
public class TheFoodApplication extends Application {

    public TheFoodApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //todo create data models
//        ParseObject.registerSubclass(DishPost.class);
//        ParseObject.registerSubclass(Restaurant.class);

        // Context, app id, client id
        Parse.initialize(this, getString(R.string.parse_application_id),
                getString(R.string.parse_client_key));

        // Initialize Facebook
        String appId = getString(R.string.facebook_app_id);
        ParseFacebookUtils.initialize(appId);

        // Required for push notifications
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

}
