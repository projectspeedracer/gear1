package com.projectspeedracer.thefoodapp.utils;

public interface Transformer<T, U> {
	U transform(T item);
}