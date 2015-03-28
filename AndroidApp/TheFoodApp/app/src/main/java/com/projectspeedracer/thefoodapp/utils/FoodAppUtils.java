package com.projectspeedracer.thefoodapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;

/**
 * Created by avkadam on 3/28/15.
 */
public class FoodAppUtils {

    public static String getShortDistance (Float distance) {
        Double distanceMi = distance * 0.000621371192; // converting to float
        distanceMi = Math.round(distanceMi * 100.0) / 100.0;
        String distranceStr = Double.toString(distanceMi);
        String distanceShort;
        String [] splitDistance = distranceStr.split("\\.");
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
            distanceShort = distranceStr; // keeps 0.06 --> 0.06
        }

        return distanceShort;
    }

    public static boolean isGooglePlayServicesAvailable(Context c, int RESULT_CODE, Activity a) {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(c);
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
                errorFragment.show(PickRestaurantActivity.getSupportFragmentManagerForHelper(), "Location Updates");
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
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public static void lowerEmphasis(Marker marker) {
        BitmapDescriptor oldMarkerIcon =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        marker.setAlpha(0.5f);
        marker.setIcon(oldMarkerIcon);
    }

    public static void emphasisMarker(Marker marker, String title) {
        BitmapDescriptor currMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        marker.setTitle(title);
        marker.setIcon(currMarkerIcon);
        marker.showInfoWindow();
        marker.setAlpha(1);
    }
}
