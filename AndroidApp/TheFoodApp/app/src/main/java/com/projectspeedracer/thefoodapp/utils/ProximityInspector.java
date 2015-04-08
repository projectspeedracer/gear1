package com.projectspeedracer.thefoodapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.ParsePush;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.models.Restaurant;

/**
 * Created by avkadam on 4/7/15.
 * Monitors and enforces user's proximity to selected restaurant...
 */
public class ProximityInspector implements GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "ProximityMonitor";

    // Create the Handler object
    Handler handler;

    private GoogleApiClient mGoogleApiClient;

    Context c;
    FragmentActivity a;

    public ProximityInspector(Context c, FragmentActivity a) {

        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        this.c = c;
        this.a = a;

        Log.i(TAG,"Will attempt to connect");
    }

    public void start () {
        handler = new Handler();

        // Create the Handler object
        Handler handler = new Handler();
// Define the task to be run here

// Execute a runnable task as soon as possible
        handler.post(runnableCode);
    }

    public void stop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here
            Log.e("Handlers", "Called in PI....");
            // Repeat this runnable code block again every 2 seconds
            handler.postDelayed(runnableCode, 2000);
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        final Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Log.e(TAG, "Location unavailable to proximity inspector");
            return;
        }

        Log.i(TAG, "Connected...");

        // No need for periodic checks..
//        start();

        // Start location updates..
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.e(TAG, "Connection suspended, PI operation cannot be performed..");
        // TODO: unsubscribe
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO: Check proximity - User still near this resturant?
        checkAndEnforceProximity(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed, PI operation cannot be performed..");
        // TODO: unsubscribe
    }

    public void checkAndEnforceProximity(Location location) {
        /* functionality is not as cyptic as the name!!!
        Simply check whether user is still in rage
        If not, then alert user that you are being 'thrown out'
        but in a little polite manner */

        Restaurant restaurant = TheFoodApplication.getCurrentRestaurant();
        boolean isInRange = PlacesUtils.IsInRange(location, Helpers.ToLocation(restaurant.getLocation()));

        Log.i(TAG, isInRange ? "User is still inside restaurant" : " User went out of this restaurant");

        if (!isInRange) {
            Log.i(TAG, "Good bye user!!!");
            goodByeUser(restaurant);
        }
    }

    private void goodByeUser(Restaurant restaurant) {
        // un-subscribe from push notification channel
        //check if user is subscribed to any restaurant, if so, unsubscribe..
        if (TheFoodApplication.subscribedChannel != null) {
            ParsePush.unsubscribeInBackground(TheFoodApplication.subscribedChannel);
            Log.e(TAG, "Unsubscribing user from push channel");
        }
        else {
            Log.e(TAG, "User was not subscribed to any channel.");
        }

        // Show dialog - it will start over
        FoodAppUtils.showGoodByeDialog(a);
    }

}
