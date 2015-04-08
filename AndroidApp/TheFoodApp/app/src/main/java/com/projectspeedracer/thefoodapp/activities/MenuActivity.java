package com.projectspeedracer.thefoodapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseUser;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.TheFoodApplication;
import com.projectspeedracer.thefoodapp.adapters.ViewPagerAdapter;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.ProximityInspector;
import com.projectspeedracer.thefoodapp.utils.SlidingTabLayout;

import java.util.List;

public class MenuActivity extends ActionBarActivity implements IDishesFetchedCallback {

	public MenuActivity() {
	}

    ProgressBar pb;
    ProximityInspector proximityInspector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
		setSupportActionBar(toolbar);

        Restaurant currentRestaurant = TheFoodApplication.getCurrentRestaurant();
        assert currentRestaurant != null : "Current restaurant not set!";

		currentRestaurant.fetchDishes(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(currentRestaurant.getName()+" Menu");

        pb = (ProgressBar) findViewById(R.id.progressBar);
        FoodAppUtils.assignProgressBarStyle(this, pb);
        pb.setVisibility(ProgressBar.VISIBLE);
	}


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.item_logout:
                FoodAppUtils.showSignOutDialog(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
	}


	@Override
	public void onDishesFetched(List<Dish> dishes) {

        pb.setVisibility(ProgressBar.GONE);

		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), dishes);

		final ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);

		final SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
		tabs.setDistributeEvenly(true);

		tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				return getResources().getColor(R.color.tabs_scroll_color);
			}
		});

		tabs.setViewPager(pager);
	}


    @Override
    protected void onResume() {
        super.onResume();
        proximityInspector = new ProximityInspector(this, this); // starts monitoring proximity
    }

    @Override
    protected void onStop() {
        if (proximityInspector != null) {
            proximityInspector.stop();
        }
        super.onStop();
    }
}
