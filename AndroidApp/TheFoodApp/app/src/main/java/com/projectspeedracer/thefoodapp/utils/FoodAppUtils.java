package com.projectspeedracer.thefoodapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.SaveCallback;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.fragments.AppDialogFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.models.Restaurant;

import java.util.List;

/**
 * Created by avkadam on 3/28/15.
 */
public class FoodAppUtils {

    public static String getShortDistance (Float distance) {
        Double distanceMi = distance * Constants.MILES_PER_METER; // converting to float
        distanceMi = Math.round(distanceMi * 100.0) / 100.0;
        String distanceStr = Double.toString(distanceMi);
        String distanceShort;
        String [] splitDistance = distanceStr.split("\\.");
        String integerPart = splitDistance[0];
        String fractionalPart = splitDistance[1];
        if (integerPart.length() > 1) {
            distanceShort = integerPart; // sets 10.02 --> 10
        }
        else if ((!integerPart.equals("0") && (fractionalPart.charAt(0) == '0'))) {
            distanceShort = integerPart; // sets 2.06 --> 2
        }
        else if (fractionalPart.charAt(0) != '0') {
            distanceShort = integerPart+"."+fractionalPart.charAt(0);
            // sets 0.77 --> 0.7
            // sets 2.71 --> 2.7
        }
        else {
            distanceShort = distanceStr; // keeps 0.06 --> 0.06
        }

        return distanceShort;
    }

    public static boolean isGooglePlayServicesAvailable(FragmentActivity activity, int RESULT_CODE, Activity a) {

        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());

        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        }

	    final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, a, RESULT_CODE);

	    // If Google Play services can provide an error dialog
	    if (errorDialog == null) {
		    return false;
	    }

	    /*ErrorDialogFragment errorFragment = new ErrorDialogFragment();
        errorFragment.setDialog(errorDialog);
        errorFragment.show(activity.getSupportFragmentManager(), "Location Updates");*/

	    final AppDialogFragment edFragment = new AppDialogFragment();
	    edFragment.setDialog(errorDialog);
	    edFragment.show(activity.getSupportFragmentManager(), "Location Updates");

	    return false;
    }

    public static void lowerEmphasis(Marker marker) {
        final BitmapDescriptor oldMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        marker.setAlpha(0.5f);
        marker.setIcon(oldMarkerIcon);
    }

    public static void emphasisMarker(Marker marker, Restaurant restaurant) {

	    final boolean inRange = PlacesUtils.IsRestaurantInRange(restaurant, PickRestaurantActivity.mGoogleApiClient);
	    final BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker(inRange
			    ? BitmapDescriptorFactory.HUE_GREEN
			    : BitmapDescriptorFactory.HUE_RED);

	    marker.setIcon(markerIcon);

        marker.setTitle(restaurant.getName());
        marker.showInfoWindow();
        marker.setAlpha(1);
    }


    public static void getDishFromObjectID(String dishObjectId, GetCallback callback) {
        ParseQuery query = new ParseQuery(Dish.class);
        query.getInBackground(dishObjectId, callback);
    }

    // Get list of all dishes from last 7 days
    public static void getAllDishesForRestaurant() {
        // orderby CreatedAt
        // restrict to last 7 days
        Restaurant restaurant = TheFoodApplication.getCurrentRestaurant();
        if (restaurant == null) {
            Log.e(Constants.TAG, "getAllDishesForRestaurant: Restaurant not selected while getting Dish details");
            return;
        }
        ParseRelation<ParseObject> relationRestaurant = restaurant.getRelation("RestaurantToPosts");
        ParseQuery query = relationRestaurant.getQuery();
        //todo: add 7 days constraint !!!

    }

    // Get list of Rating posts for a particular dish - For "Dish Details"
    public static void getAllPostsForDish(final Dish dish, FindCallback<Rating> callback) {

        final ParseRelation<Rating> relationDish = dish.getRelation("DishToPosts");
        final ParseQuery<Rating> query = relationDish.getQuery();

        // include respective User objects
        query.include(Rating.Fields.USER);
        query.include(Rating.Fields.DISH);
        query.orderByDescending("createdAt");

        // TODO: add 7 days constraint !!!
        query.findInBackground(callback);
    }

    // Get list of Rating posts for current Restaurant - For "Restaurant Wall"
    public static void getAllPostsForRestaurant() {
        // orderby CreatedAt
        // include repective User objects
    }

}
