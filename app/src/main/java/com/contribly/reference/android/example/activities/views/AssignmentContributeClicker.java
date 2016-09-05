package com.contribly.reference.android.example.activities.views;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.contribly.reference.android.example.activities.contribute;

import io.swagger.client.model.Assignment;

public class AssignmentContributeClicker implements OnClickListener {

	public static final String ASSIGNMENT = "assignment";
	
	private Activity activity;
	private Assignment assignment;

	public AssignmentContributeClicker(Activity activity, Assignment assignment) {
		this.activity = activity;
		this.assignment = assignment;
	}

	@Override
	public void onClick(View view) {
		activity.startActivity(new Intent(activity, contribute.class).putExtra(ASSIGNMENT, assignment));
	}
	
}
