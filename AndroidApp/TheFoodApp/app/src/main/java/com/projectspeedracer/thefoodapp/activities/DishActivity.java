package com.projectspeedracer.thefoodapp.activities;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.fragments.DishRatingsFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Restaurant;

public class DishActivity extends ActionBarActivity {

	Dish       dishToRate;
	Restaurant restaurant;
	String     dishObjectId;

	DishRatingsFragment dishRatingsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish);

		dishObjectId = getIntent().getStringExtra("current_dish_id");
		Log.i("RateDish", "Will Rate Dish -" + dishObjectId);
		Toast.makeText(this, "Got dish - " + dishObjectId, Toast.LENGTH_SHORT).show();

		restaurant = TheFoodApplication.getCurrentRestaurant();

		showDishPosts();
	}

	private void showDishPosts() {
		// get screen name

		DishRatingsFragment dishRatingsFragment = DishRatingsFragment.newInstance(dishObjectId);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.flDishPosts, dishRatingsFragment);
        ft.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dish, menu);
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
}
