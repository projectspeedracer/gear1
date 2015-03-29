package com.projectspeedracer.thefoodapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseTwitterUtils;

/**
 * Created by avkadam on 3/24/15.
 */
public class TheFoodApplication extends Application {

    public static final String GOOGLE_API_KEY = "AIzaSyD6UJCC4Ey_VdaWqVB-AVEdur7_yu-cAyM"; // server key - works

    private static final float DEFAULT_SEARCH_DISTANCE = 500.0f; // in feet

    public static final Boolean isLocal = true; // true for testing

    public TheFoodApplication() {
    }

    public static String getGoogleApiKey() {
        return GOOGLE_API_KEY;
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

        // Initialize Twitter
        ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key),
                                     getString(R.string.twitter_consumer_secret));

        // Required for push notifications
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static float getSearchDistance() {
        return DEFAULT_SEARCH_DISTANCE;
    }


}
