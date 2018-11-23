package com.contribly.reference.android.example.activities;

import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.api.ApiFactory;
import com.contribly.reference.android.example.services.LoggedInUserService;

import com.contribly.client.ApiException;
import com.contribly.client.api.AuthApi;
import com.contribly.client.model.Authority;
import com.contribly.client.model.User;

public class profile extends BaseActivity {

	private static final String TAG = "profile";

	private LoggedInUserService loggedInUserService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		loggedInUserService = LoggedInUserService.getInstance();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(1);

		String signedInUsersAccessToken = loggedInUserService.getLoggedInUsersAccessToken();

		// If we have an access token for a user we can consider them to be signed in.
		boolean isSignedIn = signedInUsersAccessToken != null;

		final TextView loginStatusTextView = (TextView) findViewById(R.id.loginstatus);
		if (isSignedIn) {
			loginStatusTextView.setText("Signed in with token: " + signedInUsersAccessToken);

			// To retrieve the details of the signed in user we can use out access token to make an OAuth2 authenticiated call to the API /verify endpoint.
			AuthApi authApi = ApiFactory.getAuthApi(this);
			authApi.getApiClient().setAccessToken(signedInUsersAccessToken);
			new FetchLoggedInUserDetailsTask(authApi).execute();
			
		} else {
			loginStatusTextView.setText("Not signed in");
			populateUserDetails(null);
		}
	}

	private void populateUserDetails(User user) {
		final TextView headlineTextView = (TextView) findViewById(R.id.username);
		final TextView bioTextView = (TextView) findViewById(R.id.bio);
		
		if (user != null) {			
			headlineTextView.setText(user.getDisplayName());
			headlineTextView.setVisibility(View.VISIBLE);

            if (user.getBio() != null) {
                bioTextView.setText(user.getBio());
                bioTextView.setVisibility(View.VISIBLE);
            }

		} else {
			headlineTextView.setVisibility(View.GONE);
			bioTextView.setVisibility(View.GONE);	
		}
	}
	
	private class FetchLoggedInUserDetailsTask extends AsyncTask<String, Integer, User> {
		
		private AuthApi authApi;
		
		public FetchLoggedInUserDetailsTask(AuthApi authApi) {
			super();
			this.authApi = authApi;
		}

		@Override
		protected User doInBackground(String... params) {			
			try {
				Authority authority = authApi.verifyPost();
				User user = authority.getUser();
				Log.i(TAG, "Signed in user's access token verified as: " + user);
				return user;

			} catch (ApiException e) {
				Log.w(TAG, "Verify call failed: " + e.getCode() + " / " + e.getResponseBody());
				return null;

			} catch (Exception e) {
				Log.e(TAG, "Verify call failed", e);
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(User user) {
			populateUserDetails(user);
		}
		
	}

}