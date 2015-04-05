package com.projectspeedracer.thefoodapp.activities;

import com.projectspeedracer.thefoodapp.models.Dish;

import java.util.List;

public interface IDishesFetchedCallback {
	void onDishesFetched(List<Dish> dish);
}
