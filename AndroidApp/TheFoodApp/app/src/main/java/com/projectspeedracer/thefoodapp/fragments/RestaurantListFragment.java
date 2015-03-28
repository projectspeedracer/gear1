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

import com.google.android.gms.maps.model.Marker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.adapters.RestaurantsArrayAdapter;
import com.projectspeedracer.thefoodapp.models.Restaurant;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by avkadam on 3/27/15.
 */
public class RestaurantListFragment extends Fragment implements View.OnClickListener {

    private EditText etSearch;
    private ListView lvResults;
    private ArrayList<Restaurant> listRestaurants;
    private RestaurantsArrayAdapter aRestaurants;

    private final String TAG = "RestaurantList";

    // Fields for the map radius in feet
    private float radius;



    RestarantPickListener listener;

    public interface RestarantPickListener {
        public void show_restaurant_on_map(Location location, Restaurant restaurant);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.fragment_list_restaurant, container, false);

        etSearch = (EditText) view.findViewById(R.id.etSearch);
        lvResults = (ListView) view.findViewById(R.id.lvResults);

        listener = (RestarantPickListener) getActivity();

        listRestaurants = new ArrayList();
        aRestaurants = new RestaurantsArrayAdapter(getActivity(), listRestaurants);
        lvResults.setAdapter(aRestaurants);
        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Restaurant restaurant = (Restaurant) aRestaurants.getItem(position);
                Toast.makeText(getActivity(), "Picked - "+restaurant.getName(), Toast.LENGTH_SHORT).show();
                listener.show_restaurant_on_map(restaurant.getLocation(), restaurant);
            }
        });

        Button b = (Button) view.findViewById(R.id.btnSearch);
        b.setOnClickListener(this);

        radius = TheFoodApplication.getSearchDistance();

        return view;
    }

    public void onSearchClick(View v){
        String searchQ = etSearch.getText().toString();
        if (searchQ.isEmpty()) {
            searchQ = "restaurants";
        }

        Location location = PickRestaurantActivity.getCurrentLocation();
        String currLongitude = Double.toString(location.getLongitude());
        String currLatitue = Double.toString(location.getLatitude());
        String locationQ = currLatitue+","+currLongitude;
        Toast.makeText(getActivity(), "Searching " + searchQ + " near: " + locationQ, Toast.LENGTH_SHORT).show();

//          String places_search_q="https://maps.googleapis.com/maps/api/place/search/json?location="+locationQ+"&sensor=true&key="+GOOGLE_API_KEY+
//                "&keyword="+searchQ+"&types=food&rankby=distance"; // WORKS
//
        String places_search_q="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+locationQ+
                "&key="+ TheFoodApplication.getGoogleApiKey()+
                "&keyword="+searchQ+"&rankby=distance"; // WORKS

        Log.e("GAPI", "Getting restaurant details - "+places_search_q);

        clearMarkers();
        doSearch(places_search_q);
    }

    private void clearMarkers() {
        // Clear markers if present.
        if (aRestaurants == null) {
            return;
        }

        for (int i = 0; i < aRestaurants.getCount(); i++) {
            Restaurant restaurant = (Restaurant) aRestaurants.getItem(i);
            Marker m = restaurant.getMarker();
            if (m != null){
                m.remove();
            }
        }
    }

    private void doSearch(String places_search_q) {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(getActivity(), places_search_q, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("RESP", response.toString());
                try {
                    if (response.getString("status").equals("OK")) {
                        handleSearchResp(response);
                    }
                    else {
                        Toast.makeText(getActivity(), "Error: "+response.getString("error_message"), Toast.LENGTH_SHORT).show();
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

    private void handleSearchResp(JSONObject response){
        try {
            aRestaurants.clear();
            aRestaurants.addAll(Restaurant.fromJSONArray(response.getJSONArray("results")));
            getPlaceDetails();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    static int counter=0;
    // Retrieve address from Place ID and update UI
    // Eg. https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&placeid=ChIJxTtMZDXJj4ARCgnf_1hmV6I
    // >>>>> bhimas - has 360 view
    // Eg. https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&placeid=ChIJbUvJJbXOj4ARf1axJhmN-1c
    // >>>>> dosa - has simple image
    // >>>>>>>>> Photo: Eg. https://maps.googleapis.com/maps/api/place/photo?maxwidth=1200&key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&photoreference=CnRnAAAArvbAnbfXda4dQ6DHkj83Uc79gJ0ASBSjQJgGsAbh5v8Brj9tRDtbvlaFH98pu4-XxdWAdQFLTYQKHLsiqaR4lOzOKgV9DdmUU0eaTcpOfx03KUToDy-TIRVqHRgfx_Q5BoqXU55UY0ORa7QBUcsdShIQMmQGFyomw-FJ0K-bOM6ljhoU-RDgNMD-NA84LbdEgkE5Zw_f0fE

    private ArrayList<Restaurant> getPlaceDetails() {
        ArrayList listRestaurants = new ArrayList();
        String detailsQ = "https://maps.googleapis.com/maps/api/place/details/json?key="+TheFoodApplication.getGoogleApiKey()+"&placeid=";

        for (int i = 0; i < aRestaurants.getCount(); i++) {
            counter++;
            Restaurant restaurant = (Restaurant) aRestaurants.getItem(i);
            String currPlaceId = restaurant.getPlaces_id();
            AsyncHttpClient client = new AsyncHttpClient();
            Log.e("GAPI", "Getting details of "+ restaurant.getName()+"- "+detailsQ+currPlaceId);
            client.get(getActivity(), detailsQ + currPlaceId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    for (int i = 0; i < aRestaurants.getCount(); i++) {
                        Restaurant.updateFromJSON(response, (Restaurant) aRestaurants.getItem(i));
                    }
                    aRestaurants.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(getActivity(), "Could not get details!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        //todo change to info
        Log.e(TAG, "Getting details of "+aRestaurants.getCount()+"restaurants (counter=)"+counter);
        return listRestaurants;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                onSearchClick(v);
                break;
        }
    }

}