package com.projectspeedracer.thefoodapp.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.fragments.MenuFragment;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.List;

public class DishesAdapter extends ArrayAdapter<Dish> {

    MenuFragment enclosingFragment;

	public DishesAdapter(Context context, MenuFragment fragment, List<Dish> dishes) {
		super(context, R.layout.item_dish, dishes);
        enclosingFragment = fragment;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_dish, parent, false);
		}

		final Dish dish = getItem(position);
        final double averageRating = dish.getAverageRating();

        final TextView tvName = (TextView) convertView.findViewById(R.id.tvMenuItemName);
        tvName.setText(dish.getName());


        final ImageView ratingIcon = (ImageView) convertView.findViewById(R.id.dishRatingBarIcon);
        ratingIcon.setImageResource(dish.ratingIconResId());
        ratingIcon.setTag(dish);
        ratingIcon.setOnClickListener(enclosingFragment);

        final TextView tvMenuItemDescription = (TextView) convertView.findViewById(R.id.tvMenuItemDescription);
        tvMenuItemDescription.setText(dish.getDescription());

        /*ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i("Rating", "Rating bar touched");
                }
                return true;
            }
        });*/

        final TextView tvDishRating = (TextView) convertView.findViewById(R.id.tvDishRating);
        final String ratingText = averageRating == 0
                ? getContext().getString(R.string.no_ratings)
                : new DecimalFormat("##.0").format(averageRating);
        tvDishRating.setText(ratingText);

/*
        final RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.dishRatingBarAggrigated);
        ratingBar.setRating((float) averageRating);
*/

		final String image = dish.getImage();

		if (StringUtils.isNotBlank(image)) {
			final ImageView ivDish = (ImageView) convertView.findViewById(R.id.ivMenuItem);

			ivDish.setImageResource(0);

			Picasso.with(getContext())
					.load(image)
					.into(ivDish);
		}

		return convertView;
	}
}
