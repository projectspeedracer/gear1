package com.projectspeedracer.thefoodapp.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.projectspeedracer.thefoodapp.utils.Helpers;

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
		if (stars < -1) { stars = -1; }
		if (stars > 1) { stars = 1; }

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
	}
}
