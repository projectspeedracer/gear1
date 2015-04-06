package com.projectspeedracer.thefoodapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.fragments.DishRatingsFragment;
import com.projectspeedracer.thefoodapp.fragments.RestaurantRatingsFragment;
import com.projectspeedracer.thefoodapp.models.Restaurant;

public class RestaurantActivity extends ActionBarActivity {

    Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        initializeRatingsFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restaurant = TheFoodApplication.getCurrentRestaurant();

        TextView tvName = (TextView) findViewById(R.id.tvName);
        String name = restaurant.getName();
        tvName.setText(name);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onOpenMenu(View v) {
        Intent i = new Intent(RestaurantActivity.this, MenuActivity.class);
        startActivity(i);
    }
}
