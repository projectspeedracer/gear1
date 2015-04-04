package com.projectspeedracer.thefoodapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DishesAdapter extends ArrayAdapter<Dish> {

	public DishesAdapter(Context context, List<Dish> dishes) {
		super(context, R.layout.item_dish, dishes);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_dish, parent, false);
		}

		final Dish dish = getItem(position);

		final TextView tvName = (TextView) convertView.findViewById(R.id.tvMenuItemName);
		tvName.setText(dish.getName());

		final String image = dish.getImage();

		if (image != null) {
			final ImageView ivDish = (ImageView) convertView.findViewById(R.id.ivMenuItem);

			ivDish.setImageResource(0);

			Picasso.with(getContext())
					.load(image)
					.into(ivDish);
		}

		return convertView;
	}
}
