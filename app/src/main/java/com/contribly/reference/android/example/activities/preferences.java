package com.contribly.reference.android.example.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.contribly.reference.android.example.R;

public class preferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
