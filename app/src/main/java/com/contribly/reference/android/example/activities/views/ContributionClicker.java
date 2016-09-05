package com.contribly.reference.android.example.activities.views;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import io.swagger.client.model.Contribution;

public class ContributionClicker implements OnClickListener {

	public static final String CONTRIBUTION = "contribution";
	
	private Activity activity;
	private Contribution contribution;

	public ContributionClicker(Activity activity, Contribution contribution) {
		this.activity = activity;
		this.contribution = contribution;
	}

	@Override
	public void onClick(View view) {
		activity.startActivity(new Intent(activity, com.contribly.reference.android.example.activities.contribution.class).putExtra(CONTRIBUTION, contribution));
	}
	
}
