package com.contribly.reference.android.example.activities;

import android.content.Intent;
import android.os.Bundle;

import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.services.LoggedInUserService;

public class signout extends BaseActivity {
	
	private LoggedInUserService loggedInUserService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signin);		
		loggedInUserService = LoggedInUserService.getInstance();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// The Contribly API is stateless meaning clearing the locally stored access token is enough to sign the user our.
		// No server call is required.
		loggedInUserService.clearLoggedInUsersAccessToken();
		
		redirectToProfileActivity();
	}
	
	private void redirectToProfileActivity() {
		startActivity(new Intent(this, profile.class));	// TODO no history on this step.	
	}
	
}