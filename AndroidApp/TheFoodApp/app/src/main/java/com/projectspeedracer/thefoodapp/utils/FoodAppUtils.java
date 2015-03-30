package com.projectspeedracer.thefoodapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.parse.ParseGeoPoint;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
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
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, a,
                    RESULT_CODE);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(activity.getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public static void lowerEmphasis(Marker marker) {
        BitmapDescriptor oldMarkerIcon =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        marker.setAlpha(0.5f);
        marker.setIcon(oldMarkerIcon);
    }

    public static void emphasisMarker(Marker marker, Restaurant restaurant) {
	    marker.setTitle(restaurant.getName());
        marker.showInfoWindow();
        marker.setAlpha(1);

	    final Location location = Helpers.ToLocation(restaurant.getLocation());
	    final boolean inRange = isInRange(PickRestaurantActivity.getCurrentLocation(), location);
	    final BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker(inRange
			    ? BitmapDescriptorFactory.HUE_GREEN
			    : BitmapDescriptorFactory.HUE_RED);

	    marker.setIcon(markerIcon);
    }
}
