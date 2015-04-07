package com.projectspeedracer.thefoodapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.DishActivity;
import com.projectspeedracer.thefoodapp.activities.RateDishActivity;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;

import java.util.List;

public class RestaurantRatingsFragment extends AbstractRatingsFragment {
    public static RestaurantRatingsFragment newInstance () {
        RestaurantRatingsFragment restaurantPostsFragment = new RestaurantRatingsFragment();
        return restaurantPostsFragment;
    }


    // Override specific methods here...
    @Override
    public void fetchPosts() {
        // Fetch posts for a Restaurant

        Log.i("RestaurantPost", "Will fetch posts for Restaurant -");

        Restaurant restaurant = TheFoodApplication.getCurrentRestaurant();
        getRestaurantPosts(restaurant);
    }

    private void getRestaurantPosts(final Restaurant restaurant) {
        FoodAppUtils.getAllPostsForRestaurant(new FindCallback<Rating>() {

            @Override
            public void done(List<Rating> ratings, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                Log.i(Constants.TAG, "Ratings for " + restaurant.getName() + " num: " + ratings.size());
                ratingsAdapter.clear();
                ratingsAdapter.addAll(ratings);
            }
        });
    }


   /* @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivRatingImage:
                onRatingClick(v);
                break;
        }
    }

    public void onRatingClick(View view) {

        Dish dish = (Dish) view.getTag();
        Toast.makeText(getActivity(), "Touched Rating for Dish - " + dish.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Show Rating page now.
        Intent i = new Intent(getActivity(), RateDishActivity.class);
        Log.v(Constants.TAG, "[MenuActivity] Rating dish - " + dish.getName() + " Id: " + dish.getObjectId());
        i.putExtra("current_dish_id", dish.getObjectId());
        //i.putExtra("current_dish", dish);
        startActivity(i);
    }*/

}
