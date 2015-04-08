package com.projectspeedracer.thefoodapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParsePush;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.fragments.DishRatingsFragment;
import com.projectspeedracer.thefoodapp.fragments.RestaurantRatingsFragment;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.ProximityInspector;
import com.squareup.picasso.Picasso;

public class RestaurantActivity extends ActionBarActivity {
    public static String googlePlacesPhotoUriBase = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=250&key="
            + TheFoodApplication.getGoogleApiKey()
            + "&photoreference=";
    Restaurant restaurant;

    ProximityInspector proximityInspector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        initializeRatingsFragment();

        restaurant = TheFoodApplication.getCurrentRestaurant();

        //TODO: Subscribe to this restaurant's channel
        String channel = restaurant.getPlacesId();
        ParsePush.subscribeInBackground(channel);
        TheFoodApplication.subscribedChannel = channel;

        Log.e("[PUSH]", "subscribing to " + channel);
    }



    @Override
    protected void onResume() {
        super.onResume();
        restaurant = TheFoodApplication.getCurrentRestaurant();

        TextView tvName = (TextView) findViewById(R.id.tvName);
        String name = restaurant.getName();
        tvName.setText(name);

        TextView tvCuisine = (TextView) findViewById(R.id.tvCuisine);
        String cuisine = restaurant.getCuisine();
        tvCuisine.setText(cuisine);

        TextView tvDescription = (TextView) findViewById(R.id.tvDescription);
        String description = restaurant.getDescription();
        tvDescription.setText(description);

        ImageView ivBanner = (ImageView) findViewById(R.id.ivBanner);
        String photoUrl = googlePlacesPhotoUriBase + restaurant.getPhotoId();
        Picasso.with(this)
                .load(photoUrl)
                .into(ivBanner);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);


        proximityInspector = new ProximityInspector(this, this); // starts monitoring proximity
    }

    @Override
    protected void onStop() {
        if (proximityInspector != null) {
            proximityInspector.stop();
        }
        super.onStop();
    }

    private void initializeRatingsFragment() {
        RestaurantRatingsFragment restaurantRatingsFragment = RestaurantRatingsFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.flResturantPosts, restaurantRatingsFragment);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restaurant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.item_logout:
                FoodAppUtils.showSignOutDialog(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onOpenMenu(View v) {
        Intent i = new Intent(RestaurantActivity.this, MenuActivity.class);
        startActivity(i);
    }
}
