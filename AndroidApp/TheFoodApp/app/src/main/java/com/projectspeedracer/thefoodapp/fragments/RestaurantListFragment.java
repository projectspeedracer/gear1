package com.projectspeedracer.thefoodapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.adapters.RestaurantsArrayAdapter;
import com.projectspeedracer.thefoodapp.models.GPlacesResponse;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.PlacesUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by avkadam on 3/27/15.
 */
public class RestaurantListFragment extends Fragment implements View.OnClickListener {

	private static final String GET_PLACE_INFO_URL = "https://maps.googleapis.com/maps/api/place/details/json?key=" + TheFoodApplication.getGoogleApiKey() + "&placeid=";
	private static final String TAG                = Constants.TAG;

	private RestaurantsArrayAdapter restaurantsAdapter;
	private RestaurantPickListener  listener;
	private ArrayList<Restaurant>   listRestaurants;

    ProgressBar pb;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_list_restaurant, container, false);

		listener = (RestaurantPickListener) getActivity();

		listRestaurants = new ArrayList<>();
		restaurantsAdapter = new RestaurantsArrayAdapter(getActivity(), listRestaurants);

        pb = (ProgressBar) view.findViewById(R.id.progressBar);
        FoodAppUtils.assignProgressBarStyle(getActivity(), pb);


		final ListView lvResults = (ListView) view.findViewById(R.id.lvResults);
		lvResults.setAdapter(restaurantsAdapter);
		lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Restaurant restaurant = restaurantsAdapter.getItem(position);
				Log.i(TAG, "Selected - " + restaurant.getName());
                pb.setVisibility(ProgressBar.VISIBLE); // visible till we start next activity
				listener.onPickRestaurant(restaurant);
			}
		});

		return view;
	}

	public void loadRestaurantList(String searchText) {
        pb.setVisibility(ProgressBar.VISIBLE);
		final String searchQ = searchText.isEmpty() ? "restaurants" : searchText;

		final Location location = PlacesUtils.GetCurrentLocation(PickRestaurantActivity.mGoogleApiClient);

		if (location == null) {
			Toast.makeText(getActivity(),
					"Current location was not available, please enable location services.",
					Toast.LENGTH_SHORT).show();
			return;
		}

		listener.clearAllMarkers();

		String currLongitude = Double.toString(location.getLongitude());
		String currLatitude = Double.toString(location.getLatitude());
		String locationQ = currLatitude + "," + currLongitude;

		// Toast.makeText(getActivity(), "Searching " + searchQ + " near: " + locationQ, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Searching " + searchQ + " near: " + locationQ);

        // eg. https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=37.4138286,-121.9033424&key=AIzaSyD6UJCC4Ey_VdaWqVB-AVEdur7_yu-cAyM&keyword=restaurants&rankby=distance
		String places_search_q = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + locationQ +
		                         "&key=" + TheFoodApplication.getGoogleApiKey() +
		                         "&keyword=" + searchQ + "&rankby=distance";
        places_search_q = places_search_q + "&type=food"; // Add type


		Log.v(Constants.TAG, "Getting restaurant details - " + places_search_q);

		doSearch(places_search_q);
	}

	private void doSearch(String places_search_q) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(getActivity(), places_search_q, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.e("RESP", response.toString());
				try {
					if (response.getString("status").equals("OK")) {
						//handleSearchResp(response); // with details
						handleSearchRespShort(response); // inline with Search response
					} else {
						Toast.makeText(getActivity(), "Error: " + response.getString("error_message"), Toast.LENGTH_SHORT).show();
                        doneLoading();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Toast.makeText(getActivity(), "Failed!!", Toast.LENGTH_SHORT).show();
                doneLoading();
			}
		});
	}

    private void handleSearchRespShort(JSONObject response) {
        try {
	        restaurantsAdapter.clear();
            final JSONArray placeIds = response.getJSONArray("results");

            for (int index = 0; index < placeIds.length(); ++index) {

                if (index >= TheFoodApplication.MAX_NUM_PLACES) {
                    // only interested in NUM_PLACES
                    break;
                }

                final JSONObject place = placeIds.getJSONObject(index);
                final GPlacesResponse gpr = new GPlacesResponse(place);
                final Restaurant restaurant = new Restaurant(gpr);
                listRestaurants.add(restaurant); // add to list, not adapter
                listener.restaurantSelected(restaurant, false); // to show on Map
            }



            doneLoading();

            // We dont want to fetch details yet..
            //fetchUpdatePlaceDetails();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

	private void handleSearchResp(JSONObject response) {
		try {
			restaurantsAdapter.clear();
			final JSONArray placeIds = response.getJSONArray("results");

			for (int index = 0; index < placeIds.length(); ++index) {

                if (index >= TheFoodApplication.MAX_NUM_PLACES) {
                    // only interested in NUM_PLACES
                    break;
                }

				final JSONObject place = placeIds.getJSONObject(index);
				final String name = place.getString("name");
				final String placeId = place.getString("place_id");

				final Restaurant restaurant = new Restaurant();
				restaurant.setName(name);
				restaurant.setPlacesId(placeId);

				restaurantsAdapter.add(restaurant);
			}

			fetchUpdatePlaceDetails();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	// Retrieve address from Place ID and update UI
	// Eg. https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&placeid=ChIJxTtMZDXJj4ARCgnf_1hmV6I
	// >>>>> bhimas - has 360 view
	// Eg. https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&placeid=ChIJbUvJJbXOj4ARf1axJhmN-1c
	// >>>>> dosa - has simple image
	// >>>>>>>>> Photo: Eg. https://maps.googleapis.com/maps/api/place/photo?maxwidth=1200&key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&photoreference=CnRnAAAArvbAnbfXda4dQ6DHkj83Uc79gJ0ASBSjQJgGsAbh5v8Brj9tRDtbvlaFH98pu4-XxdWAdQFLTYQKHLsiqaR4lOzOKgV9DdmUU0eaTcpOfx03KUToDy-TIRVqHRgfx_Q5BoqXU55UY0ORa7QBUcsdShIQMmQGFyomw-FJ0K-bOM6ljhoU-RDgNMD-NA84LbdEgkE5Zw_f0fE

	private void fetchUpdatePlaceDetails() {

		for (int i = 0; i < restaurantsAdapter.getCount(); i++) {
			final Restaurant restaurant = restaurantsAdapter.getItem(i);
			fetchUpdatePlace(restaurant);
		}

		Log.i(TAG, String.format("[fetchUpdatePlaceDetails] NoOfRestaurants: %s", restaurantsAdapter.getCount()));
	}

	private void fetchUpdatePlace(final Restaurant restaurant) {
		final String placesId = restaurant.getPlacesId();
		final AsyncHttpClient client = new AsyncHttpClient();

		Log.i(Constants.TAG, "Getting details of " + restaurant.getName() + " - " + GET_PLACE_INFO_URL + placesId);

		client.get(getActivity(), GET_PLACE_INFO_URL + placesId, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					final JSONObject result = response.getJSONObject(GPlacesResponse.Fields.RESULT);
					final GPlacesResponse gpr = new GPlacesResponse(result);
					restaurant.update(gpr);
                    listener.restaurantSelected(restaurant, false);

                    // we dont know if we are done with loading every restaurant,
                    // but still, consider it done...
                    doneLoading();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				Toast.makeText(getActivity(), "Could not get details!!", Toast.LENGTH_SHORT).show();
			}
		});
	}

    public String loadRawResource(int rId) {
        String content = "";

        try {
            final InputStream stream = getResources().openRawResource(rId);
            if (stream.available() > 0) {
                final byte[] bytes = new byte[stream.available()];
                stream.read(bytes);
                content = new String(bytes, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

	private static int[] OfflineSourceIds = new int[] {
			R.raw.madera,
			R.raw.quadrus,
			R.raw.bhimas,
			R.raw.dbawarchi,
			R.raw.jollibee,
			R.raw.mcdonalds,
	};

	public void loadOfflineRestaurantsData() {
        pb.setVisibility(ProgressBar.VISIBLE);
		restaurantsAdapter.clear();

		for (int rid : OfflineSourceIds) {
			try {
				final String json = loadRawResource(rid);
				if (StringUtils.isBlank(json)) { continue; }

				final JSONObject gprJson = new JSONObject(json);
				final JSONObject result = gprJson.getJSONObject(GPlacesResponse.Fields.RESULT);
				final GPlacesResponse gpr = new GPlacesResponse(result);
				final Restaurant restaurant = new Restaurant(gpr);

				Log.i(TAG, String.format("[loadOfflineRestaurantsData] Loaded data for restaurant: %s", restaurant.getName()));

				restaurantsAdapter.add(restaurant);
				listener.restaurantSelected(restaurant, false);
			} catch (JSONException e) {
				Log.e(TAG, String.format("[loadOfflineRestaurantsData] Failed loading data for restaurant resource id: %s", rid));
				e.printStackTrace();
			}
		}

        doneLoading();
	}

    private void doneLoading() {
        pb.setVisibility(ProgressBar.GONE);
        restaurantsAdapter.notifyDataSetChanged();

        if (restaurantsAdapter.getItem(0) != null) {
            // so that we keep nearest one selected on map
            listener.restaurantSelected(restaurantsAdapter.getItem(0), false);
        }
    }

	@Override
	public void onClick(View v) {

	}

	public interface RestaurantPickListener {
		public void restaurantSelected(Restaurant restaurant, boolean chosen);
		public void clearAllMarkers();
        public void onPickRestaurant(Restaurant restaurant);
	}
}
