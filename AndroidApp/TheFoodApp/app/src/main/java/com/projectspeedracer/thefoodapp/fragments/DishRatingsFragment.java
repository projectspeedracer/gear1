package com.projectspeedracer.thefoodapp.fragments;

import android.os.Bundle;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.ParseRelationNames;

import java.util.List;

/**
 * Created by avkadam on 4/4/15.
 */
public class DishRatingsFragment extends AbstractRatingsFragment {

	public static final String DISH_OBJECT_ID = "dish_object_id";

	public static DishRatingsFragment newInstance (String dishObjectId) {
        final Bundle args = new Bundle();
	    args.putString(DISH_OBJECT_ID, dishObjectId);

	    final DishRatingsFragment fragmentUserTimeline = new DishRatingsFragment();
	    fragmentUserTimeline.setArguments(args);

        return fragmentUserTimeline;
    }

    @Override
    public void fetchPosts() {
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

                Log.i(Constants.TAG, String.format("Found %s dish", dish.getName()));
                getDishRatings(dish);
            }
        });
    }

    private void getDishRatings(final Dish dish) {
	    final ParseRelation<Rating> relationDish = dish.getRelation(ParseRelationNames.DishToPosts);
	    final ParseQuery<Rating> query = relationDish.getQuery();

	    // include respective User objects
	    query.include(Rating.Fields.USER);
	    query.include(Rating.Fields.DISH);
	    query.orderByDescending("createdAt");

	    // TODO: add 7 days constraint !!!

	    query.findInBackground(new FindCallback<Rating>() {

		    @Override
		    public void done(List<Rating> ratings, ParseException e) {
			    if (e != null) {
				    e.printStackTrace();
				    return;
			    }

			    Log.i(Constants.TAG, "Ratings for "+dish.getName()+" num: "+ratings.size());
			    ratingsAdapter.addAll(ratings);
		    }
	    });

        /*FoodAppUtils.getAllPostsForDish(dish, new FindCallback<Rating>() {

            @Override
            public void done(List<Rating> ratings, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                Log.i(Constants.TAG, "Ratings for "+dish.getName()+" num: "+ratings.size());
                ratingsAdapter.addAll(ratings);
            }
        });*/
    }
}
