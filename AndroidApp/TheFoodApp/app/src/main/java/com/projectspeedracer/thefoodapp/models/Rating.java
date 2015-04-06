package com.projectspeedracer.thefoodapp.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.projectspeedracer.thefoodapp.utils.FoodAppUtils;
import com.projectspeedracer.thefoodapp.utils.Helpers;

import org.apache.commons.lang3.StringUtils;

@ParseClassName("Ratings")
public class Rating extends ParseObject {

    public Rating() {
    }

    public int getId() {
		return getInt(Fields.ID);
	}

	public void setId(int rid) {
		put(Fields.ID, rid);
	}

    public Dish getDish() {
        // expects 'include("dish")' to be done in initial query
        return (Dish) getParseObject(Fields.DISH);
    }

    public void setDish(Dish dish) {
        put(Fields.DISH, dish);
    }

    public Restaurant getRestaurant() {
        // expects 'include("restaurants")' to be done in initial query
        return (Restaurant) getParseObject(Fields.RESTAURANT);
    }

    public void setRestaurant(Restaurant restaurant) {
        put(Fields.RESTAURANT, restaurant);
    }

    public ParseUser getUser() {
        return getParseUser(Fields.USER);
    }

    public void setUser(ParseUser user) {
        put(Fields.USER, user);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(Fields.LOCATION);
    }

    public void setLocation(ParseGeoPoint location) {
        put(Fields.LOCATION, location);
    }

	public int getHostId() {
		return getInt(Fields.HOST_ID);
	}

	public void setHostId(int hostId) {
		put(Fields.HOST_ID, hostId);
	}

	public HostType getHostType() {
		return (HostType) get(Fields.HOST_TYPE);
	}

	public void setHostType(HostType type) {
		put(Fields.HOST_TYPE, type);
	}

	public String getName() {
		return getString(Fields.NAME);
	}

	public void setName(String name) {
		Helpers.EnsureNotBlank(name, "Rating name cannot be empty");
		put(Fields.NAME, name);
	}

	public int getStars() {
		return getInt(Fields.STARS);
	}

	public void setStars(int stars) {
//		if (stars < -1) { stars = -1; }
//		if (stars > 1) { stars = 1; }

		put(Fields.STARS, stars);
	}

	public String getComments() {
		return getString(Fields.COMMENTS)
;	}

	public void setComments(String comments) {
		put(Fields.COMMENTS, comments);
	}

	public static enum HostType {
		DISH(1), RESTAURANT(2);

		private int type;

		private HostType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

		@Override public String toString() {
			switch (type) {
				case 1: return "DISH";
				case 2: return "RESTAURANT";
				default: return "";
			}
		}
	}

	public static class Fields {
		public static final String ID        = "rid";
		public static final String NAME      = "NAME";
		public static final String STARS     = "stars";
		public static final String COMMENTS  = "COMMENTS";
		public static final String HOST_ID   = "restaurant_id";
		public static final String HOST_TYPE = "host_type";
        public static final String DISH       = "dish";
        public static final String RESTAURANT = "restaurant";
        public static final String USER       = "user";
        public static final String LOCATION   = "location";
	}

    private static final String[] GoodMsgSpec = {
            " loved %s",
            " %s was awesome!!!"
    };

    private static final String[] OkayMsgSpec = {
            " found %s to be okay...",
            " says %s is not bad"
    };

    private static final String[] BadMsgSpec = {
            " says %s is not so good",
            " says %s was disappointing"
    };

    private static String[] ChooseMessageSpec(int stars) {
        if (stars > 2) { return GoodMsgSpec; }
        if (stars > 1) { return OkayMsgSpec; }
        return BadMsgSpec;
    }

    public String generateExpression(String userName) {
        final String[] msgSpec = ChooseMessageSpec(getStars());
        final int index = FoodAppUtils.GetRandomInt(0, msgSpec.length - 1);
        final String expression = String.format(msgSpec[index], getDish().getName());
        return (StringUtils.isNoneBlank(userName) ? userName : "") + expression;
    }
}
