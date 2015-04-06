package com.projectspeedracer.thefoodapp.fragments;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;

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

}
