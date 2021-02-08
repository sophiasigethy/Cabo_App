package msp.group3.caboclient.CaboView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

import msp.group3.caboclient.R;

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
        addSlide(AppIntroFragment.newInstance("Cabo", "Cabo is a round-based Online-Multiplayer Cardgame for up to 4 Players.",
                R.drawable.cabo_logo, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
        addSlide(AppIntroFragment.newInstance("The Game", "Every player has 4 cards." +
                        "The goal is to collect as few points as possible by swapping drawn cards with your own, or by using action-cards, to swap cards with the other players.\n",
                R.drawable.card_13, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
        addSlide(AppIntroFragment.newInstance("Rules", "At the beginning of a round, every player can look at and memorize 2 cards.\n" +
                        "When it's your turn, you can draw a card.\n" +
                        "There are normal cards and action cards.",
                R.drawable.introduction_rules, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
        addSlide(AppIntroFragment.newInstance("Normal Cards", "Cards of the values -1 - 6 are normal cards. You can use these only to swap with your cards or simply discard them.\n",
                R.drawable.card_0, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
        addSlide(AppIntroFragment.newInstance("Action Cards",
                        "Peek: 7+8 Allow you to take a look at one of your cards\n" +
                        "Spy: 9+10 Allow you to take a look at any enemy card\n" +
                        "Swap: 11+12 Allow you to swap any 2 cards on the field",
                R.drawable.introduction_card_types, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
        addSlide(AppIntroFragment.newInstance("Card Values",
                        "Every Card is worth its indicated number.\n",
                R.drawable.card_4, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
        addSlide(AppIntroFragment.newInstance("Rounds", "If you think, your points are low enough, you can call CABO!\n" +
                        "This will end your turn, and everyone else has 1 last turn.\n" +
                        "Afterwards the points of every player are summed up and a new round begins.",
                R.drawable.introduction_winning, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
        addSlide(AppIntroFragment.newInstance("Winning", "By default the game ends as soon as the first player has reached 100 points. The player with the lowest score at this point wins.\n" +
                        "The max score can also be adjusted in the settings.",
                R.drawable.introduction_winning, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
        addSlide(AppIntroFragment.newInstance("Gameplay", "To interact in the game, you can click cards," +
                        " swipe or pinch zoom the table to move around, chat with other players, or show your emotions by changing your emoji.",
                R.drawable.introduction_gameplay, ContextCompat.getColor(getApplicationContext(), R.color.dark_purple),
                ContextCompat.getColor(getApplicationContext(), R.color.beige), ContextCompat.getColor(getApplicationContext(), R.color.beige)));
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