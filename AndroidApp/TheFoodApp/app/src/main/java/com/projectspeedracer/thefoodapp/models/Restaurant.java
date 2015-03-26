package com.projectspeedracer.thefoodapp.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.projectspeedracer.thefoodapp.utils.Helpers;

@ParseClassName("Restaurants")
public class Restaurant extends ParseObject {

	public String getEmail() {
		return getString(Fields.EMAIL);
	}

	public void setEmail(String email) {
		Helpers.EnsureNotBlank(email, "Invalid email address");
		put(Fields.EMAIL, email);
	}

	public String getContactPerson() {
		return getString(Fields.CONTACT_PERSON);
	}

	public void setContactPerson(String contactPerson) {
		put(Fields.CONTACT_PERSON, contactPerson == null ? "" : contactPerson);
	}

	public Address getAddress() {
		return (Address) get(Fields.ADDRESS);
	}

	public void setAddress(Address address) {
		put(Fields.ADDRESS, address);
	}

	public String getLatitude() {
		return getString(Fields.LATITUDE);
	}

	public void setLatitude(String latitude) {
		put(Fields.LATITUDE, latitude);
	}

	public String getLocationId() {
		return getString(Fields.LOCATION_ID);
	}

	public void setLocationId(String locationId) {
		put(Fields.LOCATION_ID, locationId);
	}

	public String getLongitude() {
		return getString(Fields.LONGITUDE);
	}

	public void setLongitude(String longitude) {
		put(Fields.LONGITUDE, longitude);
	}

	public String getName() {
		return getString(Fields.NAME);
	}

	public void setName(String restaurant_name) {
		put(Fields.NAME, restaurant_name);
	}

	public String getWebsiteUrl() {
		return getString(Fields.URL);
	}

	public void setWebsiteUrl(String url) {
		put(Fields.URL, url);
	}

	public String getPhone() {
		return getString(Fields.PHONE);
	}

	public void setPhone(String phone) {
		put(Fields.PHONE, phone);
	}

	public String getDescription() {
		return getString(Fields.DESCRIPTION);
	}

	public void setDescription(String brief_description) {
		put(Fields.DESCRIPTION, brief_description);
	}

	public String getBusinessType() {
		return getString(Fields.BUSINESS_TYPE);
	}

	public void setBusinessType(String businessType) {
		put(Fields.BUSINESS_TYPE, businessType);
	}

	public class Address {
		public String address1;
		public String address2;
		public String city;
		public String state;
		public String zip;
		public String country = "US";
	}

	public static class Fields {
		public static final String NAME           = "name";
		public static final String EMAIL          = "email";
		public static final String CONTACT_PERSON = "contact_person";
		public static final String BUSINESS_TYPE  = "business_type";
		public static final String LATITUDE       = "latitude";
		public static final String LONGITUDE      = "longitude";
		public static final String LOCATION_ID    = "location_id";
		public static final String URL            = "website_url";
		public static final String DESCRIPTION    = "description";
		public static final String PHONE          = "phone";
		public static final String ADDRESS        = "ADDRESS";
	}
}

/*"restaurant_info": {
        "address_1": "803 Gervais St.",
        "address_2": "",
        "brief_description": "Sample restaurant on OpenMenu. From this powerful website, to mobile, to Facebook, a restaurant's OpenMenu can power your entire online presence.",
        "business_type": "independent",
        "city_town": "Columbia",
        "country": "US",
        "fax": "(555) 888-8888",
        "full_description": "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Vestibulum ac libero neque, quis laoreet dolor. Proin vitae erat lacus. \r\n\t \r\nAliquam sed lectus ligula, sed pharetra tortor. Nam rutrum ipsum ut quam vestibulum vestibulum. Ut posuere rhoncus quam, id semper ligula bibendum quis. Vestibulum accumsan, neque id tristique accumsan, risus diam vehicula massa, at facilisis ligula tortor et elit. Nulla facilisi. Mauris ultrices volutpat lorem eu convallis.",
        "latitude": "34.000146",
        "location_id": "x12345",
        "longitude": "-81.038241",
        "mobile": "0",
        "omf_file_url": "http://openmenu.com/menu/sample",
        "omf_private": "0",
        "phone": "(555) 777-7777",
        "postal_code": "29202",
        "restaurant_name": "My Restaurant",
        "state_province": "SC",
        "utc_offset": "-5.00",
        "website_url": "http://openmenu.com"
    }*/
