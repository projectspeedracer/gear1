package com.projectspeedracer.thefoodapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.models.Restaurant;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;


public class RestaurantAdapter extends ArrayAdapter<Restaurant> {
    public RestaurantAdapter(Context context, List<Restaurant> objects, int resource) {
        super(context, R.layout.lv_item_restaurant, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Restaurant restaurant = getItem(position);
        //check if using recycling view, if not we need to inflate
        if (convertView == null) {
            //create a new view by template
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.lv_item_restaurant,
                    parent, false);
        }
        //look up the views for populating the data (image, caption)
        ImageView ivRestaurantPhoto = (ImageView) convertView.findViewById(R.id.ivRestaurantPhoto);
        TextView tvRestaurantName = (TextView) convertView.findViewById(R.id.tvRestaurantName);
        TextView tvRestaurantAddress = (TextView) convertView.findViewById(R.id.tvRestaurantAddress);
        Button btRestaurantReview = (Button) convertView.findViewById(R.id.btRestaurantReview);

        tvRestaurantName.setText(restaurant.getName());
        tvRestaurantAddress.setText(restaurant.getAddress());
        ivRestaurantPhoto.setImageResource(0);
        Picasso.with(getContext()).load(restaurant.getPhotoUrl()).into(ivRestaurantPhoto);
        return convertView;
    }
}
