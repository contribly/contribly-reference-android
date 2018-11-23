package com.contribly.reference.android.example.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonParser;
import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.api.ApiFactory;
import com.contribly.reference.android.example.services.LoggedInUserService;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class signin extends BaseActivity {

    private static final String TAG = "signin";

    private LoggedInUserService loggedInUserService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        loggedInUserService = LoggedInUserService.getInstance();

        Button button = (Button) findViewById(R.id.signInButton);
        button.setOnClickListener(signInButtonClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private OnClickListener signInButtonClickListener = new OnClickListener() {
        public void onClick(View v) {
            postSignRequest();
        }
    };

    private void postSignRequest() {
        final String username = ((EditText) findViewById(R.id.username)).getText().toString();
        final String password = ((EditText) findViewById(R.id.password)).getText().toString();

        new AuthenticateUsernameAndPasswordTask().execute(username, password, ApiFactory.consumerKey(this), ApiFactory.consumerSecret(this));
    }

    private void persistAccessTokenAndRedirectToProfileActivity(String token) {
        loggedInUserService.setLoggedInUsersAccessToken(token);
        startActivity(new Intent(this, assignments.class));    // TODO no history on this step.
    }

    private class AuthenticateUsernameAndPasswordTask extends AsyncTask<String, Integer, String> {

        public AuthenticateUsernameAndPasswordTask() {
            super();
        }

        @Override
        protected String doInBackground(String... params) {
            final String username = params[0];
            final String password = params[1];
            final String consumerKey = params[2];
            final String consumerSecret = params[3];

            // To sign in a user we submit their credentials to the API with an OAuth2 password grant request. We should receive a Contribly API access token in exchange.
            Log.i(TAG, "Attempting password grant call for username: " + username);

            // Swagger code gen doesn't currently provide auto generated grant calls so we'll compose this request manually.
            try {
                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(buildPasswordGrantRequest(username, password, consumerKey, consumerSecret)).execute();
                if (response.code() == 200) {
                    String body = response.body().string();
                    Log.i(TAG, "Token response: " + body);

                    // Extract access token from grant reply
                    String token = new JsonParser().parse(body).getAsJsonObject().get("access_token").getAsString();
                    Log.i(TAG, "Got access token: " + token);

                    // This Contribly access token allows us to perform authenticated actions as this user.
                    // Our app should retain this access token and can now consider itself signed in as that user.
                    return token;

                } else {
                    Log.w(TAG, "Failed token response: " + response.code() + " / " + response.body().string());
                    return null;
                }

            } catch (IOException e) {
                Log.e(TAG, "Password grant request failed", e);
                return null;
            }

        }

        private Request buildPasswordGrantRequest(String username, String password, String consumerKey, String consumerSecret) {
            RequestBody formBody = new FormEncodingBuilder().
                    add("grant_type", "anonymous").
                    build();

            String clientAuth = "Basic " + Base64.encodeToString((consumerKey + ":" + consumerSecret).getBytes(), Base64.NO_WRAP);
            Log.i(TAG, "Auth header: " + clientAuth);
            return new Request.Builder().url(ApiFactory.apiUrl + "/token").addHeader("Authorization", clientAuth).post(formBody).build();
        }

        @Override
        protected void onPostExecute(String token) {
            persistAccessTokenAndRedirectToProfileActivity(token);
        }

    }

}