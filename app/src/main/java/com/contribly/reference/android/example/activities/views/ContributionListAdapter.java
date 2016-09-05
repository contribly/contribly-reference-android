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
import io.swagger.client.model.Contribution;

public class ContributionListAdapter extends ArrayAdapter<Contribution> {

	private final Context context;
	private final Activity activity;
	private final ContributionDescriptionBuilder contentDescriptionBuilder;
	
	public ContributionListAdapter(Context context, int viewResourceId, Activity activity) {
		super(context, viewResourceId);
		this.context = context;
		this.activity = activity;
		this.contentDescriptionBuilder = new ContributionDescriptionBuilder();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		View view = convertView;
		if (view == null) {
			view = inflateNewRowView();
		}
		
		final Contribution contribution = getItem(position);
		populateContentView(view, contribution);
		return view;		
	}

	private void populateContentView(View view, Contribution contribution) {
        final TextView nameTextView = (TextView) view.findViewById(R.id.headline);
		nameTextView.setText(contribution.getHeadline());
		
		final ImageView imageView = (ImageView) view.findViewById(R.id.contributionImage);
		Artifact artifact = ArtifactFinder.findMainArtifact(contribution);
        if (artifact != null) {
			imageView.setVisibility(View.VISIBLE);
			Picasso.with(context).load(artifact.getUrl()).placeholder(R.drawable.logo).into(imageView);

		} else {
			imageView.setVisibility(View.GONE);
		}
		
		final TextView descriptionTextView = (TextView) view.findViewById(R.id.description);		
		descriptionTextView.setText(contentDescriptionBuilder.renderDescription(contribution));
		
		view.setOnClickListener(new ContributionClicker(activity, contribution));
	}
	
	private View inflateNewRowView() {
		final LayoutInflater mInflater = LayoutInflater.from(context);
		return mInflater.inflate(R.layout.contributionrow, null);
	}

}