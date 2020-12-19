package msp.group3.caboclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * this is an example for a zoomable and scrollable layout
 */
public class InGameActivity extends AppCompatActivity {

    private com.otaliastudios.zoom.ZoomLayout zoomLayout;
    private ImageButton player1_card1;
    private ImageButton player1_card2;
    private ImageButton player1_card3;
    private ImageButton player1_card4;

    private ImageButton player2_card1;
    private ImageButton player2_card2;
    private ImageButton player2_card3;
    private ImageButton player2_card4;

    private ImageButton player3_card1;
    private ImageButton player3_card2;
    private ImageButton player3_card3;
    private ImageButton player3_card4;

    private ImageButton player4_card1;
    private ImageButton player4_card2;
    private ImageButton player4_card3;
    private ImageButton player4_card4;

    private ImageButton chatButton;
    private ImageButton settingsButton;
    private Button caboButton;
    private Button zoomOutButton;

    private ImageButton playedCardsStackButton;
    private ImageButton pickCardsStackButton;

    private InGameChatFragment chatFragment;

    private List<ImageButton> playerCardButtons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ingame_activity);
        zoomLayout = (com.otaliastudios.zoom.ZoomLayout) findViewById(R.id.zoomlayout);
        chatButton = (ImageButton) findViewById(R.id.chat_button);
        settingsButton = (ImageButton) findViewById(R.id.settings_button);
        zoomOutButton = (Button) findViewById(R.id.zoom_out_button);
        caboButton = (Button) findViewById(R.id.cabo_button);
        playedCardsStackButton = (ImageButton) findViewById(R.id.played_cards_imageButton);
        pickCardsStackButton = (ImageButton) findViewById(R.id.pick_card_imageButton);
        instantiatePlayerCardDecks();

        chatFragment = new InGameChatFragment();
        Collections.addAll(playerCardButtons, player1_card1, player1_card2, player1_card3, player1_card4,
                player2_card1, player2_card2, player2_card3, player2_card4,
                player3_card1, player3_card2, player3_card3, player3_card4,
                player4_card1, player4_card2, player4_card3, player4_card4);

        setAllCardsOnClickListeners();

        //TODO: Chat fragment integration
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_chat, InGameChatFragment.class, null)
                    .hide(chatFragment)
                    .commit();
        }*/

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Open chat...", Toast.LENGTH_SHORT).show();
                //getSupportFragmentManager().beginTransaction().show(chatFragment);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Open settings...", Toast.LENGTH_SHORT).show();
            }
        });

        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomLayout.zoomTo(1.0f, true);
            }
        });

        caboButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "CABO!.", Toast.LENGTH_SHORT).show();
            }
        });

        caboButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "CABO!", Toast.LENGTH_SHORT).show();
            }
        });

        playedCardsStackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Discard card...", Toast.LENGTH_SHORT).show();
            }
        });

        pickCardsStackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Pick card...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAllCardsOnClickListeners(){
        for(ImageButton cardButton : playerCardButtons){
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(),
                            "Card clicked: "+getResources().getResourceEntryName(cardButton.getId()), Toast.LENGTH_SHORT).show();
                    //growCardAnimation(cardButton);
                }
            });
        }
    }


    private void instantiatePlayerCardDecks(){
        player1_card1 = findViewById(R.id.player1_card1_imageButton);
        player1_card2 = findViewById(R.id.player1_card2_imageButton);
        player1_card3 = findViewById(R.id.player1_card3_imageButton);
        player1_card4 = findViewById(R.id.player1_card4_imageButton);

        player2_card1 = findViewById(R.id.player2_card1_imageButton);
        player2_card2 = findViewById(R.id.player2_card2_imageButton);
        player2_card3 = findViewById(R.id.player2_card3_imageButton);
        player2_card4 = findViewById(R.id.player2_card4_imageButton);

        player3_card1 = findViewById(R.id.player3_card1_imageButton);
        player3_card2 = findViewById(R.id.player3_card2_imageButton);
        player3_card3 = findViewById(R.id.player3_card3_imageButton);
        player3_card4 = findViewById(R.id.player3_card4_imageButton);

        player4_card1 = findViewById(R.id.player4_card1_imageButton);
        player4_card2 = findViewById(R.id.player4_card2_imageButton);
        player4_card3 = findViewById(R.id.player4_card3_imageButton);
        player4_card4 = findViewById(R.id.player4_card4_imageButton);
    }

    private void growCardAnimation(ImageButton card){
        //bounds remain the same only image changes
        ObjectAnimator.ofFloat(card, "scaleX", 1.0f, 1.3f).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "scaleY", 1.0f, 1.3f).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "x", -15).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "y", -15).setDuration(600).start();
    }

}
