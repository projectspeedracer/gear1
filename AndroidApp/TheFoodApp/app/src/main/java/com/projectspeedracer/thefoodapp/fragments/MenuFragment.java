package com.projectspeedracer.thefoodapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.activities.DishActivity;
import com.projectspeedracer.thefoodapp.activities.RateDishActivity;
import com.projectspeedracer.thefoodapp.adapters.DishesAdapter;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment implements View.OnClickListener {

    protected ArrayList<Dish> dishes = new ArrayList<>();
    protected DishesAdapter dishesAdapter;
    protected ListView lvMenuCategory;
    private static final String TAG = Constants.TAG;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){


        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.fragment_menu_category, container, false);

        assert TheFoodApplication.getCurrentRestaurant() != null : "Expected non-null restaurant object";

        // TODO: Show progress overlay !!!

        dishesAdapter = new DishesAdapter(getActivity(), dishes);

        final GridView lvDishes = (GridView) view.findViewById(R.id.lvDishes);
        lvDishes.setAdapter(dishesAdapter);
        lvDishes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dish dish = dishesAdapter.getItem(position);
                //Toast.makeText(getActivity(), "Picked - " + restaurant.getName(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Selected - "+dish.getName());
//				listener.restaurantSelected(restaurant);
                onRateDish(dish);
            }
        });



        final ParseQuery<Dish> query = ParseQuery.getQuery(Dish.class);
        query.whereEqualTo(Dish.Fields.RESTAURANT_ID, TheFoodApplication.getCurrentRestaurant().getPlacesId());

        query.findInBackground(new FindCallback<Dish>() {
            @Override
            public void done(List<Dish> dishes, ParseException e) {
                if (e != null) {
                    final String errorText = "Failed to query dishes";

                    Log.e(Constants.TAG, errorText + ". " + e.getMessage());
                    e.printStackTrace();

                    Toast.makeText(getActivity(), errorText, Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(Constants.TAG, String.format("Found %s dishes", dishes.size()));
                dishesAdapter.addAll(dishes);
                for (final Dish dish: dishes) {
                    FoodAppUtils.getAllPostsForDish(dish, new FindCallback<Rating>() {

                        @Override
                        public void done(List<Rating> ratings, ParseException e) {
                            int totalStars = 0;
                            if (e != null) {
                                e.printStackTrace();
                                return;
                            }
                            if (ratings.size() > 0) {

                                for (Rating r : ratings) {
                                    totalStars += r.getStars();
                                }
                                float average = (float)totalStars / (float)ratings.size();
                                if (average < 1 && average > 3) {
                                    Log.e(Constants.TAG, "Something fishy about average - " + average);
                                    average = 2;
                                }
                                Log.i(Constants.TAG, "Ratings for "+dish.getName()+" Total: "+totalStars+" num: "+ratings.size()+" Avg: "+average);
                                dish.setStarAverage(average);
                                dishesAdapter.notifyDataSetChanged();
                            }
                            else {
                                Log.i(Constants.TAG, "No ratings for dish - "+dish.getName());
                            }
                        }
                    });
                }

            }
        });

        // xxxx 1. Get this restaurant row from Parse
        // 2. Get dishes for this restaurant from Parse (based on restaurantId)
        // 3. Get ratings for dishes and restaurant from Parse (based on restaurantId and dishId)


        return view;
    }

    @Override
    public void onClick(View v) {

    }

    void onRateDish(Dish dish) {
        Intent i = new Intent(getActivity(), DishActivity.class);
        Log.v(TAG, "Rating dish - "+dish.getName() + " Id: "+dish.getObjectId());
        i.putExtra("current_dish_id", dish.getObjectId());
//        i.putExtra("current_dish", dish);
        getActivity().startActivity(i);
    }
}
