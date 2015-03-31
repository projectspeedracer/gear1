package com.projectspeedracer.thefoodapp.models;

import android.location.Location;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Restaurants")
public class Restaurant extends ParseObject {

	private List<Rating> ratings = new ArrayList<>();

	public Restaurant() {
	}

	// NOTE: json root should be result !!!
	public Restaurant(IRestaurantInfoProvider provider) {
		update(provider);
	}

	public void update(IRestaurantInfoProvider provider) {
		// TODO: Parse and set restaurant information

		setPlacesId(provider.getPlacesId());
		setName(provider.getName());
        setAddress(provider.getAddress());
		setLocation(provider.getLocation());
		setIconUrl(provider.getIconUrl());
		setWebsiteUrl(provider.getWebsiteUrl());

		final List<String> photoIds = provider.getPhotoIds();
		if (photoIds.size() > 0) {
			setPhotoId(photoIds.get(0));
		}
	}

	// region Getters and Setters

	public String getPlacesId() {
		return getString(Fields.PLACES_ID);
	}

	public void setPlacesId(String placesId) {
		Helpers.EnsureNotBlank(placesId, "Expected non-empty places id");
		put(Fields.PLACES_ID, placesId);
	}

    public String getName() {
        return getString(Fields.NAME);
    }

    public void setName(String restaurant_name) {
        put(Fields.NAME, restaurant_name);
    }

    public String getAddress() {
        return getString(Fields.ADDRESS);
    }

    public void setAddress(String restaurant_name) {
        put(Fields.ADDRESS, restaurant_name);
    }

    public String getEmail() {
		return getString(Fields.EMAIL);
	}

	public void setEmail(String email) {
		Helpers.EnsureNotBlank(email, "Invalid email address");
		put(Fields.EMAIL, email);
	}

	public String getContactPerson() {
		return getString(Fields.CONTACT_PERSON);
	}

	public void setContactPerson(String contactPerson) {
		put(Fields.CONTACT_PERSON, contactPerson == null ? "" : contactPerson);
	}

	/*public Address getAddress() {
		return (Address) getParseObject(Fields.ADDRESS);
	}

	public void setAddress(Address address) {
		put(Fields.ADDRESS, address);
	}*/

	public ParseGeoPoint getLocation() {
		return getParseGeoPoint(Fields.LOCATION_ID);
	}

	public void setLocation(ParseGeoPoint location) {
		put(Fields.LOCATION_ID, location);
	}

	public void setLocation(Location location) {
		setLocation(location.getLatitude(), location.getLongitude());
	}

	public void setLocation(double latitude, double longitude) {
		put(Fields.LOCATION_ID, new ParseGeoPoint(latitude, longitude));
	}

	public String getWebsiteUrl() {
		return getString(Fields.URL);
	}

	public void setWebsiteUrl(String url) {
		put(Fields.URL, url);
	}

	public String getPhone() {
		return getString(Fields.PHONE);
	}

	public void setPhone(String phone) {
		put(Fields.PHONE, phone);
	}

	public String getDescription() {
		return getString(Fields.DESCRIPTION);
	}

	public void setDescription(String brief_description) {
		put(Fields.DESCRIPTION, brief_description);
	}

	public String getBusinessType() {
		return getString(Fields.BUSINESS_TYPE);
	}

	public void setBusinessType(String businessType) {
		put(Fields.BUSINESS_TYPE, businessType);
	}

	public String getPhotoId(String photoId) {
		return getString(Fields.PHOTO_ID);
	}

	public void setPhotoId(String photoId) {
		put(Fields.PHOTO_ID, photoId);
	}

	public String getPhotoId() {
		return getString(Fields.PHOTO_ID);
	}

	public String getIconUrl(String iconUrl) {
		return getString(Fields.ICON_URL);
	}

	public void setIconUrl(String iconUrl) {
		put(Fields.ICON_URL, iconUrl);
	}

	// endregion

	// region Inner Classes

	public static class Fields {
		public static final String PLACES_ID         = "places_id";
		public static final String NAME              = "name";
		public static final String EMAIL             = "email";
		public static final String CONTACT_PERSON    = "contact_person";
		public static final String BUSINESS_TYPE     = "business_type";
		public static final String LOCATION_ID       = "location_id";
		public static final String URL               = "website_url";
		public static final String DESCRIPTION       = "description";
		public static final String PHONE             = "phone";
		public static final String ADDRESS           = "address";
		public static final String PHOTO_ID          = "photo_id";
		public static final String ICON_URL          = "icon_url";
		public static final String FORMATTED_ADDRESS = "formatted_address";
	}

	// endregion

    public Boolean isInMyRange() {
        return FoodAppUtils.isInRange(PickRestaurantActivity.getCurrentLocation(), Helpers.ToLocation(this.getLocation()));
    }
}