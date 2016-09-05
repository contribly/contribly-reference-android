package com.contribly.reference.android.example.activities.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.contribly.reference.android.example.R;
import com.squareup.picasso.Picasso;

import io.swagger.client.model.Artifact;
import io.swagger.client.model.Assignment;

public class AssignmentListAdapter extends ArrayAdapter<Assignment> {

	private final Context context;
	private final Activity activity;
	
	private final AssignmentDescriptionBuilder assignmentDescriptionBuilder;
	
	public AssignmentListAdapter(Context context, int viewResourceId, Activity activity) {
		super(context, viewResourceId);
		this.context = context;
		this.activity = activity;
		this.assignmentDescriptionBuilder = new AssignmentDescriptionBuilder();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		View view = convertView;
		if (view == null) {
			view = inflateNewRowView();
		}
		
		final Assignment assignment = getItem(position);
		populateAssignmentView(view, assignment);
		return view;		
	}

	private void populateAssignmentView(View view, Assignment assignment) {
		final String name = assignment.getName();
		
		final TextView nameTextView = (TextView) view.findViewById(R.id.assignmentRowName);
		nameTextView.setText(name);

		Artifact cover = assignment.getCover() != null ? ArtifactFinder.findArtifact(assignment.getCover()) : null;

		ImageView coverView = (ImageView) view.findViewById(R.id.assignmentRowCover);
		if (cover != null) {
			coverView.setVisibility(View.VISIBLE);
			coverView.setScaleType(ImageView.ScaleType.FIT_START);
			Picasso.with(context).load(cover.getUrl()).placeholder(R.drawable.coverplaceholder).into(coverView);

		} else {
			coverView.setVisibility(View.GONE);
		}

		final TextView metaDataTextView = (TextView) view.findViewById(R.id.assignmentRowMetadata);
		metaDataTextView.setText(assignmentDescriptionBuilder.composeMetaDataDescription(assignment));
		
		view.setOnClickListener(new AssignmentClicker(activity, assignment));
	}
	
	private View inflateNewRowView() {
		final LayoutInflater mInflater = LayoutInflater.from(context);
		return mInflater.inflate(R.layout.assignmentrow, null);
	}

}