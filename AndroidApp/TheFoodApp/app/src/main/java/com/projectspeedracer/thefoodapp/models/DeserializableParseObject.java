package com.projectspeedracer.thefoodapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.parse.ParseACL;
import com.parse.ParseObject;

public class DeserializableParseObject extends ParseObject {
	@JsonIgnore
	@Override public String getObjectId() {
		return super.getObjectId();
	}

	public void setObjectId(String id) {
		super.setObjectId(id);
	}

	@JsonIgnore
	@Override public ParseACL getACL() {
		return super.getACL();
	}

	public void setACL(ParseACL acl) {
		super.setACL(acl);
	}

	@JsonIgnore
	@Override public boolean isDirty() {
		return super.isDirty();
	}

	@JsonIgnore
	@Override public boolean isDataAvailable() {
		return super.isDataAvailable();
	}
}
