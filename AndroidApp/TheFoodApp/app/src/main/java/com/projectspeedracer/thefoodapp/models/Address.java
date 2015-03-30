package com.projectspeedracer.thefoodapp.models;

import com.parse.ParseObject;

import org.json.JSONArray;

public class Address extends ParseObject {

	private String text;

	@Override public String toString() {
		return text;
	}

	public static Address From(String text) {
		// TODO: Construct address object from pre-formatted text
		final Address address = new Address();
		address.text = text == null ? "" : text;
		return address;
	}

	public static Address From(JSONArray array) {
		return null;
		/*"address_components" : [
			{
				"long_name" : "1380",
					"short_name" : "1380",
					"types" : [ "street_number" ]
			},
			{
				"long_name" : "South Main Street",
					"short_name" : "S Main St",
					"types" : [ "route" ]
			},
			{
				"long_name" : "Milpitas",
					"short_name" : "Milpitas",
					"types" : [ "locality", "political" ]
			},
			{
				"long_name" : "California",
					"short_name" : "CA",
					"types" : [ "administrative_area_level_1", "political" ]
			},
			{
				"long_name" : "United States",
					"short_name" : "US",
					"types" : [ "country", "political" ]
			},
			{
				"long_name" : "95035",
					"short_name" : "95035",
					"types" : [ "postal_code" ]
			}
		]*/
	}

	public String getAddress1() {
		return getString(Fields.ADDRESS1);
	}

	public void setAddress1(String address1) {
		put(Fields.ADDRESS1, address1);
	}

	public String getAddress2() {
		return getString(Fields.ADDRESS2);
	}

	public void setAddress2(String address2) {
		put(Fields.ADDRESS1, address2);
	}

	public String getCity() {
		return getString(Fields.CITY);
	}

	public void setCity(String city) {
		put(Fields.CITY, city);
	}

	public String getState() {
		return getString(Fields.STATE);
	}

	public void setState(String state) {
		put(Fields.STATE, state);
	}

	public String getZip() {
		return getString(Fields.ZIP);
	}

	public void setZip(String zip) {
		put(Fields.ZIP, zip);
	}

	public String getCountry() {
		return getString(Fields.COUNTRY);
	}

	public void setCountry(String country) {
		put(Fields.COUNTRY, country);
	}

	public static class Fields {
		public static final String ADDRESS1 = "address1";
		public static final String ADDRESS2 = "address2";
		public static final String CITY = "city";
		public static final String STATE = "state";
		public static final String ZIP = "zip";
		public static final String COUNTRY = "country";
	}
}