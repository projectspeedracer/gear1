package com.projectspeedracer.thefoodapp.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.projectspeedracer.thefoodapp.utils.PlacesUtils;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by avkadam on 3/27/15.
 */
public class RestaurantListFragment extends Fragment implements View.OnClickListener {

	private static final String TAG = Constants.TAG;

	private EditText                etSearch;
	private RestaurantsArrayAdapter aRestaurants;
	private RestaurantPickListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_list_restaurant, container, false);

		etSearch = (EditText) view.findViewById(R.id.etSearch);
		ListView lvResults = (ListView) view.findViewById(R.id.lvResults);

		listener = (RestaurantPickListener) getActivity();

		ArrayList<Restaurant> listRestaurants = new ArrayList<>();
		aRestaurants = new RestaurantsArrayAdapter(getActivity(), listRestaurants);
		lvResults.setAdapter(aRestaurants);
		lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Restaurant restaurant = aRestaurants.getItem(position);
				//Toast.makeText(getActivity(), "Picked - " + restaurant.getName(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Selected - "+restaurant.getName());
				listener.restaurantSelected(restaurant);
			}
		});

		Button b = (Button) view.findViewById(R.id.btnSearch);
		b.setOnClickListener(this);

		//radius = TheFoodApplication.getSearchDistance();

		return view;
	}

	public void loadRestaurantList() {
		final String searchText = etSearch.getText().toString();
		final String searchQ = searchText.isEmpty() ? "restaurants" : searchText;

		Location location = PlacesUtils.GetCurrentLocation(PickRestaurantActivity.mGoogleApiClient);
		String currLongitude = Double.toString(location.getLongitude());
		String currLatitude = Double.toString(location.getLatitude());
		String locationQ = currLatitude + "," + currLongitude;

		// Toast.makeText(getActivity(), "Searching " + searchQ + " near: " + locationQ, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Searching " + searchQ + " near: " + locationQ);

		final String places_search_q = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + locationQ +
		                         "&key=" + TheFoodApplication.getGoogleApiKey() +
		                         "&keyword=" + searchQ + "&rankby=distance"; // WORKS

		Log.v(Constants.TAG, "Getting restaurant details - " + places_search_q);

		listener.clearAllMarkers();

		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

		if (TheFoodApplication.isLocal) {
			// For testing
			loadDummy();
		} else {
			// Actual search
			doSearch(places_search_q);
		}
	}

	private void doSearch(String places_search_q) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(getActivity(), places_search_q, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.e("RESP", response.toString());
				try {
					if (response.getString("status").equals("OK")) {
						handleSearchResp(response);
					} else {
						Toast.makeText(getActivity(), "Error: " + response.getString("error_message"), Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Toast.makeText(getActivity(), "Failed!!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void handleSearchResp(JSONObject response) {
		try {
			aRestaurants.clear();
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

				aRestaurants.add(restaurant);
			}

			fetchUpdatePlaceDetails();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	static int counter = 0;
	// Retrieve address from Place ID and update UI
	// Eg. https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&placeid=ChIJxTtMZDXJj4ARCgnf_1hmV6I
	// >>>>> bhimas - has 360 view
	// Eg. https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&placeid=ChIJbUvJJbXOj4ARf1axJhmN-1c
	// >>>>> dosa - has simple image
	// >>>>>>>>> Photo: Eg. https://maps.googleapis.com/maps/api/place/photo?maxwidth=1200&key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&photoreference=CnRnAAAArvbAnbfXda4dQ6DHkj83Uc79gJ0ASBSjQJgGsAbh5v8Brj9tRDtbvlaFH98pu4-XxdWAdQFLTYQKHLsiqaR4lOzOKgV9DdmUU0eaTcpOfx03KUToDy-TIRVqHRgfx_Q5BoqXU55UY0ORa7QBUcsdShIQMmQGFyomw-FJ0K-bOM6ljhoU-RDgNMD-NA84LbdEgkE5Zw_f0fE

	private void fetchUpdatePlaceDetails() {

		for (int i = 0; i < aRestaurants.getCount(); i++) {
			final Restaurant restaurant = aRestaurants.getItem(i);
			fetchUpdatePlace(restaurant);
		}

		aRestaurants.notifyDataSetChanged();

		Log.i(TAG, "fetchUpdatePlaceDetails: " + aRestaurants.getCount() + "restaurants (counter=)" + counter);
	}

	private static final String GET_PLACE_INFO_URL = "https://maps.googleapis.com/maps/api/place/details/json?key=" + TheFoodApplication.getGoogleApiKey() + "&placeid=";


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
                    aRestaurants.notifyDataSetChanged();
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

	private void loadDummy() {
		String jsonData1 = "{\"html_attributions\":[],\"status\":\"OK\",\"result\":{\"place_id\":\"ChIJcROz_crOj4ARUkCpDr8lR50\",\"icon\":\"http:\\/\\/maps.gstatic.com\\/mapfiles\\/place_api\\/icons\\/restaurant-71.png\",\"reviews\":[{\"author_name\":\"janette belda\",\"aspects\":[{\"type\":\"overall\",\"rating\":3}],\"time\":1407833163,\"text\":\"I know the yum yum chickenjoy when i come to 100 weeks it was closed\",\"rating\":5,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/114282481758722154019\"}],\"scope\":\"GOOGLE\",\"website\":\"http:\\/\\/www.jollibeeusa.com\\/\",\"user_ratings_total\":1,\"international_phone_number\":\"+1 408-719-1344\",\"adr_address\":\"<span class=\\\"street-address\\\">447 Great Mall Drive<\\/span>, <span class=\\\"locality\\\">Milpitas<\\/span>, <span class=\\\"region\\\">CA<\\/span> <span class=\\\"postal-code\\\">95035<\\/span>, <span class=\\\"country-name\\\">United States<\\/span>\",\"url\":\"https:\\/\\/plus.google.com\\/108113487726910343063\\/about?hl=en-US\",\"reference\":\"CmRcAAAAg8YmFLL-YXAQVEhEgBiDY5sXG2oBf2X4AiYQO549c5zivl1kiLXHpvXdbm4HSpSk-ki4TxC3Stc9TOriUxcs0dh3U0w0EyQmBsmVW9A-TCr_tbEqMpnOi5Ynj7RCxfuzEhAxvBszjIlBb7oP9bvccHhIGhRDQiHAXdq9ZLgutQ0zRGC6JFnN4Q\",\"opening_hours\":{\"periods\":[{\"open\":{\"time\":\"0800\",\"day\":0},\"close\":{\"time\":\"2000\",\"day\":0}},{\"open\":{\"time\":\"0800\",\"day\":1},\"close\":{\"time\":\"2100\",\"day\":1}},{\"open\":{\"time\":\"0800\",\"day\":2},\"close\":{\"time\":\"2100\",\"day\":2}},{\"open\":{\"time\":\"0800\",\"day\":3},\"close\":{\"time\":\"2100\",\"day\":3}},{\"open\":{\"time\":\"0800\",\"day\":4},\"close\":{\"time\":\"2100\",\"day\":4}},{\"open\":{\"time\":\"0800\",\"day\":5},\"close\":{\"time\":\"2100\",\"day\":5}},{\"open\":{\"time\":\"0800\",\"day\":6},\"close\":{\"time\":\"2100\",\"day\":6}}],\"open_now\":true,\"weekday_text\":[\"Monday: 8:00 am – 9:00 pm\",\"Tuesday: 8:00 am – 9:00 pm\",\"Wednesday: 8:00 am – 9:00 pm\",\"Thursday: 8:00 am – 9:00 pm\",\"Friday: 8:00 am – 9:00 pm\",\"Saturday: 8:00 am – 9:00 pm\",\"Sunday: 8:00 am – 8:00 pm\"]},\"geometry\":{\"location\":{\"lng\":-121.899188,\"lat\":37.416083}},\"utc_offset\":-420,\"id\":\"c365fd34e799af545a5811c56107e0ea8fcb99ff\",\"vicinity\":\"447 Great Mall Drive, Milpitas\",\"address_components\":[{\"types\":[\"street_number\"],\"short_name\":\"447\",\"long_name\":\"447\"},{\"types\":[\"route\"],\"short_name\":\"Great Mall Dr\",\"long_name\":\"Great Mall Drive\"},{\"types\":[\"locality\",\"political\"],\"short_name\":\"Milpitas\",\"long_name\":\"Milpitas\"},{\"types\":[\"administrative_area_level_3\",\"political\"],\"short_name\":\"San Jose\",\"long_name\":\"San Jose\"},{\"types\":[\"administrative_area_level_2\",\"political\"],\"short_name\":\"Santa Clara County\",\"long_name\":\"Santa Clara County\"},{\"types\":[\"administrative_area_level_1\",\"political\"],\"short_name\":\"CA\",\"long_name\":\"California\"},{\"types\":[\"country\",\"political\"],\"short_name\":\"US\",\"long_name\":\"United States\"},{\"types\":[\"postal_code\"],\"short_name\":\"95035\",\"long_name\":\"95035\"}],\"name\":\"Jollibee\",\"formatted_address\":\"447 Great Mall Drive, Milpitas, CA 95035, United States\",\"formatted_phone_number\":\"(408) 719-1344\",\"types\":[\"restaurant\",\"food\",\"establishment\"]}}";
		String jsonData2 = "{\"html_attributions\":[],\"status\":\"OK\",\"result\":{\"place_id\":\"ChIJ-2Oez7XOj4AR0YIq0mqoK7U\",\"icon\":\"http:\\/\\/maps.gstatic.com\\/mapfiles\\/place_api\\/icons\\/restaurant-71.png\",\"reviews\":[{\"author_name\":\"A Google User\",\"aspects\":[{\"type\":\"overall\",\"rating\":3}],\"time\":1318412027,\"text\":\"\",\"rating\":5,\"language\":\"en\"}],\"scope\":\"GOOGLE\",\"website\":\"http:\\/\\/www.mcdonalds.com\\/\",\"user_ratings_total\":1,\"international_phone_number\":\"+1 408-935-8225\",\"adr_address\":\"<span class=\\\"street-address\\\">1249 Great Mall Drive<\\/span>, <span class=\\\"locality\\\">Milpitas<\\/span>, <span class=\\\"region\\\">CA<\\/span> <span class=\\\"postal-code\\\">95035<\\/span>, <span class=\\\"country-name\\\">United States<\\/span>\",\"url\":\"https:\\/\\/plus.google.com\\/110387571272333961905\\/about?hl=en-US\",\"reference\":\"CmReAAAALT2d-b3TK1t9cOT7u4O1mnQytrtDeIhGmBTaLP4r7y8i32ix0hR3LcPIpuqPkMkROwC3duoDQWh2kdutJR-Q1rTtl7YS8v1eKKXDxEpuaMl8xXe9I2_5L8iVFvr7l8--EhCat05zfmeDKS834g4BAXE2GhS4YU3kUtXp-yREmGlxNsp8BnLITQ\",\"opening_hours\":{\"periods\":[{\"open\":{\"time\":\"0500\",\"day\":0},\"close\":{\"time\":\"0100\",\"day\":1}},{\"open\":{\"time\":\"0500\",\"day\":1},\"close\":{\"time\":\"0100\",\"day\":2}},{\"open\":{\"time\":\"0500\",\"day\":2},\"close\":{\"time\":\"0100\",\"day\":3}},{\"open\":{\"time\":\"0500\",\"day\":3},\"close\":{\"time\":\"0000\",\"day\":5}},{\"open\":{\"time\":\"0500\",\"day\":5},\"close\":{\"time\":\"0000\",\"day\":0}}],\"open_now\":true,\"weekday_text\":[\"Monday: 5:00 am – 1:00 am\",\"Tuesday: 5:00 am – 1:00 am\",\"Wednesday: 5:00 am – 12:00 am\",\"Thursday: Open 24 hours\",\"Friday: 5:00 am – 12:00 am\",\"Saturday: Open 24 hours\",\"Sunday: 5:00 am – 1:00 am\"]},\"geometry\":{\"location\":{\"lng\":-121.897766,\"lat\":37.412731}},\"utc_offset\":-420,\"price_level\":1,\"photos\":[{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAhFsj4mTiRQ_zptcPV5ZK5eBDM7IZbuUB87M1tRccbeggy_Z5hrPMqkr_T0MxYsL6tktP8SpmtAlztWhhw2oKGaDysH8s25XBNsWD0o4qv4Wxg_g3i3EOPd0Ss1axIBBF4wb4z5QMKmd6kGLLOm-OzBIQWQT3WGG67Mk9P1ks2HZKUBoUqsjmDr2IxUiKZSMkZq-5Sr24cA0\",\"width\":2512,\"height\":1600},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAA1uWNaBiKqpzaTL7P2Dv4sVUBPc0nIgPI6yHHe_DVhEfsLZDaT6WP4uwrjOxycQtLSZQv2lk1uQ5I8SBmwKki-txQBUndjINqicF9RYBMEnB8ZZ0DZtJwXznyh4dnVy0iyjkWjras65xDmlxLRXD50BIQ2RidZgaWQP8bT32xJDfcPBoUNxmgu_W5s1-JTeACUPsHtni8r2o\",\"width\":2400,\"height\":1600},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAeB-0yN7x-nMaATTyizzjRSPmDdfNccQhQJSc0hYHtylJAcOgDE6IbRow2w1jc-oUZx81gHGEVxcRzoh7jOqDH0BZXGMx0ZEFkoVzCCbS4vtC0EzcjvJIPu6uj1jtghnLahKj6_HVblF-VW-xr12O5xIQMo7Gx4F5czed8mtfBgY_XxoU7_44LaIbGkBMg8bP9-jKtjybZ48\",\"width\":2400,\"height\":1600},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAN6XXaPXISvVzrV1yoKTH8joYmbm39z-NLK9KoE2slaRB_EzO1ARyWtYAA7u-CSBvdcEg_GTmKNmLCvZC7MC5oCkTxTDAYuV2J--IDm06Vw3EMWuHXldONxsxrLyW4Iq0pEhaHx4Ukja7CObsxa_XKhIQi0d_iYiQEwQXWcmG3sshKxoUWooSMTpyF25vJHgMyRCRooBoo20\",\"width\":2400,\"height\":1600},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAH1o5ndkBsScanvoVul3-AssBsIOIKhckQFVWNxMgvLWGPZrKZBvUB0opfM9RMKBzIeln-1QKr1RGzcDtropAy9ARbF6Mks0-PcJ0d3H6UV1N2Aw6_epIBAuJUtQA-M2w1Kl-cKbKid6v5I2yEQagHBIQD9cQDenMy7efN2vdkuIrEBoUMrZguht7mYmMfZfmpYCndYLITl0\",\"width\":2400,\"height\":1600},{\"html_attributions\":[\"From a Google User\"],\"photo_reference\":\"CnRvAAAAft6Pbz2VBKf4fFQ12gRuGbcA29l6D5RiKEaqBpYKje-RQTBrZFswJ97g4P4CUhS4w0MmfsdKmUsOLEV5ARMBR7ss1kcuEze0nN_MRz0qujpj-32CudnEKQvhvOt3eMAPg456YFw8mmjGf-kC2xJT4RIQATi89HYnbKMlrz6NkaK44xoUXi89ET95aEWWC9TNEKBSnJEcyok\",\"width\":960,\"height\":540}],\"id\":\"041b9813dfa9673b6260b89ed5b1233c33fcd823\",\"vicinity\":\"1249 Great Mall Drive, Milpitas\",\"address_components\":[{\"types\":[\"street_number\"],\"short_name\":\"1249\",\"long_name\":\"1249\"},{\"types\":[\"route\"],\"short_name\":\"Great Mall Dr\",\"long_name\":\"Great Mall Drive\"},{\"types\":[\"locality\",\"political\"],\"short_name\":\"Milpitas\",\"long_name\":\"Milpitas\"},{\"types\":[\"administrative_area_level_1\",\"political\"],\"short_name\":\"CA\",\"long_name\":\"California\"},{\"types\":[\"country\",\"political\"],\"short_name\":\"US\",\"long_name\":\"United States\"},{\"types\":[\"postal_code\"],\"short_name\":\"95035\",\"long_name\":\"95035\"}],\"name\":\"McDonald's\",\"formatted_address\":\"1249 Great Mall Drive, Milpitas, CA 95035, United States\",\"formatted_phone_number\":\"(408) 935-8225\",\"types\":[\"restaurant\",\"food\",\"establishment\"]}}";
		String jsonData3 = "{\"html_attributions\":[],\"status\":\"OK\",\"result\":{\"place_id\":\"ChIJbUvJJbXOj4ARf1axJhmN-1c\",\"icon\":\"http:\\/\\/maps.gstatic.com\\/mapfiles\\/place_api\\/icons\\/restaurant-71.png\",\"reviews\":[{\"author_name\":\"Vishal Mistry\",\"aspects\":[{\"type\":\"overall\",\"rating\":3}],\"time\":1421547728,\"text\":\"Most dishes were prepared with good taste and quantity. Restaurant server were efficient and welcoming. Biryani was good, and chicken lollipop was amazing. We have been there many times. \",\"rating\":5,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/110924487513259425721\"},{\"author_name\":\"Maria Princilin Syril\",\"aspects\":[{\"type\":\"overall\",\"rating\":0}],\"time\":1427496760,\"text\":\"Except the location of this restaurant, nothing much attractive.\\nWorst management. you need to wait a long. Food is average. \",\"rating\":2,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/115258181748344842544\"},{\"author_name\":\"Abhijeet Bhalerao\",\"aspects\":[{\"type\":\"overall\",\"rating\":3}],\"time\":1423250944,\"text\":\"Love the biryani here. Its spicy so have it only if you can handle it. Never really crowded (for dinner at least) and is my go-to place on Fridays.\",\"rating\":5,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/100114709404056701635\"},{\"author_name\":\"Srivatsava Guduri\",\"aspects\":[{\"type\":\"overall\",\"rating\":2}],\"time\":1412480083,\"text\":\"Awesome vijayawada biryani and karuvepillai fish \\nBut the place is little bit noisy\",\"rating\":4,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/104495262863226030568\"},{\"author_name\":\"rajesh kumar\",\"aspects\":[{\"type\":\"overall\",\"rating\":0}],\"time\":1426998380,\"text\":\"Most worst and slow service. Food is not even hot. Tastes bad\",\"rating\":1,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/117101611891762104088\"}],\"scope\":\"GOOGLE\",\"website\":\"http:\\/\\/www.dosabawarchi.us\\/\",\"user_ratings_total\":42,\"international_phone_number\":\"+1 408-945-9000\",\"adr_address\":\"<span class=\\\"street-address\\\">1380 South Main Street<\\/span>, <span class=\\\"locality\\\">Milpitas<\\/span>, <span class=\\\"region\\\">CA<\\/span> <span class=\\\"postal-code\\\">95035<\\/span>, <span class=\\\"country-name\\\">United States<\\/span>\",\"url\":\"https:\\/\\/plus.google.com\\/116187669004710473251\\/about?hl=en-US\",\"reference\":\"CnRrAAAAQ5stzGC5w4U6bZYxiewe6wwNL3pCmXlkjhaZBeoxNDBP9sQbcvEW6XMTggiRBaGZliYnschyav0hEhhUbKd7rn2iRGnZGSGbvtQdi6oO5GLoKC3XlXRlkZkdLE-LkyY_v01keQObTRDgxOAZZ9Jq1xIQI7to2QeTqQsS8x2qsRz82RoUnzHyKfHCD4B5gE3-QGLDBi0bLkI\",\"opening_hours\":{\"periods\":[{\"open\":{\"time\":\"1130\",\"day\":0},\"close\":{\"time\":\"1430\",\"day\":0}},{\"open\":{\"time\":\"1800\",\"day\":0},\"close\":{\"time\":\"2130\",\"day\":0}},{\"open\":{\"time\":\"1130\",\"day\":1},\"close\":{\"time\":\"1430\",\"day\":1}},{\"open\":{\"time\":\"1800\",\"day\":1},\"close\":{\"time\":\"2200\",\"day\":1}},{\"open\":{\"time\":\"1130\",\"day\":2},\"close\":{\"time\":\"1430\",\"day\":2}},{\"open\":{\"time\":\"1800\",\"day\":2},\"close\":{\"time\":\"2200\",\"day\":2}},{\"open\":{\"time\":\"1130\",\"day\":3},\"close\":{\"time\":\"1430\",\"day\":3}},{\"open\":{\"time\":\"1800\",\"day\":3},\"close\":{\"time\":\"2200\",\"day\":3}},{\"open\":{\"time\":\"1130\",\"day\":4},\"close\":{\"time\":\"1430\",\"day\":4}},{\"open\":{\"time\":\"1800\",\"day\":4},\"close\":{\"time\":\"2200\",\"day\":4}},{\"open\":{\"time\":\"1130\",\"day\":5},\"close\":{\"time\":\"1430\",\"day\":5}},{\"open\":{\"time\":\"1800\",\"day\":5},\"close\":{\"time\":\"2230\",\"day\":5}},{\"open\":{\"time\":\"1130\",\"day\":6},\"close\":{\"time\":\"1430\",\"day\":6}},{\"open\":{\"time\":\"1800\",\"day\":6},\"close\":{\"time\":\"2230\",\"day\":6}}],\"open_now\":false,\"weekday_text\":[\"Monday: 11:30 am – 2:30 pm, 6:00 – 10:00 pm\",\"Tuesday: 11:30 am – 2:30 pm, 6:00 – 10:00 pm\",\"Wednesday: 11:30 am – 2:30 pm, 6:00 – 10:00 pm\",\"Thursday: 11:30 am – 2:30 pm, 6:00 – 10:00 pm\",\"Friday: 11:30 am – 2:30 pm, 6:00 – 10:30 pm\",\"Saturday: 11:30 am – 2:30 pm, 6:00 – 10:30 pm\",\"Sunday: 11:30 am – 2:30 pm, 6:00 – 9:30 pm\"]},\"geometry\":{\"location\":{\"lng\":-121.901719,\"lat\":37.411302}},\"utc_offset\":-420,\"price_level\":2,\"photos\":[{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAuTR8K1Xu68E5kTirB9Fs9yFpX1COHyiagNfiml_q8fktTupIuVHNQgHgvRLQqvRAFHuGiKdIJ0FUP6Ndc_VQKnGlzOM-d3HPQqJiOXyCilC_l3QSVr1kshXiiRmy66FQtCOap9pPYvtKqs-iPnGnjxIQPUpdZKOHviumuGls1Ap9aRoUx29S8GJdX7xifjB8Y2NJriwgjSQ\",\"width\":2048,\"height\":1463},{\"html_attributions\":[\"<a href=\\\"https:\\/\\/plus.google.com\\/118268451358807750636\\\">Shyam Bansal<\\/a>\"],\"photo_reference\":\"CnRoAAAA-Z70RLvNhxp0iqmiuGq7WyUddPM_CV7xZvEtSzkIBpMOKhQTSAhiRhcBxDLAz8mdcmkjcGaYfgTQrH4yJ7SrOzDidR75sb0tpANK59dbJTcjTwEJmAVRgLLqsz-zxLmv5HwlUgiVqPEpfeerbjjpwBIQ0wmDUAFrckFI61eb-EbJ8BoUagUIC7BKHff22u2YXnc0TIOjALU\",\"width\":1224,\"height\":1632},{\"html_attributions\":[\"<a href=\\\"https:\\/\\/plus.google.com\\/118268451358807750636\\\">Shyam Bansal<\\/a>\"],\"photo_reference\":\"CnRtAAAAlR-MuYOx0DBT9_OJ5LkazlR7HHs3UkIiqsADoX6QeEbqJla2nG1JVS1nOTuuxR1NobPH23m4Epk5tY7K4OpdnSkeSxU-VfslM3laxBhkzawoUKsi8k7IKl7CcKv52t-ZNFD8oTHYFK7S2U01qjM77BIQZS58sbcN85Ve5pHrOQ_zVhoU3DmIg9rvcsqdMQ2MsVw6brH5lSY\",\"width\":816,\"height\":612},{\"html_attributions\":[\"<a href=\\\"https:\\/\\/plus.google.com\\/108759428171436394179\\\">Carlos Sanchez Lopez<\\/a>\"],\"photo_reference\":\"CnRtAAAAe-3kDyFIcWy6NaCrBEyPd1fPP7FL6grXW4biHwzH6-30q1BZPEsWYeJl-suLg0GrqDm4ApXMFLhEubl0i96KE2KmJnB3P155Yy5Aa6yVFuUisO8OSCLlBDGrcTEvfuekd9K6-gihaao43JJuppzfCxIQKMHes-muoLo8S5y6tdzFSBoURp0k3_R7IgK6RSC0LXl1AtG6jqg\",\"width\":1296,\"height\":972},{\"html_attributions\":[\"<a href=\\\"https:\\/\\/plus.google.com\\/118268451358807750636\\\">Shyam Bansal<\\/a>\"],\"photo_reference\":\"CnRtAAAAEHbbMBkdjkbRpNpJQUFiBckGEq5PyRXb9qIhu5jJKjxMCyVfDYwSzXUbeM2be-a5LE-yePiU8RqLw55Q99WVwmXhVbzKosddQ1D2l-gEkwVYQP7bMoqagFwK62jbsEc2pEv7oniWGpgQmmR_HwRyExIQ71a16-QCW4w5twhFaNpjUxoU5vZDWQPIsjjBhryOsuaQNGngHsU\",\"width\":816,\"height\":612}],\"id\":\"e461c257a7c8e1a43b9cc250edf49b12f97a6daf\",\"vicinity\":\"1380 South Main Street, Milpitas\",\"address_components\":[{\"types\":[\"street_number\"],\"short_name\":\"1380\",\"long_name\":\"1380\"},{\"types\":[\"route\"],\"short_name\":\"S Main St\",\"long_name\":\"South Main Street\"},{\"types\":[\"locality\",\"political\"],\"short_name\":\"Milpitas\",\"long_name\":\"Milpitas\"},{\"types\":[\"administrative_area_level_1\",\"political\"],\"short_name\":\"CA\",\"long_name\":\"California\"},{\"types\":[\"country\",\"political\"],\"short_name\":\"US\",\"long_name\":\"United States\"},{\"types\":[\"postal_code\"],\"short_name\":\"95035\",\"long_name\":\"95035\"}],\"name\":\"Dosa Bawarchi Restaurant\",\"formatted_address\":\"1380 South Main Street, Milpitas, CA 95035, United States\",\"formatted_phone_number\":\"(408) 945-9000\",\"rating\":3.5,\"types\":[\"restaurant\",\"food\",\"establishment\"]}}";
		String jsonData4 = "{\"html_attributions\":[],\"status\":\"OK\",\"result\":{\"place_id\":\"ChIJxTtMZDXJj4ARCgnf_1hmV6I\",\"icon\":\"http:\\/\\/maps.gstatic.com\\/mapfiles\\/place_api\\/icons\\/restaurant-71.png\",\"reviews\":[{\"author_name\":\"Kishore Telidevara\",\"aspects\":[{\"type\":\"overall\",\"rating\":0}],\"time\":1426908794,\"text\":\"They used to serve good food earlier, now the quality is bad. over cooked food, maybe stale food cooked again, or frozen food reheated\",\"rating\":1,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/103564136276289816778\"},{\"author_name\":\"Shyam Bansal\",\"aspects\":[{\"type\":\"food\",\"rating\":3},{\"type\":\"decor\",\"rating\":1},{\"type\":\"service\",\"rating\":1}],\"time\":1393303442,\"text\":\"Lately the North Indian thali quality has deteriorated so will think twice about going to this place.  Ask for more sambar, dal etc as needed complimentarily, since the quantity in the thali is limited. \",\"rating\":4,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/118268451358807750636\"},{\"author_name\":\"Rangaprabhu Parthasarathy\",\"aspects\":[{\"type\":\"overall\",\"rating\":1}],\"time\":1376414231,\"text\":\"After a shopping stint at the Great Mall in Milpitas, our family and that of a friend of ours wanted to get an Indian dinner. My friend suggested Tirupathi Bhimas- a place I have never been to but one that my wife had visited and had a decent opinion about. I agreed to give the place a shot- if not anything, atleast to get a review in (such if the power of writing my own blog :) )\\n\\nWe arrived pretty late on a Saturday night. The place was reasonably crowded but not really full. The waiter having deciphered that we all shared a common language, decided to pitch in with recommendations so forceful it was almost as if he knew wanted us to eat. It was almost comical seeing him push what he thought we should be eating.\\n\\nWe had two tired and hungry kids. So we decided to order a simple curd rice for one of them while the other was going to share his mother’s South Indian thali. For the rest of the group, we ordered an adai avial (adai resembles a dosa but is much thicker because of the lentil batter it uses), szechuan fried rice and another plate of curd rice. The curd rice arrived quickly and we realised almost immediately that it was spicy. Yes, you are allowed to ask- how and why in the world is curd rice spicy. Beats me. The pattern was starting to show. My wife who ordered the thali complained that everything was a tad on the spicy side. She had to use the kesari in her thali to give my son his meal from hers. My szechuan fried rice was spicy beyond expectations. To be fair, I was warned that it was a spicy dish. Just that I did not anticipate the level of spice it carried. The kitchen was closing soon. I had to order a cup of the rava kesari just to quench the spice in my mouth. All in all, the spice was a tad too much for all of us to handle.\",\"rating\":3,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/113224422168461556238\"},{\"author_name\":\"Anand Kumar\",\"aspects\":[{\"type\":\"food\",\"rating\":1},{\"type\":\"decor\",\"rating\":1},{\"type\":\"service\",\"rating\":1}],\"time\":1368929704,\"text\":\"I go to this place whenever I crave for simple south Indian food. The food is not greasy at all and the sauces that go with dosa and chutney are good as of late. Dosa is a dish that has to be always cooked to order and so could never wrong when you order this dish. \",\"rating\":3,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/114774806354613814502\"},{\"author_name\":\"ash b\",\"aspects\":[{\"type\":\"food\",\"rating\":0},{\"type\":\"decor\",\"rating\":0},{\"type\":\"service\",\"rating\":0}],\"time\":1355700597,\"text\":\"I'm not going to this place again. I ordered mini tiffin which consisted of one small idli (was surprised by the size of it, it was really mini ), one vada ( medium size ,which was good with sambar), one uncooked paper thin dosa with a tablespoon of potato masala inside it, pongal had too much hing in it so i couldn't eat , tomato chutney was basically ketchup and the  sweet ponga,I'm pretty sure had gone bad.  \\nThis place is pricier than saravan bhanavan and  it lacks both in quality and quantity of food served.\",\"rating\":2,\"language\":\"en\",\"author_url\":\"https:\\/\\/plus.google.com\\/113445692627746698447\"}],\"scope\":\"GOOGLE\",\"website\":\"http:\\/\\/www.tirupathibhimas.us\\/contact-us.html\",\"user_ratings_total\":40,\"international_phone_number\":\"+1 408-945-1010\",\"adr_address\":\"<span class=\\\"street-address\\\">1208 South Abel Street<\\/span>, <span class=\\\"locality\\\">Milpitas<\\/span>, <span class=\\\"region\\\">CA<\\/span> <span class=\\\"postal-code\\\">95035<\\/span>, <span class=\\\"country-name\\\">United States<\\/span>\",\"url\":\"https:\\/\\/plus.google.com\\/111562485236971638551\\/about?hl=en-US\",\"reference\":\"CnRkAAAANRN2CqKGNnilqGc3GXDVslFCfGMPC6RD8b8Ya0dszcYR2TQpoNRkQmNgBn9ahhUcJEyyg89hqwyk-rSW_5xT5Avq_lkABWrUh4o70-4KB6h0KIYuCuVdmSBqOQlU9I8sU1pOKy7ztQaxvibb4DWlDxIQhzqyDl9aRj_nlLiFheamHRoU54ShPCg-5NvntLIxSj_GK8vQoyQ\",\"opening_hours\":{\"periods\":[{\"open\":{\"time\":\"1130\",\"day\":0},\"close\":{\"time\":\"1430\",\"day\":0}},{\"open\":{\"time\":\"1800\",\"day\":0},\"close\":{\"time\":\"2130\",\"day\":0}},{\"open\":{\"time\":\"1130\",\"day\":2},\"close\":{\"time\":\"1400\",\"day\":2}},{\"open\":{\"time\":\"1800\",\"day\":2},\"close\":{\"time\":\"2130\",\"day\":2}},{\"open\":{\"time\":\"1130\",\"day\":3},\"close\":{\"time\":\"1400\",\"day\":3}},{\"open\":{\"time\":\"1800\",\"day\":3},\"close\":{\"time\":\"2130\",\"day\":3}},{\"open\":{\"time\":\"1130\",\"day\":4},\"close\":{\"time\":\"1400\",\"day\":4}},{\"open\":{\"time\":\"1800\",\"day\":4},\"close\":{\"time\":\"2130\",\"day\":4}},{\"open\":{\"time\":\"1130\",\"day\":5},\"close\":{\"time\":\"1430\",\"day\":5}},{\"open\":{\"time\":\"1800\",\"day\":5},\"close\":{\"time\":\"2200\",\"day\":5}},{\"open\":{\"time\":\"1130\",\"day\":6},\"close\":{\"time\":\"1430\",\"day\":6}},{\"open\":{\"time\":\"1800\",\"day\":6},\"close\":{\"time\":\"2200\",\"day\":6}}],\"open_now\":false,\"weekday_text\":[\"Monday: Closed\",\"Tuesday: 11:30 am – 2:00 pm, 6:00 – 9:30 pm\",\"Wednesday: 11:30 am – 2:00 pm, 6:00 – 9:30 pm\",\"Thursday: 11:30 am – 2:00 pm, 6:00 – 9:30 pm\",\"Friday: 11:30 am – 2:30 pm, 6:00 – 10:00 pm\",\"Saturday: 11:30 am – 2:30 pm, 6:00 – 10:00 pm\",\"Sunday: 11:30 am – 2:30 pm, 6:00 – 9:30 pm\"]},\"geometry\":{\"location\":{\"lng\":-121.90338,\"lat\":37.412874}},\"utc_offset\":-420,\"price_level\":1,\"photos\":[{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAw4CfeoNsjLcl3iuXkkHjYJYAT8JhLxDf_rnsP9KrycfRfZ-kOJGYOUQI9ITKhzDefQn7tnlF5Y9aZn7Y3Caoina8aYjck4vpLEK-r6-IxT_o3Bo2InC5UWDoPwmhnsPq1txonZbVI0rfL_5sdoM4sRIQuVaXx8T96TJqhvag3RFXPBoUCGETCWmuiy9aAE5obfGNZgR3y2Y\",\"width\":1280,\"height\":853},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAZGEZp_amubVLErwFqr8vMEbKSv9YfDFbnS6Cq2IZiYtBfMFNlpqthfAxPorD8RQjsIh_EMhXVq47TvXaKFqcUptBwK075m5fJYxLy2TRIIc5K5eTZ-15siLmyx4ZoXm_ctztcjyCykBlwh8D7cwZkBIQ5Q0JOfeQQ5U7GthGHeEtdhoUjJlOQzdAcURvneGI_o_6QprHslA\",\"width\":1280,\"height\":853},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAw7zApJ2M3ziFlgJtJo1UEzHqkhkVigX8iIAkxoNCMjR3w9bVQblqkOpSnt-yNoIMr-CuY-mKsSA1qUdkxlOB2TVauV8dcmybZdckelSqGOJFp9-u-WKtHC4V93zcJX3jXugHV4RCyoecxjsuhtAAxxIQR8IChaHeByKJrFFJHeI1kBoU4WyPp91gBn8Hl8pqEm5BrXtfPhk\",\"width\":1280,\"height\":853},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAA8U1D-c6K5_HegDit0p6N_wdbPph3ee6PPsfbZkh_1aCDVh-XAHGYE9R9uqeWOzICjP3jy5yLN0rmCFDuffJmj2xQpoEXLHJqhQB2gzGkFgA00tR6tLXroJ1naVPVe-JZXTraWnjqW9OU7-5zngkruRIQULozgIIGgorOOd_drN6ZdRoU2v_FCze_XTmQYAVJIMR1znMRNGs\",\"width\":1280,\"height\":853},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAvIGqcyfKKgnOqMb_FZS-GufRy-kVmLqZPjqBeMrAagbqA5umz5bzYmn_Y8cyoBIcJPuInBW-j57GjpT3xiFbjujq3XTbcTyi4OprDJ3OghYkN-UvamU4PevWvrLaGA0uzf80wmSf3nGP8GlsiznrWhIQoAb8JVokyS7O7ZJ43_lK1RoUHxb0grSO_Fhmwm9HJUKX-pt7j5o\",\"width\":1280,\"height\":853},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAGTBov9a9eg7vRYe2rpJ2baU7F98C75xK0zDqpdTK-K1yy7o8_rT3cBRIMd59ilJydOESH1D-baFyi3jhY8AH9fcfoHTNNDRbTNq36raVe2lXKqBVPKAmyRF23qpOwXEoOpqoGqWT3mvm1PvZqRzw3RIQAsciVzpHU72UO41KsXEv0xoUDwywFPPMtPBmYOiumK5X7sCjXOI\",\"width\":853,\"height\":1280},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAAu07RiNh_i9YcOU-LWNBwEPRlFydycz0xFKB9SU8G1h0O7NZt4bN71l457L48OJkS23wFDIkHEiYh0UdHjTUFtM1nBxYMmIhNT7r_bIBTx7e_LFO684dGYfUeOQlgnT91SkUdSVqtqOybJrxZbS24QBIQQj4PbTzbVWczhuW53Z6d_hoUkN4cZW1NpynhvfU7PdgqwBt9Uuo\",\"width\":853,\"height\":1280},{\"html_attributions\":[],\"photo_reference\":\"CnRnAAAA85AHoh6p37Ee6ExGRU-yGCvAAAtFIYHnjdPU4IqMdIYgZeN4FkaTjiScou0gGZTRJQGcBGZqdYjFWfQEO2Ph5M6a2elX20-kBfLTAR2SCFsxOAg_wGe0nw2LKOult-TqjNP6BSE9WsUIhvsnHB5lCRIQcJ0ih1zgitXSfk7rXRWMwxoUbMWzNuZmg-5LP5SLXJrZo2OeynA\",\"width\":1280,\"height\":853},{\"html_attributions\":[\"<a href=\\\"https:\\/\\/plus.google.com\\/111502100155913804168\\\">ManojE Pandalam<\\/a>\"],\"photo_reference\":\"CoQBfAAAADnnFWW0ohNKkbd9XPKXtokpD0Eo6usinPMZ-0gjZ2b-am-VgwNmHAmmA8v1HvhNQScbIklMIw_1MJrkEZdmEjUtxxP-PLzkVGxiwHC2tkliQ6Pf6FehrOLvBUsQbEnR3GvLL_e6uFH3PVubM1Bd6mzSe5fU0O9ZogLn06n_Rrs4EhA0qXY3m7AWqx7H4Sx-FdDvGhTYivc9fGPhHGd1KRVT4wk7GCiw5Q\",\"width\":484,\"height\":578},{\"html_attributions\":[\"<a href=\\\"https:\\/\\/plus.google.com\\/111502100155913804168\\\">ManojE Pandalam<\\/a>\"],\"photo_reference\":\"CoQBcgAAANyJQdBFSmbPFap3UOxFki1HEIG5PbfERQgv1UKUDUWPDQf93yn2qF_NkbOWophIcHkIU0DtsL4ks6PMZqwHe4PPvxg2rFmhZ7gKnEDD8QvqhcHw5XLOV6m4uwGLLuZcSg5THixNFiq93Ndhd0PpAyWCA48T3kIV-BLLeuf4mO3pEhARavMxGBNutOGMRY6cClIoGhQ9fipMGuQSZSI6DgsonMGYiCbc4g\",\"width\":645,\"height\":411}],\"id\":\"6c39505bfcc96814a1406be727f56c6869c150c2\",\"vicinity\":\"1208 South Abel Street, Milpitas\",\"address_components\":[{\"types\":[\"street_number\"],\"short_name\":\"1208\",\"long_name\":\"1208\"},{\"types\":[\"route\"],\"short_name\":\"S Abel St\",\"long_name\":\"South Abel Street\"},{\"types\":[\"locality\",\"political\"],\"short_name\":\"Milpitas\",\"long_name\":\"Milpitas\"},{\"types\":[\"administrative_area_level_1\",\"political\"],\"short_name\":\"CA\",\"long_name\":\"California\"},{\"types\":[\"country\",\"political\"],\"short_name\":\"US\",\"long_name\":\"United States\"},{\"types\":[\"postal_code\"],\"short_name\":\"95035\",\"long_name\":\"95035\"}],\"name\":\"Tirupathi Bhimas\",\"formatted_address\":\"1208 South Abel Street, Milpitas, CA 95035, United States\",\"formatted_phone_number\":\"(408) 945-1010\",\"rating\":3.1,\"types\":[\"restaurant\",\"food\",\"establishment\"]}}";

		ArrayList<String> listRestaurants = new ArrayList<>();
		listRestaurants.add(jsonData4);
		listRestaurants.add(jsonData3);
		listRestaurants.add(jsonData2);
		listRestaurants.add(jsonData1);

		aRestaurants.clear();

		for (int i = 0; i < listRestaurants.size(); i++) {
			counter++;

			try {
				final JSONObject gprJson = new JSONObject(listRestaurants.get(i));
				final JSONObject result = gprJson.getJSONObject(GPlacesResponse.Fields.RESULT);
				final GPlacesResponse gpr = new GPlacesResponse(result);
				final Restaurant restaurant = new Restaurant(gpr);
				aRestaurants.add(restaurant);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			aRestaurants.notifyDataSetChanged();
		}

		Log.i(TAG, "loadDummy: " + listRestaurants.size() + "restaurants (counter=)" + counter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSearch:
				loadRestaurantList();
				break;
		}
	}

	public interface RestaurantPickListener {
		public void restaurantSelected(Restaurant restaurant);
		public void clearAllMarkers();
	}
}