package msp.group3.caboclient.CaboView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import msp.group3.caboclient.R;

public class LicenseActivity extends Activity {

	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.license_activity);
		LinearLayout ll = (LinearLayout) findViewById(R.id.licenseLayout);

		//Copyright 2020 Henning Dodenhof - CircleImageView
		TextView circleImageViewTv = new TextView(getApplicationContext());
		circleImageViewTv.setTextColor(ContextCompat.getColor(this, R.color.beige));
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
		okHttpTv.setTextColor(ContextCompat.getColor(this, R.color.beige));
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
		appIntroTV.setTextColor(ContextCompat.getColor(this, R.color.beige));
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

		//Copyright 2018 Airbnb, Inc. Lottie-Android
		TextView lottieTV = new TextView(getApplicationContext());
		lottieTV.setTextColor(ContextCompat.getColor(this, R.color.beige));
		lottieTV.setText("Lottie-android\n\nCopyright 2018 Airbnb, Inc.\n" +
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

		//Copyright  Copyright 2017 Otalia Studios ZoomLayout
		TextView zoomLayoutTV = new TextView(getApplicationContext());
		zoomLayoutTV.setTextColor(ContextCompat.getColor(this, R.color.beige));
		zoomLayoutTV.setText("ZoomLayout\n\nCopyright 2017 Otalia Studios\n" +
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
		groovyTv.setTextColor(ContextCompat.getColor(this, R.color.beige));
		groovyTv.setText("Groovy\n\nGroovy is licensed under the terms of the Apache License, Version 2.0\n\n");

		//Copyright Icons
		TextView iconsTv = new TextView(getApplicationContext());
		iconsTv.setTextColor(ContextCompat.getColor(this, R.color.beige));
		iconsTv.setText("Icons made by\n\n" +
				"Freepik, Pixel Perfect, Smashicons, Lyolya, Becris\n\n" +
				"https://www.flaticon.com/\n\n");

		//Copyright Animation
		TextView animationsTv = new TextView(getApplicationContext());
		animationsTv.setTextColor(ContextCompat.getColor(this, R.color.beige));
		animationsTv.setText("Lottiefiles (Animations)\n\n" +
				"Magic Particles by Kristján Freyr Einarsson\n" +
				"Highlight by Janmesh Gharat\n" +
				"Star burts animation by Nitin Prajapati\n" +
				"Countdown timer by Dmytro Korolkov\n" +
				"Connection by Clement Denis\n" +
				"Tap by Daria Ledeneva\n\n" +
				"https://www.lottiefiles.com/\n\n");

		// Copyright free music
		TextView musicLicenseTV = new TextView(getApplicationContext());
		musicLicenseTV.setTextColor(ContextCompat.getColor(this, R.color.beige));
		musicLicenseTV.setText("Royalty Free Music from Bensound\n\n");

		ll.addView(musicLicenseTV);
		ll.addView(circleImageViewTv);
		ll.addView(iconsTv);
		ll.addView(animationsTv);
		ll.addView(appIntroTV);
		ll.addView(okHttpTv);
		ll.addView(groovyTv);
		ll.addView(lottieTV);
		ll.addView(zoomLayoutTV);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public void exit(View view) {
		super.onBackPressed();
	}
}
