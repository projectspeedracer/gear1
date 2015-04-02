package com.projectspeedracer.thefoodapp.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.activities.PickRestaurantActivity;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MenuArrayAdapter extends ArrayAdapter<Dish> {

    public MenuArrayAdapter(Context context, List<Dish> objects) {
        super(context, R.layout.item_dish, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tvMenuItemName;
        TextView tvMenuItemDescription;
        TextView tvDishRating;
        ImageView ivMenuItem;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_dish, parent, false);
        }
        Dish dish = (Dish) getItem(position);

        // >>>>>>>>> Photo: Eg. https://maps.googleapis.com/maps/api/place/photo?maxwidth=1200&key=AIzaSyB0YUvMN8cjlP41ZC-IGajc9m2J5oEn4nE&photoreference=CnRnAAAArvbAnbfXda4dQ6DHkj83Uc79gJ0ASBSjQJgGsAbh5v8Brj9tRDtbvlaFH98pu4-XxdWAdQFLTYQKHLsiqaR4lOzOKgV9DdmUU0eaTcpOfx03KUToDy-TIRVqHRgfx_Q5BoqXU55UY0ORa7QBUcsdShIQMmQGFyomw-FJ0K-bOM6ljhoU-RDgNMD-NA84LbdEgkE5Zw_f0fE
        ivMenuItem = (ImageView) convertView.findViewById(R.id.ivMenuItem);

        tvMenuItemName = (TextView) convertView.findViewById(R.id.tvMenuItemName);
        tvMenuItemName.setText(dish.getName());

        tvMenuItemDescription = (TextView) convertView.findViewById(R.id.tvMenuItemDescription);
        tvMenuItemDescription.setText(dish.getDescription());

        tvDishRating = (TextView) convertView.findViewById(R.id.tvDishRating);
        tvDishRating.setText("4.5");

        return convertView;
    }
}