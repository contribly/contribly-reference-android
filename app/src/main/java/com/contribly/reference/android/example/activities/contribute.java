package com.contribly.reference.android.example.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.contribly.client.api.AssignmentApi;
import com.contribly.client.model.Assignment;
import com.contribly.client.model.Contribution;
import com.contribly.reference.android.example.R;
import com.contribly.reference.android.example.activities.views.AssignmentContributeClicker;
import com.contribly.reference.android.example.api.ApiFactory;
import com.contribly.reference.android.example.services.ContributionPostingService;
import com.google.common.collect.Lists;
import com.squareup.picasso.Picasso;

import java.util.List;

public class contribute extends BaseActivity implements LocationListener {

    private static final String TAG = "contribute";

    private static final String ASSIGNMENT = AssignmentContributeClicker.ASSIGNMENT;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int PICK_MEDIA_PERMISSION_REQUEST = 1;

    private Uri image;
    private Location location = null;
    private Assignment assignment;
    private List<Assignment> assignments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contribute);

        if (this.getIntent().getExtras() != null) {
            if (this.getIntent().getExtras().get(ASSIGNMENT) != null) {
                assignment = (Assignment) this.getIntent().getExtras().get(ASSIGNMENT);
            }
        }

        Button chooseImageButton = findViewById(R.id.chooseImage);
        chooseImageButton.setOnClickListener(new ContributeImageClicker(this));

        new FetchAssignmentsTask(ApiFactory.getAssignmentApi(this)).execute(ApiFactory.ownedBy(this));

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                ImageView imagePreview = (ImageView) findViewById(R.id.image);
                receiveAndPreviewSelectedImage(imagePreview, uri);
            }
        }

        View currentFocus = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        View contributeForm = (View) findViewById(R.id.contributeForm);

        Button contributeButton = (Button) findViewById(R.id.contributeButton);
        contributeButton.setOnClickListener(contributionClickListener);
        contributeForm.setVisibility(View.VISIBLE);

        registerForLocationUpdates();
    }

    private void populateAssignmentDropdown(List<Assignment> assignments) {
        final List<String> assignmentNames = Lists.newArrayList();
        for (Assignment assignment : assignments) {
            assignmentNames.add(assignment.getName());
        }

        Spinner assignmentDropdown = (Spinner) findViewById(R.id.assignmentDropdown);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, assignmentNames);   // TODO Assignment name may not be unique. No dropdown key/value ui in Android?
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignmentDropdown.setAdapter(dataAdapter);

        if (assignment != null) {
            final int selectedIndex = assignmentNames.indexOf(assignment.getName());
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
        final String selectedAssignmentName = (String) assignmentDropdown.getSelectedItem();

        // Compose a new contribution from the user's form submission and the handset location if available
        Contribution newContribution = new Contribution();
        newContribution.setHeadline(headline);
        newContribution.setBody(body);

        Assignment selectedAssignment = findAssignmentByName(selectedAssignmentName, assignments);
        newContribution.setAssignment(selectedAssignment);

        // TODO location

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

    private Assignment findAssignmentByName(String name, List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            if (assignment.getName().equals(name)) {
                return assignment;
            }
        }
        return null;
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
        this.image = uri;
        imagePreview.setVisibility(View.VISIBLE);
        Picasso.with(this).load(uri).resize(640, 640).centerCrop().into(imagePreview);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Log.i(TAG, "Chosen gallery image path was: " + picturePath);
            Uri imageUri = Uri.parse("file:///" + picturePath);

            ImageView imagePreview = (ImageView) findViewById(R.id.contributionImagePreview);
            receiveAndPreviewSelectedImage(imagePreview, imageUri);
        }
    }

    private class ContributeImageClicker implements OnClickListener {

        private com.contribly.reference.android.example.activities.contribute contribute;

        public ContributeImageClicker(contribute contribute) {
            this.contribute = contribute;
        }

        @Override
        public void onClick(View view) {
            // Recent versions of Android allow the used to grant and revoke permissions at runtime; we neeed to check that we have permission to access the user's media.
            if (ActivityCompat.checkSelfPermission(contribute, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            } else {
                Log.w(TAG, "Need permission to access media");
                ActivityCompat.requestPermissions(contribute, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_MEDIA_PERMISSION_REQUEST);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.i(TAG, "Got request permissions result for " + requestCode + ": " + permissions);
        switch (requestCode) {
            case PICK_MEDIA_PERMISSION_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);

                } else {
                    // TODO warn the user that this means we can't upload media
                }
                break;
        }
    }

    private class FetchAssignmentsTask extends AsyncTask<String, Integer, List<Assignment>> {    // TODO duplication?

        private final AssignmentApi api;

        public FetchAssignmentsTask(AssignmentApi api) {
            super();
            this.api = api;
        }

        @Override
        protected List<Assignment> doInBackground(String... params) {
            final String username = params[0];
            try {
                return api.assignmentsGet(username, 1, 20, null, null, null, true, null, null);

            } catch (Exception e) {
                Log.e(TAG, "Failed to load assignments list");
                return Lists.newArrayList();    // TODO notify this error to the user
            }
        }

        @Override
        protected void onPostExecute(List<Assignment> loadedAssignments) {
            assignments = loadedAssignments;
            populateAssignmentDropdown(loadedAssignments);
        }

    }

}