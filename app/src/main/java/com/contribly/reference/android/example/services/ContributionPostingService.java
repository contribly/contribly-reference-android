package com.contribly.reference.android.example.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.activities.profile;
import com.contribly.reference.android.example.api.ApiFactory;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Date;

import io.swagger.client.ApiException;
import io.swagger.client.api.ContributionApi;
import io.swagger.client.api.MediaApi;
import io.swagger.client.model.Contribution;
import io.swagger.client.model.Media;
import io.swagger.client.model.MediaUsage;

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
		String accessToken = LoggedInUserService.getInstance().getLoggedInUsersAccessToken();	// TODO potential race conditional; has the user signed out in the meantime? The access token should be past in as part of the message.
		Log.i(TAG, "Received contibution to post: " + newContribution + " / media: " + media);

		// Submit the contribution.
		// If the contribution includes media then we need to submit that to the media endpoint before referencing the resulting media element in a contribution media usage.
		MediaUsage mediaUsage = media != null ? submitMedia(media, accessToken) : null;

		if (mediaUsage != null) {
			newContribution.getMediaUsages().add(mediaUsage);
		}

		Log.i(TAG, "Posting contribution in background: " + newContribution);
		Contribution submittedContribution = submitContribution(newContribution, accessToken);

		// Notify the user that their contribution has been submitted
		afterPost(submittedContribution);

		return;
	}

	private MediaUsage submitMedia(Uri mediaUri, String accessToken) {
		MediaApi mediaApi = ApiFactory.getMediaApi(this);
		mediaApi.getApiClient().setAccessToken(accessToken);
		ContentResolver cr = getContentResolver();

		try {
			Media media = mediaApi.mediaPost(IOUtils.toByteArray(cr.openInputStream(mediaUri)));
			Log.i(TAG, "Media posted to: " + media);

			// Use the new media element to build a media usage for inclusion in our contribution
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
		final Context context = getApplicationContext();
		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		final String text = "Your contribution has been submitted: " + result.getHeadline();
		final Notification notification = new Notification(R.drawable.logo, text, new Date().getTime());
		Intent notificationIntent = new Intent(context, profile.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);		
		notification.setLatestEventInfo(context, text, text, contentIntent);
		notificationManager.notify(1, notification);
	}

}