package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * this is an example for a zoomable and scrollable layout
 */
public class InGameActivity extends AppCompatActivity {

    private com.otaliastudios.zoom.ZoomLayout zoomLayout;

    private ImageButton chatButton;
    private ImageButton settingsButton;
    private Button caboButton;
    private ImageButton zoomButton;
    private int zoomBtnCount = 0;

    private ImageButton playedCardsStackButton;
    private ImageButton pickCardsStackButton;

    private InGameChatFragment chatFragment;

    private List<ImageButton> playerCardButtons = new ArrayList<>();
    private List<de.hdodenhof.circleimageview.CircleImageView> playerPics = new ArrayList<>();
    private List<TextView> playerStats = new ArrayList<>();
    private List<TextView> playerNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ingame_activity);

        //link layout
        zoomLayout = (com.otaliastudios.zoom.ZoomLayout) findViewById(R.id.zoomlayout);
        chatButton = (ImageButton) findViewById(R.id.chat_button);
        settingsButton = (ImageButton) findViewById(R.id.settings_button);
        zoomButton = (ImageButton) findViewById(R.id.zoom_button);
        caboButton = (Button) findViewById(R.id.cabo_button);
        playedCardsStackButton = (ImageButton) findViewById(R.id.played_cards_imageButton);
        pickCardsStackButton = (ImageButton) findViewById(R.id.pick_card_imageButton);

        chatFragment = new InGameChatFragment();

        //TODO insert Number of Players here
        setUpPlayerStats(4);

        setUpOnClickListeners();

        //TODO: Chat fragment integration
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_chat, InGameChatFragment.class, null)
                    .hide(chatFragment)
                    .commit();
        }*/


    }

    private void addChatFragment(){
        // Begin the transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment
        ft.replace(R.id.fragment_chat_container, new InGameChatFragment());
            // or ft.add(R.id.your_placeholder, new FooFragment());
// Complete the changes added above
        ft.commit();
    }

    private void setUpOnClickListeners(){

        setAllCardsOnClickListeners();

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Open chat...", Toast.LENGTH_SHORT).show();
                addChatFragment();

            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "Open settings...", Toast.LENGTH_SHORT).show();
            }
        });

        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(zoomBtnCount%2==0){
                    zoomLayout.zoomTo(1.3f, true);
                    zoomButton.setImageResource(R.drawable.zoom_in);
                }
                else{
                    zoomLayout.zoomTo(1.0f, true);
                    zoomButton.setImageResource(R.drawable.zoom_out);
                }
                zoomBtnCount++;
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

    private void setUpPlayerStats(int nrPlayers){

        Collections.addAll(playerCardButtons, findViewById(R.id.player1_card1_imageButton), findViewById(R.id.player1_card2_imageButton), findViewById(R.id.player1_card3_imageButton), findViewById(R.id.player1_card4_imageButton),
                findViewById(R.id.player2_card1_imageButton), findViewById(R.id.player2_card2_imageButton), findViewById(R.id.player2_card3_imageButton), findViewById(R.id.player2_card4_imageButton),
                findViewById(R.id.player3_card1_imageButton), findViewById(R.id.player3_card2_imageButton), findViewById(R.id.player3_card3_imageButton), findViewById(R.id.player3_card4_imageButton),
                findViewById(R.id.player4_card1_imageButton), findViewById(R.id.player4_card2_imageButton), findViewById(R.id.player4_card3_imageButton), findViewById(R.id.player4_card4_imageButton));

        Collections.addAll(playerPics, findViewById(R.id.player1_image_game), findViewById(R.id.player2_image_game), findViewById(R.id.player3_image_game), findViewById(R.id.player4_image_game));

        Collections.addAll(playerStats, findViewById(R.id.player1_info_game), findViewById(R.id.player2_info_game), findViewById(R.id.player3_info_game), findViewById(R.id.player4_info_game));

        Collections.addAll(playerNames, findViewById(R.id.player1_name_game), findViewById(R.id.player2_name_game), findViewById(R.id.player3_name_game), findViewById(R.id.player4_name_game));

        for(int i=nrPlayers; i<4; i++){
            playerPics.get(i).setVisibility(View.INVISIBLE);
            playerStats.get(i).setVisibility(View.INVISIBLE);
            playerNames.get(i).setVisibility(View.INVISIBLE);
        }

        for(int i=nrPlayers*4; i<16; i++){
            playerCardButtons.get(i).setVisibility(View.INVISIBLE);
        }
    }

    private void growCardAnimation(ImageButton card){
        //bounds remain the same only image changes
        ObjectAnimator.ofFloat(card, "scaleX", 1.0f, 1.3f).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "scaleY", 1.0f, 1.3f).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "x", -15).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "y", -15).setDuration(600).start();
    }

}
