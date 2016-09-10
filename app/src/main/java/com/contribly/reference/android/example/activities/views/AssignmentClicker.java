package com.contribly.reference.android.example.activities.views;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.contribly.client.model.Assignment;

public class AssignmentClicker implements OnClickListener {

	public static final String ASSIGNMENT = "assignment";

	private Activity activity;
	private Assignment assignment;

	public AssignmentClicker(Activity activity, Assignment assignment) {
		this.activity = activity;
		this.assignment = assignment;
	}

	@Override
	public void onClick(View view) {
		activity.startActivity(new Intent(activity, com.contribly.reference.android.example.activities.assignment.class).putExtra(ASSIGNMENT, assignment));
	}

}
