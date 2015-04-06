package com.projectspeedracer.thefoodapp.models;

import android.util.Log;
import android.widget.ImageView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.projectspeedracer.thefoodapp.R;
import com.projectspeedracer.thefoodapp.utils.Constants;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import com.projectspeedracer.thefoodapp.utils.ParseRelationNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ParseClassName("Dishes")
public class Dish extends DeserializableParseObject {
	public static final String ENABLED  = "1";
	public static final String DISABLED = "0";

	@JsonIgnore
	private List<Rating> ratings = new ArrayList<>();

	public Dish() {
	}

	public void update(Dish other) {
		setName(other.getName());
		setRestaurantId(other.getRestaurantId());
		setCategory(other.getCategory());
		setDescription(other.getDescription());
		setImage(other.getImage());
		setPrice(other.getPrice());
		setAverageRating(other.getAverageRating());
		setCalories(other.getCalories());
		setGlutenFree(other.getGlutenFree());
		setHalal(other.isHalal());
		setVegan(other.isVegan());
		setVegetarian(other.isVegetarian());
		setEnabled(other.isEnabled());
	}

	public List<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(Collection<Rating> ratings) {
		if (ratings == null || ratings.size() == 0) { return; }

		this.ratings.clear();

		for (Rating r : ratings) {
			if (r == null) { continue; }
			this.ratings.add(r);
		}

		updateAverageRating();
	}

	public void addRatings(Rating... ratings) {
		if (ratings == null || ratings.length == 0) { return; }

		for (Rating r : ratings) {
			if (r == null) { continue; }
			this.ratings.add(r);
		}

		updateAverageRating();
	}

	public float calculateAverageRating() {
		if (ratings.size() == 0) {
			return 0;
		}

		int totalStars = 0;

		for (Rating r : ratings) {
			totalStars += r.getStars();
		}

		final float average = (float) totalStars / (float) ratings.size();
		final boolean fishy = average < 1 && average > 3;
		final float safeAverage = fishy ? 2 : average;
		Log.i(Constants.TAG, String.format("Ratings Average: %s (%s)", safeAverage, fishy ? "FISHY AVERAGE" : "√"));

		final String msg = String.format("[%s] Total Stars: %s. Num: %s. Average: %s",
				getName(),
				totalStars,
				ratings.size(),
				safeAverage);

		Log.i(Constants.TAG, msg);

		return safeAverage;
	}

	public void fetchRatings(FindCallback<Rating> callback) {

		final ParseRelation<Rating> relationDish = this.getRelation(ParseRelationNames.DishToPosts);
		final ParseQuery<Rating> query = relationDish.getQuery();

		query.include(Rating.Fields.USER);
		query.include(Rating.Fields.DISH);
		query.orderByDescending(Fields.CREATED_AT);

		// TODO: add 7 days constraint !!!
		query.findInBackground(callback);
	}

	private void updateAverageRating() {
		final double averageRating = calculateAverageRating();
		setAverageRating(averageRating);
	}

	// region Serializable Getters and Setters

	public String getName() {
		return getString(Fields.NAME);
	}

	public void setName(String name) {
		put(Fields.NAME, name == null ? "???" : name);
	}

	public int getRestaurantId() {
		return getInt(Fields.RESTAURANT_ID);
	}

	public void setRestaurantId(int restaurantId) {
		put(Fields.RESTAURANT_ID, restaurantId);
	}

	public String getDescription() {
		return getString(Fields.DESCRIPTION);
	}

	public void setDescription(String description) {
		put(Fields.DESCRIPTION, description == null ? "" : description);
	}

	public String getImage() {
		return getString(Fields.IMAGE_URL);
	}

	public void setImage(String imageUrl) {
		put(Fields.IMAGE_URL, imageUrl);
	}

	public double getPrice() {
		return getDouble(Fields.PRICE);
	}

	public void setPrice(double price) {
		Helpers.EnsurePositive(price, "Expected positive price value");
		put(Fields.PRICE, price);
	}

	public double getAverageRating() {
		return getDouble(Fields.AVERAGE_RATING);
	}

	public void setAverageRating(double averageRating) {
		put(Fields.AVERAGE_RATING, averageRating);
	}

	public String getCalories() {
		return getString(Fields.CALORIES);
	}

	public void setCalories(String calories) {
		put(Fields.CALORIES, calories == null ? "" : calories);
	}

	public boolean getGlutenFree() {
		return getBoolean(Fields.GLUTEN_FREE);
	}

	public void setGlutenFree(boolean glutenFree) {
		put(Fields.GLUTEN_FREE, glutenFree)
		;
	}

	public boolean isHalal() {
		return getBoolean(Fields.HALAL);
	}

	public void setHalal(boolean halal) {
		put(Fields.HALAL, halal);
	}

	public boolean isVegan() {
		return getBoolean(Fields.VEGAN);
	}

	public void setVegan(boolean vegan) {
		put(Fields.VEGAN, vegan);
	}

	public boolean isVegetarian() {
		return getBoolean(Fields.VEGETARIAN);
	}

	public void setVegetarian(boolean vegetarian) {
		put(Fields.VEGETARIAN, vegetarian);
	}

	public String getCategory() {
		return getString(Fields.CATEGORIES);
	}

	public void setCategory(String category) {
		put(Fields.CATEGORIES, category);
	}

	public boolean isEnabled() {
		return getBoolean(Fields.ENABLED);
	}

	public void setEnabled(boolean enabled) {
		put(Fields.ENABLED, enabled);
	}

	// endregion

	public static class Fields {
		public static final String ID            = "id";
		public static final String NAME          = "name";
		public static final String RESTAURANT_ID = "restaurantId";
		public static final String DESCRIPTION   = "description";
		public static final String PRICE         = "price";
		public static final String IMAGES        = "images";
		public static final String CATEGORIES    = "category";
		public static final String CALORIES      = "calories";
		public static final String GLUTEN_FREE   = "gluten_free";
		public static final String HALAL         = "halal";
		public static final String VEGAN         = "vegan";
		public static final String VEGETARIAN    = "vegetarian";
		public static final String ENABLED       = "enabled";
		public static final String IMAGE_URL     = "imageUrl";
		public static final String CREATED_AT    = "createdAt";
        public static final String AVERAGE_RATING = "averageRating";
	}

    /*public String getRatingDescription() {
        final double averageRating = getAverageRating();
        if (averageRating >= 2.5f) {
            return "I loved it!";
        } else if (averageRating >= 2f) {
            return "It was excellent";
        } else if (averageRating >= 1.5f) {
            return "It was ok";
        } else if (averageRating >= 1f) {
            return "It was disappointing";
        } else {
            return "No information";
        }
    }*/

    public int getRatingIconResID() {
        final double averageRating = getAverageRating();
        if (averageRating >= 2.5f) {
            return R.drawable.good;
        } else if (averageRating >= 1.5f) {
            return R.drawable.meh;
        } else if (averageRating >= 1f) {
            return R.drawable.bad;
        } else {
            return R.drawable.meh;
        }
    }

}