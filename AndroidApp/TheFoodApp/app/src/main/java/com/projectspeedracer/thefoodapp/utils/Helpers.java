package com.projectspeedracer.thefoodapp.utils;

import org.apache.commons.lang3.StringUtils;

public class Helpers {
	public static <T> void EnsureNotNull(T obj, String message) {
		if (obj == null) {
			throw new NullPointerException(message);
		}
	}

	public static void EnsureNotBlank(String text, String message) {
		if (StringUtils.isBlank(text)) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void EnsurePositive(double d, String message) {
		if (d < 0) {
			final String errorText = StringUtils.isBlank(message)
					? "Expected positive value. Found " + String.valueOf(d)
					: message;

			throw new IllegalArgumentException(errorText);
		}
	}
}