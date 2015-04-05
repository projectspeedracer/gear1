package com.projectspeedracer.thefoodapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.models.Dish;
import com.projectspeedracer.thefoodapp.models.Rating;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

/**
 * Created by avkadam on 4/4/15.
 */
public class PostsAdapter extends ArrayAdapter<Rating> {
    public PostsAdapter(Context context, List<Rating> objects) {
        super(context, R.layout.item_dish_post, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_dish_post, parent, false);

            holder = new ViewHolder();
            holder.ivRatingImage = (ImageView) convertView.findViewById(R.id.ivRatingImage);
            holder.tvComments = (TextView) convertView.findViewById(R.id.tvComments);
            holder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            holder.tvRating = (TextView) convertView.findViewById(R.id.tvRating);
            holder.ivDishImage = (ImageView) convertView.findViewById(R.id.ivDishImage);
            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();

        Rating rating = getItem(position);

        // Load Profile image using Picasso
        //holder.ivDishImage.setImageResource(0);

        ParseUser user = rating.getUser();
        if (user != null) {
            String userName = user.get("appUserName").toString();
            holder.tvUserName.setText(userName);
        }
        else {
            holder.tvUserName.setText("Harry Potter");
        }

        holder.tvComments.setText(rating.getComments());

        holder.tvRating.setText(String.valueOf(rating.getStars()));

        holder.ivRatingImage.setImageResource(0);
        if (rating.getStars() > 2) {
            holder.ivRatingImage.setImageResource(R.drawable.good);
        }
        else if (rating.getStars() > 1) {
            holder.ivRatingImage.setImageResource(R.drawable.meh);
        }
        else {
            holder.ivRatingImage.setImageResource(R.drawable.bad);
        }

        Dish dish = rating.getDish();

        // TODO: see if we have Dish image that user posted
        final String image = (dish != null) ? dish.getImage() : null;
        if (image != null) {


            holder.ivDishImage.setImageResource(0);

            Picasso.with(getContext())
                    .load(image)
                    .into(holder.ivDishImage);
        }

        return convertView;
    }

    public class ViewHolder {
        TextView tvUserName;
        TextView tvComments;
        TextView tvRating;
        ImageView ivRatingImage;
        ImageView ivDishImage;
    }
}
