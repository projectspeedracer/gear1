package com.projectspeedracer.thefoodapp.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RestaurantsArrayAdapter extends ArrayAdapter<Restaurant> {
	public static String googlePlacesPhotoUriBase = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=250&key="
	                                                + TheFoodApplication.getGoogleApiKey()
	                                                + "&photoreference=";

	public RestaurantsArrayAdapter(Context context, List<Restaurant> objects) {
		super(context, R.layout.item_restaurant, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tvName;
		TextView tvAddress;
		TextView tvDistance;
		ImageView ivBanner;
		TextView tvRestaurantRating;

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_restaurant, parent, false);
		}
		Restaurant restaurant = (Restaurant) getItem(position);

		// >>>>>>>>> Photo: Eg. https://maps.googleapis.com/maps/api/place/photo?maxwidth=1200&key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&photoreference=CnRnAAAArvbAnbfXda4dQ6DHkj83Uc79gJ0ASBSjQJgGsAbh5v8Brj9tRDtbvlaFH98pu4-XxdWAdQFLTYQKHLsiqaR4lOzOKgV9DdmUU0eaTcpOfx03KUToDy-TIRVqHRgfx_Q5BoqXU55UY0ORa7QBUcsdShIQMmQGFyomw-FJ0K-bOM6ljhoU-RDgNMD-NA84LbdEgkE5Zw_f0fE
		ivBanner = (ImageView) convertView.findViewById(R.id.ivBanner);
		String photoUrl = googlePlacesPhotoUriBase + restaurant.getPhotoId();
		Picasso.with(getContext())
				.load(photoUrl)
				.into(ivBanner);

		tvName = (TextView) convertView.findViewById(R.id.tvName);
		tvName.setText(restaurant.getName());

		tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
		tvAddress.setText(restaurant.getAddress());

		tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);

		final Location location = Helpers.ToLocation(restaurant.getLocation());

		if (location != null) {
			Float distance = PickRestaurantActivity.getCurrentLocation().distanceTo(location);
			String distanceShort = FoodAppUtils.getShortDistance(distance);
			tvDistance.setText(distanceShort + " mi");
		}

        /*tvRestaurantRating = (TextView) convertView.findViewById(R.id.tvRestaurantRating);
        if (restaurant.getRating() != null) {
            tvRestaurantRating.setText(String.valueOf(restaurant.getRating()));
        } else {
            tvRestaurantRating.setText("");
        }*/

		return convertView;
	}
}
