package com.contribly.reference.android.example.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.contribly.reference.android.example.R;

public class BaseActivity extends Activity {
	
	@Override
	public final boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public final boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.assignments:
			this.startActivity(new Intent(this, assignments.class));
			return true;

		case R.id.profile:
			this.startActivity(new Intent(this, profile.class));
			return true;
			
		case R.id.signin:
			this.startActivity(new Intent(this, signin.class));
			return true;
			
		case R.id.signout:
			this.startActivity(new Intent(this, signout.class));
			return true;
			
		case R.id.preferences:
			this.startActivity(new Intent(this, preferences.class));
			return true;
		}
		
		return false;
	}

}
