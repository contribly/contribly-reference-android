package com.contribly.reference.android.example.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.contribly.client.ApiException;
import com.contribly.client.api.ContributionApi;
import com.contribly.client.api.MediaApi;
import com.contribly.client.model.Contribution;
import com.contribly.client.model.Media;
import com.contribly.client.model.MediaUsage;
import com.contribly.reference.android.example.api.ApiFactory;
import com.google.gson.JsonParser;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class ContributionPostingService extends IntentService {

	public static final String NEW_CONTRIBUTION = "newContribution";
	public static final String MEDIA = "media";

	private static final String TAG = "ContributionPosting";

	public ContributionPostingService() {
		super("");
	}

	public ContributionPostingService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Unmarshall the contribution details past to us from the contribute activity
		final Bundle extras = intent.getExtras();
		final Contribution newContribution = (Contribution) extras.getSerializable(NEW_CONTRIBUTION);
		final String mediaUriString = (String) extras.getSerializable(MEDIA);
		final Uri media = mediaUriString != null ? Uri.parse(mediaUriString) : null;

		Log.i(TAG, "Received contribution to post: " + newContribution + " / media: " + media);

		// Obtain an anonymous access token to use for this submission
        final String consumerKey = ApiFactory.consumerKey(this);
        final String consumerSecret = ApiFactory.consumerSecret(this);
		String accessToken = getAnonymousAccessToken(consumerKey, consumerSecret);

		// Submit the contribution.
		// If the contribution includes media then we need to submit it to the media endpoint before referencing the resulting media element in a contribution media usage.
		MediaUsage mediaUsage = media != null ? submitMedia(media, accessToken) : null;
		if (mediaUsage != null) {
			newContribution.getMediaUsages().add(mediaUsage);
		}

		Log.i(TAG, "Posting contribution: " + newContribution);
		Contribution submittedContribution = submitContribution(newContribution, accessToken);

		// Notify the user that their contribution has been submitted
		afterPost(submittedContribution);

		return;
	}

	private String getAnonymousAccessToken(String consumerKey, String consumerSecret) {
        // Swagger code gen doesn't currently provide auto generated grant calls so we'll compose this request manually.
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(buildPasswordGrantRequest(consumerKey, consumerSecret)).execute();
            if (response.code() == 200) {
                String body = response.body().string();
                Log.i(TAG, "Token response: " + body);

                // Extract access token from grant reply
                String token = new JsonParser().parse(body).getAsJsonObject().get("access_token").getAsString();
                Log.i(TAG, "Got access token: " + token);

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

    private Request buildPasswordGrantRequest(String consumerKey, String consumerSecret) {
        RequestBody formBody = new FormEncodingBuilder().
                add("grant_type", "anonymous").
                build();

        String clientAuth = "Basic " + Base64.encodeToString((consumerKey + ":" + consumerSecret).getBytes(), Base64.NO_WRAP);
        return new Request.Builder().url(ApiFactory.apiUrl + "/token").addHeader("Authorization", clientAuth).post(formBody).build();
    }

	private MediaUsage submitMedia(Uri mediaUri, String accessToken) {
		Log.i(TAG, "Posting media: " + mediaUri);

		MediaApi mediaApi = ApiFactory.getMediaApi(this);
		mediaApi.getApiClient().setAccessToken(accessToken);
		ContentResolver cr = getContentResolver();

		try {
			Media media = mediaApi.mediaPost(IOUtils.toByteArray(cr.openInputStream(mediaUri)));
			Log.i(TAG, "Media posted to: " + media);

			// Use the new media element to build a media usage for inclusion in our contribution
			// TODO this is in the wrong place; push up
			MediaUsage newMediaUsage = new MediaUsage();
			newMediaUsage.setMedia(media);
			return newMediaUsage;

		} catch (ApiException e) {
			Log.w(TAG, "Media post failed", e);
		} catch (IOException e) {
			Log.e(TAG, "Media post failed", e);
		}
		return null;
	}

	private Contribution submitContribution(Contribution newContribution, String accessToken) {
		ContributionApi contributionApi = ApiFactory.getContributionApi(this);
		contributionApi.getApiClient().setAccessToken(accessToken);

		try {
			Contribution postedContribution = contributionApi.contributionsPost(newContribution);
			Log.i(TAG, "Posted contribution: " + postedContribution);
			return postedContribution;

		} catch (Exception e) {
			Log.e(TAG, "Failed to post contribution", e);
		}

		return null;
	}

	private void afterPost(Contribution result) {
	    /* TODO invalid notification in latest SDK
		final Context context = getApplicationContext();
		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		final String text = "Your contribution has been submitted: " + result.getHeadline();

		final Notification notification = new Notification(R.drawable.logo, text, new Date().getTime());
		Intent notificationIntent = new Intent(context, assignments.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);		
        notification.contentIntent = contentIntent;
		notificationManager.notify(1, notification);
		*/
	}

}