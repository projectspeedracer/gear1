package com.projectspeedracer.thefoodapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.projectspeedracer.thefoodapp.fragments.MenuFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.Helpers;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

	private List<Dish> dishes;
	private String[] pagerTitles;


	public ViewPagerAdapter(FragmentManager fm, List<Dish> dishes) {
		super(fm);
		this.dishes = dishes == null ? new ArrayList<Dish>() : dishes;
		// TODO: If dishes empty, use a default list of page titles !!!
		reloadPageTitles(this.dishes);
	}

	public void reloadPageTitles(List<Dish> dishes) {
		final Map<String, List<Dish>> categoryGroup = Helpers.GroupBy(dishes, new Helpers.Transformer<Dish, String>() {
			@Override
			public String transform(Dish item) {
				final String category = item.getCategory();
				return StringUtils.isBlank(category) ? Constants.DEFAULT_DISH_CATEGORY : category;
			}
		});

		final Set<String> categories = categoryGroup.keySet();
		this.pagerTitles = categories.toArray(new String[categories.size()]);
	}

	@Override
	public Fragment getItem(int position) {

		switch (position) {
			case 0:
				return new MenuFragment(this.dishes);

			default:
				return new MenuFragment(this.dishes);
		}
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return this.pagerTitles[position];
    }

    @Override
    public int getCount() {
        return this.pagerTitles.length;
    }
}