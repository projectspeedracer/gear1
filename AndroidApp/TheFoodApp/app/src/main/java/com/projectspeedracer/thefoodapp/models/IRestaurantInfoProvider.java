package com.projectspeedracer.thefoodapp.models;

import android.location.Location;

import java.util.List;

public interface IRestaurantInfoProvider {
	public String getPlacesId();
	public String getName();
	public List<String> getPhotoIds();
	public Location getLocation();
	public String getPhone();
	public String getIconUrl();
	public String getWebsiteUrl();
}
