package com.contribly.reference.android.example.utils;

public class Plurals {

	public static String getPrural(String word, int count) {
		if (count != 1) {
			return word + "s";
		}
		return word;
	}

}
