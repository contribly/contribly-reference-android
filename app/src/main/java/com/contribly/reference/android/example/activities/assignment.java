package com.contribly.reference.android.example.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.activities.views.ArtifactFinder;
import com.contribly.reference.android.example.activities.views.AssignmentClicker;
import com.contribly.reference.android.example.activities.views.AssignmentContributeClicker;
import com.contribly.reference.android.example.activities.views.AssignmentDescriptionBuilder;
import com.contribly.reference.android.example.activities.views.ContributionListAdapter;
import com.contribly.reference.android.example.api.ApiFactory;
import com.contribly.reference.android.example.model.ResultSet;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.swagger.client.ApiResponse;
import io.swagger.client.api.ContributionApi;
import io.swagger.client.model.Artifact;
import io.swagger.client.model.Assignment;
import io.swagger.client.model.Contribution;

public class assignment extends BaseActivity {
	
	private static final String ASSIGNMENT = AssignmentClicker.ASSIGNMENT;
	private static final String TAG = "assignment";

	private AssignmentDescriptionBuilder assignmentDescriptionBuilder;
	
	private Assignment assignment;
	private ListView contentList;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.assignment);

        if (this.getIntent().getExtras() != null) {
	        if (this.getIntent().getExtras().get(ASSIGNMENT) != null) {
	        	assignment = (Assignment) this.getIntent().getExtras().get(ASSIGNMENT);
	        }
		}

        this.assignmentDescriptionBuilder = new AssignmentDescriptionBuilder();

		boolean assignmentIsOpen = true;	// TODO needs to be calculated

		View header = inflateHeaderView();
		Button contributeToThisAssignmentButton = (Button) header.findViewById(R.id.contributeToAssignment);
		TextView assignmentStatusTextView = (TextView) header.findViewById(R.id.assignmentStatus);

		if (assignmentIsOpen) {
			contributeToThisAssignmentButton.setVisibility(View.VISIBLE);
			assignmentStatusTextView.setText("This assignment is open");
			assignmentStatusTextView.setVisibility(View.VISIBLE);
			contributeToThisAssignmentButton.setOnClickListener(new AssignmentContributeClicker(this, assignment));

		} else {
			contributeToThisAssignmentButton.setVisibility(View.GONE);
			assignmentStatusTextView.setText("This assignment is closed");
			assignmentStatusTextView.setVisibility(View.VISIBLE);
		}

		contentList = (ListView) findViewById(R.id.list);
		contentList.addHeaderView(header);

        final TextView assignmentNameTextView = (TextView) findViewById(R.id.assignmentName);
        assignmentNameTextView.setText(assignment.getName());

    	final TextView metaDataTextView = (TextView) findViewById(R.id.assignmentMetadata);
		metaDataTextView.setText(assignmentDescriptionBuilder.composeMetaDataDescription(assignment));

		final TextView descriptionTextView = (TextView) findViewById(R.id.assignmentDescription);
		descriptionTextView.setText(Html.fromHtml(assignment.getDescription()));	// Some clients choose to use HTML in the assignment description field.

		FetchContributionsTask fetchContentTask = new FetchContributionsTask(ApiFactory.getContributionApi(this));
		fetchContentTask.execute(assignment.getId());

        Artifact cover = assignment.getCover() != null ? ArtifactFinder.findArtifact(assignment.getCover()) : null;
		if (cover != null) {
            ImageView imageView = (ImageView) findViewById(R.id.cover);
            imageView.setVisibility(View.VISIBLE);
            imageView.setScaleType(ScaleType.FIT_START);
			Picasso.with(this).load(cover.getUrl()).into(imageView);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void renderContributionResults(ResultSet resultSet) {
		final ContributionListAdapter contributionListAdapter = new ContributionListAdapter(getApplicationContext(), R.layout.contributionrow, this);
		for (Contribution contribution : resultSet.getContributions()) {
			contributionListAdapter.add(contribution);
		}
		contentList.setAdapter(contributionListAdapter);
	}

	private View inflateHeaderView() {
		final LayoutInflater mInflater = LayoutInflater.from(this);
		return mInflater.inflate(R.layout.assignmentheader, null);
	}

	private class FetchContributionsTask extends AsyncTask<String, Integer, ResultSet> {

		private final ContributionApi api;

		public FetchContributionsTask(ContributionApi api) {
			super();
			this.api = api;
		}

		@Override
		protected ResultSet doInBackground(String... params) {
			final String query = params[0];
			try {
                ApiResponse<List<Contribution>> listApiResponse = api.contributionsGetWithHttpInfo(query, null, null, null, null, null, null, null, null, null, null, null);
                Long totalContributions = Long.parseLong(listApiResponse.getHeaders().get("X-Total-Count").get(0));
                return new ResultSet(totalContributions, listApiResponse.getData());

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(ResultSet resultSet) {
			renderContributionResults(resultSet);
		}

	}

}