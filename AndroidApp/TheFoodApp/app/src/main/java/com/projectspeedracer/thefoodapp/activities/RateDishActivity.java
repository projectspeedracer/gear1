package com.projectspeedracer.thefoodapp.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Arrays;

public class RateDishActivity extends ActionBarActivity {

    Dish dishToRate;
    Restaurant restaurant;
    EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_dish);

//        Dish dish = (Dish) getIntent().getSerializableExtra("current_dish");
        final String dishObjectId = getIntent().getStringExtra("current_dish_id");
        Log.i("RateDish", "Will Rate Dish -"+ dishObjectId);
        Toast.makeText(this, "Got dish - "+ dishObjectId, Toast.LENGTH_SHORT).show();

        restaurant = TheFoodApplication.getCurrentRestaurant();

        etMessage = (EditText) findViewById(R.id.etMessage);
        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.etSearch ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    postRating();
                    return true;
                }
                return false;
            }
        });


        // 1. Get Dish object
        FoodAppUtils.getDishFromObjectID(dishObjectId, new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject dish, ParseException e) {
                if (e != null) {
                    final String errorText = "Failed to query dish for ID - "+dishObjectId;

                    Log.e(Constants.TAG, errorText + ". " + e.getMessage());
                    e.printStackTrace();

                    return;
                }

                dishToRate = (Dish) dish;

                Log.i(Constants.TAG, String.format("Found %s dish", dishToRate.getName()));
            }
        });

    }

    private  void postRating() {
        // Safety checks
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



        // 2. Create Rating Post Obejct

        final Rating rating = new Rating();

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBarDish);
        Float ratingNum = ratingBar.getRating();


        String rateMessage = etMessage.getText().toString();


        Log.i("Rate", "Ready to post for "+dishToRate.getName() + " Stars = "+ratingNum);

        // 2.1 - Set rating
//        rating.setStars(new Integer(ratingNum.toString()));
        rating.setStars(Math.round(ratingNum));
        rating.setComments(rateMessage);

        // 3. Put Restaurant, Dish, User and GeoPoint
//        rating.setDish(dishToRate);
//        rating.setRestaurant(restaurant);
//        rating.setUser(ParseUser.getCurrentUser());
        rating.setLocation(restaurant.getLocation());
        rating.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.i(Constants.TAG, "Rating save callback: "
                        + (e != null ? "FAILED!" : "SUCCESS")
                        + " . Starts: " + rating.getStars() + " Comments: " + rating.getComments());
                if (e != null) {
                    e.printStackTrace();
                }
                else {
                    // add relations with Dish, Restaurant and User
                    addRelations(rating);
                }
            }
        });


        // 7. Save objects
//        rating.saveInBackground();
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

    class ParseSaveCallback implements SaveCallback {

        private String prefix;
        private String[] args;

        public ParseSaveCallback(String prefix, String[] args) {
            this.prefix = prefix;
            this.args = args;
        }

        @Override
        public void done(ParseException e) {
            final boolean saved = e != null;
            final String message = String.format("%s: %s %s",
                    prefix,
                    saved ? "SUCCESS." : "FAILED!",
                    Arrays.toString(args));

            Log.i(Constants.TAG, message);

            if (e != null) {
                e.printStackTrace();
            }
        }
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
}