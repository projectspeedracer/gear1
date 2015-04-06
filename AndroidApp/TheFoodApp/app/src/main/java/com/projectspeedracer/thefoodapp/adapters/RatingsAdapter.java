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
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by avkadam on 4/4/15.
 */
public class RatingsAdapter extends ArrayAdapter<Rating> {
    public RatingsAdapter(Context context, List<Rating> ratings) {
        super(context, R.layout.item_dish_post, ratings);
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
            holder.tvTimeAgo = (TextView) convertView.findViewById(R.id.tvTimeAgo);
            holder.ivDishImage = (ImageView) convertView.findViewById(R.id.ivDishImage);
            convertView.setTag(holder);
        }

        holder = (ViewHolder) convertView.getTag();

        Rating rating = getItem(position);

        // Load Profile image using Picasso
        //holder.ivDishImage.setImageResource(0);

        ParseUser user = rating.getUser();
        String userName = "Harry Potter";
        if (user != null) {
            userName = user.get("appUserName").toString();
            holder.tvUserName.setText(userName);
        }


        holder.tvComments.setText(rating.getComments());

        holder.tvTimeAgo.setText(FoodAppUtils.getRelativeTimeAgo(rating.getCreatedAt().toString()));

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

        final Dish dish = rating.getDish();
        final String image = (dish != null) ? dish.getImage() : null;

        if (dish != null) {
            String expressiveMessage = rating.generateExpression(userName);
            holder.tvUserName.setText(expressiveMessage);
        }


        if (StringUtils.isNotBlank(image)) {
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
        TextView tvTimeAgo;
        ImageView ivRatingImage;
        ImageView ivDishImage;
    }
}
