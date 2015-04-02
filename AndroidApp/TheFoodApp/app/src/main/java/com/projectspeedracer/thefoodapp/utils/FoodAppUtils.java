package com.projectspeedracer.thefoodapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.fragments.AppDialogFragment;
import com.projectspeedracer.thefoodapp.models.Restaurant;

/**
 * Created by avkadam on 3/28/15.
 */
public class FoodAppUtils {

    // Conversion from meters to mi
    private static final double MILES_PER_METER = 0.000621371192;
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    public static String getShortDistance (Float distance) {
        Double distanceMi = distance * MILES_PER_METER; // converting to float
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

    public static Boolean isInRange(Location myLocation, Location placeLocation) {
        float distance = myLocation.distanceTo(placeLocation); // in meters
        float radius = TheFoodApplication.getSearchDistance();
	    return (radius * METERS_PER_FEET) >= distance;
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
}
