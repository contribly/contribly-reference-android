package com.contribly.reference.android.example.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.contribly.client.api.AssignmentApi;
import com.contribly.client.api.AuthApi;
import com.contribly.client.api.ContributionApi;
import com.contribly.client.api.MediaApi;

public class ApiFactory {	// Factory class for producing configured API instances.
	
	private static final String TAG = "ApiFactory";

	public static final String apiUrl = "https://api.contribly.com/1";

	public static AuthApi getAuthApi(Context context) {
		AuthApi authApi = new AuthApi();
		authApi.getApiClient().setBasePath(apiUrl);
		return authApi;
	}

	public static AssignmentApi getAssignmentApi(Context context) {
		AssignmentApi assignmentApi = new AssignmentApi();
		assignmentApi.getApiClient().setBasePath(apiUrl);
		return assignmentApi;
	}

	public static ContributionApi getContributionApi(Context context) {
		ContributionApi contributionApi = new ContributionApi();
		contributionApi.getApiClient().setBasePath(apiUrl);
		return contributionApi;
	}

	public static MediaApi getMediaApi(Context context) {
		MediaApi mediaApi = new MediaApi();
		mediaApi.getApiClient().setBasePath(apiUrl);
		return mediaApi;
	}

	public static String consumerKey(Context context) {	// TODO Not really in the correct place; indicates that settings need to be abstracted somewhere sensible.
		return getPreferenceValue(context, "consumerKey", "");
	}

	public static String consumerSecret(Context context) {	// TODO Not really in the correct place; indicates that settings need to be abstracted somewhere sensible.
		return getPreferenceValue(context, "consumerSecret", "");
	}

	public static String ownedBy(Context context) {	// TODO Not really in the correct place; indicates that settings need to be abstracted somewhere sensible.
		return getPreferenceValue(context, "ownedBy", "");
	}
	
	private static String apiUrl(Context context) {
		return getPreferenceValue(context, "apiUrl", apiUrl);
	}
		
	private static String getPreferenceValue(Context context, String key, String defaultValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String value = prefs.getString(key, defaultValue);
		Log.i(TAG, "Configuration preference " + key + " is: " + value);
		return value;
	}

}
