package com.projectspeedracer.thefoodapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;

import java.util.List;

/**
 * Created by avkadam on 4/4/15.
 */
public class DishPostsFragment extends PostsListFragment {

//    public Dish currentDish;

    public static DishPostsFragment newInstance (String dishObjectId) {
        DishPostsFragment fragmentUserTimeline = new DishPostsFragment();
        Bundle args = new Bundle();
        args.putString("dish_object_id", dishObjectId);
        fragmentUserTimeline.setArguments(args);

        return fragmentUserTimeline;
    }


    // Override specific methods here...
    @Override
    public void fetchPosts() {
        // Fetch posts for a particular Dish
        final String dishObjectId = getArguments().getString("dish_object_id");
        Log.i("DishPost", "Will fetch posts for Dish -" + dishObjectId);

        // 1. Get Dish object
        FoodAppUtils.getDishFromObjectID(dishObjectId, new GetCallback<Dish>() {

            @Override
            public void done(Dish dish, ParseException e) {
                if (e != null) {
                    final String errorText = "Failed to query dish for ID - " + dishObjectId;

                    Log.e(Constants.TAG, errorText + ". " + e.getMessage());
                    e.printStackTrace();

                    return;
                }

//                currentDish = (Dish) dish;

                Log.i(Constants.TAG, String.format("Found %s dish", dish.getName()));
                getDishPosts(dish);
            }
        });

    }

    private void getDishPosts(final Dish dish) {
        FoodAppUtils.getAllPostsForDish(dish, new FindCallback<Rating>() {

            @Override
            public void done(List<Rating> ratings, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

//                for (Rating r : ratings) {
//
//                }
                Log.i(Constants.TAG, "Ratings for "+dish.getName()+" num: "+ratings.size());
                postsAdapter.addAll(ratings);
            }
        });
    }
}
