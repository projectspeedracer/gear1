package com.projectspeedracer.thefoodapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.fragments.AppDialogFragment;
import com.projectspeedracer.thefoodapp.models.GPlacesResponse;
import com.projectspeedracer.thefoodapp.models.Restaurant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlacesUtils {

	public static boolean IsRestaurantInRange(Restaurant restaurant, GoogleApiClient client) {
		return IsInRange(GetCurrentLocation(client), Helpers.ToLocation(restaurant.getLocation()));
	}

	public static Location GetCurrentLocation(GoogleApiClient client) {
		assert client != null : "Expected non-null GoogleApiClient instance";
		return LocationServices.FusedLocationApi.getLastLocation(client);
	}

	public static boolean IsInRange(Location myLocation, Location destination) {
		if (myLocation == null || destination == null) { return false; }

		final float distance = myLocation.distanceTo(destination); // in meters
		final float radius = TheFoodApplication.getSearchDistance();
		return (radius * Constants.METERS_PER_FEET) >= distance;
	}

	public static void SetMarkerStyle(Marker marker, String title, float colorHue) {
		final BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker(colorHue);
		SetMarkerStyle(marker, title, markerIcon, 1);
	}

	public static void SetMarkerStyle(Marker marker, String title, float colorHue, float alpha) {
		final BitmapDescriptor markerIcon = BitmapDescriptorFactory.defaultMarker(colorHue);
		SetMarkerStyle(marker, title, markerIcon, alpha);
	}

	public static void SetMarkerStyle(Marker marker, String title, BitmapDescriptor markerIcon, float alpha) {
		assert marker != null : "Expected non-null Marker object";

		marker.setIcon(markerIcon);
		marker.setAlpha(alpha);

		if (title != null) {
			marker.setTitle(title);
			marker.showInfoWindow();
		}
	}

	public static void HighlightRestaurantMarker(Marker marker, Restaurant restaurant) {
		final boolean inRange = PlacesUtils.IsRestaurantInRange(restaurant, PickRestaurantActivity.mGoogleApiClient);
		final float colorHue = inRange ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED;
		SetMarkerStyle(marker, restaurant.getName(), colorHue);
	}

	public static void LowlightRestaurantMarker(Marker marker) {
		SetMarkerStyle(marker, null, BitmapDescriptorFactory.HUE_BLUE, 0.5f);
	}

	/*public static ArrayList<Restaurant> ToRestaurants(JSONArray results) {
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
	}*/
}
