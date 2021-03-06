package com.projectspeedracer.thefoodapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.fragments.AbstractRatingsFragment;
import com.projectspeedracer.thefoodapp.fragments.DishRatingsFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.projectspeedracer.thefoodapp.utils.ProximityInspector;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public class DishActivity extends ActionBarActivity implements AbstractRatingsFragment.OnDishRatedListner {

	private static final String TAG                = Constants.TAG;
	public static final  int    REQUEST_CODE_START = 1024;

	private Dish   currentDish;
	private String dishObjectId;

    ProgressBar pb;
    ProximityInspector proximityInspector;

/*
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

            setupViews();
		}
	};
*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish);

//        dishObjectId = getIntent().getStringExtra("current_dish_id");
//        FoodAppUtils.LogToast(getApplicationContext(), "Dish {" + dishObjectId + "}");
//        Log.i(Constants.TAG, "Dish {" + dishObjectId + "}");

		try {
			final String json = getIntent().getStringExtra("dish");
			final Dish dish = Helpers.FromJson(json, Dish.class);
			Log.i(TAG, "WOW!" + dish.getName());
            currentDish = dish;
            dishObjectId = dish.getId();
		} catch (Exception ex) {
			FoodAppUtils.LogToast(this, "ERROR: " + ex.getMessage());
		}

		final LinearLayout ratingDisplayBox = (LinearLayout) findViewById(R.id.ratingDisplayBox);
        ratingDisplayBox.setOnTouchListener(new View.OnTouchListener() {
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

        pb = (ProgressBar) findViewById(R.id.progressBar);
        FoodAppUtils.assignProgressBarStyle(this, pb);
        pb.setVisibility(ProgressBar.VISIBLE);

        initializeRatingsFragment();

        setupViews();
    }

    private void setupViews() {

        pb.setVisibility(ProgressBar.GONE);

        String name = currentDish.getName();
        Log.i(Constants.TAG, String.format("Found %s dish", name));
        //final RatingBar ratingBar = (RatingBar) findViewById(R.id.dishRatingBarAggrigated);
        final ImageView dishRatingBarIcon = (ImageView) findViewById(R.id.dishRatingBarIcon);
        double averageRating = currentDish.getAverageRating();
        dishRatingBarIcon.setImageResource(currentDish.ratingIconResId());

        // Show rating
        TextView tvDishRating = (TextView) findViewById(R.id.tvDishRating);
        final String ratingText = averageRating == 0
                ? getString(R.string.no_ratings_detailed_msg)
                : new DecimalFormat("##.0").format(averageRating);
        tvDishRating.setText(ratingText);

        // Show image
        final String image = currentDish.getImage();
        if (StringUtils.isNotBlank(image)) {
            final ImageView ivDish = (ImageView) findViewById(R.id.ivMenuItem);

            ivDish.setImageResource(0);

            Picasso.with(this)
                    .load(image)
                    .into(ivDish);
        }

        // show description
        TextView tvMenuItemDescription = (TextView) findViewById(R.id.tvMenuItemDescription);
        String description = currentDish.getDescription();
        if (StringUtils.isNoneBlank(description)){
            tvMenuItemDescription.setText(description);
        }
        else {
            // Show name if description is not available
            tvMenuItemDescription.setText(name);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        FoodAppUtils.fetchDish(dishObjectId, OnDishFetched);
        proximityInspector = new ProximityInspector(this, this); // starts monitoring proximity

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
        String objectId = dish.getId();
        Log.v(Constants.TAG, "Rating dish - " + dish.getName() + " Id: " + objectId);
        i.putExtra("current_dish_id", objectId);
        //i.putExtra("current_dish", dish);
        startActivityForResult(i, DishActivity.REQUEST_CODE_START);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DishActivity.REQUEST_CODE_START) {
            if (resultCode == Activity.RESULT_OK) {
                final String json = data.getStringExtra("dish");
                final Dish returnedDish = Helpers.FromJsonSafe(json, Dish.class);

                if (returnedDish != null) {
                    currentDish.update(returnedDish);
                    setupViews();
                }
                else {
                    FoodAppUtils.LogToast(this, "[DishActivity] Returned dish is null");
                }

                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        // Okay, we don't want to log out from here. So Menu is removed from XML
        switch (id) {
            case R.id.item_logout:
                FoodAppUtils.showSignOutDialog(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void finish() {
        // Give the dish to parent activity
		if (currentDish != null) {
			try {
				final String json = Helpers.AsJson(currentDish);
				final Intent data = new Intent();
				data.putExtra("dish", json);
				setResult(RESULT_OK, data);
			} catch (JsonProcessingException e) {
				FoodAppUtils.LogToast(this, "[DishActivity] ERROR! Failed to pass back dish details. " + e.getMessage());
				e.printStackTrace();
			}
		}

		super.finish();
	}

    @Override
    public void onDishRated(Dish returnedDish) {
        currentDish.update(returnedDish);
        setupViews();
    }

    @Override
    protected void onStop() {
        if (proximityInspector != null) {
            proximityInspector.stop();
        }
        super.onStop();
    }
}
