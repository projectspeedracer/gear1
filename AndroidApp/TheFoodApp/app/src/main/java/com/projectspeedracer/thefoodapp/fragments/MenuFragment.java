package com.projectspeedracer.thefoodapp.fragments;

import android.app.Activity;
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
import android.widget.Toast;

import com.android.internal.util.Predicate;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.projectspeedracer.thefoodapp.utils.Transformer;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MenuFragment extends Fragment implements View.OnClickListener {

	private static final String TAG = Constants.TAG;

	protected ArrayList<Dish> dishes = new ArrayList<>();
	protected DishesAdapter dishesAdapter;
	protected final String category;

	public MenuFragment() {
		super();
		this.category = Constants.DEFAULT_DISH_CATEGORY;
	}

	public MenuFragment(List<Dish> dishes, String category) {
		super();
		this.dishes.clear();
		this.dishes.addAll(dishes);
		this.category = category;
	}

	@Override
    public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState){

		assert TheFoodApplication.getCurrentRestaurant() != null : "Expected non-null restaurant object";

		final View view = inflater.inflate(R.layout.fragment_menu_category, container, false);

        // TODO: Show progress overlay !!!

        dishesAdapter = new DishesAdapter(getActivity(), this, dishes);

        final GridView lvDishes = (GridView) view.findViewById(R.id.lvDishes);
        lvDishes.setAdapter(dishesAdapter);
        lvDishes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dish dish = dishesAdapter.getItem(position);
                Log.i(TAG, "Selected - "+ dish.getName());
                OnClickDish(dish);
            }
        });

        return view;
    }

    public void OnClickDish(Dish dish) {
	    Log.v(TAG, String.format("Showing Dish: %s (ID: %s)",  dish.getName(), dish.getObjectId()));

	    final Intent intent = new Intent(getActivity(), DishActivity.class);
//	    intent.putExtra("current_dish_id", dish.getObjectId());

        try {
		    final String json = Helpers.AsJson(dish);
		    intent.putExtra("dish", json);
	    } catch (Exception ex) {
		    final String message = "MAYDAY! " + ex.getMessage();
		    Log.e(TAG, message);
		    Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	    }

	    startActivityForResult(intent, DishActivity.REQUEST_CODE_START);
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DishActivity.REQUEST_CODE_START) {
			if (resultCode == Activity.RESULT_OK) {
				final String json = data.getStringExtra("dish");
				final Dish returnedDish = Helpers.FromJsonSafe(json, Dish.class);

				if (returnedDish != null) {
					final Dish match = Helpers.FindItem(dishes, new Predicate<Dish>() {
						@Override
						public boolean apply(Dish item) {
							return item.getId().equals(returnedDish.getId());
						}
					});

					if (match != null) {
						match.update(returnedDish);
						dishesAdapter.notifyDataSetChanged();
					}
				}

				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dishRatingBarIcon:
                onRatingClick(v);
                break;
        }
	}

    public void onRatingClick(View view) {

        Dish dish = (Dish) view.getTag();
//		Toast.makeText(getActivity(), "Touched Rating for Dish - " + dish.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Show Rating page now.
        Intent i = new Intent(getActivity(), RateDishActivity.class);
        Log.v(Constants.TAG, "[MenuActivity] Rating dish - " + dish.getName() + " Id: " + dish.getObjectId());
        i.putExtra("current_dish_id", dish.getObjectId());
        //i.putExtra("current_dish", dish);
//        startActivity(i);
        startActivityForResult(i, DishActivity.REQUEST_CODE_START);
	}
}