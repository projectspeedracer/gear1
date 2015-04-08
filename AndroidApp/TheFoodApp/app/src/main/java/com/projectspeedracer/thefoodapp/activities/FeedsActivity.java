package com.projectspeedracer.thefoodapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.adapters.DishesAdapter;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FeedsActivity extends ActionBarActivity implements View.OnClickListener {

	private List<Dish> dishes = new ArrayList<>();
	private DishesAdapter dishesAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feeds);

		assert TheFoodApplication.getCurrentRestaurant() != null : "Expected non-null restaurant object";

		// TODO: Show progress overlay !!!

//		dishesAdapter = new DishesAdapter(getApplicationContext(), this, dishes);

		final ListView lvDishes = (ListView) findViewById(R.id.lvDishes);
		lvDishes.setAdapter(dishesAdapter);

		final ParseQuery<Dish> query = ParseQuery.getQuery(Dish.class);
		query.whereEqualTo(Dish.Fields.RESTAURANT_ID, TheFoodApplication.getCurrentRestaurant().getPlacesId());

		query.findInBackground(new FindCallback<Dish>() {
			@Override
			public void done(List<Dish> dishes, ParseException e) {
				if (e != null) {
					final String errorText = "Failed to query dishes";

					Log.e(Constants.TAG, errorText + ". " + e.getMessage());
					e.printStackTrace();

					Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT).show();
				    return;
			    }

				Log.i(Constants.TAG, String.format("Found %s dishes", dishes.size()));

				dishesAdapter.addAll(dishes);
		    }
	    });

	    // xxxx 1. Get this restaurant row from Parse
	    // 2. Get dishes for this restaurant from Parse (based on restaurantId)
	    // 3. Get ratings for dishes and restaurant from Parse (based on restaurantId and dishId)
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feeds, menu);

        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(FeedsActivity.this, SettingsActivity.class));
                return true;
            }
        });

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

    @Override
    public void onClick(View v) {

    }
}
