package com.projectspeedracer.thefoodapp.utils;

import com.projectspeedracer.thefoodapp.TheFoodApplication;

public class PlayUtils {

	public static String PHOTOS_URL_BASE = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=250&key=";

	private String apiKey;

	public PlayUtils(String apiKey) {
		Helpers.EnsureNotBlank(apiKey, "Expected non-empty Google API Key");
		this.apiKey = apiKey;
	}

	public String getPlacesPhotoUriBase() {
		return PHOTOS_URL_BASE + apiKey + "&photoreference=";
	}
}
