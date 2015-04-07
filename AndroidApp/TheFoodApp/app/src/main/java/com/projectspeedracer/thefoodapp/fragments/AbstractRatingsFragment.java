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
import android.widget.ListView;
import android.widget.Toast;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.activities.DishActivity;
import com.projectspeedracer.thefoodapp.activities.RateDishActivity;
import com.projectspeedracer.thefoodapp.adapters.RatingsAdapter;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;

import java.util.ArrayList;

public abstract class AbstractRatingsFragment extends Fragment implements View.OnClickListener {
	protected RatingsAdapter ratingsAdapter;

    OnDishRatedListner listener;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_list_posts, container, false);

        listener = (OnDishRatedListner) getActivity();
		setupViews(view);
		// fetchTweets(FIRST_PAGE);

		return view;
	}

    @Override
    public void onResume() {
        super.onResume();
        fetchPosts();
    }

    private void setupViews(View parent) {
		ListView lvPosts = (ListView) parent.findViewById(R.id.lvPosts);
		ArrayList<Rating> lPosts = new ArrayList<>();
		ratingsAdapter = new RatingsAdapter(getActivity(), this, lPosts);
		lvPosts.setAdapter(ratingsAdapter);
	}

	public abstract void fetchPosts();

    @Override
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
        startActivityForResult(i, DishActivity.REQUEST_CODE_START);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DishActivity.REQUEST_CODE_START) {
            if (resultCode == Activity.RESULT_OK) {
                final String json = data.getStringExtra("dish");
                final Dish returnedDish = Helpers.FromJsonSafe(json, Dish.class);

                if (returnedDish != null) {
                    listener.onDishRated(returnedDish);
                }
                else {
                    FoodAppUtils.LogToast(getActivity(), "[DishActivity] Returned dish is null");
                }

                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public interface OnDishRatedListner {
        public void onDishRated(Dish returnedDish);
    }
}
