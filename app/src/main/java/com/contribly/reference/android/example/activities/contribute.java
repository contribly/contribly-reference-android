package com.contribly.reference.android.example.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.activities.views.AssignmentContributeClicker;
import com.contribly.reference.android.example.api.ApiFactory;
import com.contribly.reference.android.example.services.ContributionPostingService;
import com.contribly.reference.android.example.services.LoggedInUserService;
import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;

import io.swagger.client.api.AssignmentApi;
import io.swagger.client.model.Assignment;
import io.swagger.client.model.Contribution;

public class contribute extends BaseActivity implements LocationListener {

	private static final String TAG = "contribute";

	private static final String ASSIGNMENT = AssignmentContributeClicker.ASSIGNMENT;
    private static final int RESULT_LOAD_IMAGE = 1;

	private LoggedInUserService loggedInUserService;

	private Uri image;
	private Location location = null;
	private Assignment assignment;
	private List<Assignment> assignments;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.contribute);      
        
		loggedInUserService = LoggedInUserService.getInstance();

        if (this.getIntent().getExtras() != null) {
	        if (this.getIntent().getExtras().get(ASSIGNMENT) != null) {
	        	assignment = (Assignment) this.getIntent().getExtras().get(ASSIGNMENT);
	        }
		}
        
		ImageView imagePreview = (ImageView) findViewById(R.id.contributionImagePreview);
        imagePreview.setOnClickListener(new ContributeImageClicker());
		
        new FetchAssignmentsTask(ApiFactory.getAssignmentApi(this)).execute(ApiFactory.ownedBy(this));

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

		if (Intent.ACTION_SEND.equals(action)) {
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				receiveAndPreviewSelectedImage(imagePreview, uri);
			}
		}
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
		TextView contributeLoginHint = (TextView) findViewById(R.id.contributeLoginStatus);
		View contributeForm = (View) findViewById(R.id.contributeForm);

		final boolean isSignedIn = loggedInUserService.getLoggedInUsersAccessToken() != null;
		if (isSignedIn) {
			Button contributeButton = (Button) findViewById(R.id.contributeButton);
			contributeButton.setOnClickListener(contributionClickListener);
			contributeForm.setVisibility(View.VISIBLE);
			contributeLoginHint.setVisibility(View.GONE);

		} else {
			contributeForm.setVisibility(View.GONE);
			contributeLoginHint.setText("You must be signed in to contribute");
			contributeLoginHint.setVisibility(View.VISIBLE);
		}
		
		registerForLocationUpdates();
	}

	private void populateAssignmentDropdown(List<Assignment> assignments) {
		final List<String> assignmentIds = Lists.newArrayList();
	    for (Assignment assignment : assignments) {
			assignmentIds.add(assignment.getUrlWords());
		}
	    
		Spinner assignmentDropdown = (Spinner) findViewById(R.id.assignmentDropdown);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, assignmentIds);	// TODO use assignment names as labels
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignmentDropdown.setAdapter(dataAdapter);
        
        if (assignment != null) {
        	final int selectedIndex = assignmentIds.indexOf(assignment.getUrlWords());
        	if (selectedIndex > -1) {
        		assignmentDropdown.setSelection(selectedIndex);
        	}
        }
	}

	@Override
	protected void onPause() {
		super.onPause();
		turnOffLocationUpdates();
	}

	private void contribute() {
        EditText headlineInput = (EditText) findViewById(R.id.headline);
        final String headline = headlineInput.getText().toString();
        
        EditText bodyInput = (EditText) findViewById(R.id.body);
        final String body = bodyInput.getText().toString();	

        Spinner assignmentDropdown = (Spinner) findViewById(R.id.assignmentDropdown);
        final String selectedAssignment = (String) assignmentDropdown.getSelectedItem();

		// Compose a new contribution from the user's form submission and the handset location if available
		Contribution newContribution = new Contribution();
		newContribution.setHeadline(headline);
		newContribution.setBody(body);
		newContribution.setAssignment(assignments.get(0));	// TODO implement
		// TODO location and image

		Log.i(TAG, "Composed new contribution to submit: " + newContribution);

		// Pass the new contribution to a background service for submission to the Contribly API
		final Intent intent = new Intent(this, ContributionPostingService.class);

        final Bundle extras = new Bundle();
		extras.putSerializable(ContributionPostingService.NEW_CONTRIBUTION, newContribution);
		if (image != null) {
			extras.putSerializable(ContributionPostingService.MEDIA, image.toString());
		}
        intent.putExtras(extras);

		startService(intent);
		
		Toast toast = Toast.makeText(getApplicationContext(), "Uploading new contribution in the background", Toast.LENGTH_SHORT);
		toast.show();
		
		this.startActivity(new Intent(this, assignments.class));
		return;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i(TAG, "Location changed");
		this.location = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onProviderEnabled(String provider) {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);	
		//location = locationManager.getLastKnownLocation(provider);
		//Log.i(TAG, "location set to: " + location.toString());
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub		
	}
	
	private void registerForLocationUpdates() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		/*
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60 * 1000, 500, this);
		}
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60 * 1000, 500, this);
		}
		*/
	}
	
	private void turnOffLocationUpdates() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		//locationManager.removeUpdates(this);
	}
	
	private OnClickListener contributionClickListener = new OnClickListener() {
	    public void onClick(View v) {
	    	contribute();
	    }
	};

	private void receiveAndPreviewSelectedImage(ImageView imagePreview, Uri uri) {
		Log.i(TAG, "Accepting selected image from URI: " + uri);

		try {
			ContentResolver cr = getContentResolver();
			byte[] imageBytes = IOUtils.toByteArray(cr.openInputStream(uri));

			Log.i(TAG, "Received shared image");
			this.image = uri;

			showImagePreview(imageBytes, imagePreview);
		} catch (Exception e) {
			Log.e(TAG, "Error while receiving selected image: " + uri, e);
		}
	}

	private void showImagePreview(byte[] image, ImageView imagePreview) {	// TODO should be a background task
		Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
		Matrix matrix = new Matrix();
		matrix.postScale(0.2f, 0.2f);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

		imagePreview.setImageBitmap(resizedBitmap);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
 
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
 
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            
            Log.i(TAG, "Choosen gallery image path was: " + picturePath);
           
            Uri imageUri = Uri.parse("file:///" + picturePath);			
             
            ImageView imagePreview = (ImageView) findViewById(R.id.contributionImagePreview);
           
			receiveAndPreviewSelectedImage(imagePreview, imageUri);	        
        }     
    }
	
	private class ContributeImageClicker implements OnClickListener {
		
		@Override
		public void onClick(View view) {
			Intent i = new Intent(
	                Intent.ACTION_PICK,
	                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	         
	        startActivityForResult(i, RESULT_LOAD_IMAGE);
		}
		
	}
	
	private class FetchAssignmentsTask extends AsyncTask<String, Integer, List<Assignment>> {	// TODO duplication?

		private final AssignmentApi api;

		public FetchAssignmentsTask(AssignmentApi api) {
			super();
			this.api = api;
		}

		@Override
		protected List<Assignment> doInBackground(String... params) {
			final String username = params[0];
			try {
				return api.assignmentsGet(username, 1, 20, null, null, null, true, null);
				
			} catch (Exception e) {
				Log.e(TAG, "Failed to load assignments list");
				return Lists.newArrayList();	// TODO notify this error to the user
			}
		}

		@Override
		protected void onPostExecute(List<Assignment> loadedAssignments) {
			assignments = loadedAssignments;
			populateAssignmentDropdown(loadedAssignments);
		}
		
	}
	
}