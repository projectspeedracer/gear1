package com.projectspeedracer.thefoodapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.fragments.RestaurantListFragment;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;

import java.util.ArrayList;

public class PickRestaurantActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        GoogleApiClient.OnConnectionFailedListener,
        RestaurantListFragment.RestarantPickListener {

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    public static GoogleApiClient mGoogleApiClient;
    private Fragment listRestaurantFragment;

    private final String TAG = "PickRestaurant";

    ArrayList<Marker> markers;

    // Slow updates
    private long UPDATE_INTERVAL = 60000 * 5;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000 * 5; /* 5 secs */

    private Marker previousMarker;

    // Represents the circle around a map
    private Circle mapCircle;

    Location lastLocation;

    // Fields for the map radius in feet
    private float radius;

    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;


    /*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_restaurant);

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

        // Begin the transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the container with the new fragment
        listRestaurantFragment = new RestaurantListFragment();
        ft.replace(R.id.listFragmentHolder, listRestaurantFragment);
        ft.hide(mapFragment);
        ft.commit();

        radius = TheFoodApplication.getSearchDistance();

        markers = new ArrayList();
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
    }

    /*
	 * Called when the Activity is no longer visible.
	 */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /*
     * Called when the Activity is resumed. Updates the view.
     */
    @Override
    protected void onResume() {
        super.onResume();


        // Get the latest search distance preference
        radius = TheFoodApplication.getSearchDistance();
        // Checks the last saved location to show cached data if it's available. todo: use lastLocation

        // Checks the last saved location to show cached data if it's available
        if (lastLocation != null) {
            LatLng myLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            // If the search distance preference has been changed, move
            // map to new bounds.
//            if (lastRadius != radius) {
//                updateZoom(myLatLng);
//            }
            // Update the circle map
            updateCircle(myLatLng);
        }
    }


    /*
     * Handle results returned to the FragmentActivity by Google Play services
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.connect();
                        break;
                }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pick_restaurant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miShowHide:
                showHideMap(item);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Log.v(TAG, "Map Fragment was loaded properly!");
            map.setMyLocationEnabled(true);

            // Now that map has loaded, let's get our location!
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            connectClient();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void connectClient() {
        // Connect the client.
        if ( (FoodAppUtils.isGooglePlayServicesAvailable(this, CONNECTION_FAILURE_RESOLUTION_REQUEST, this))
                && (mGoogleApiClient != null) ) {
            mGoogleApiClient.connect();
        }
    }

    // Static API to return current Location.
    public static Location getCurrentLocation() {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public static FragmentManager getSupportFragmentManagerForHelper() {
        return getSupportFragmentManagerForHelper();
    }

    /*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
    @Override
    public void onConnected(Bundle bundle) {
        // Display the connection status
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
//            Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "GPS location was found!");
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);
            startLocationUpdates();
            onLocationChanged(location);
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    /*
     * Called by Location Services if the connection to the location client
     * drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    private Marker showMarkerAtPoint(LatLng point, Restaurant restaurant) {
        // Creates and adds marker to the map
        Marker marker = map.addMarker(new MarkerOptions()
                .position(point));

        FoodAppUtils.emphasisMarker(marker, restaurant);

        // Animate marker using drop effect
        dropPinEffect(marker);

        return marker;
    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
//        long start = SystemClock.uptimeMillis();
        final long duration = 1000;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            long start = SystemClock.uptimeMillis();
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
//                    start = SystemClock.uptimeMillis();
//                    handler.postDelayed(this, 15);

                }
            }
        });
    }


    // Listener for RestaurantListFragment
    @Override
    public void show_restaurant_on_map(Location location, Restaurant restaurant) {

        if (previousMarker != null) {
            // lower the emphasis on other markers
            FoodAppUtils.lowerEmphasis(previousMarker);
        }

        Marker currMarker = restaurant.getMarker();
        if (currMarker == null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            currMarker = showMarkerAtPoint(latLng, restaurant);
            restaurant.setMarker(currMarker);
        }
        else {
            FoodAppUtils.emphasisMarker(currMarker, restaurant);
        }

        previousMarker = currMarker;
    }

    // Listener for RestaurantListFragment
    @Override
    public void clear_result() {
        // Clear the map for new search
        previousMarker = null;
    }

    public void showHideMap(MenuItem mi) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.setCustomAnimations(android.R.anim.fade_in, R.animator.top_to_bottom_fragment);
//            ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        ft.setCustomAnimations(R.animator.top_to_bottom_fragment, R.animator.bottom_to_top_fragment);

        if (mapFragment.isHidden()) {
            // Request is to show the map
            ft.show(mapFragment);
            mi.setTitle(R.string.hide_map);
        } else {
            // Request is to hide the map
            ft.hide(mapFragment);
            mi.setTitle(R.string.show_map);
        }

        ft.commit();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.v(TAG, msg);

        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
            // If the location hasn't changed by more than 10 meters, ignore it.
            Log.v(TAG, "Ignoring minute location update");
            return;
        }
        lastLocation = location;

        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Update map radius indicator
        updateCircle(myLatLng);
    }

    /*
     * Helper method to get the Parse GEO point representation of a location
     */
    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

   /*
    * Displays a circle on the map representing the search radius
    */
    private void updateCircle(LatLng myLatLng) {
        radius = TheFoodApplication.getSearchDistance();
        if (mapCircle == null) {
            mapCircle =
                    mapFragment.getMap().addCircle(
                            new CircleOptions().center(myLatLng).radius(radius * METERS_PER_FEET));
            int baseColor = Color.DKGRAY;
            mapCircle.setStrokeColor(baseColor);
            mapCircle.setStrokeWidth(2);
            mapCircle.setFillColor(Color.argb(50, Color.red(baseColor), Color.green(baseColor),
                    Color.blue(baseColor)));
        }
        mapCircle.setCenter(myLatLng);
        mapCircle.setRadius(radius * METERS_PER_FEET); // Convert radius in feet to meters.
    }

}