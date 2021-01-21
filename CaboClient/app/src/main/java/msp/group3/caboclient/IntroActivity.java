package msp.group3.caboclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {
    final static String TAG = "IntroActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFirstStartup = readStartupFlagFromDisk();
        Log.d(TAG, "IsFirstStartup: " + isFirstStartup);

        if (isFirstStartup) {
            addSlides();
        } else {
            setContentView(R.layout.activity_intro);
            Intent myIntent = new Intent(IntroActivity.this, LoginActivity.class);
            IntroActivity.this.startActivity(myIntent);
            finish();
        }
    }

    private void addSlides() {
        addSlide(AppIntroFragment.newInstance("First App Into", "First App Intro Details",
                R.drawable.card_clubs_2, ContextCompat.getColor(getApplicationContext(), R.color.black)));
        addSlide(AppIntroFragment.newInstance("Second App Into", "Second App Intro Details",
                R.drawable.card_clubs_3, ContextCompat.getColor(getApplicationContext(), R.color.black)));
        addSlide(AppIntroFragment.newInstance("Third App Into", "Third App Intro Details",
                R.drawable.card_clubs_4, ContextCompat.getColor(getApplicationContext(), R.color.black)));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        writeStartupFlagToDisk();

        Intent myIntent = new Intent(IntroActivity.this, LoginActivity.class);
        IntroActivity.this.startActivity(myIntent);
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        writeStartupFlagToDisk();

        Intent myIntent = new Intent(IntroActivity.this, LoginActivity.class);
        IntroActivity.this.startActivity(myIntent);
        finish();
    }
    private void writeStartupFlagToDisk() {
        SharedPreferences sharedPref = this.getSharedPreferences("preference_file_key", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isFirstStartup", false);
        editor.commit();
    }
    private boolean readStartupFlagFromDisk() {
        SharedPreferences sharedPref = this.getSharedPreferences("preference_file_key", this.MODE_PRIVATE);

        return sharedPref.getBoolean("isFirstStartup", true);
    }
}