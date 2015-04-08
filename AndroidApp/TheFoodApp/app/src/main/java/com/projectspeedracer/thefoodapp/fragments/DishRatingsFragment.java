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

    private Dish currentDish;


	public static DishRatingsFragment newInstance (String dishObjectId) {
        final Bundle args = new Bundle();
	    args.putString(DISH_OBJECT_ID, dishObjectId);

	    final DishRatingsFragment dishPostsFragment = new DishRatingsFragment();
	    dishPostsFragment.setArguments(args);

        return dishPostsFragment;
    }

    @Override
    public void fetchPosts(final int pageNum) {
        final String dishObjectId = getArguments().getString("dish_object_id");
        Log.i("DishPost", "Will fetch posts for Dish -" + dishObjectId);

        if (pageNum == FIRST_PAGE) {
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

                    currentDish = dish;

                    Log.i(Constants.TAG, String.format("Found %s dish", dish.getName()));
                    fetchDishRatings(dish, pageNum);
                }
            });
        }
        else {
            // use existing dish
            Log.i(Constants.TAG, String.format("Fetching page %d for %s dish", pageNum, currentDish.getName()));
            fetchDishRatings(currentDish, pageNum);
        }
    }

    private void fetchDishRatings(final Dish dish, final int pageNum) {
	    final ParseRelation<Rating> relationDish = dish.getRelation(ParseRelationNames.DishToPosts);
	    final ParseQuery<Rating> query = relationDish.getQuery();

	    // include respective User objects
	    query.include(Rating.Fields.USER);
	    query.include(Rating.Fields.DISH);
	    query.orderByDescending("createdAt");

        //for pagination
        query.setLimit(Constants.NUM_ITEMS_PER_QUERY);
        query.setSkip((pageNum - 1) * Constants.NUM_ITEMS_PER_QUERY);

	    // TODO: add 7 days constraint !!!

	    query.findInBackground(new FindCallback<Rating>() {

		    @Override
		    public void done(List<Rating> ratings, ParseException e) {
                sdRefresh.setRefreshing(false);
			    if (e != null) {
				    e.printStackTrace();
				    return;
			    }

                Log.i(Constants.TAG, "Ratings for " + dish.getName() + " ("+pageNum+ ")"+" num: " + ratings.size());

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

    @Override
    void OnClickDish(Dish dish) {
        // We don't want to do anything in DishActivity.
        Log.i(Constants.TAG, "Ignoring item click from dish lists");
    }
}
