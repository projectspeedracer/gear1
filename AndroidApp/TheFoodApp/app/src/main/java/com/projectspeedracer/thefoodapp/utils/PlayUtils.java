package com.projectspeedracer.thefoodapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.fragments.AppDialogFragment;

public class PlayUtils {
	private static final String DIALOG_TAG = "Location Updates";

	public static String PHOTOS_URL_BASE = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=250&key=";

	private String apiKey;

	public PlayUtils(String apiKey) {
		Helpers.EnsureNotBlank(apiKey, "Expected non-empty Google API Key");
		this.apiKey = apiKey;
	}

	public String getPlacesPhotoUri(String imageName) {
		return PHOTOS_URL_BASE + apiKey + "&photoreference=" + imageName;
	}

	public String getSearchAreaUri(String location, String searchTerm) {
		return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
		       + location
		       + "&key=" + TheFoodApplication.getGoogleApiKey()
		       + "&keyword=" + searchTerm
		       + "&rankby=distance";
	}

	public static int isGooglePlayServicesAvailable(FragmentActivity activity) {

		final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());
		final boolean available = ConnectionResult.SUCCESS == resultCode;
		Log.i(Constants.TAG, String.format("Google Play Services is %s available", available ? "" : "*NOT*"));
		return resultCode;
	}

	public void showPlayServicesErrorDialog(int errorCode, FragmentActivity activity, int requestCode) {
		final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, activity, requestCode);

		if (errorDialog != null) {
			final AppDialogFragment edFragment = new AppDialogFragment();
			edFragment.setDialog(errorDialog);
			edFragment.show(activity.getSupportFragmentManager(), DIALOG_TAG);
		}
	}
}
