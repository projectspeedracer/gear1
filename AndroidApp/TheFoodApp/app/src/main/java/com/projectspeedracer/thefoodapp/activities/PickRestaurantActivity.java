package com.projectspeedracer.thefoodapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.adapters.CustomMarkerWindowAdapter;
import com.projectspeedracer.thefoodapp.fragments.PlateRateDialogFragment;
import com.projectspeedracer.thefoodapp.fragments.RestaurantListFragment;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.projectspeedracer.thefoodapp.utils.PlacesUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
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

        unsubscribeStaleChannels();
    }

    private void unsubscribeStaleChannels() {
        List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
        if (subscribedChannels != null) {
            for (String channel : subscribedChannels) {
                ParsePush.unsubscribeInBackground(channel);
            }
        }
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

        //check if user is subscribed to any restaurant, if so, unsubscribe..
        if (TheFoodApplication.subscribedChannel != null) {
            ParsePush.unsubscribeInBackground(TheFoodApplication.subscribedChannel);
        }

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

            case R.id.item_logout:
                FoodAppUtils.showSignOutDialog(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {

        final Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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

        if (TheFoodApplication.isLocal) {
            listRestaurantFragment.loadOfflineRestaurantsData();
        }
        else {
            listRestaurantFragment.loadRestaurantList("");
        }
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
        if (!connectionResult.hasResolution()) {
	        Toast.makeText(getApplicationContext(), "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
            return;
        }

	    try {
		    connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
	    } catch (IntentSender.SendIntentException e) {
		    e.printStackTrace();
	    }
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
		// dropPinEffect(marker);
		return marker;
	}

	// region RestaurantListFragment Listener

	@Override
    public void restaurantSelected(Restaurant restaurant, boolean chosen) {
        showRestaurantOnMap(restaurant);

//		final boolean inRange = PlacesUtils.IsRestaurantInRange(restaurant, mGoogleApiClient);

		/*final Button btnEnter = (Button) findViewById(R.id.btnEnter);
		btnEnter.setText(inRange
				? getString(R.string.enter_into) + " " + restaurant.getName()
				: restaurant.getName()+" "+getString(R.string.get_closer));
*/
        TheFoodApplication.storeCurrentRestaurant(restaurant, chosen,
                                                  (chosen == false) ? null : RestaurantSaveCallback); // Choose, store at backend
    }

    public final SaveCallback RestaurantSaveCallback = new SaveCallback() {
        @Override
        public void done(ParseException e) {
            final String msg = String.format("Restaurant save callback: %s. Name: %s. Id: %s",
                    e != null ? "FAILED!" : "SUCCESS",
                    TheFoodApplication.getCurrentRestaurant().getName(),
                    TheFoodApplication.getCurrentRestaurant().getPlacesId());

            Log.i(Constants.TAG, msg);
            // Start Activity only after saving a Restaurant successfully!!!
            startActivity(new Intent(getApplicationContext(), RestaurantActivity.class));

            // TODO: stop progress bar!!!
        }
    };

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
		final FragmentTransaction ftMap = getSupportFragmentManager().beginTransaction();
		ftMap.setCustomAnimations(R.animator.top_to_bottom_fragment, R.animator.bottom_to_top_fragment);

		FragmentTransaction ftList = getSupportFragmentManager().beginTransaction();
		ftList.setCustomAnimations(R.animator.top_to_bottom_fragment, R.animator.bottom_to_top_fragment);
		//ft.setCustomAnimations(android.R.anim.fade_in, R.animator.top_to_bottom_fragment);
		//ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);

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

	@Override
	public void onPickRestaurant(Restaurant restaurant) {

		if (!PlacesUtils.IsRestaurantInRange(restaurant, mGoogleApiClient)) {
			Toast.makeText(this, restaurant.getName() +" is not in range. Get closer to enter.", Toast.LENGTH_SHORT).show();
            listRestaurantFragment.cancel();
			return;
		}

		restaurantSelected(restaurant, true);

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

	protected void startLocationUpdates() {
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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

    private void doSearch() {
        final String searchTerm = etSearch.getText().toString();

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        listRestaurantFragment.loadRestaurantList(searchTerm); // black searchTerm means nearby 'Restaurants'
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
}
