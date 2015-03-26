package com.projectspeedracer.thefoodapp.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.projectspeedracer.thefoodapp.utils.Helpers;
import java.util.List;

@ParseClassName("Dish")
public class Dish extends ParseObject {
	public static final String ENABLED  = "1";
	public static final String DISABLED = "0";

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

	public String getDescription() {
		return getString(Fields.DESCRIPTION);
	}

	public void setDescription(String description) {
		put(Fields.DESCRIPTION, description == null ? "" : description);
	}

	public String[] getImages() {
		final List<String> images = getList(Fields.IMAGES);
		return images.toArray(new String[images.size()]);
	}

	public void setImages(String[] images) {
		put(Fields.IMAGES, images == null ? new String[0] : images);
	}

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

	public boolean isEnabled() {
		return getBoolean(Fields.ENABLED);
	}

	public void setEnabled(boolean enabled) {
		put(Fields.ENABLED, enabled);
	}

	public static class Fields {
		public static final String ID          = "id";
		public static final String NAME        = "name";
		public static final String DESCRIPTION = "description";
		public static final String PRICE       = "price";
		public static final String IMAGES      = "images";
		public static final String CALORIES    = "calories";
		public static final String GLUTEN_FREE = "gluten_free";
		public static final String HALAL       = "halal";
		public static final String VEGAN       = "vegan";
		public static final String VEGETARIAN  = "vegetarian";
		public static final String ENABLED     = "enabled";
	}
}

/*"menu_item_description": "Our calamari is lightly fried and tossed with a sweet and spicy Asian mango sauce.",
"menu_item_heat_index": "2",
"menu_item_images": [],
"menu_item_name": "Calamari",
"menu_item_options": [],
"menu_item_price": null,
"menu_item_sizes": [
	{
		"menu_item_size_calories": null,
		"menu_item_size_description": "",
		"menu_item_size_name": "small",
		"menu_item_size_price": "6.95"
	},
	{
		"menu_item_size_calories": null,
		"menu_item_size_description": "",
		"menu_item_size_name": "large",
		"menu_item_size_price": "8.95"
	}
],
"menu_item_tags": [],
"special": "1",
"vegan": null,
"vegetarian": null*/