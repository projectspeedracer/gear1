package com.projectspeedracer.thefoodapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.projectspeedracer.thefoodapp.fragments.MenuFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.projectspeedracer.thefoodapp.utils.Transformer;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

	private static final String TAG = Constants.TAG;

	private Map<String, List<Dish>> categoryDishMap = new LinkedHashMap<>();

	public ViewPagerAdapter(FragmentManager fm, List<Dish> dishes) {
		super(fm);

		// TODO: If dishes empty, use a default list of page titles !!!
		reloadPageTitles(dishes);
	}

	public void reloadPageTitles(List<Dish> dishes) {
		this.categoryDishMap = Helpers.GroupBy(dishes, new Transformer<Dish, String>() {
			@Override
			public String transform(Dish item) {
				final String category = item.getCategory();
				return StringUtils.isBlank(category) ? Constants.DEFAULT_DISH_CATEGORY : category;
			}
		});
	}

	@Override
	public Fragment getItem(int position) {
		final Map.Entry<String, List<Dish>> entry = getEntryAt(position);
		assert entry != null : "Unexpected null menu fragment";
		return new MenuFragment(entry.getValue(), entry.getKey());
    }

    @Override
    public CharSequence getPageTitle(int position) {
	    final Map.Entry<String, List<Dish>> entry = getEntryAt(position);
	    return entry.getKey();
    }

    @Override
    public int getCount() {
        return categoryDishMap.size();
    }

	private LinkedHashMap.Entry<String, List<Dish>> getEntryAt(int position) {
		Helpers.EnsureTruth(position >= 0 && position < categoryDishMap.size(), "No menu fragment found at position " + position);

		int index = 0;

		for (LinkedHashMap.Entry<String, List<Dish>> entry : categoryDishMap.entrySet()) {
			if (index == position) {
				return entry;
			}

			++index;
		}

		return null;
	}
}