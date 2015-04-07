package com.projectspeedracer.thefoodapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class RateDishActivity extends ActionBarActivity {

	private static final String TAG = RateDishActivity.class.getSimpleName();

	private Dish dishToRate;
	private Restaurant restaurant;
	private EditText etMessage;

    String dishObjectId;

    private float selectedRating = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_dish);
        dishObjectId = getIntent().getStringExtra("current_dish_id");
	    /*final String json = getIntent().getStringExtra("dish");
	    final Dish dish = Helpers.FromJsonSafe(json, Dish.class);

	    if (dish == null) {
		    final String msg = "FAILED launching RatingDishActivity. Could not obtain the dish object.";
		    LogToast(this, msg);
		    // TODO: finish(); - When the serialization works ok !!!
	    }*/

        Log.i(TAG, "Rating Dish (" + dishObjectId + ") ...");

        restaurant = TheFoodApplication.getCurrentRestaurant();

        etMessage = (EditText) findViewById(R.id.etMessage);
        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.etSearch || actionId == EditorInfo.IME_ACTION_DONE) {
                    postRating();
                    return true;
                }

                return false;
            }
        });

        // 1. Get Dish object
        FoodAppUtils.fetchDish(dishObjectId, new GetCallback<Dish>() {

	        @Override
	        public void done(Dish dish, ParseException e) {
		        if (e != null) {
			        final String errorText = "[RateDish]Failed to query dish for ID - " + dishObjectId;

			        Log.e(TAG, errorText + ". " + e.getMessage());
			        e.printStackTrace();

			        return;
		        }

		        dishToRate = dish;

                setupViews();

		        Log.i(TAG, String.format("Found %s dish", dishToRate.getName()));
	        }
        });



        // click handlers for rating buttons
        findViewById(R.id.ratingDishBad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRating = 1.0f;
                ((ImageView) findViewById(R.id.ratingDishBad)).setImageResource(R.drawable.bad);
                ((ImageView) findViewById(R.id.ratingDishMeh)).setImageResource(R.drawable.meh_grey);
                ((ImageView) findViewById(R.id.ratingDishGood)).setImageResource(R.drawable.good_grey);
            }
        });
        findViewById(R.id.ratingDishMeh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRating = 2.0f;
                ((ImageView) findViewById(R.id.ratingDishMeh)).setImageResource(R.drawable.meh);
                ((ImageView) findViewById(R.id.ratingDishBad)).setImageResource(R.drawable.bad_grey);
                ((ImageView) findViewById(R.id.ratingDishGood)).setImageResource(R.drawable.good_grey);
            }
        });
        findViewById(R.id.ratingDishGood).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRating = 3.0f;
                ((ImageView) findViewById(R.id.ratingDishGood)).setImageResource(R.drawable.good);
                ((ImageView) findViewById(R.id.ratingDishMeh)).setImageResource(R.drawable.meh_grey);
                ((ImageView) findViewById(R.id.ratingDishBad)).setImageResource(R.drawable.bad_grey);
            }
        });



    }

    private void setupViews() {
        String name = dishToRate.getName();
        final String image = dishToRate.getImage();
        if (StringUtils.isNotBlank(image)) {
            final ImageView ivDish = (ImageView) findViewById(R.id.ivMenuItem);

            ivDish.setImageResource(0);

            Picasso.with(this)
                    .load(image)
                    .into(ivDish);
        }
        TextView tvMenuItemDescription = (TextView) findViewById(R.id.tvMenuItemName);
        tvMenuItemDescription.setText(name);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Rate "+name);
    }

    private  void postRating() {
        if (restaurant == null) {
            Toast.makeText(this, "Restaurant not selected!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dishToRate == null) {
            Toast.makeText(this, "Dish not selected!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ParseUser.getCurrentUser() == null) {
            Toast.makeText(this, "You are not signed in!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Create Rating Post Object

	    float ratingNum = selectedRating;
	    final String rateMessage = etMessage.getText().toString();

	    Log.i(TAG, "Ready to post for " + dishToRate.getName() + " Stars = "+ratingNum);

	    // 2.1 - Set rating
	    final Rating rating = new Rating();
//        rating.setStars(new Integer(ratingNum.toString()));
        rating.setStars(Math.round(ratingNum));
        rating.setComments(rateMessage);

        // 3. Put Restaurant, Dish, User and GeoPoint
        rating.setDish(dishToRate);
        rating.setRestaurant(restaurant);
        rating.setUser(ParseUser.getCurrentUser());
        rating.setLocation(restaurant.getLocation());
        rating.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final String msg = String.format("Rating save callback: %s. Stars: %s. Comments: %s",
                        e != null ? "FAILED" : "SUCCESS",
                        rating.getStars(),
                        rating.getComments());

                Log.i(TAG, msg);


                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                addRelations(rating);

                // Now fetch dish again and return the result to parent activity
                FoodAppUtils.fetchDish(dishToRate.getObjectId(), OnDishFetched);
            }
        });


        // 7. Save objects
        // rating.saveInBackground();
    }


	public void onPostRating(View v) {
        postRating();
    }

    private void addRelations(final Rating rating) {

        final String[] info = new String[] {
                "Stars: " + rating.getStars(),
                "Comments: " + rating.getComments()
        };

        // 4. Add Dish -> Post relation
        ParseRelation<ParseObject> relationDish = dishToRate.getRelation("DishToPosts");
        relationDish.add(rating);
        dishToRate.saveInBackground(new ParseSaveCallback("Dish/Rating Update: ", info));

        // 5. Add Restaurant -> Post relation
        ParseRelation<ParseObject> relationRestaurant = restaurant.getRelation("RestaurantToPosts");
        relationRestaurant.add(rating);
        restaurant.saveInBackground(new ParseSaveCallback("Restaurant/Rating Update: ", info));

        // 6. Add User -> Post relation
        ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> relationUser = user.getRelation("UserToPosts");
        relationUser.add(rating);
        user.saveInBackground(new ParseSaveCallback("User/Rating Update: ", info));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rate_dish, menu);
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

	class ParseSaveCallback implements SaveCallback {

		private String prefix;
		private String[] args;

		public ParseSaveCallback(String prefix, String[] args) {
			this.prefix = prefix;
			this.args = args;
		}

		@Override
		public void done(ParseException e) {
			final boolean saved = e == null;

			final String message = String.format("%s: %s %s",
					prefix,
					saved ? "SUCCESS." : "FAILED!",
					Arrays.toString(args));

			Log.i(TAG, message);

			if (!saved) {
				e.printStackTrace();
			}
		}
	}

    @Override
    public void finish() {
        // Give the dish to parent activity
        if (dishToRate != null) {
            try {
                final String json = Helpers.AsJson(dishToRate);
                final Intent data = new Intent();
                data.putExtra("dish", json);
                setResult(RESULT_OK, data);
            } catch (JsonProcessingException e) {
                FoodAppUtils.LogToast(this, "[RateDishActivity] ERROR! Failed to pass back dish details. " + e.getMessage());
                e.printStackTrace();
            }
        }

        super.finish();
    }

    // Once fetched, return it to caller...
    private final GetCallback<Dish> OnDishFetched = new GetCallback<Dish>() {

        @Override
        public void done(Dish dish, ParseException e) {
            if (e != null) {
                final String errorText = "[RateDish] Failed to query dish for ID - " + dishObjectId;
                Log.e(Constants.TAG, errorText + ". " + e.getMessage());
                e.printStackTrace();
                return;
            }

            if (dishToRate == null) {
                dishToRate = dish;
            } else {
                dishToRate.update(dish);
            }

            finish();
        }
    };

}
