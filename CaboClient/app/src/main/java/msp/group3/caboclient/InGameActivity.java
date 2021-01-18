package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * this is an example for a zoomable and scrollable layout
 */
public class InGameActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {
    private WebSocketClient webSocketClient;
    private Communicator communicator;

    private com.otaliastudios.zoom.ZoomLayout zoomLayout;

    private ImageButton chatButton;
    private ImageButton settingsButton;
    private Button caboButton;
    private ImageButton zoomButton;
    private Button peekButton;
    private Button spyButton;
    private Button switchButton;
    private LinearLayout pickedCardButtonContainer;
    private ImageView pickedCardBigImageview;
    private ImageButton ownEmojiButton;
    private LinearLayout emojiSelectionContainer;
    private ImageButton happyEmojiButton;
    private ImageButton veryHappyEmojiButton;
    private ImageButton tongueEmojiButton;
    private ImageButton shockedEmojiButton;
    private ImageButton angryEmojiButton;
    private TextView updateText;
    private com.airbnb.lottie.LottieAnimationView cardSwapAnimation;
    private com.airbnb.lottie.LottieAnimationView tapPickCardAnimation;
    private com.airbnb.lottie.LottieAnimationView timerAnimation;


    private ImageView cardSwapBg;
    private int zoomBtnCount = 0;
    private int chatButtonCount = 0;
    private int nrCardsSelected = 0;


    private ImageButton playedCardsStackButton;
    private ImageView playedCardsStackGlow;
    private ImageView player1CardsGlow;
    private ImageButton pickCardsStackButton;

    private int MAX_PLAYERS = 0;

    private androidx.fragment.app.FragmentContainerView chatFragmentContainer;

    private final List<ImageButton> player1CardButtons = new ArrayList<>();
    private final List<ImageButton> player2CardButtons = new ArrayList<>();
    private final List<ImageButton> player3CardButtons = new ArrayList<>();
    private final List<ImageButton> player4CardButtons = new ArrayList<>();
    private final List<List<ImageButton>> otherPlayerButtonLists = new ArrayList<>();

    private final List<de.hdodenhof.circleimageview.CircleImageView> playerPics = new ArrayList<>();
    private final List<TextView> playerStats = new ArrayList<>();
    private final List<TextView> playerNames = new ArrayList<>();
    private final List<Integer> player1CardClickCounts = new ArrayList<>();
    private final List<Integer> player2CardClickCounts = new ArrayList<>();
    private final List<Integer> player3CardClickCounts = new ArrayList<>();
    private final List<Integer> player4CardClickCounts = new ArrayList<>();
    private final List<com.airbnb.lottie.LottieAnimationView> playerHighlightAnimations = new ArrayList<>();


    private final List<ConstraintLayout> playerOverviews = new ArrayList<>();

    private Player me;
    private ArrayList<Player> otherPlayers = new ArrayList<>();
    private int playingPlayerId = 0;

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
        peekButton = (Button) findViewById(R.id.peek_button);
        peekButton.setVisibility(View.INVISIBLE);
        spyButton = (Button) findViewById(R.id.spy_button);
        spyButton.setVisibility(View.INVISIBLE);
        switchButton = (Button) findViewById(R.id.switch_button);
        switchButton.setVisibility(View.INVISIBLE);
        pickedCardButtonContainer = (LinearLayout) findViewById(R.id.picked_card_button_container);
        pickedCardButtonContainer.setVisibility(View.INVISIBLE);
        pickedCardBigImageview = (ImageView) findViewById(R.id.picked_card_big_imageview);
        ownEmojiButton = (ImageButton) findViewById(R.id.player1_emoji);
        emojiSelectionContainer = (LinearLayout) findViewById(R.id.emoji_selection_container);
        emojiSelectionContainer.setVisibility(View.INVISIBLE);
        happyEmojiButton = (ImageButton) findViewById(R.id.emoji_happy_button);
        veryHappyEmojiButton = (ImageButton) findViewById(R.id.emoji_very_happy_button);
        tongueEmojiButton = (ImageButton) findViewById(R.id.emoji_tongue_button);
        shockedEmojiButton = (ImageButton) findViewById(R.id.emoji_shocked);
        angryEmojiButton = (ImageButton) findViewById(R.id.emoji_angry);
        updateText = (TextView) findViewById(R.id.update_text);
        updateText.setVisibility(View.INVISIBLE);
        cardSwapAnimation = findViewById(R.id.card_swap_animationView);
        cardSwapAnimation.setVisibility(View.INVISIBLE);
        cardSwapBg = findViewById(R.id.card_swap_animationView_bg);
        cardSwapBg.setVisibility(View.INVISIBLE);
        tapPickCardAnimation = findViewById(R.id.tap_pick_animationView);
        tapPickCardAnimation.setVisibility(View.INVISIBLE);
        timerAnimation = findViewById(R.id.timer_animationView);
        timerAnimation.setVisibility(View.INVISIBLE);

        playedCardsStackButton = (ImageButton) findViewById(R.id.played_cards_imageButton);
        playedCardsStackGlow = findViewById(R.id.card_glow_imageview);
        playedCardsStackGlow.setVisibility(View.INVISIBLE);
        player1CardsGlow = findViewById(R.id.player1_card_glow_imageview);
        player1CardsGlow.setVisibility(View.INVISIBLE);
        pickCardsStackButton = (ImageButton) findViewById(R.id.pick_card_imageButton);

        setUpPlayerStats();
        setUpOnClickListeners();

        pickCardsStackButton.setEnabled(false);
        //playedCardsStackButton.setEnabled(false);

        //TODO: Chat fragment integration
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("some_int", 0);
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_chat, InGameChatFragment.class, bundle)
                    .commit();
        }

        chatFragmentContainer = findViewById(R.id.fragment_chat);
        chatFragmentContainer.setVisibility(View.INVISIBLE);


        communicator = Communicator.getInstance(this);
        webSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);

        try {
            communicator.sendMessage(JSON_commands.askForInitialSettings("text"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setUpOnClickListeners() {

        //initiatePeekAndSwapAction();
        //initiateInitialCardLookUp();
        //initiateSpyAction();

        //testIndicatePlayerTurn(1);


        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatButtonCount++;
                if (chatButtonCount % 2 == 0) {
                    chatFragmentContainer.setVisibility(View.INVISIBLE);
                } else {
                    chatFragmentContainer.setVisibility(View.VISIBLE);
                }
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
                if (zoomBtnCount % 2 == 0) {
                    zoomLayout.zoomTo(1.3f, true);
                    zoomButton.setImageResource(R.drawable.zoom_in);
                } else {
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
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendCabo("cabo")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        pickCardsStackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendPickCard("memorized")));
                    Log.d("----------------------PICK", "clicked pick card and send to server");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tapPickCardAnimation.setVisibility(View.INVISIBLE);
                        growCardGlowAnimation(playedCardsStackGlow);
                        growCardGlowAnimation(player1CardsGlow);
                        cardPopAnimation(pickCardsStackButton);
                    }
                });
            }
        });

        ownEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiSelectionContainer.setVisibility(View.VISIBLE);
            }
        });

        happyEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_happy);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
            }
        });

        veryHappyEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_very_happy);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
            }
        });

        tongueEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_tounge);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
            }
        });

        shockedEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_shocked);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
            }
        });

        angryEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_angry);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void setPlayer1CardsOnClickListeners(int cardsAllowed) {
        Log.d("-----------ON CLICK LISTENER PLAYER 1", "initiating");
        for (ImageButton cardButton : player1CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (cardButton.isSelected()) {
                        nrCardsSelected--;
                        cardButton.setSelected(false);
                        /*Toast.makeText(getApplicationContext(),
                                "Cards selected: "+nrCardsSelected, Toast.LENGTH_SHORT).show();*/
                    } else {
                        cardButton.setSelected(true);
                        nrCardsSelected++;
                        if (nrCardsSelected > cardsAllowed) {
                            for (ImageButton otherCard : player1CardButtons) {
                                if (otherCard != cardButton && otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player3CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player4CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player2CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                        }
                    }
                    if(pickedCardButtonContainer.getVisibility()==View.VISIBLE){
                        switchButton.setVisibility(View.VISIBLE);
                    }
                    if(nrCardsSelected==cardsAllowed){
                        peekButton.setEnabled(true);
                    }
                    Log.d("-----------ON CLICK LISTENER PLAYER 1", "#selected: "+nrCardsSelected);
                    Log.d("-----------ON CLICK LISTENER PLAYER 1", "#allowed: "+cardsAllowed);

                    player1CardsGlow.setVisibility(View.INVISIBLE);
                    playedCardsStackGlow.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void setPlayer2CardsOnClickListeners(int cardsAllowed){
        for(ImageButton cardButton : player2CardButtons){
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   /* Toast.makeText(getApplicationContext(),
                            "Card clicked: "+getResources().getResourceEntryName(cardButton.getId()), Toast.LENGTH_SHORT).show();*/

                    zoomInOnSelectedCard(cardButton);

                    if(cardButton.isSelected()){
                        cardButton.setSelected(false);
                        nrCardsSelected--;
                    }
                    else{
                        cardButton.setSelected(true);
                        nrCardsSelected++;
                        if(nrCardsSelected>cardsAllowed){
                            for( ImageButton otherCard : player2CardButtons){
                                if(otherCard != cardButton && otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player3CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player4CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player1CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                        }

                    }
                    if(nrCardsSelected==cardsAllowed){
                        spyButton.setEnabled(true);
                        peekButton.setEnabled(true);
                    }
                }
            });
        }
    }

    private void setPlayer3CardsOnClickListeners(int cardsAllowed){
        for(ImageButton cardButton : player3CardButtons){
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   /* Toast.makeText(getApplicationContext(),
                            "Card clicked: "+getResources().getResourceEntryName(cardButton.getId()), Toast.LENGTH_SHORT).show();*/

                    zoomInOnSelectedCard(cardButton);

                    if(cardButton.isSelected()){
                        cardButton.setSelected(false);
                        nrCardsSelected--;
                    }
                    else{
                        cardButton.setSelected(true);
                        nrCardsSelected++;
                        if(nrCardsSelected>cardsAllowed){
                            for( ImageButton otherCard : player3CardButtons){
                                if(otherCard != cardButton && otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player2CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player4CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player1CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                        }

                    }
                    if(nrCardsSelected==cardsAllowed){
                        spyButton.setEnabled(true);
                        peekButton.setEnabled(true);
                    }
                }
            });
        }
    }

    private void setPlayer4CardsOnClickListeners(int cardsAllowed){
        for(ImageButton cardButton : player4CardButtons){
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   /* Toast.makeText(getApplicationContext(),
                            "Card clicked: "+getResources().getResourceEntryName(cardButton.getId()), Toast.LENGTH_SHORT).show();*/

                    zoomInOnSelectedCard(cardButton);

                    if(cardButton.isSelected()){
                        cardButton.setSelected(false);
                        nrCardsSelected--;
                    }
                    else{
                        cardButton.setSelected(true);
                        nrCardsSelected++;
                        if(nrCardsSelected>cardsAllowed){
                            for( ImageButton otherCard : player4CardButtons){
                                if(otherCard != cardButton && otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player3CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player2CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for( ImageButton otherCard : player1CardButtons){
                                if(otherCard.isSelected()){
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                        }
                    }
                    if(nrCardsSelected==cardsAllowed){
                        spyButton.setEnabled(true);
                        peekButton.setEnabled(true);
                    }
                }
            });
        }
    }

    private void deactivateAllOnCardClickListeners(){
        deactivatePlayer1OnClickListeners();
        deactivatePlayer2OnClickListeners();
        deactivatePlayer3OnClickListeners();
        deactivatePlayer4OnClickListeners();
    }

    private void deactivatePlayer1OnClickListeners(){
        for(ImageButton cardButton : player1CardButtons){
            cardButton.setOnClickListener(null);
        }
    }

    private void deactivatePlayer2OnClickListeners(){
        for(ImageButton cardButton : player2CardButtons){
            cardButton.setOnClickListener(null);
        }
    }

    private void deactivatePlayer3OnClickListeners(){
        for(ImageButton cardButton : player3CardButtons){
            cardButton.setOnClickListener(null);
        }
    }

    private void deactivatePlayer4OnClickListeners(){
        for(ImageButton cardButton : player4CardButtons){
            cardButton.setOnClickListener(null);
        }
    }


    private void setUpPlayerStats() {

        Collections.addAll(player1CardButtons, findViewById(R.id.player1_card1_imageButton), findViewById(R.id.player1_card2_imageButton), findViewById(R.id.player1_card3_imageButton), findViewById(R.id.player1_card4_imageButton));
        Collections.addAll(player2CardButtons, findViewById(R.id.player2_card1_imageButton), findViewById(R.id.player2_card2_imageButton), findViewById(R.id.player2_card3_imageButton), findViewById(R.id.player2_card4_imageButton));
        Collections.addAll(player3CardButtons, findViewById(R.id.player3_card1_imageButton), findViewById(R.id.player3_card2_imageButton), findViewById(R.id.player3_card3_imageButton), findViewById(R.id.player3_card4_imageButton));
        Collections.addAll(player4CardButtons, findViewById(R.id.player4_card1_imageButton), findViewById(R.id.player4_card2_imageButton), findViewById(R.id.player4_card3_imageButton), findViewById(R.id.player4_card4_imageButton));

        Collections.addAll(playerPics, findViewById(R.id.player1_image_game), findViewById(R.id.player2_image_game), findViewById(R.id.player3_image_game), findViewById(R.id.player4_image_game));

        Collections.addAll(playerStats, findViewById(R.id.player1_info_game), findViewById(R.id.player2_info_game), findViewById(R.id.player3_info_game), findViewById(R.id.player4_info_game));

        Collections.addAll(playerNames, findViewById(R.id.player1_name_game), findViewById(R.id.player2_name_game), findViewById(R.id.player3_name_game), findViewById(R.id.player4_name_game));

        Collections.addAll(player1CardClickCounts, 0, 0, 0, 0);
        Collections.addAll(player2CardClickCounts, 0, 0, 0, 0);
        Collections.addAll(player3CardClickCounts, 0, 0, 0, 0);
        Collections.addAll(player4CardClickCounts, 0, 0, 0, 0);

        Collections.addAll(playerOverviews, findViewById(R.id.player1_stats_game), findViewById(R.id.player2_stats_game), findViewById(R.id.player3_stats_game), findViewById(R.id.player4_stats_game));

        Collections.addAll(playerHighlightAnimations, findViewById(R.id.player1_highlight_animationView), findViewById(R.id.player2_highlight_animationView),
                findViewById(R.id.player3_highlight_animationView), findViewById(R.id.player4_highlight_animationView));

        Collections.addAll(otherPlayerButtonLists, player2CardButtons, player3CardButtons, player4CardButtons);

        for(int i=0; i<4; i++){
            player1CardButtons.get(i).setVisibility(View.INVISIBLE);
            player2CardButtons.get(i).setVisibility(View.INVISIBLE);
            player3CardButtons.get(i).setVisibility(View.INVISIBLE);
            player4CardButtons.get(i).setVisibility(View.INVISIBLE);
            playerPics.get(i).setVisibility(View.INVISIBLE);
            playerStats.get(i).setVisibility(View.INVISIBLE);
            playerNames.get(i).setVisibility(View.INVISIBLE);
            playerOverviews.get(i).setVisibility(View.INVISIBLE);
            playerHighlightAnimations.get(i).setVisibility(View.INVISIBLE);
        }
    }

    private void visualizePlayerStats(int nrPlayers){
        playerPics.get(0).setVisibility(View.VISIBLE);
        playerStats.get(0).setVisibility(View.VISIBLE);
        playerNames.get(0).setVisibility(View.VISIBLE);
        playerOverviews.get(0).setVisibility(View.VISIBLE);
        for(ImageButton card : player1CardButtons){
            card.setVisibility(View.VISIBLE);
        }

        for(int i=1; i<nrPlayers; i++){
            playerPics.get(i).setVisibility(View.VISIBLE);
            playerStats.get(i).setVisibility(View.VISIBLE);
            playerNames.get(i).setVisibility(View.VISIBLE);
            playerOverviews.get(i).setVisibility(View.VISIBLE);
            for(ImageButton cardButton : otherPlayerButtonLists.get(i-1)){
                cardButton.setVisibility(View.VISIBLE);
            }
            //playerHighlightAnimations.get(i).setVisibility(View.VISIBLE);
        }
    }

    private void growCardGlowAnimation(ImageView card){
        //bounds remain the same only image changes
        AlphaAnimation fade_in = new AlphaAnimation(0f, 1f);
        fade_in.setDuration(2000);
        fade_in.setFillAfter(true);
        card.startAnimation(fade_in);

        ScaleAnimation grow_in = new ScaleAnimation(0.8f, 1f, 0.8f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow_in.setDuration(1000);
        grow_in.setFillAfter(true);
        card.startAnimation(grow_in);
    }

    private void growCardGlowAnimationOut(ImageView card){
        //bounds remain the same only image changes
        AlphaAnimation fade_out = new AlphaAnimation(1f, 0f);
        fade_out.setDuration(2000);
        fade_out.setFillAfter(true);
        card.startAnimation(fade_out);

        ScaleAnimation grow_out = new ScaleAnimation(1.0f, 0f, 1.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow_out.setDuration(1000);
        grow_out.setFillAfter(true);
        card.startAnimation(grow_out);
    }

    private void cardPopAnimation(ImageButton button){
        ScaleAnimation grow_in = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow_in.setDuration(500);
        grow_in.setStartOffset(500);
        button.startAnimation(grow_in);
        ScaleAnimation grow_out = new ScaleAnimation(1.2f, 1f, 1.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow_out.setDuration(500);
        grow_out.setStartOffset(500);
        button.startAnimation(grow_out);
    }

    //TODO insert actual image of card depending on value
    private void animateCardTurn(ImageButton cardButton){
        final ObjectAnimator oa1 = ObjectAnimator.ofFloat(cardButton, "scaleX", 1f, 0f);
        final ObjectAnimator oa2 = ObjectAnimator.ofFloat(cardButton, "scaleX", 0f, 1f);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());
        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Card card = getCardFromButton(cardButton);
                cardButton.setImageResource(getCardResource(card));
                oa2.start();
            }
        });
        oa1.start();
    }

    private Card getCardFromButton(ImageButton cardButton){
        if(player1CardButtons.contains(cardButton)){
            return me.getMyCards().get(player1CardButtons.indexOf(cardButton));
        }
        if(player2CardButtons.contains(cardButton)){
            return otherPlayers.get(0).getMyCards().get(player2CardButtons.indexOf(cardButton));
        }
        if(player3CardButtons.contains(cardButton)){
            return otherPlayers.get(1).getMyCards().get(player3CardButtons.indexOf(cardButton));
        }
        if(player4CardButtons.contains(cardButton)){
            return otherPlayers.get(2).getMyCards().get(player4CardButtons.indexOf(cardButton));
        }
        return null;
    }

    private void animateCardTurnBack(ImageButton cardButton){
        final ObjectAnimator oa1 = ObjectAnimator.ofFloat(cardButton, "scaleX", 1f, 0f);
        final ObjectAnimator oa2 = ObjectAnimator.ofFloat(cardButton, "scaleX", 0f, 1f);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());
        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cardButton.setSelected(false);
                cardButton.setImageResource(R.drawable.card_button);
                oa2.start();
            }
        });
        oa1.start();
    }

    //TODO put card as parameter
    private void showPickedCardInContainer(Card card){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pickedCardBigImageview.setImageResource(R.drawable.card_back);
                pickedCardButtonContainer.setVisibility(View.VISIBLE);
                final ObjectAnimator oa1 = ObjectAnimator.ofFloat(pickedCardBigImageview, "scaleX", 1f, 0f);
                final ObjectAnimator oa2 = ObjectAnimator.ofFloat(pickedCardBigImageview, "scaleX", 0f, 1f);
                oa1.setInterpolator(new DecelerateInterpolator());
                oa2.setInterpolator(new AccelerateDecelerateInterpolator());
                oa1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        pickedCardBigImageview.setImageResource(getCardResource(card));
                        oa2.start();
                    }
                });
                oa1.start();
                enablePlayedCardStackButton(card);
                setPlayer1CardsOnClickListeners(1);
                switchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        playSwapAnimation();
                        deactivateAllOnCardClickListeners();
                        switchButton.setVisibility(View.INVISIBLE);
                        makePickedCardContainerDisappear();
                        growCardGlowAnimationOut(playedCardsStackGlow);
                        growCardGlowAnimationOut(player1CardsGlow);
                        for(int i = 0; i<player1CardButtons.size(); i++){
                            if(player1CardButtons.get(i).isSelected()){
                                try {
                                    webSocketClient.send(String.valueOf(JSON_commands.swapPickedCardWithOwnCards(me.getMyCards().get(i))));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for(ImageButton cardButton : player1CardButtons){
                                    cardButton.setSelected(false);
                                }
                            }
                        });

                    }
                });
            }
        });
    }

    private void enablePlayedCardStackButton(Card pickedCard){
        playedCardsStackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d("----------------------SEND", "play picked card");
                    webSocketClient.send(String.valueOf(JSON_commands.playPickedCard()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(pickedCard.getValue()!=7 && pickedCard.getValue()!=8 && pickedCard.getValue()!=9 && pickedCard.getValue()!=10 && pickedCard.getValue()!=11 && pickedCard.getValue()!=12){
                    try {
                        Log.d("----------------------SEND", "finish");
                        webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pickCardsStackButton.setEnabled(false);
                        growCardGlowAnimationOut(player1CardsGlow);
                        growCardGlowAnimationOut(playedCardsStackGlow);
                        cardPopAnimation(playedCardsStackButton);
                        /*Toast.makeText(getApplicationContext(),
                                "Discard card...", Toast.LENGTH_SHORT).show();*/
                        makePickedCardContainerDisappear();
                        switchButton.setVisibility(View.INVISIBLE);
                        for(ImageButton cardButton : player1CardButtons){
                            cardButton.setSelected(false);
                        }
                        deactivateAllOnCardClickListeners();
                    }
                });
            }
        });
        playedCardsStackButton.setEnabled(true);
    }

    private void makePickedCardContainerDisappear(){
        pickedCardButtonContainer.setVisibility(View.INVISIBLE);
        pickedCardBigImageview.setImageResource(R.drawable.card_back);
    }

    private void zoomInOnSelectedCard(ImageButton cardButton){
        int[] location = new int[2];
        cardButton.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.d("-----------PAN", "x: "+zoomLayout.getPanX()/zoomLayout.getZoom()+ " y: "+zoomLayout.getPanY()/zoomLayout.getZoom());
        Log.d("-----------SCREEN", "x: "+width+ " y: "+height);
        Log.d("-----------CARD POSITION", "x: "+x+ " y: "+y);
        //zoomLayout.panTo( 2*width-zoomLayout.getScaledPanX()+x, 2*height-zoomLayout.getScaledPanY()+y, true);
    }

    //TODO replace resources
    private int getCardResource(Card card){
        switch(card.getValue()){
            case -1: return R.drawable.card_joker_1;
            case 0: return R.drawable.card_hearts_k;
            case 1: return R.drawable.card_hearts_a;
            case 2: return R.drawable.card_clubs_2;
            case 3: return R.drawable.card_clubs_3;
            case 4: return R.drawable.card_clubs_4;
            case 5: return R.drawable.card_clubs_5;
            case 6: return R.drawable.card_clubs_6;
            case 7: return R.drawable.card_clubs_7;
            case 8: return R.drawable.card_clubs_8;
            case 9: return R.drawable.card_clubs_9;
            case 10: return R.drawable.card_clubs_10;
            case 11: return R.drawable.card_clubs_j;
            case 12: return R.drawable.card_clubs_q;
            case 13: return R.drawable.card_clubs_k;
        }
        return 0;
    }

    private void initiateCardAction(Card pickedCard) throws JSONException {
        int value = pickedCard.getValue();
        switch (value) {
            case 7:
                initiatePeekAction();
                break;
            case 8:
                initiatePeekAction();
                break;
            case 9:
                initiateSpyAction();
                break;
            case 10:
                initiateSpyAction();
                break;
            case 11:
                initiateBlindSwapAction();
                break;
            case 12:
                initiateBlindSwapAction();
                break;
            case 13:
                //initiatePeekAndSwapAction();
                break;
           // default:
           //     webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
           //     break;
        }
    }

    //TODO
    private void initiatePeekAndSwapAction() {
        peekButton.setVisibility(View.VISIBLE);
        updateText.setVisibility(View.VISIBLE);
        peekButton.setEnabled(false);
        updateText.setText("Please choose 2 cards");
        setPlayer1CardsOnClickListeners(2);
        setPlayer2CardsOnClickListeners(2);
        setPlayer3CardsOnClickListeners(2);
        setPlayer4CardsOnClickListeners(2);

        ArrayList<ImageButton> selectedCards = new ArrayList<>();

        peekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ImageButton cardButton : player1CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                    }
                }
                for(ImageButton cardButton : player2CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                    }
                }
                for(ImageButton cardButton : player3CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                    }
                }
                for(ImageButton cardButton : player4CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                    }
                }
                for(ImageButton card : selectedCards){
                    animateCardTurn(card);
                }
                updateText.setText("Do you want to swap?");
                peekButton.setVisibility(View.INVISIBLE);
                switchButton.setVisibility(View.VISIBLE);
                switchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(ImageButton card : selectedCards){
                            animateCardTurnBack(card);
                        }
                        playSwapAnimation();
                        updateText.setVisibility(View.INVISIBLE);
                        switchButton.setVisibility(View.INVISIBLE);
                        deactivateAllOnCardClickListeners();
                    }
                });
                nrCardsSelected = 0;
            }
        });
    }

    //TODO send selected cards to server
    private void initiateBlindSwapAction() {
        switchButton.setVisibility(View.VISIBLE);
        updateText.setVisibility(View.VISIBLE);
        updateText.setText("Please choose 2 cards");
        setPlayer1CardsOnClickListeners(2);
        setPlayer2CardsOnClickListeners(2);
        setPlayer3CardsOnClickListeners(2);
        setPlayer4CardsOnClickListeners(2);

        ArrayList<ImageButton> selectedCards = new ArrayList<>();

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ImageButton cardButton : player1CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                        cardButton.setSelected(false);
                    }
                }
                for(ImageButton cardButton : player2CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                        cardButton.setSelected(false);
                    }
                }
                for(ImageButton cardButton : player3CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                        cardButton.setSelected(false);
                    }
                }
                for(ImageButton cardButton : player4CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                        cardButton.setSelected(false);
                    }
                }
                nrCardsSelected = 0;
                playSwapAnimation();
                updateText.setVisibility(View.INVISIBLE);
                switchButton.setVisibility(View.INVISIBLE);
                deactivateAllOnCardClickListeners();
                //TODO
                try {
                    Card card1 = getCardObjectFromButton(selectedCards.get(0));
                    Player card1Owner = getCardOwner(selectedCards.get(0));
                    Card card2 = getCardFromButton(selectedCards.get(1));
                    Player card2Owner = getCardOwner(selectedCards.get(1));
                    Log.d("-----------SEND TO SERVER SWAP", "card1"+card1.getValue()+" by "+card1Owner.getName());
                    Log.d("-----------SEND TO SERVER SWAP", "card2"+card2.getValue()+" by "+card2Owner.getName());

                    webSocketClient.send(String.valueOf(JSON_commands.useFunctionalitySwap(card1, card1Owner, card2, card2Owner)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private Player getCardOwner(ImageButton cardButton){
        for(int i=0; i<otherPlayers.size(); i++){
            if(otherPlayerButtonLists.get(i).contains(cardButton)){
                return otherPlayers.get(i);
            }
        }
        if(player1CardButtons.contains(cardButton)){
            return me;
        }
        return null;
    }
    //TODO nochmal anschauen
    private Card getCardObjectFromButton(ImageButton cardButton){
        if(player1CardButtons.contains(cardButton)){
            int index = player1CardButtons.indexOf(cardButton);
            return me.getMyCards().get(index);
        }
        if(player2CardButtons.contains(cardButton)){
            int index = player2CardButtons.indexOf(cardButton);
            return otherPlayers.get(0).getMyCards().get(index);
        }
        if(player3CardButtons.contains(cardButton)){
            int index = player3CardButtons.indexOf(cardButton);
            return otherPlayers.get(1).getMyCards().get(index);
        }
        if(player4CardButtons.contains(cardButton)){
            int index = player4CardButtons.indexOf(cardButton);
            return otherPlayers.get(2).getMyCards().get(index);
        }
        return null;
    }

    private void playSwapAnimation() {
        cardSwapAnimation.setVisibility(View.VISIBLE);
        cardSwapBg.setVisibility(View.VISIBLE);
        cardSwapAnimation.playAnimation();
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                cardSwapAnimation.playAnimation();
                cardSwapAnimation.setVisibility(View.INVISIBLE);
                cardSwapBg.setVisibility(View.INVISIBLE);
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    private void initiateSpyAction() {
        spyButton.setVisibility(View.VISIBLE);
        spyButton.setEnabled(false);
        updateText.setVisibility(View.VISIBLE);
        updateText.setText("Please choose an enemy card");
        setPlayer2CardsOnClickListeners(1);
        setPlayer3CardsOnClickListeners(1);
        setPlayer4CardsOnClickListeners(1);

        nrCardsSelected = 0;
        spyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                for(ImageButton cardButton : player2CardButtons){
                    if(cardButton.isSelected()){
                        animateCardTurn(cardButton);
                        setCountdownTimer(cardButton);
                        spyButton.setVisibility(View.INVISIBLE);
                    }
                }
                for(ImageButton cardButton : player3CardButtons){
                    if(cardButton.isSelected()){
                        animateCardTurn(cardButton);
                        setCountdownTimer(cardButton);
                        spyButton.setVisibility(View.INVISIBLE);
                    }
                }
                for(ImageButton cardButton : player4CardButtons){
                    if(cardButton.isSelected()){
                        animateCardTurn(cardButton);
                        setCountdownTimer(cardButton);
                        spyButton.setVisibility(View.INVISIBLE);
                    }
                }
                updateText.setVisibility(View.INVISIBLE);
                deactivateAllOnCardClickListeners();
                nrCardsSelected = 0;
                Card selectedCard=null;
                Player spiedOnPlayer = null;
                for(int i = 0; i<otherPlayers.size(); i++){
                    if(getSelectedCard(otherPlayerButtonLists.get(i), otherPlayers.get(i))!=null){
                        selectedCard=getSelectedCard(otherPlayerButtonLists.get(i), otherPlayers.get(0));
                        spiedOnPlayer=otherPlayers.get(i);

                    }
                }
                try {
                    //TimeUnit.SECONDS.sleep(10);
                    //TODO send player that is spied on NOT me
                    webSocketClient.send(String.valueOf(JSON_commands.useFunctionalitySpy(selectedCard, spiedOnPlayer)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initiatePeekAction() {
        peekButton.setVisibility(View.VISIBLE);
        updateText.setVisibility(View.VISIBLE);
        updateText.setText("Please choose 1 of your cards");
        peekButton.setEnabled(false);

        nrCardsSelected = 0;
        setPlayer1CardsOnClickListeners(1);

        peekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("-----------Peek", "button clicked");

                for(ImageButton cardButton : player1CardButtons){
                    if(cardButton.isSelected()){
                        animateCardTurn(cardButton);
                        setCountdownTimer(cardButton);
                        peekButton.setVisibility(View.INVISIBLE);
                    }
                }

                try {
                    webSocketClient.send(String.valueOf(JSON_commands.useFunctionalityPeek(getSelectedCard(player1CardButtons, me))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateText.setVisibility(View.INVISIBLE);
                        deactivateAllOnCardClickListeners();
                        nrCardsSelected = 0;

                    }
                });
            }
        });
    }

    private Card getSelectedCard(List<ImageButton> cardButtons, Player owner){
        for(int i=0; i<cardButtons.size(); i++){
            if(cardButtons.get(i).isSelected()){
                return owner.getMyCards().get(i);
            }
        }
        return null;
    }

    private void initiateInitialCardLookUp(){
        peekButton.setVisibility(View.VISIBLE);
        updateText.setVisibility(View.VISIBLE);
        updateText.setText("Select 2 of your cards to look at");
        peekButton.setEnabled(false);
        Log.d("-----------Card lookup", "initiating");
        setPlayer1CardsOnClickListeners(2);

        ArrayList<ImageButton> selectedCards = new ArrayList<>();

        peekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(ImageButton cardButton : player1CardButtons){
                    if(cardButton.isSelected()){
                        selectedCards.add(cardButton);
                    }
                    for(ImageButton card: selectedCards){
                        animateCardTurn(card);
                        setCountdownTimer(card);
                    }
                    peekButton.setVisibility(View.INVISIBLE);
                    new CountDownTimer(10000, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            try {
                                webSocketClient.send(String.valueOf(JSON_commands.sendMemorized("memorized")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                updateText.setVisibility(View.INVISIBLE);
                deactivateAllOnCardClickListeners();
                nrCardsSelected = 0;
            }
        });
    }

    private void setCountdownTimer(ImageButton cardButton){
        updateText.setText("Please remember the card");
        timerAnimation.setVisibility(View.VISIBLE);
        timerAnimation.playAnimation();

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                timerAnimation.setVisibility(View.INVISIBLE);
                timerAnimation.cancelAnimation();
                updateText.setVisibility(View.INVISIBLE);
                updateText.setVisibility(View.INVISIBLE);
                animateCardTurnBack(cardButton);
            }

        }.start();
    }

    private void showSpiedOnCard(Player spiedOnPlayer, Card card){
        if(spiedOnPlayer.getId()==me.getId()){
            ImageButton cardButton = player1CardButtons.get(getCardIndex(me, card));
            updateText.setText("You are being spied on");
            cardButton.setImageResource(R.drawable.card_spied);
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    cardButton.setImageResource(R.drawable.card_button);
                }

            }.start();
        }
        else{
            int playerIndex = otherPlayers.indexOf(spiedOnPlayer);
            int cardIndex = getCardIndex(spiedOnPlayer, card);
            ImageButton cardButton = otherPlayerButtonLists.get(playerIndex).get(cardIndex);
            cardButton.setImageResource(R.drawable.card_spied);
            updateText.setText(spiedOnPlayer.getName()+" is being spied on");
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    cardButton.setImageResource(R.drawable.card_button);
                }

            }.start();
        }
    }

    //TODO
    private void showSwappedCards(Player swappingPlayer, Card card){
        int playerIndex = otherPlayers.indexOf(swappingPlayer);
        int cardIndex = swappingPlayer.getMyCards().indexOf(card);
        ImageButton cardButton = otherPlayerButtonLists.get(playerIndex).get(cardIndex);
        cardButton.setImageResource(R.drawable.card_swapped);

        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                cardButton.setImageResource(R.drawable.card_button);
            }

        }.start();

    }

    //TODO write own indexOf
    private void showPeekedOnCard(Player peekingPlayer, Card card){
        int playerIndex = otherPlayers.indexOf(peekingPlayer);
        //int cardIndex = peekingPlayer.getMyCards().indexOf(card); //for some reason returns -1
        int cardIndex = getCardIndex(peekingPlayer, card);
        ImageButton cardButton = otherPlayerButtonLists.get(playerIndex).get(cardIndex);
        cardButton.setImageResource(R.drawable.card_spied);
        updateText.setText(peekingPlayer.getName()+" is peeking");

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                cardButton.setImageResource(R.drawable.card_button);
            }

        }.start();

    }

    private int getCardIndex(Player peekingPlayer, Card card){
        for(int i=0; i<peekingPlayer.getMyCards().size(); i++){
            if(peekingPlayer.getMyCards().get(i).equalsCard(card)){
                return i;
            }
        }
        return 0;
    }

    private void indicatePlayerTurn(Player player){
        for(com.airbnb.lottie.LottieAnimationView animation : playerHighlightAnimations){
            animation.setVisibility(View.INVISIBLE);
            animation.cancelAnimation();
        }
        if(player==me){
            playerHighlightAnimations.get(0).setVisibility(View.VISIBLE);
            playerHighlightAnimations.get(0).playAnimation();
        }
        else{
            int index = otherPlayers.indexOf(player)+1;
            playerHighlightAnimations.get(index).setVisibility(View.VISIBLE);
            playerHighlightAnimations.get(index).playAnimation();
        }
    }

    public void scaleView(View v, float factor) {
        Animation anim = new ScaleAnimation(
                1f, factor, // Start and end values for the X axis scaling
                1f, factor,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        v.startAnimation(anim);
    }

    private void displayDiscardedCard(Card card){
        playedCardsStackButton.setImageResource(getCardResource(card));

    }

    //TODO display picked Card to User
    private void displayPickedCard(Card card){

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        try {
            processExtraData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void processExtraData() throws JSONException {
        Intent intent = getIntent();
        String key = intent.getStringExtra("key");
        if (key != null) {
            switch (key) {
                case "Willkommen":

                    break;
                case "myID":
                    Toast.makeText(this, "This is my Toast message!",
                            Toast.LENGTH_LONG).show();
                    break;
                case "test2":
                    Toast.makeText(this, "client!",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    @Override
    public void handleTextMessage(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);

        if (jsonObject.has("chatMessage")) {
            String chatText = jsonObject.get("chatMessage").toString();
            // TODO pauline: den String chatText einfach nur anzeigen :)
        }

        if (jsonObject.has("sendMAXPlayer")) {
            int maxPlayer = (int) jsonObject.get("sendMAXPlayer");
            MAX_PLAYERS=maxPlayer;
            Log.d("----------------------MAXPLAYERS", "playerNr: "+maxPlayer);
            visualizePlayerStats(maxPlayer);
            // TODO pauline: hier wurde die Anzahl Spieler geschickt
        }
        if (jsonObject.has("initialMe")) {
            JSONObject js = jsonObject.getJSONObject("initialMe");
            if (js.has("me")) {
                String jsonString = js.get("me").toString();
                Gson gson = new Gson();
                me = gson.fromJson(jsonString, Player.class);
                // hier wurde me gesetzt
                Log.d("----------------------ME", "my name: "+me.getName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playerNames.get(0).setText(me.getName());
                        initiateInitialCardLookUp();
                    }
                });

                //webSocketClient.send(String.valueOf(JSON_commands.sendMemorized("memorized")));
            }
        }
        /*if (jsonObject.has("initialOtherPlayer")) {
            JSONObject js = jsonObject.getJSONObject("initialOtherPlayer");
            if (js.has("otherPlayer")) {
                String jsonString = js.get("otherPlayer").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() != me.getId()) {
                    if (!containsPlayer(player)) {
                        //TODO set player name
                        otherPlayers.add(player);
                        int index = otherPlayers.indexOf(player);
                        if (otherPlayers.size() == (MAX_PLAYERS - 1)) {
                            showNames();
                        }

                    }
                }
            }

        }*/
        if (jsonObject.has("initialOtherPlayer")) {
            JSONObject js = jsonObject.getJSONObject("initialOtherPlayer");
            Gson gson = new Gson();
            if (js.has("players")) {
                String jsonString = js.get("players").toString();
                List<Player> players = gson.fromJson(jsonString, new TypeToken<List<Player>>() {
                }.getType());
                otherPlayers.addAll(players);
                showNames();
            }

        }

        //TODO: wenn alles angezeigt wurde und der Spieler seine Karten angeschaut hat, muss folgendes gesendet werden:
        // webSocketClient.send(String.valueOf(JSON_commands.sendMemorized("memorized")));

        if (jsonObject.has("nextPlayer")) {
            int nextPlayerId = jsonObject.getInt("nextPlayer");
            playingPlayerId = nextPlayerId;
            Log.d("----------------------NEXT PLAYER", "id: " + nextPlayerId);
            Log.d("----------------------MY ID", "id: " + nextPlayerId);
            //updateText.setText("Its your turn: "+getPlayerById(nextPlayerId).getName());
            if (me.getId() == nextPlayerId) {
                //indicatePlayerTurn(me);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        indicatePlayerTurn(me);
                        updateText.setVisibility(View.VISIBLE);
                        updateText.setText("Pick a card");
                        tapPickCardAnimation.setVisibility(View.VISIBLE);
                        pickCardsStackButton.setEnabled(true);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Player player = getPlayerById(nextPlayerId);
                        if (player != null) {
                            indicatePlayerTurn(player);
                            updateText.setVisibility(View.VISIBLE);
                            updateText.setText("It's " + getPlayerById(nextPlayerId).getName() + "'s turn");
                        }
                    }
                });
            }
            //TODO pauline: call method which shows Client who's turn it is
            // dann muss der player auf den nachziehstapel tippen, um eine karte zu ziehen
            // du kannst immer über me.getStatus() überprüfen, ob der Client wirklich spielen darf -> der Stautus muss gleich "playing" siehe typeDefs sein
            //webSocketClient.send(String.valueOf(JSON_commands.sendPickCard("memorized")));
        }

        if (jsonObject.has("pickedCard")) {
            JSONObject js = jsonObject.getJSONObject("pickedCard");
            if (js.has("card")) {
                String jsonString = js.get("card").toString();
                Gson gson = new Gson();
                Card card = gson.fromJson(jsonString, Card.class);
                Log.d("----------------------PICKED CARD", "card value: " + card.getValue());

                //TODO pauline: das ist die Karte, die der Spieler von Nachziehstapel gezogen hat
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateText.setText("Swap or play the card");
                        showPickedCardInContainer(card);
                    }
                });

                //danach hat er folgende Möglichkeiten:
                //1. karte mit eigner Karte tauschen: webSocketClient.send(String.valueOf(JSON_commands.swapPickedCardWithOwnCards(card)));
                //2. karte ablegen und die Funktionalität nutzen:  webSocketClient.send(String.valueOf(JSON_commands.playPickedCard(card)));
                //webSocketClient.send(String.valueOf(JSON_commands.swapPickedCardWithOwnCards(card)));
                // webSocketClient.send(String.valueOf(JSON_commands.playPickedCard(card)));
            }

        }
        if (jsonObject.has("discardedCard")) {
            JSONObject js = jsonObject.getJSONObject("discardedCard");
            if (js.has("card")) {
                String jsonString = js.get("card").toString();
                Gson gson = new Gson();
                Card card = gson.fromJson(jsonString, Card.class);
                //TODO Pauline: dies ist die Karte, die der Spieler (der gerade an der Reihe ist) abgelegt (auf den Ablegestapel),
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateText.setText("A card has been discarded");
                        displayDiscardedCard(card);
                    }
                });
                //Indem er die gezogene Karte mit seiner eigenen Karte tauscht (!)
                //diese Karte hat sich also vorher unter den 4 eigenen Karten befunden und wurde jetzt mit der gezogenen Karte getauscht
            }

        }

        if (jsonObject.has("playedCard")) {
            JSONObject js = jsonObject.getJSONObject("playedCard");
            if (js.has("card")) {
                String jsonString = js.get("card").toString();
                Gson gson = new Gson();
                Card card = gson.fromJson(jsonString, Card.class);
                //TODO Pauline: dies ist die Karte, die der Spieler (der gerade an der Reihe ist)gezogen und abgelegt hat (auf den Ablegestapel)
                displayDiscardedCard(card);
                Log.d("----------------------MY STATUS", me.getStatus());

                if (me.getStatus().equals(TypeDefs.playing)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("----------------------ACTION", "trying to play action");

                            try {
                                initiateCardAction(card);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateText.setText("A card is being played");
                        }
                    });
                }
                //danach kann er die Funktionalität nutzen:
                // je nachdem muss dann an den Server dies gesandt werden:
                //webSocketClient.send(String.valueOf(JSON_commands.useFunctionalityPeek(card)));
                //webSocketClient.send(String.valueOf(JSON_commands.useFunctionalitySpy(card, me)));
                // webSocketClient.send(String.valueOf(JSON_commands.useFunctionalitySwap(card, me, card, me)));

            }


        }

        if (jsonObject.has("useFunctionalityPeek")) {
            JSONObject js = jsonObject.getJSONObject("useFunctionalityPeek");
            String json = js.get("card").toString();

            Log.d("----------------------PEEK ACTION CONFIRMED BY SERVER", me.getStatus());

            Gson gson = new Gson();
            Card card = gson.fromJson(json, Card.class);

            //TODO Pauline: das ist die Karte, die der Spieler bei sich selbst anschaut -> anzeigen für alle Spieler
            if (me.getStatus().equals(TypeDefs.waiting)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Player peekingPlayer = null;
                        for(Player player : otherPlayers){
                            if(player.getId()==playingPlayerId){
                                peekingPlayer=player;
                                Log.d("----------------------PEEK ACTION CONFIRMED BY SERVER", "peeker:" +peekingPlayer.getName());
                            }
                        }
                        showPeekedOnCard(peekingPlayer, card);
                    }
                });
            }

            if (me.getStatus().equals(TypeDefs.playing)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 10000);
                    }
                });

            }

        }
        if (jsonObject.has("useFunctionalitySpy")) {

            JSONObject js = jsonObject.getJSONObject("useFunctionalitySpy");
            String json1 = js.get("card").toString();
            String json2 = js.get("spyedPlayer").toString();

            Log.d("----------------------PEEK ACTION CONFIRMED BY SERVER", me.getStatus());

            Gson gson = new Gson();
            Card card = gson.fromJson(json1, Card.class);

            Player spyedPlayer = gson.fromJson(json2, Player.class);

            //TODO pauline: das ist der Spieler und die Karte des Spielers, die angeschaut wird, von dem Spieler der gerade dran ist
            //Hier das spyen anzeigen bzw erlauben
            // danach finish aufrufen
            if (me.getStatus().equals(TypeDefs.waiting)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showSpiedOnCard(spyedPlayer, card);
                    }
                });
            }
            if (me.getStatus().equals(TypeDefs.playing)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 10000);
                    }
                });
            }

        }
        if (jsonObject.has("useFunctionalitySwap")) {

            JSONObject js = jsonObject.getJSONObject("useFunctionalitySwap");
            String json1 = js.get("card1").toString();
            String json2 = js.get("card2").toString();
            String json3 = js.get("player1").toString();
            String json4 = js.get("player1").toString();
            Gson gson = new Gson();
            Card card1 = gson.fromJson(json1, Card.class);

            Card card2 = gson.fromJson(json2, Card.class);
            Player player1 = gson.fromJson(json3, Player.class);
            Player player2 = gson.fromJson(json4, Player.class);

            Log.d("----------------------SWAP ACTION CONFIRMED BY SERVER", me.getStatus());


            //TODO Pauline: das sind die Karten und zugehörigen Spieler, die vertauscht wurden
            if (me.getStatus().equals(TypeDefs.waiting)) {

            }

            if (me.getStatus().equals(TypeDefs.playing))
            webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));

        }

        if (jsonObject.has("updatePlayer")) {
            JSONObject js = jsonObject.getJSONObject("updatePlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                updateCards(player);
            }
        }

        if (jsonObject.has("statusupdatePlayer")) {
            JSONObject js = jsonObject.getJSONObject("statusupdatePlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() == me.getId()) {
                    me.setStatus(player.getStatus());
                }else{
                    getPlayerById(player.getId()).setStatus(player.getStatus());
                }
            }
        }

        //TODO Pauline: wenn der Spieler seinen Zug beendet hat sende:
        //webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));

        //TODO Pauline: wenn der Spieler Cabo Button drückt, sende:
        //webSocketClient.send(String.valueOf(JSON_commands.sendCabo("cabo")));

        if (jsonObject.has("score")) {
            JSONObject js = jsonObject.getJSONObject("score");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                updateScores(player);
                String winner = getNameOfWinner();
                //TODO Pauline: die Scores sind jetzt in allen Spielern upgedated : player.getScore(); und können somit angezeigt werden
                // winner ist der Name des Gewinners
            }
        }
    }

    private void showNames() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < otherPlayers.size(); i++) {
                    if (i + 1 < playerNames.size()) {
                        playerNames.get(i + 1).setText(otherPlayers.get(i).getName());
                    } else {
                        Log.d("----------------------NAMES", "OUT OF BOUNDS");
                    }
                }

            }
        });
    }
    
    public void updateCards(Player updatedPlayer) {
        if (updatedPlayer.getId() == me.getId()) {
            me.updateCards(updatedPlayer);
        } else {
            for (Player player : otherPlayers) {
                if (player.getId() == updatedPlayer.getId()) {
                    ArrayList<Card> oldcards = player.getMyCards();
                    player.updateCards(updatedPlayer);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i=0; i <player.getMyCards().size(); i++){
                                if(!oldcards.get(i).equalsCard(player.getMyCards().get(i))){
                                    Log.d("----------------------SWAPPED CARD", "index: "+i);
                                    showSwappedCards(player, player.getMyCards().get(i));
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    public boolean containsPlayer(Player player) {
        for (Player otherPlayer : otherPlayers) {
            if (player.getId() == otherPlayer.getId()) {
                return true;
            }
        }
        return false;
    }

    public void updateScores(Player updatedPlayer) {
        if (updatedPlayer.getId() == me.getId()) {
            me.updateScore(updatedPlayer);
        } else {
            for (Player player : otherPlayers) {
                if (player.getId() == updatedPlayer.getId()) {
                    player.updateScore(updatedPlayer);
                }
            }
        }
    }


    public String getNameOfWinner() {
        ArrayList<Integer> scores = new ArrayList<>();
        scores.add(me.getScore());
        for (Player player : otherPlayers) {
            scores.add(player.getScore());
        }
        Collections.sort(scores);
        int winnerScore = scores.get(scores.size() - 1);
        return getWinner(winnerScore);
    }

    public String getWinner(int winnerScore) {
        if (me.getScore() == winnerScore) {
            return me.getName();
        } else {
            for (Player player : otherPlayers) {
                if (player.getScore() == winnerScore) {
                    return player.getName();
                }
            }
        }
        return "";
    }

    public Player getPlayerById(int id) {
        for (Player player : otherPlayers) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

}
