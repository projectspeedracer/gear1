package com.projectspeedracer.thefoodapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.fragments.DishRatingsFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;

import java.text.DecimalFormat;

public class DishActivity extends ActionBarActivity {

	private static final String TAG = Constants.TAG;

	private Dish   currentDish;
	private String dishObjectId;

	private final GetCallback<Dish> OnDishFetched = new GetCallback<Dish>() {

		@Override
		public void done(Dish dish, ParseException e) {
			if (e != null) {
				final String errorText = "Failed to query dish for ID - " + dishObjectId;
				Log.e(Constants.TAG, errorText + ". " + e.getMessage());
				e.printStackTrace();
				return;
			}

			if (currentDish == null) {
				currentDish = dish;
			} else {
				currentDish.update(dish);
			}

			Log.i(Constants.TAG, String.format("Found %s dish", currentDish.getName()));
			final RatingBar ratingBar = (RatingBar) findViewById(R.id.dishRatingBarAggrigated);
			double averageRating = currentDish.getAverageRating();
			ratingBar.setRating((float) averageRating);

			TextView tvDishRating = (TextView) findViewById(R.id.tvDishRating);
			final String ratingText = averageRating == 0
					? "0"
					: new DecimalFormat("##.0").format(averageRating);
			tvDishRating.setText(ratingText);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish);

		dishObjectId = getIntent().getStringExtra("current_dish_id");
		FoodAppUtils.LogToast(getApplicationContext(), "Dish {" + dishObjectId + "}");

		try {
			final String json = getIntent().getStringExtra("dish");
			final Dish dish = Helpers.FromJson(json, Dish.class);
			Log.i(TAG, "WOW!" + dish.getName());
		} catch (Exception ex) {
			Log.e(TAG, "ERROR: " + ex.getMessage());
		}

		final RatingBar ratingBar = (RatingBar) findViewById(R.id.dishRatingBarAggrigated);
		ratingBar.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Log.i("Rating", "Rating bar touched");
					if (currentDish == null) {
						return false;
					}

					onRateDish(currentDish);
				}
				return true;
			}
		});

        //FoodAppUtils.fetchDish(dishObjectId, OnDishFetched);

        initializeRatingsFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FoodAppUtils.fetchDish(dishObjectId, OnDishFetched);
    }

    private void initializeRatingsFragment() {
        DishRatingsFragment dishRatingsFragment = DishRatingsFragment.newInstance(dishObjectId);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.flDishPosts, dishRatingsFragment);
        ft.commit();
    }


    private void onRateDish(Dish dish) {
        Intent i = new Intent(this, RateDishActivity.class);
        Log.v(Constants.TAG, "Rating dish - " + dish.getName() + " Id: " + dish.getObjectId());
        i.putExtra("current_dish_id", dish.getObjectId());
        //i.putExtra("current_dish", dish);
        startActivity(i);
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
