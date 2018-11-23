package com.contribly.reference.android.example.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.activities.views.AssignmentListAdapter;
import com.contribly.reference.android.example.api.ApiFactory;

import java.util.List;

import com.contribly.client.ApiResponse;
import com.contribly.client.api.AssignmentApi;
import com.contribly.client.model.Assignment;

public class assignments extends BaseActivity {
	
	private static final String TAG = "assignments";
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.assignments);
        
    	FetchAssignmentsTask fetchAssignmentsTask = new FetchAssignmentsTask(ApiFactory.getAssignmentApi(this));
		fetchAssignmentsTask.execute(ApiFactory.ownedBy(this));
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
	}
	
	private void renderResults(List<Assignment> assignments) {
		final AssignmentListAdapter listAdapater = new AssignmentListAdapter(getApplicationContext(), R.layout.assignmentrow, this);
		for (Assignment assignment : assignments) {
			listAdapater.add(assignment);
		}
		
		final ListView assignmentsList = findViewById(R.id.list);
		assignmentsList.setAdapter(listAdapater);
	}
	
	private class FetchAssignmentsTask extends AsyncTask<String, Integer, List<Assignment>> {

		private final AssignmentApi api;

		public FetchAssignmentsTask(AssignmentApi api) {
			super();
			this.api = api;
		}

		@Override
		protected List<Assignment> doInBackground(String... params) {
			final String ownedBy = params[0];
			try {
				// Call the Contribly API for a list on currently open assignments
				// The actual call is handled by Swagger code gen client library which has been auto generated from the Contribly API's Swagger definition
				ApiResponse<List<Assignment>> listApiResponse = api.assignmentsGetWithHttpInfo(ownedBy, 1, 20, null, null, null, null, null, null);
				return listApiResponse.getData();

			} catch (Exception e) {
				Log.e(TAG, "Failed to load assignments list", e);
				return Lists.newArrayList();	// TODO notify this error to the user
			}
		}

		@Override
		protected void onPostExecute(List<Assignment> assignments) {
			renderResults(assignments);
		}

	}

}