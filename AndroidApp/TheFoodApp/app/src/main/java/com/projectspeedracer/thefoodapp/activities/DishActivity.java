package com.projectspeedracer.thefoodapp.activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.fragments.DishRatingsFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;

public class DishActivity extends ActionBarActivity {

	Dish       dishToShow;
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

        final RatingBar ratingBar = (RatingBar) findViewById(R.id.dishRatingBarAggrigated);
        ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i("Rating", "Rating bar touched");
                    if (dishToShow == null){
                        return false;
                    }
                    onRateDish(dishToShow);
                }
                return true;
            }
        });

        // 1. Get Dish object
        FoodAppUtils.fetchDish(dishObjectId, new GetCallback<Dish>() {

            @Override
            public void done(Dish dish, ParseException e) {
                if (e != null) {
                    final String errorText = "Failed to query dish for ID - " + dishObjectId;

                    Log.e(Constants.TAG, errorText + ". " + e.getMessage());
                    e.printStackTrace();

                    return;
                }

                dishToShow = dish;

                Log.i(Constants.TAG, String.format("Found %s dish", dish.getName()));
                final RatingBar ratingBar = (RatingBar) findViewById(R.id.dishRatingBarAggrigated);
                ratingBar.setRating(dish.getAverageRating());
            }
        });


		showDishPosts();
	}

    void onRateDish(Dish dish) {
        Intent i = new Intent(this, RateDishActivity.class);
        Log.v(Constants.TAG, "Rating dish - "+dish.getName() + " Id: "+dish.getObjectId());
        i.putExtra("current_dish_id", dish.getObjectId());
//        i.putExtra("current_dish", dish);
        startActivity(i);
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
