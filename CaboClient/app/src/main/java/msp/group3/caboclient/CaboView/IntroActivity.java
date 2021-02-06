package msp.group3.caboclient.CaboView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import msp.group3.caboclient.R;

import android.util.Log;
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
        addSlide(AppIntroFragment.newInstance("Cabo", "Cabo is a round-based Online-Multiplayer Cardgame for up to 4 Players",
                R.drawable.cabo_logo, ContextCompat.getColor(getApplicationContext(), R.color.black)));
        addSlide(AppIntroFragment.newInstance("The Game", "Every Player has 4 cards, of which 2 are unknown at the beginning" +
                        "The goal of the game is to collect as few points as possible\n" +
                        "This can be achieved by swapping drawn cards with your own, or by using action-cards, to swap cards with the other players\n",
                R.drawable.card_clubs_3, ContextCompat.getColor(getApplicationContext(), R.color.black)));
        addSlide(AppIntroFragment.newInstance("Rules", "At the beginning of the game, every player can look at 2 of the 4 cards\n" +
                        "Make sure, you remember those\n" +
                        "At the beginning of your turns, you can draw a card\n" +
                        "There are normal cards and action cards",
                R.drawable.introduction_rules, ContextCompat.getColor(getApplicationContext(), R.color.black)));
        addSlide(AppIntroFragment.newInstance("Card Types", "Cards 2 - 6 are normal cards. You can use these only to swap them with your cards\n" +
                        "Cards 7 - Queens are action cards:\n" +
                        "7+8 Allow you to take a look at one of your cards\n" +
                        "9+10 Allow you to take a look at any card on the field\n" +
                        "J+Q Allow you to swap one of your cards with any card on the field",
                R.drawable.introduction_card_types, ContextCompat.getColor(getApplicationContext(), R.color.black)));
        addSlide(AppIntroFragment.newInstance("Goal", "The goal of the game is to collect as few points as possible\n" +
                        "Every Card with a number is worth equal to it´s number\n" +
                        "Jack is worth 11 Points, Queen is worth 12 Points and the King is worth 13 Points\n" +
                        "Ace is worth 0 Points and the Joker is worth -1 Point\n" +
                        "You have to try to get rid of cards with high points to win the game",
                R.drawable.card_clubs_4, ContextCompat.getColor(getApplicationContext(), R.color.black)));
        addSlide(AppIntroFragment.newInstance("Winning", "If you think, your points are low enough, instead of drawing a card you can call CABO!\n" +
                        "This will end your turn, and everyone else has 1 last turn\n" +
                        "Afterwards the points of every player are summed up and the one with fewest points is the winner",
                R.drawable.introduction_winning, ContextCompat.getColor(getApplicationContext(), R.color.black)));
        addSlide(AppIntroFragment.newInstance("Gameplay", "To interact in the game, you can click cards," +
                        " swipe the table to move around, chat with other players, or show your emotions by changing your Emoji",
                R.drawable.introduction_gameplay, ContextCompat.getColor(getApplicationContext(), R.color.black)));
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