package com.projectspeedracer.thefoodapp.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.projectspeedracer.thefoodapp.utils.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParseClassName("Dishes")
public class Dish extends ParseObject {
	public static final String ENABLED  = "1";
	public static final String DISABLED = "0";

	private List<Rating> ratings = new ArrayList<>();

	public void setRatings(Rating... ratings) {
		Collections.addAll(this.ratings, ratings);
	}

	public String getId() {
		return getString(Fields.ID);
	}

	public void setId(String id) {
		Helpers.EnsureNotBlank(id, "Null or empty dish id");
		put(Fields.ID, id);
	}

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

	/*public String[] getImages() {
		final List<String> images = getList(Fields.IMAGES);
		return images.toArray(new String[images.size()]);
	}

	public void setImages(String[] images) {
		put(Fields.IMAGES, images == null ? new String[0] : images);
	}*/

	public double getPrice() {
		return getDouble(Fields.PRICE);
	}

	public void setPrice(double price) {
		Helpers.EnsurePositive(price, "Expected positive price value");
		put(Fields.PRICE, price);
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
	}
}