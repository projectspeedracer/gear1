package com.projectspeedracer.thefoodapp.models;

import android.location.Location;
import android.util.Log;

import com.android.internal.util.Predicate;
import com.projectspeedracer.thefoodapp.utils.Helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GPlacesResponse implements IRestaurantInfoProvider {
	private String placesId;
	private String name;
    private String address; // formatted_address
	private String phone;
	private String iconUrl;
	private String websiteUrl;
	private List<String> photoIds = new ArrayList<>();
	private Location     location = new Location("");

	// NOTE: Root of the json object should be result
	public GPlacesResponse(JSONObject result) throws JSONException {
		Helpers.EnsureNotNull(result, "Expected non-null places response");

		this.placesId   = ExtractPlaceId(result);
		this.name       = ExtractRestaurantName(result);
        this.address    = ExtractRestaurantAddress(result);
		this.location   = ExtractLocation(result);
		this.phone      = ExtractPhoneNumber(result);
		this.iconUrl    = ExtractIconUrl(result);
		this.websiteUrl = ExtractWebsiteUrl(result);

		final List<String> pidList = ExtractRestaurantImages(result, null);
        if (pidList != null) {
            photoIds.addAll(pidList);
        }
	}

	public static String ExtractWebsiteUrl(JSONObject result) throws JSONException {
        if (result.has(Fields.WEBSITE)) {
            return result.getString(Fields.WEBSITE);
        }
        else {
            return "";
        }
	}

	public static String ExtractPhoneNumber(JSONObject result) throws JSONException {
        if (result.has(Fields.PHONE)) {
            return result.getString(Fields.PHONE);
        }
        else {
            return "";
        }
	}

	public static String ExtractRestaurantName(JSONObject result) throws JSONException {
		return result.getString(Fields.NAME);
	}

    public static String ExtractRestaurantAddress(JSONObject result) throws JSONException {
        if (result.has(Fields.ADDRESS)) {
            return result.getString(Fields.ADDRESS);
        }
        else if (result.has(Fields.VICINITY)) {
            return result.getString(Fields.VICINITY);
        }
        else {
            return "";
        }
    }

	public static String ExtractPlaceId(JSONObject result) throws JSONException {
		return result.getString(Fields.PLACE_ID);
	}

	public static String ExtractIconUrl(JSONObject result) throws JSONException {
		return result.getString(Fields.ICON);
	}

	public static String FindOptimalRestaurantImageId(JSONObject json) throws JSONException {

		final List<String> photoIds = ExtractRestaurantImages(json, new Predicate<JSONObject>() {
			@Override
			public boolean apply(JSONObject photo) {
				try {
					final int height = Integer.valueOf(photo.getString(Fields.HEIGHT));
					final int width = Integer.valueOf(photo.getString(Fields.WIDTH));
					return width > height;
				} catch (JSONException ex) {
					Log.e("FoodApp", ex.getMessage());
					ex.printStackTrace();
				}

				return false;
			}
		});

		return photoIds == null || photoIds.size() == 0 ? null : photoIds.get(0);
	}

	public static List<String> ExtractRestaurantImages(JSONObject json, Predicate<JSONObject> predicate) throws JSONException {
        if (!json.has(Fields.PHOTOS)) {
            return null;
        }
        final JSONArray photos = json.getJSONArray(Fields.PHOTOS);
		final int numPhotos = photos.length();

		if (numPhotos == 0) {
			return null;
		}

		List<String> filteredPhotos = new ArrayList<>();

		for (int index = 0; index < numPhotos; ++index) {
			final JSONObject photo = photos.getJSONObject(index);
			final String photoReference = photo.getString(Fields.PHOTO_REFERENCE);

			final boolean include = predicate == null || predicate.apply(photo);

			if (include) {
				filteredPhotos.add(photoReference);
			}
		}

		return filteredPhotos;
	}

	public static Location ExtractLocation(JSONObject json) throws JSONException {
		final JSONObject jsonLocation = json.getJSONObject(Fields.GEOMETRY).getJSONObject(Fields.LOCATION);

		final double lat = jsonLocation.getDouble(Fields.LATITUDE);
		final double lng = jsonLocation.getDouble(Fields.LONGITUDE);

		final Location location = new Location("");
		location.setLatitude(lat);
		location.setLongitude(lng);

		return location;
	}

	// region Getters and Setters

	public String getPlacesId() {
		return placesId;
	}

	public String getName() {
		return name;
	}

	public List<String> getPhotoIds() {
		return photoIds;
	}

	public Location getLocation() {
		return location;
	}

	public String getPhone() {
		return phone;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

    public String getAddress() {
        return address;
    }

    // endregion

	public static class Fields {
		public static final String RESULT          = "result";
		//public static       String ID              = "id";
		public static final String PLACE_ID        = "place_id";
		public static final String PHOTOS          = "photos";
		public static       String PHONE           = "international_phone_number";
		public static final String NAME            = "name";
		public static final String ICON            = "icon";
		public static final String WEBSITE         = "website";
		final static        String HEIGHT          = "height";
		final static        String WIDTH           = "width";
		public static final String PHOTO_REFERENCE = "photo_reference";
		final static        String GEOMETRY        = "geometry";
		final static        String LOCATION        = "location";
		final static        String LATITUDE        = "lat";
		final static        String LONGITUDE       = "lng";
        final static        String ADDRESS         = "formatted_address";
        final static        String VICINITY        = "vicinity";
	}
}
