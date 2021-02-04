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

		//Copyright 2020 Henning Dodenhof - CircleImageView
		TextView circleImageViewTv = new TextView(getApplicationContext());
		circleImageViewTv.setTextColor(Color.BLACK);
		circleImageViewTv.setText("CircleImageView\n\nCopyright 2014 - 2020 Henning Dodenhof\n" +
				"\n" +
				"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
				"you may not use this file except in compliance with the License.\n" +
				"You may obtain a copy of the License at\n" +
				"\n" +
				"    http://www.apache.org/licenses/LICENSE-2.0\n" +
				"\n" +
				"Unless required by applicable law or agreed to in writing, software\n" +
				"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
				"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
				"See the License for the specific language governing permissions and\n" +
				"limitations under the License.\n\n");

		//Copyright 2019 Square, Inc. - OkHttp
		TextView okHttpTv = new TextView(getApplicationContext());
		okHttpTv.setTextColor(Color.BLACK);
		okHttpTv.setText("OkHttp\n\nCopyright 2019 Square, Inc.\n" +
				"\n" +
				"Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
				"you may not use this file except in compliance with the License.\n" +
				"You may obtain a copy of the License at\n" +
				"\n" +
				"   http://www.apache.org/licenses/LICENSE-2.0\n" +
				"\n" +
				"Unless required by applicable law or agreed to in writing, software\n" +
				"distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
				"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
				"See the License for the specific language governing permissions and\n" +
				"limitations under the License.\n\n");

		//Copyright (C) 2015-2020 AppIntro Developers - AppIntro
		TextView appIntroTV = new TextView(getApplicationContext());
		appIntroTV.setTextColor(Color.BLACK);
		appIntroTV.setText("AppIntro\n\nCopyright (C) 2015-2020 AppIntro Developers\n" +
				"\n" +
				"    Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
				"    you may not use this file except in compliance with the License.\n" +
				"    You may obtain a copy of the License at\n" +
				"\n" +
				"       http://www.apache.org/licenses/LICENSE-2.0\n" +
				"\n" +
				"    Unless required by applicable law or agreed to in writing, software\n" +
				"    distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
				"    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
				"    See the License for the specific language governing permissions and\n" +
				"    limitations under the License.\n\n");

		//Copyright 2019 Square, Inc. - Groovy
		TextView groovyTv = new TextView(getApplicationContext());
		groovyTv.setTextColor(Color.BLACK);
		groovyTv.setText("Groovy\n\nGroovy is licensed under the terms of the Apache License, Version 2.0\n\n");

		//TODO: ZoomLayout, Airbnb/Lottie,

		// Copyright free music
		TextView musicLicenseTV = new TextView(getApplicationContext());
		musicLicenseTV.setTextColor(Color.BLACK);
		musicLicenseTV.setText("Royalty Free Music from Bensound");

		ll.addView(musicLicenseTV);
		ll.addView(circleImageViewTv);
		ll.addView(appIntroTV);
		ll.addView(okHttpTv);
		ll.addView(groovyTv);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
