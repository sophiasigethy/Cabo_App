package msp.group3.caboclient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LicenseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.license_activity);
		LinearLayout ll = (LinearLayout) findViewById(R.id.licenseLayout);

		//Copyright 2015 Ingo Schwarz - TableView
		TextView someLicenseTV = new TextView(getApplicationContext());
		someLicenseTV.setTextColor(Color.BLACK);
		someLicenseTV.setText("Some Component\n\nCopyright (C) 2015 The Author\n" +
				"\n" +
				"Here you can find the Copyright text, we should copy from the Repository" +
				"where we got some code from, which was not included in android out of the box");

		//Copyright 2015 Ingo Schwarz - TableView
		TextView musicLicenseTV = new TextView(getApplicationContext());
		musicLicenseTV.setTextColor(Color.BLACK);
		musicLicenseTV.setText("Royalty Free Music from Bensound");

		ll.addView(musicLicenseTV);
		ll.addView(someLicenseTV);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
