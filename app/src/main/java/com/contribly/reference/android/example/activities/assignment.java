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

import com.contribly.client.ApiResponse;
import com.contribly.client.api.ContributionApi;
import com.contribly.client.model.Artifact;
import com.contribly.client.model.Assignment;
import com.contribly.client.model.Contribution;

public class assignment extends BaseActivity {

    private static final String ASSIGNMENT = AssignmentClicker.ASSIGNMENT;

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


        View header = inflateHeaderView();
        Button contributeToThisAssignmentButton = header.findViewById(R.id.contributeToAssignment);
        TextView assignmentStatusTextView = header.findViewById(R.id.assignmentStatus);

        boolean assignmentIsOpen = assignment.getOpen();
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

        contentList = findViewById(R.id.list);
        contentList.addHeaderView(header);

        final TextView assignmentNameTextView = findViewById(R.id.assignmentName);
        assignmentNameTextView.setText(assignment.getName());

        final TextView metaDataTextView = findViewById(R.id.assignmentMetadata);
        metaDataTextView.setText(assignmentDescriptionBuilder.composeMetaDataDescription(assignment));

        final TextView descriptionTextView = findViewById(R.id.assignmentDescription);
        if (assignment.getDescription() != null) {
            // Some clients choose to use HTML in the assignment description field
            descriptionTextView.setText(Html.fromHtml(assignment.getDescription()));
        }

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
                ApiResponse<List<Contribution>> listApiResponse = api.contributionsGetWithHttpInfo(query, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
                Long totalContributions = Long.parseLong(listApiResponse.getHeaders().get("x-total-count").get(0));
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