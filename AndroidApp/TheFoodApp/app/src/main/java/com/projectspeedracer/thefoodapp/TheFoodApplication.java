package com.projectspeedracer.thefoodapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.SaveCallback;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.ProximityInspector;

/**
 * Created by avkadam on 3/24/15.
 */
public class TheFoodApplication extends Application {

	public static final String GOOGLE_API_KEY = "AIzaSyD6UJCC4Ey_VdaWqVB-AVEdur7_yu-cAyM"; // server key - works

	public static final float DEFAULT_SEARCH_DISTANCE = 500.0f * 10000; // in feet

	public static final Boolean isLocal = true; // true for testing

	public static final int MAX_NUM_PLACES = 6;

	public static Restaurant currentRestaurant;

    public static String subscribedChannel;

	public TheFoodApplication() {
	}

	public static String getGoogleApiKey() {
		return GOOGLE_API_KEY;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		ParseObject.registerSubclass(Dish.class);
        ParseObject.registerSubclass(Rating.class);
		ParseObject.registerSubclass(Restaurant.class);

		initializeParse();
	}

	private void initializeParse() {
		Parse.enableLocalDatastore(this);

		Parse.initialize(this,
				getString(R.string.parse_application_id),
				getString(R.string.parse_client_key));

		initializeFacebook();

		ParseTwitterUtils.initialize(
				getString(R.string.twitter_consumer_key),
				getString(R.string.twitter_consumer_secret));

		// Required for push notifications
		ParseInstallation.getCurrentInstallation().saveInBackground();
	}

	private void initializeFacebook() {
		String appId = getString(R.string.facebook_app_id);
		ParseFacebookUtils.initialize(appId);
	}

	public static float getSearchDistance() {
		return DEFAULT_SEARCH_DISTANCE;
	}

	public static void storeCurrentRestaurant(final Restaurant restaurant, boolean chosen, final SaveCallback callback) {

        currentRestaurant = restaurant;

		// TODO: Start progress overlay !!!
       if (!chosen) {
           // Not saving in backend yet
           return;
       }

        assert callback != null : "Save callback is null while saving restaurant!!!";


		final ParseQuery<Restaurant> query = ParseQuery.getQuery(Restaurant.class);
		query.whereEqualTo(Restaurant.Fields.PLACES_ID, restaurant.getPlacesId());

		final String msg = String.format("Sending out query for Restaurant %s Field: %s. Value: %s",
				restaurant.getName(),
				Restaurant.Fields.PLACES_ID,
				restaurant.getPlacesId());

		Log.i(Constants.TAG, msg);

		query.getFirstInBackground(new GetCallback<Restaurant>() {
			@Override
			public void done(Restaurant r, ParseException e) {
				final boolean restaurantExists = (r != null);

                if (r != null && e != null) {
                    e.printStackTrace();
                }

				final String name = restaurantExists ? r.getName() : restaurant.getName();
				final String id = restaurantExists ? r.getPlacesId() : restaurant.getPlacesId();

				final String msg = String.format("Restaurant %s (%s) %s",
						name,
						id,
						restaurantExists ? "already exists" : "saved");

				Log.i(Constants.TAG, msg);

				if (restaurantExists) {
                    currentRestaurant = r;
                    callback.done(null); // Fake it till you make it!!!
					return;
				}

				restaurant.saveInBackground(callback);
			}
		});
	}

	public static Restaurant getCurrentRestaurant() {
		return currentRestaurant;
	}
}
