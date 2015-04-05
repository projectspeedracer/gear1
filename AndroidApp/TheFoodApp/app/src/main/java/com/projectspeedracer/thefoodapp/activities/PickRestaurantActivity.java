package com.projectspeedracer.thefoodapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.projectspeedracer.thefoodapp.adapters.CustomMarkerWindowAdapter;
import com.projectspeedracer.thefoodapp.fragments.RestaurantListFragment;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.projectspeedracer.thefoodapp.utils.PlacesUtils;

import java.util.HashMap;
import java.util.Map;

public class PickRestaurantActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        GoogleApiClient.OnConnectionFailedListener,
		RestaurantListFragment.RestaurantPickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        View.OnClickListener {

	public static final String TAG = Constants.TAG;

	public static GoogleApiClient mGoogleApiClient;

	private HashMap<String, Marker> markerPlacesIdMap = new HashMap<>();
    private HashMap<Marker, Restaurant> markerRestaurantMap = new HashMap<>();
	private RestaurantListFragment listRestaurantFragment;
	private SupportMapFragment mapFragment;
	private Circle mapCircle;
	private GoogleMap map;

    private Location lastLocation;

    private EditText etSearch;

    /*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_restaurant);

        Button b = (Button) findViewById(R.id.btnSearch);
        b.setOnClickListener(this);

        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.etSearch || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();
                    return true;
                }
                return false;
            }
        });

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

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    listRestaurantFragment = new RestaurantListFragment();
        ft.replace(R.id.listFragmentHolder, listRestaurantFragment);
        ft.hide(mapFragment);
        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
	    // Connect the client.
	    if ( (FoodAppUtils.isGooglePlayServicesAvailable(this, CONNECTION_FAILURE_RESOLUTION_REQUEST, this))
	            && (mGoogleApiClient != null) ) {
	        mGoogleApiClient.connect();
	    }
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Checks the last saved location to show cached data if it's available.
        // TODO: use lastLocation

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
	    if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST) {
		    if (resultCode == Activity.RESULT_OK) {
			    mGoogleApiClient.connect();
		    }
	    }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pick_restaurant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.miShowHide:
                showHideMap(item);
                return true;

            case R.id.action_settings:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMap(GoogleMap googleMap) {
        map = googleMap;

        if (map == null) {
            Toast.makeText(this, "ERROR! Got a null Map object reference.", Toast.LENGTH_SHORT).show();
	        return;
        }

	    Log.v(TAG, "Map Fragment was loaded properly!");

	    map.setMyLocationEnabled(true);
	    map.setOnMarkerClickListener(this);
	    map.setOnInfoWindowClickListener(this);
	    map.setInfoWindowAdapter(new CustomMarkerWindowAdapter(getLayoutInflater()));

	    mGoogleApiClient = new GoogleApiClient.Builder(this)
			    .addApi(LocationServices.API)
			    .addConnectionCallbacks(this)
			    .addOnConnectionFailedListener(this).build();

	    if (mGoogleApiClient != null
	        && FoodAppUtils.isGooglePlayServicesAvailable(this, CONNECTION_FAILURE_RESOLUTION_REQUEST, this)) {
		    mGoogleApiClient.connect();
	    }
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
        if (location == null) {
	        Toast.makeText(this, "Current location was not available, please enable location services.", Toast.LENGTH_SHORT).show();
	        return;
        }

	    //Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
	    Log.v(TAG, "GPS location was found!");

	    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
	    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
	    map.animateCamera(cameraUpdate);
	    startLocationUpdates();
	    onLocationChanged(location);

	    listRestaurantFragment.loadRestaurantList("");
    }

    protected void startLocationUpdates() {
	    LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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

	private Marker addNewMarker(Restaurant restaurant) {
		final Location location = Helpers.ToLocation(restaurant.getLocation());
        if (location == null) {
            return null;
        }
		final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		final MarkerOptions position = new MarkerOptions().position(latLng);
		final Marker marker = map.addMarker(position);

		FoodAppUtils.emphasisMarker(marker, restaurant);

        // Animation not useful as this happens in background now
//		dropPinEffect(marker);
		return marker;
	}


	// region RestaurantListFragment Listener

	@Override
    public void restaurantSelected(Restaurant restaurant) {
        showRestaurantOnMap(restaurant);

//		final boolean inRange = PlacesUtils.IsRestaurantInRange(restaurant, mGoogleApiClient);

		/*final Button btnEnter = (Button) findViewById(R.id.btnEnter);
		btnEnter.setText(inRange
				? getString(R.string.enter_into) + " " + restaurant.getName()
				: restaurant.getName()+" "+getString(R.string.get_closer));
*/
        TheFoodApplication.storeCurrentRestaurant(restaurant);
    }

	@Override
	public void clearAllMarkers() {
		for (Map.Entry<String, Marker> entry : markerPlacesIdMap.entrySet()) {
			entry.getValue().remove();
		}

		markerPlacesIdMap.clear();
        markerRestaurantMap.clear();
	}

	// endregion

	public void showRestaurantOnMap(Restaurant restaurant) {
		final String placesId = restaurant.getPlacesId();
		Boolean newMarkerAdded = false;

		if (!markerPlacesIdMap.containsKey(placesId)) {
			Marker marker = addNewMarker(restaurant);
            if (marker != null) {
                markerPlacesIdMap.put(restaurant.getPlacesId(), marker);
                markerRestaurantMap.put(marker, restaurant);
                newMarkerAdded = true;
            }
		}

		for (Map.Entry<String, Marker> entry : markerPlacesIdMap.entrySet()) {
			final boolean current = entry.getKey().equals(placesId);
			final Marker marker = entry.getValue();

			if (current) {
				if (!newMarkerAdded) {
					FoodAppUtils.emphasisMarker(marker, restaurant);
				}
			} else {
				FoodAppUtils.lowerEmphasis(marker);
			}
		}
	}

	public void showHideMap(MenuItem mi) {

		FragmentTransaction ftMap = getSupportFragmentManager().beginTransaction();
        FragmentTransaction ftList = getSupportFragmentManager().beginTransaction();
//            ft.setCustomAnimations(android.R.anim.fade_in, R.animator.top_to_bottom_fragment);
//            ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
		ftMap.setCustomAnimations(R.animator.top_to_bottom_fragment, R.animator.bottom_to_top_fragment);
        ftList.setCustomAnimations(R.animator.top_to_bottom_fragment, R.animator.bottom_to_top_fragment);

		if (mapFragment.isHidden()) {
			// Request is to show the map
            ftList.hide(listRestaurantFragment);
            ftList.commit();
			ftMap.show(mapFragment);
            ftMap.commit();
			mi.setTitle(R.string.hide_map);
		} else {
			// Request is to hide the map
            ftList.show(listRestaurantFragment);
            ftList.commit();
			ftMap.hide(mapFragment);
            ftMap.commit();
			mi.setTitle(R.string.show_map);
		}

	}

	@Override
	public void onLocationChanged(Location location) {

		Log.v(TAG, String.format("Updated Location: %f, %f", location.getLatitude(), location.getLongitude()));

		final ParseGeoPoint currentPoint = Helpers.ToParseGeoPoint(location);
		final double distance = lastLocation == null
				? 0
				: currentPoint.distanceInKilometersTo(Helpers.ToParseGeoPoint(lastLocation));


		if (distance < 0.01 && (lastLocation != null)) {
			// If the location hasn't changed by more than 10 meters, ignore it.
			Log.v(TAG, "Ignoring minute location update");
			return;
		}
		lastLocation = location;
		updateCircle(new LatLng(location.getLatitude(), location.getLongitude()));
	}

	private void updateCircle(LatLng myLatLng) {
		final double radius = TheFoodApplication.getSearchDistance();

		if (mapCircle == null) {
			final int fillColor = Color.argb(50,
					Color.red(Color.DKGRAY),
					Color.green(Color.DKGRAY),
					Color.blue(Color.DKGRAY));

			final CircleOptions circleOptions = new CircleOptions().center(myLatLng).radius(radius * Constants.METERS_PER_FEET);
			mapCircle = mapFragment.getMap().addCircle(circleOptions);
			mapCircle.setStrokeColor(getResources().getColor(R.color.accent));
			mapCircle.setStrokeWidth(2);
			mapCircle.setFillColor(fillColor);
		}

		mapCircle.setCenter(myLatLng);
		mapCircle.setRadius(radius * Constants.METERS_PER_FEET); // Convert radius in feet to meters.
	}

    public void onPickRestaurant(View v) {
        final Restaurant restaurant = TheFoodApplication.getCurrentRestaurant();

        if (restaurant == null) {
            Toast.makeText(this, "Please select a restaurant.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PlacesUtils.IsRestaurantInRange(restaurant, mGoogleApiClient)) {
	        Toast.makeText(this, restaurant.getName() +" is not in range. Get closer to enter.", Toast.LENGTH_SHORT).show();
            return;
        }

	    startActivity(new Intent(this, RestaurantActivity.class));
    }

    @Override
    public void onPickRestaurant(Restaurant restaurant) {

        if (!PlacesUtils.IsRestaurantInRange(restaurant, mGoogleApiClient)) {
            Toast.makeText(this, restaurant.getName() +" is not in range. Get closer to enter.", Toast.LENGTH_SHORT).show();
            return;
        }
        restaurantSelected(restaurant);
        startActivity(new Intent(this, RestaurantActivity.class));
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        Restaurant restaurant = markerRestaurantMap.get(marker);
        showRestaurantOnMap(restaurant);
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Restaurant restaurant = markerRestaurantMap.get(marker);
        onPickRestaurant(restaurant);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                doSearch();
                break;
        }
    }

    private void doSearch() {
        String searchQ = etSearch.getText().toString();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        listRestaurantFragment.loadRestaurantList(searchQ);
    }

}