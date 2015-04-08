package com.projectspeedracer.thefoodapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.activities.DishActivity;
import com.projectspeedracer.thefoodapp.activities.RateDishActivity;
import com.projectspeedracer.thefoodapp.adapters.EndlessScrollListener;
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
    SwipeRefreshLayout sdRefresh;

    public static final int FIRST_PAGE = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_list_posts, container, false);

        listener = (OnDishRatedListner) getActivity();
		setupViews(view);

		return view;
	}

    @Override
    public void onResume() {
        super.onResume();
        fetchPostsPre(FIRST_PAGE);
    }

    private void setupViews(View parent) {
		ListView lvPosts = (ListView) parent.findViewById(R.id.lvPosts);
		ArrayList<Rating> lPosts = new ArrayList<>();
		ratingsAdapter = new RatingsAdapter(getActivity(), this, lPosts);
		lvPosts.setAdapter(ratingsAdapter);

        lvPosts.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemCount) {
                fetchPostsPre(page);
                Log.e("LoadMore", "===== Done loading more...");
            }
        });
        lvPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Rating rating = ratingsAdapter.getItem(position);
                Dish dish = rating.getDish();
                if (dish != null) {
                    Log.i(Constants.TAG, "[RatingList] Dish selected - " + dish.getName());
                    OnClickDish(dish);
                }
            }
        });

        sdRefresh = (SwipeRefreshLayout) parent.findViewById(R.id.swipeContainer);
        sdRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPostsPre(FIRST_PAGE);
            }
        });

        sdRefresh.setColorSchemeResources(android.R.color.holo_orange_light, android.R.color.holo_red_light,
                android.R.color.holo_blue_bright, android.R.color.holo_green_light);
	}

    public void fetchPostsPre(int pageNum) {
        boolean isNetworkActive = FoodAppUtils.isNetworkAvailable(getActivity());

        if (!isNetworkActive) {
            // next page, nothing can be done, sorry!!
            Toast.makeText(getActivity(), "No internet, please try later..", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pageNum == FIRST_PAGE){
            // clear if its a new page that user is requesting
            Log.e(Constants.TAG, "Cleanup for first page");
            ratingsAdapter.clear();
        }
        else {

        }

        // fetch posts now..
        fetchPosts(pageNum);
    }

    public abstract void fetchPosts(int page);

    // Useful in Restaurant activity to jump directly to dishes.
    void OnClickDish(Dish dish) {
        Log.v(Constants.TAG, String.format("Showing Dish: %s (ID: %s)",  dish.getName(), dish.getObjectId()));

        final Intent intent = new Intent(getActivity(), DishActivity.class);
        intent.putExtra("current_dish_id", dish.getObjectId());

        try {
            final String json = Helpers.AsJson(dish);
            intent.putExtra("dish", json);
        } catch (Exception ex) {
            final String message = "MAYDAY! " + ex.getMessage();
            Log.e(Constants.TAG, message);
            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

        startActivityForResult(intent, DishActivity.REQUEST_CODE_START);

    }



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
//        Toast.makeText(getActivity(), "Touched Rating for Dish - " + dish.getName(), Toast.LENGTH_SHORT).show();
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
