package com.contribly.reference.android.example.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.activities.views.ArtifactFinder;
import com.contribly.reference.android.example.activities.views.ContributionClicker;
import com.contribly.reference.android.example.activities.views.ContributionDescriptionBuilder;
import com.squareup.picasso.Picasso;

import io.swagger.client.model.Artifact;
import io.swagger.client.model.Contribution;

public class contribution extends BaseActivity {

	private static final String CONTENT = ContributionClicker.CONTRIBUTION;

	private ContributionDescriptionBuilder contentDescriptionBuilder;

	private Contribution contribution;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contribution);

		if (this.getIntent().getExtras() != null) {
			if (this.getIntent().getExtras().get(CONTENT) != null) {
				contribution = (Contribution) this.getIntent().getExtras().get(CONTENT);
			}
		}

		this.contentDescriptionBuilder = new ContributionDescriptionBuilder();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		final TextView headlineTextView = (TextView) findViewById(R.id.headline);
		headlineTextView.setText(contribution.getHeadline());
		
		final TextView descriptionTextView = (TextView) findViewById(R.id.description);
		descriptionTextView.setText(contentDescriptionBuilder.renderDescription(contribution));

		Artifact artifact = ArtifactFinder.findMainArtifact(contribution);
		final ImageView imageView = (ImageView) findViewById(R.id.image);
		if (artifact != null) {
			imageView.setVisibility(View.VISIBLE);
			Picasso.with(this).load(artifact.getUrl()).placeholder(R.drawable.logo).into(imageView);
		} else {
			imageView.setVisibility(View.GONE);
		}
	}

}