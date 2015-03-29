package com.projectspeedracer.thefoodapp.models;

import android.location.Location;

import com.google.android.gms.maps.model.Marker;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by avkadam on 3/28/15.
 */
public class Restaurant {
    private String name;
    private String address; // formatted_address
    private String places_id;
    private String photo_id;
    private Double rating;

    private Marker marker; // to show a pin on map

    private Location location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPlaces_id() {
        return places_id;
    }

    public void setPlaces_id(String places_id) {
        this.places_id = places_id;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Double getRating() { return rating; }

    public void setRating(float Double) { this.rating = rating; }

    public static void updateFromJSON(JSONObject jsonObject, Restaurant restaurant) {
        try {
            if (jsonObject.getString("status").equals("OK")) {
                if (restaurant.places_id.equals(jsonObject.getJSONObject("result").getString("place_id"))) {
                    restaurant.address = jsonObject.getJSONObject("result").getString("formatted_address");
                    restaurant.rating = jsonObject.getJSONObject("result").getDouble("rating");

                    // Save location
                    JSONObject jsonLocation = jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
                    Double lat = jsonLocation.getDouble("lat");
                    Double lng = jsonLocation.getDouble("lng");
                    restaurant.location = new Location("");
                    restaurant.location.setLatitude(lat);
                    restaurant.location.setLongitude(lng);

                    // Choose a photo
                    //photos[x]->photo_reference
                    //photos[x]->height, width
                    JSONArray photos = jsonObject.getJSONObject("result").getJSONArray("photos");
                    for (int i = 0; i < photos.length(); i++) {
                        JSONObject photo = photos.getJSONObject(i);
                        restaurant.photo_id = photo.getString("photo_reference");
                        int height = new Integer(photo.getString("height").toString());
                        int width = new Integer(photo.getString("height").toString());
                        if (width > height) {
                            // preferred image, done choose
                            break;
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Restaurant fromJSON(JSONObject jsonObject) {
        Restaurant restaurant = new Restaurant();
        try {
            restaurant.name = jsonObject.getString("name");
            restaurant.places_id = jsonObject.getString("place_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurant;
    }

    public static Restaurant fromJSONLocal(JSONObject jsonObject) {
        Restaurant restaurant = new Restaurant();
        try {


            JSONObject resultJson = jsonObject.getJSONObject("result");
            restaurant.address = resultJson.getString("formatted_address");

            if (resultJson.has("rating")) {
                restaurant.rating = resultJson.getDouble("rating");
            }

            restaurant.places_id = resultJson.getString("place_id");
            restaurant.name = resultJson.getString("name");


            // Save location
            JSONObject jsonLocation = resultJson.getJSONObject("geometry").getJSONObject("location");
            Double lat = jsonLocation.getDouble("lat");
            Double lng = jsonLocation.getDouble("lng");
            restaurant.location = new Location("");
            restaurant.location.setLatitude(lat);
            restaurant.location.setLongitude(lng);

            // Choose a photo
            //photos[x]->photo_reference
            //photos[x]->height, width
            JSONArray photos = resultJson.getJSONArray("photos");
            for (int i = 0; i < photos.length(); i++) {
                JSONObject photo = photos.getJSONObject(i);
                restaurant.photo_id = photo.getString("photo_reference");
                int height = new Integer(photo.getString("height").toString());
                int width = new Integer(photo.getString("height").toString());
                if (width > height) {
                    // preferred image, done choose
                    break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return restaurant;
    }


    public static ArrayList<Restaurant> fromJSONArray(JSONArray jsonArray) {
        ArrayList listRestaurants = new ArrayList();
        int num_places_processed = 0;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                num_places_processed++;
                if (num_places_processed > TheFoodApplication.MAX_NUM_PLACES) {
                    // only interested in NUM_PLACES
                    break;
                }
                JSONObject restaurantJSON = jsonArray.getJSONObject(i);
                listRestaurants.add(fromJSON(restaurantJSON));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listRestaurants;
    }

    public Boolean isInMyRange() {
        return FoodAppUtils.isInRange(PickRestaurantActivity.getCurrentLocation(), this.getLocation());
    }


}