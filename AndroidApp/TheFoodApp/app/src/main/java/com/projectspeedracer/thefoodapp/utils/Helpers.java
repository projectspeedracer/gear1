package com.projectspeedracer.thefoodapp.utils;

import android.location.Location;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.parse.ParseGeoPoint;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helpers {

	public static void EnsureTruth(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

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
	
	public static <T, G> Map<G, List<T>> GroupBy(Collection<T> items, Transformer<T, G> transformer) {
		final Map<G, List<T>> map = new HashMap<>();

		for (T item : items) {
			final G group = transformer.transform(item);

			if (!map.containsKey(group)) {
				final List<T> values = new ArrayList<>();
				values.add(item);
				map.put(group, values);
			} else {
				map.get(group).add(item);
			}
		}

		return map;
	}

	public static Location ToLocation(ParseGeoPoint pgp) {
        if (pgp == null) {
            return null;
        }

		final Location location = new Location("");
		location.setLatitude(pgp.getLatitude());
		location.setLongitude(pgp.getLongitude());
		return location;
	}

	public static ParseGeoPoint ToParseGeoPoint(Location location) {
		return new ParseGeoPoint(location.getLatitude(), location.getLongitude());
	}

	public static <T> String AsJson(T object) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
		mapper.enable(SerializationFeature.WRITE_NULL_MAP_VALUES);

		if (!mapper.canSerialize(object.getClass())) {
			return null;
		}

		final ObjectWriter writer = mapper.writer();
		return writer.writeValueAsString(object);
	}

	public static <T> T FromJsonSafe(String json, Class<T> clazz) {
		try {
			return FromJson(json, clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static <T> T FromJson(String json, Class<T> clazz) throws IOException {
		if (StringUtils.isBlank(json)) { return null; }
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		return mapper.readValue(json, clazz);
	}
}
