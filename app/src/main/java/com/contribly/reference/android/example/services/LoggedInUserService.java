package com.contribly.reference.android.example.services;

import android.util.Log;

public class LoggedInUserService {	// Replace with your preferred method of storing the logged in users access token locally.
	
	private static final String TAG = "LoggedInUserService";

	private static LoggedInUserService instance;
	
	private static String accessToken;
	
    public synchronized static LoggedInUserService getInstance() {	// Attempting to implement an in-memory key store. The Singleton pattern is not reliable in production!
    	if (instance == null) {
    		instance = new LoggedInUserService();
    	}
    	return instance;
    }
	
	public String getLoggedInUsersAccessToken() {
		return accessToken;
	}
	
	public void setLoggedInUsersAccessToken(String newAccessToken) {
		accessToken = newAccessToken;
		Log.i(TAG, "Logged in user access token set");
	}

	public void clearLoggedInUsersAccessToken() {
		accessToken = null;
	}

}
