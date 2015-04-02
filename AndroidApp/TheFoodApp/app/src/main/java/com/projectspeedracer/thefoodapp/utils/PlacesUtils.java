package com.projectspeedracer.thefoodapp.utils;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.models.GPlacesResponse;
import com.projectspeedracer.thefoodapp.models.Restaurant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlacesUtils {

	public static Location GetCurrentLocation(GoogleApiClient client) {
		assert client != null : "Expected non-null GoogleApiClient instance";
		return LocationServices.FusedLocationApi.getLastLocation(client);
	}

	public static boolean IsRestaurantInRange(Restaurant restaurant, GoogleApiClient client) {
		return FoodAppUtils.isInRange(GetCurrentLocation(client), Helpers.ToLocation(restaurant.getLocation()));
	}

	public static ArrayList<Restaurant> ToRestaurants(JSONArray results) {
		ArrayList<Restaurant> restaurants = new ArrayList<>();
		try {
			for (int i = 0; i < results.length(); i++) {
				final JSONObject result = results.getJSONObject(i);
				final GPlacesResponse gpr = new GPlacesResponse(result);
				final Restaurant restaurant = new Restaurant(gpr); // ToRestaurant(result);
				restaurants.add(restaurant);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return restaurants;
	}

	public static Restaurant ToRestaurant(JSONObject json) {

		Restaurant restaurant = null;

		try {
			final GPlacesResponse gpr = new GPlacesResponse(json);

			// TODO: Parse and set other restaurant information

			restaurant = new Restaurant();
			restaurant.setPlacesId(gpr.getPlacesId());
			restaurant.setName(gpr.getName());
			restaurant.setLocation(gpr.getLocation());
			restaurant.setIconUrl(gpr.getIconUrl());
			//restaurant.setWebsiteUrl(gpr.getWebsiteUrl());

			final List<String> photoIds = gpr.getPhotoIds();
			if (photoIds.size() > 0) {
				restaurant.setPhotoId(photoIds.get(0));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return restaurant;
	}
}
