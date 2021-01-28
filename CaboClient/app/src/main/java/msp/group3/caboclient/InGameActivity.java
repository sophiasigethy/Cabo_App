package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;


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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static msp.group3.caboclient.TypeDefs.*;

/**
 * this is an example for a zoomable and scrollable layout
 */
public class InGameActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {
    private static final String TAG_CHAT = "chat_fragment";
    private static final String TAG_SETTINGS = "settings_fragment";
    protected WebSocketClient webSocketClient;
    private Communicator communicator;

    private com.otaliastudios.zoom.ZoomLayout zoomLayout;

    private ImageButton chatButton;
    private ImageView chatNotificationBubble;
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
    private ImageView cardContainerOverlaySwap;
    private ImageView cardContainerOverlayPeek;
    private ImageView cardContainerOverlaySpy;
    private TextView centerText;
    private int round = 1;
    private String noAccount = "";
    private int cardDrawCount = 0;
    private TextView hintTextCardStack;
    private TextView hintTextOwnCards;
    private TextView hintEmoji;
    private ImageButton endGameReturnButton;

    private com.airbnb.lottie.LottieAnimationView cardSwapAnimation;
    private com.airbnb.lottie.LottieAnimationView tapPickCardAnimation;
    private com.airbnb.lottie.LottieAnimationView timerAnimation;
    private com.airbnb.lottie.LottieAnimationView hintArrowCardStack;
    private com.airbnb.lottie.LottieAnimationView hintArrowOwnCards;
    private com.airbnb.lottie.LottieAnimationView hintEmojiArrow;
    private com.airbnb.lottie.LottieAnimationView endGameStars;


    private ImageView cardSwapBg;
    private int zoomBtnCount = 0;
    private int chatButtonCount = 0;
    private int nrCardsSelected = 0;


    private ImageButton playedCardsStackButton;
    private ImageView playedCardsStackGlow;
    private ImageView player1CardsGlow;
    private ImageButton pickCardsStackButton;

    private int MAX_PLAYERS = 0;
    private boolean isParty = false;

    private androidx.fragment.app.FragmentContainerView chatFragmentContainer;

    private final List<ImageButton> player1CardButtons = new ArrayList<>();
    private final List<ImageButton> player2CardButtons = new ArrayList<>();
    private final List<ImageButton> player3CardButtons = new ArrayList<>();
    private final List<ImageButton> player4CardButtons = new ArrayList<>();
    private final List<List<ImageButton>> otherPlayerButtonLists = new ArrayList<>();

    private final List<de.hdodenhof.circleimageview.CircleImageView> playerPics = new ArrayList<>();
    private final List<TextView> playerStats = new ArrayList<>();
    private final List<TextView> playerNames = new ArrayList<>();
    private final List<com.airbnb.lottie.LottieAnimationView> playerHighlightAnimations = new ArrayList<>();
    private final List<com.airbnb.lottie.LottieAnimationView> playerCaboAnimations = new ArrayList<>();
    private final List<ImageView> otherPlayerEmojis = new ArrayList<>();
    private final List<ImageView> otherPlayersCardGlows = new ArrayList<>();


    private final List<ConstraintLayout> playerOverviews = new ArrayList<>();

    protected Player me;
    private ArrayList<Player> otherPlayers = new ArrayList<>();
    private int playingPlayerId = 0;
    protected String entireChatText = "";

    private boolean initialRound = true;
    private Player caboplayer = null;
    private androidx.fragment.app.FragmentContainerView settingsFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ingame_activity);

        //link layout
        zoomLayout = (com.otaliastudios.zoom.ZoomLayout) findViewById(R.id.zoomlayout);
        chatButton = (ImageButton) findViewById(R.id.chat_button);
        chatNotificationBubble = (ImageView) findViewById(R.id.chat_notification_bubble);
        chatNotificationBubble.setVisibility(View.INVISIBLE);
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
        centerText = findViewById(R.id.center_text);
        centerText.setVisibility(View.INVISIBLE);
        cardSwapAnimation = findViewById(R.id.card_swap_animationView);
        cardSwapAnimation.setVisibility(View.INVISIBLE);
        cardSwapBg = findViewById(R.id.card_swap_animationView_bg);
        cardSwapBg.setVisibility(View.INVISIBLE);
        tapPickCardAnimation = findViewById(R.id.tap_pick_animationView);
        tapPickCardAnimation.setVisibility(View.INVISIBLE);
        timerAnimation = findViewById(R.id.timer_animationView);
        timerAnimation.setVisibility(View.INVISIBLE);
        cardContainerOverlaySwap = findViewById(R.id.picked_card_big_imageview_swap);
        cardContainerOverlayPeek = findViewById(R.id.picked_card_big_imageview_peek);
        cardContainerOverlaySpy = findViewById(R.id.picked_card_big_imageview_spy);
        hintArrowOwnCards = findViewById(R.id.hint_arrow_player_cards);
        hintArrowOwnCards.setVisibility(View.INVISIBLE);
        hintArrowCardStack = findViewById(R.id.hint_arrow_card_stack);
        hintArrowCardStack.setVisibility(View.INVISIBLE);
        hintTextCardStack = findViewById(R.id.hint_card_stack_text);
        hintTextCardStack.setVisibility(View.INVISIBLE);
        hintTextOwnCards = findViewById(R.id.hint_player_cards_text);
        hintTextOwnCards.setVisibility(View.INVISIBLE);
        hintEmoji = findViewById(R.id.hint_emoji);
        hintEmoji.setVisibility(View.INVISIBLE);
        hintEmojiArrow = findViewById(R.id.hint_arrow_emoji);
        hintEmojiArrow.setVisibility(View.INVISIBLE);
        endGameStars = findViewById(R.id.game_end_stars);
        endGameStars.setVisibility(View.INVISIBLE);
        endGameReturnButton = findViewById(R.id.end_game_return_button);
        endGameReturnButton.setVisibility(View.INVISIBLE);

        playedCardsStackButton = (ImageButton) findViewById(R.id.played_cards_imageButton);
        playedCardsStackGlow = findViewById(R.id.card_glow_imageview);
        playedCardsStackGlow.setVisibility(View.INVISIBLE);
        player1CardsGlow = findViewById(R.id.player1_card_glow_imageview);
        player1CardsGlow.setVisibility(View.INVISIBLE);
        pickCardsStackButton = (ImageButton) findViewById(R.id.pick_card_imageButton);

        setUpPlayerStats();
        setUpOnClickListeners();
        hideActionDisplay();

        pickCardsStackButton.setEnabled(false);

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putInt("some_int", 0);
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_chat, InGameChatFragment.class, bundle)
                    //.add(R.id.fragment_settings, SettingsFragment.class, bundle)
                    //.add(R.id.fragment_chat, new InGameChatFragment(), TAG_CHAT)
                    //.add(R.id.fragment_settings, new SettingsFragment(), TAG_SETTINGS)
                    .commit();
        }

        chatFragmentContainer = findViewById(R.id.fragment_chat);
        chatFragmentContainer.setVisibility(View.INVISIBLE);

        //settingsFragmentContainer = findViewById(R.id.fragment_settings);

        //switchToSettingsFragment();


        communicator = Communicator.getInstance(this);
        webSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);

        readNoLogIn(getIntent());

        try {
            communicator.sendMessage(JSON_commands.askForInitialSettings("text"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        try {
            communicator.sendMessage(JSON_commands.leaveGame());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void switchToSettingsFragment() {
        SettingsFragment fragA = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.detach(getSupportFragmentManager().findFragmentByTag(TAG_CHAT));
        fragmentTransaction.attach(fragA);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void switchToChatFragment() {
        InGameChatFragment fragA = (InGameChatFragment) getSupportFragmentManager().findFragmentByTag(TAG_CHAT);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.detach(getSupportFragmentManager().findFragmentByTag(TAG_CHAT));
        fragmentTransaction.attach(fragA);
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void setUpOnClickListeners() {

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatNotificationBubble.setVisibility(View.INVISIBLE);
                chatButtonCount++;
                if (chatButtonCount % 2 == 0) {
                    InGameChatFragment fragment_obj = (InGameChatFragment) getSupportFragmentManager().
                            findFragmentById(R.id.fragment_chat);
                    fragment_obj.textInput.onEditorAction(EditorInfo.IME_ACTION_DONE);
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
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendCabo("cabo")));
                    webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
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
                        caboButton.setEnabled(false);
                        caboButton.setAlpha(0.3f);
                        tapPickCardAnimation.setVisibility(View.INVISIBLE);
                        growCardGlowAnimation(playedCardsStackGlow);
                        growCardGlowAnimation(player1CardsGlow);
                        cardPopAnimation(pickCardsStackButton);
                        pickCardsStackButton.setEnabled(false);
                    }
                });
            }
        });

        ownEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintEmoji.setVisibility(View.INVISIBLE);
                hintEmojiArrow.setVisibility(View.INVISIBLE);
                emojiSelectionContainer.setVisibility(View.VISIBLE);
            }
        });

        happyEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_happy);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendSmiley(smiling)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        veryHappyEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_very_happy);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendSmiley(laughing)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        tongueEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_tounge);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendSmiley(tongueOut)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        shockedEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_shocked);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendSmiley(shocked)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        angryEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ownEmojiButton.setImageResource(R.drawable.emoji_angry);
                emojiSelectionContainer.setVisibility(View.INVISIBLE);
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendSmiley(angry)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setPlayer1CardsOnClickListeners(int cardsAllowed) {
        Log.d("-----------ON CLICK LISTENER PLAYER 1", "initiating");
        for (ImageButton cardButton : player1CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    hideHint();

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
                            for (ImageButton otherCard : player3CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player4CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player2CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                        }
                    }
                    if (pickedCardButtonContainer.getVisibility() == View.VISIBLE) {
                        switchButton.setVisibility(View.VISIBLE);

                    }
                    if (nrCardsSelected == cardsAllowed) {
                        peekButton.setEnabled(true);
                        peekButton.setAlpha(1f);
                        switchButton.setEnabled(true);
                        switchButton.setAlpha(1f);
                    }
                    Log.d("-----------ON CLICK LISTENER PLAYER 1", "#selected: " + nrCardsSelected);
                    Log.d("-----------ON CLICK LISTENER PLAYER 1", "#allowed: " + cardsAllowed);

                    player1CardsGlow.setVisibility(View.INVISIBLE);
                    playedCardsStackGlow.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void setPlayer2CardsOnClickListeners(int cardsAllowed) {
        for (ImageButton cardButton : player2CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   /* Toast.makeText(getApplicationContext(),
                            "Card clicked: "+getResources().getResourceEntryName(cardButton.getId()), Toast.LENGTH_SHORT).show();*/

                    zoomInOnSelectedCard(cardButton);

                    if (cardButton.isSelected()) {
                        cardButton.setSelected(false);
                        nrCardsSelected--;
                    } else {
                        cardButton.setSelected(true);
                        nrCardsSelected++;
                        if (nrCardsSelected > cardsAllowed) {
                            for (ImageButton otherCard : player2CardButtons) {
                                if (otherCard != cardButton && otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player3CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player4CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player1CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                        }

                    }
                    if (nrCardsSelected == cardsAllowed) {
                        switchButton.setEnabled(true);
                        switchButton.setAlpha(1f);
                        spyButton.setEnabled(true);
                        spyButton.setAlpha(1f);
                        peekButton.setEnabled(true);
                        peekButton.setAlpha(1f);
                    }
                }
            });
        }
    }

    private void setPlayer3CardsOnClickListeners(int cardsAllowed) {
        for (ImageButton cardButton : player3CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   /* Toast.makeText(getApplicationContext(),
                            "Card clicked: "+getResources().getResourceEntryName(cardButton.getId()), Toast.LENGTH_SHORT).show();*/

                    zoomInOnSelectedCard(cardButton);

                    if (cardButton.isSelected()) {
                        cardButton.setSelected(false);
                        nrCardsSelected--;
                    } else {
                        cardButton.setSelected(true);
                        nrCardsSelected++;
                        if (nrCardsSelected > cardsAllowed) {
                            for (ImageButton otherCard : player3CardButtons) {
                                if (otherCard != cardButton && otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player2CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player4CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player1CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                        }

                    }
                    if (nrCardsSelected == cardsAllowed) {
                        switchButton.setEnabled(true);
                        switchButton.setAlpha(1f);
                        spyButton.setEnabled(true);
                        spyButton.setAlpha(1f);
                        peekButton.setEnabled(true);
                        peekButton.setAlpha(1f);
                    }
                }
            });
        }
    }

    private void setPlayer4CardsOnClickListeners(int cardsAllowed) {
        for (ImageButton cardButton : player4CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   /* Toast.makeText(getApplicationContext(),
                            "Card clicked: "+getResources().getResourceEntryName(cardButton.getId()), Toast.LENGTH_SHORT).show();*/

                    zoomInOnSelectedCard(cardButton);

                    if (cardButton.isSelected()) {
                        cardButton.setSelected(false);
                        nrCardsSelected--;
                    } else {
                        cardButton.setSelected(true);
                        nrCardsSelected++;
                        if (nrCardsSelected > cardsAllowed) {
                            for (ImageButton otherCard : player4CardButtons) {
                                if (otherCard != cardButton && otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player3CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player2CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                            for (ImageButton otherCard : player1CardButtons) {
                                if (otherCard.isSelected()) {
                                    otherCard.setSelected(false);
                                    nrCardsSelected--;
                                }
                            }
                        }
                    }
                    if (nrCardsSelected == cardsAllowed) {
                        switchButton.setEnabled(true);
                        switchButton.setAlpha(1f);
                        spyButton.setEnabled(true);
                        spyButton.setAlpha(1f);
                        peekButton.setEnabled(true);
                        peekButton.setAlpha(1f);
                    }
                }
            });
        }
    }

    private void deactivateAllOnCardClickListeners() {
        deactivatePlayer1OnClickListeners();
        deactivatePlayer2OnClickListeners();
        deactivatePlayer3OnClickListeners();
        deactivatePlayer4OnClickListeners();
    }

    private void deactivateAllButtons() {
        deactivateAllOnCardClickListeners();
        caboButton.setEnabled(false);
        caboButton.setAlpha(0.3f);
        pickCardsStackButton.setEnabled(false);
        pickCardsStackButton.setEnabled(false);
        tapPickCardAnimation.setVisibility(View.INVISIBLE);
    }

    private void deactivatePlayer1OnClickListeners() {
        for (ImageButton cardButton : player1CardButtons) {
            cardButton.setOnClickListener(null);
            cardButton.setSelected(false);
        }
    }

    private void deactivatePlayer2OnClickListeners() {
        for (ImageButton cardButton : player2CardButtons) {
            cardButton.setOnClickListener(null);
            cardButton.setSelected(false);
        }
    }

    private void deactivatePlayer3OnClickListeners() {
        for (ImageButton cardButton : player3CardButtons) {
            cardButton.setOnClickListener(null);
            cardButton.setSelected(false);
        }
    }

    private void deactivatePlayer4OnClickListeners() {
        for (ImageButton cardButton : player4CardButtons) {
            cardButton.setOnClickListener(null);
            cardButton.setSelected(false);
        }
    }

    private void hideActionDisplay() {
        cardContainerOverlaySwap.setVisibility(View.INVISIBLE);
        cardContainerOverlaySpy.setVisibility(View.INVISIBLE);
        cardContainerOverlayPeek.setVisibility(View.INVISIBLE);

    }

    private void showCardAction(Card pickedCard) {
        int value = pickedCard.getValue();
        ImageView overlay = null;
        switch (value) {
            case 7:
                overlay = cardContainerOverlayPeek;
                break;
            case 8:
                overlay = cardContainerOverlayPeek;
                break;
            case 9:
                overlay = cardContainerOverlaySpy;
                break;
            case 10:
                overlay = cardContainerOverlaySpy;
                break;
            case 11:
                overlay = cardContainerOverlaySwap;
                break;
            case 12:
                overlay = cardContainerOverlaySwap;
                break;
        }

        if (overlay != null) {
            overlay.setVisibility(View.VISIBLE);
        }

        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                hideActionDisplay();
            }

        }.start();
    }


    private void setUpPlayerStats() {

        Collections.addAll(player1CardButtons, findViewById(R.id.player1_card1_imageButton), findViewById(R.id.player1_card2_imageButton), findViewById(R.id.player1_card3_imageButton), findViewById(R.id.player1_card4_imageButton));
        Collections.addAll(player2CardButtons, findViewById(R.id.player2_card1_imageButton), findViewById(R.id.player2_card2_imageButton), findViewById(R.id.player2_card3_imageButton), findViewById(R.id.player2_card4_imageButton));
        Collections.addAll(player3CardButtons, findViewById(R.id.player3_card1_imageButton), findViewById(R.id.player3_card2_imageButton), findViewById(R.id.player3_card3_imageButton), findViewById(R.id.player3_card4_imageButton));
        Collections.addAll(player4CardButtons, findViewById(R.id.player4_card1_imageButton), findViewById(R.id.player4_card2_imageButton), findViewById(R.id.player4_card3_imageButton), findViewById(R.id.player4_card4_imageButton));

        Collections.addAll(playerPics, findViewById(R.id.player1_image_game), findViewById(R.id.player2_image_game), findViewById(R.id.player3_image_game), findViewById(R.id.player4_image_game));

        Collections.addAll(playerStats, findViewById(R.id.player1_info_game), findViewById(R.id.player2_info_game), findViewById(R.id.player3_info_game), findViewById(R.id.player4_info_game));

        Collections.addAll(playerNames, findViewById(R.id.player1_name_game), findViewById(R.id.player2_name_game), findViewById(R.id.player3_name_game), findViewById(R.id.player4_name_game));

        Collections.addAll(playerOverviews, findViewById(R.id.player1_stats_game), findViewById(R.id.player2_stats_game), findViewById(R.id.player3_stats_game), findViewById(R.id.player4_stats_game));

        Collections.addAll(playerHighlightAnimations, findViewById(R.id.player1_highlight_animationView), findViewById(R.id.player2_highlight_animationView),
                findViewById(R.id.player3_highlight_animationView), findViewById(R.id.player4_highlight_animationView));

        Collections.addAll(otherPlayerButtonLists, player2CardButtons, player3CardButtons, player4CardButtons);

        Collections.addAll(otherPlayerEmojis, findViewById(R.id.player2_emoji), findViewById(R.id.player3_emoji), findViewById(R.id.player4_emoji));

        Collections.addAll(otherPlayersCardGlows, findViewById(R.id.player2_card_glow_imageview), findViewById(R.id.player3_card_glow_imageview), findViewById(R.id.player4_card_glow_imageview));

        Collections.addAll(playerCaboAnimations, findViewById(R.id.player1_cabo), findViewById(R.id.player2_cabo), findViewById(R.id.player3_cabo), findViewById(R.id.player4_cabo));
        for (int i = 0; i < 4; i++) {
            player1CardButtons.get(i).setVisibility(View.INVISIBLE);
            player2CardButtons.get(i).setVisibility(View.INVISIBLE);
            player3CardButtons.get(i).setVisibility(View.INVISIBLE);
            player4CardButtons.get(i).setVisibility(View.INVISIBLE);
            playerPics.get(i).setVisibility(View.INVISIBLE);
            playerStats.get(i).setVisibility(View.INVISIBLE);
            playerNames.get(i).setVisibility(View.INVISIBLE);
            playerOverviews.get(i).setVisibility(View.INVISIBLE);
            playerHighlightAnimations.get(i).setVisibility(View.INVISIBLE);
            playerCaboAnimations.get(i).setVisibility(View.INVISIBLE);
        }

        for (ImageView glow : otherPlayersCardGlows) {
            glow.setVisibility(View.INVISIBLE);
        }
    }

    private void visualizePlayerStats(int nrPlayers) {
        playerPics.get(0).setVisibility(View.VISIBLE);
        playerStats.get(0).setVisibility(View.VISIBLE);
        playerNames.get(0).setVisibility(View.VISIBLE);
        playerOverviews.get(0).setVisibility(View.VISIBLE);

        for (ImageButton card : player1CardButtons) {
            card.setVisibility(View.VISIBLE);
        }

        for (int i = 1; i < nrPlayers; i++) {
            playerPics.get(i).setVisibility(View.VISIBLE);
            playerStats.get(i).setVisibility(View.VISIBLE);
            playerNames.get(i).setVisibility(View.VISIBLE);
            playerOverviews.get(i).setVisibility(View.VISIBLE);
            for (ImageButton cardButton : otherPlayerButtonLists.get(i - 1)) {
                cardButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void growCardGlowAnimation(ImageView card) {
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

    private void growCardGlowAnimationOut(ImageView card) {
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

    private void visualizeOtherPlayerCardGlows() {
        for (int i = 0; i < otherPlayers.size(); i++) {
            otherPlayersCardGlows.get(i).setVisibility(View.VISIBLE);
            growCardGlowAnimation(otherPlayersCardGlows.get(i));
        }
    }

    private void hideOtherPlayerCardGlows() {
        for (int i = 0; i < otherPlayers.size(); i++) {
            growCardGlowAnimationOut(otherPlayersCardGlows.get(i));
        }
    }

    private void cardPopAnimation(ImageButton button) {
        ScaleAnimation grow_in = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow_in.setDuration(500);
        grow_in.setStartOffset(500);
        button.startAnimation(grow_in);
        ScaleAnimation grow_out = new ScaleAnimation(1.2f, 1f, 1.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow_out.setDuration(500);
        grow_out.setStartOffset(500);
        button.startAnimation(grow_out);
    }

    private void animateCardTurn(ImageButton cardButton) {
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

    private Card getCardFromButton(ImageButton cardButton) {
        if (player1CardButtons.contains(cardButton)) {
            return me.getMyCards().get(player1CardButtons.indexOf(cardButton));
        }
        if (player2CardButtons.contains(cardButton)) {
            return otherPlayers.get(0).getMyCards().get(player2CardButtons.indexOf(cardButton));
        }
        if (player3CardButtons.contains(cardButton)) {
            return otherPlayers.get(1).getMyCards().get(player3CardButtons.indexOf(cardButton));
        }
        if (player4CardButtons.contains(cardButton)) {
            return otherPlayers.get(2).getMyCards().get(player4CardButtons.indexOf(cardButton));
        }
        return null;
    }

    private void animateCardTurnBack(ImageButton cardButton) {
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
    private void showPickedCardInContainer(Card card) {
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
                        showCardAction(card);
                        if (cardDrawCount == 1) {
                            showHint();
                        }
                    }
                });
                oa1.start();
                enablePlayedCardStackButton(card);
                setPlayer1CardsOnClickListeners(1);
                switchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        for (int i = 0; i < player1CardButtons.size(); i++) {
                            if (player1CardButtons.get(i).isSelected()) {
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
                                deactivateAllOnCardClickListeners();
                                playSwapAnimation();
                                switchButton.setVisibility(View.INVISIBLE);
                                makePickedCardContainerDisappear();
                                growCardGlowAnimationOut(playedCardsStackGlow);
                                growCardGlowAnimationOut(player1CardsGlow);
                                for (ImageButton cardButton : player1CardButtons) {
                                    cardButton.setSelected(false);
                                }
                            }
                        });

                    }
                });
            }
        });
    }

    private void enablePlayedCardStackButton(Card pickedCard) {
        playedCardsStackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d("----------------------SEND", "play picked card");
                    webSocketClient.send(String.valueOf(JSON_commands.playPickedCard()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (pickedCard.getValue() != 7 && pickedCard.getValue() != 8 && pickedCard.getValue() != 9 && pickedCard.getValue() != 10 && pickedCard.getValue() != 11 && pickedCard.getValue() != 12) {
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
                        hideHint();
                        pickCardsStackButton.setEnabled(false);
                        growCardGlowAnimationOut(player1CardsGlow);
                        growCardGlowAnimationOut(playedCardsStackGlow);
                        cardPopAnimation(playedCardsStackButton);
                        makePickedCardContainerDisappear();
                        switchButton.setVisibility(View.INVISIBLE);
                        for (ImageButton cardButton : player1CardButtons) {
                            cardButton.setSelected(false);
                        }
                        deactivateAllOnCardClickListeners();
                    }
                });
            }
        });
        playedCardsStackButton.setEnabled(true);
    }

    private void makePickedCardContainerDisappear() {
        pickedCardButtonContainer.setVisibility(View.INVISIBLE);
        pickedCardBigImageview.setImageResource(R.drawable.card_back);
    }

    private void zoomInOnSelectedCard(ImageButton cardButton) {
        int[] location = new int[2];
        cardButton.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.d("-----------PAN", "x: " + zoomLayout.getPanX() / zoomLayout.getZoom() + " y: " + zoomLayout.getPanY() / zoomLayout.getZoom());
        Log.d("-----------SCREEN", "x: " + width + " y: " + height);
        Log.d("-----------CARD POSITION", "x: " + x + " y: " + y);
        //zoomLayout.panTo( 2*width-zoomLayout.getScaledPanX()+x, 2*height-zoomLayout.getScaledPanY()+y, true);
    }

    //TODO replace resources
    private int getCardResource(Card card) {
        switch (card.getValue()) {
            case -1:
                return R.drawable.card_joker_1;
            case 0:
                return R.drawable.card_hearts_k;
            case 1:
                return R.drawable.card_hearts_a;
            case 2:
                return R.drawable.card_clubs_2;
            case 3:
                return R.drawable.card_clubs_3;
            case 4:
                return R.drawable.card_clubs_4;
            case 5:
                return R.drawable.card_clubs_5;
            case 6:
                return R.drawable.card_clubs_6;
            case 7:
                return R.drawable.card_clubs_7;
            case 8:
                return R.drawable.card_clubs_8;
            case 9:
                return R.drawable.card_clubs_9;
            case 10:
                return R.drawable.card_clubs_10;
            case 11:
                return R.drawable.card_clubs_j;
            case 12:
                return R.drawable.card_clubs_q;
            case 13:
                return R.drawable.card_clubs_k;
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

        }
    }

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
                for (ImageButton cardButton : player1CardButtons) {
                    if (cardButton.isSelected()) {
                        selectedCards.add(cardButton);
                    }
                }
                for (ImageButton cardButton : player2CardButtons) {
                    if (cardButton.isSelected()) {
                        selectedCards.add(cardButton);
                    }
                }
                for (ImageButton cardButton : player3CardButtons) {
                    if (cardButton.isSelected()) {
                        selectedCards.add(cardButton);
                    }
                }
                for (ImageButton cardButton : player4CardButtons) {
                    if (cardButton.isSelected()) {
                        selectedCards.add(cardButton);
                    }
                }
                for (ImageButton card : selectedCards) {
                    animateCardTurn(card);
                }
                updateText.setText("Do you want to swap?");
                peekButton.setVisibility(View.INVISIBLE);
                switchButton.setVisibility(View.VISIBLE);
                switchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (ImageButton card : selectedCards) {
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

    private void initiateBlindSwapAction() {
        if (caboplayer != null && otherPlayers.size() == 1 && otherPlayers.get(0).getId() == caboplayer.getId()) {
            updateText.setText("All other players are blocked");
            try {
                webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            switchButton.setVisibility(View.VISIBLE);
            switchButton.setAlpha(0.3f);
            switchButton.setEnabled(false);
            updateText.setVisibility(View.VISIBLE);
            updateText.setText("Please choose 2 cards");
            setPlayer1CardsOnClickListeners(2);
            setPlayer2CardsOnClickListeners(2);
            setPlayer3CardsOnClickListeners(2);
            setPlayer4CardsOnClickListeners(2);

            growCardGlowAnimation(player1CardsGlow);
            visualizeOtherPlayerCardGlows();
            deactivateCaboPlayer();

            ArrayList<ImageButton> selectedCards = new ArrayList<>();
            nrCardsSelected = 0;

            switchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    growCardGlowAnimationOut(player1CardsGlow);
                    hideOtherPlayerCardGlows();
                    for (ImageButton cardButton : player1CardButtons) {
                        if (cardButton.isSelected()) {
                            selectedCards.add(cardButton);
                            cardButton.setSelected(false);
                        }
                    }
                    for (ImageButton cardButton : player2CardButtons) {
                        if (cardButton.isSelected()) {
                            selectedCards.add(cardButton);
                            cardButton.setSelected(false);
                        }
                    }
                    for (ImageButton cardButton : player3CardButtons) {
                        if (cardButton.isSelected()) {
                            selectedCards.add(cardButton);
                            cardButton.setSelected(false);
                        }
                    }
                    for (ImageButton cardButton : player4CardButtons) {
                        if (cardButton.isSelected()) {
                            selectedCards.add(cardButton);
                            cardButton.setSelected(false);
                        }
                    }
                    playSwapAnimation();
                    updateText.setVisibility(View.INVISIBLE);
                    switchButton.setVisibility(View.INVISIBLE);
                    try {
                        Card card1 = getCardFromButton(selectedCards.get(0));
                        Player card1Owner = getCardOwner(selectedCards.get(0));
                        Card card2 = getCardFromButton(selectedCards.get(1));
                        Player card2Owner = getCardOwner(selectedCards.get(1));
                        Log.d("-----------SEND TO SERVER SWAP", "card1 " + card1.getValue() + " by " + card1Owner.getName());
                        Log.d("-----------SEND TO SERVER SWAP", "card2 " + card2.getValue() + " by " + card2Owner.getName());

                        webSocketClient.send(String.valueOf(JSON_commands.useFunctionalitySwap(card1, card1Owner, card2, card2Owner)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    deactivateAllOnCardClickListeners();
                    nrCardsSelected = 0;
                }
            });
        }
    }

    private Player getCardOwner(ImageButton cardButton) {
        if (player1CardButtons.contains(cardButton)) {
            int index = player1CardButtons.indexOf(cardButton);
            return me;
        }
        if (player2CardButtons.contains(cardButton)) {
            int index = player2CardButtons.indexOf(cardButton);
            return otherPlayers.get(0);
        }
        if (player3CardButtons.contains(cardButton)) {
            int index = player3CardButtons.indexOf(cardButton);
            return otherPlayers.get(1);
        }
        if (player4CardButtons.contains(cardButton)) {
            int index = player4CardButtons.indexOf(cardButton);
            return otherPlayers.get(2);
        }
        return null;
    }


    private void playSwapAnimationOther() {
        cardSwapAnimation.setVisibility(View.VISIBLE);
        cardSwapBg.setVisibility(View.VISIBLE);
        cardSwapAnimation.playAnimation();
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                cardSwapAnimation.setVisibility(View.INVISIBLE);
                cardSwapBg.setVisibility(View.INVISIBLE);
            }

        }.start();
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
                    Log.d("----------------------SEND FINISH:", "after swap");
                    webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    private void initiateSpyAction() {
        if (caboplayer != null && otherPlayers.size() == 1 && otherPlayers.get(0).getId() == caboplayer.getId()) {
            updateText.setText("All other players are blocked");
            try {
                webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            spyButton.setVisibility(View.VISIBLE);
            spyButton.setEnabled(false);
            spyButton.setAlpha(0.3f);
            updateText.setVisibility(View.VISIBLE);
            updateText.setText("Please choose an enemy card");
            setPlayer2CardsOnClickListeners(1);
            setPlayer3CardsOnClickListeners(1);
            setPlayer4CardsOnClickListeners(1);

            visualizeOtherPlayerCardGlows();
            deactivateCaboPlayer();

            nrCardsSelected = 0;
            spyButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    hideOtherPlayerCardGlows();
                    for (ImageButton cardButton : player2CardButtons) {
                        if (cardButton.isSelected()) {
                            animateCardTurn(cardButton);
                            setCountdownTimer(cardButton);
                            spyButton.setVisibility(View.INVISIBLE);
                        }
                    }
                    for (ImageButton cardButton : player3CardButtons) {
                        if (cardButton.isSelected()) {
                            animateCardTurn(cardButton);
                            setCountdownTimer(cardButton);
                            spyButton.setVisibility(View.INVISIBLE);
                        }
                    }
                    for (ImageButton cardButton : player4CardButtons) {
                        if (cardButton.isSelected()) {
                            animateCardTurn(cardButton);
                            setCountdownTimer(cardButton);
                            spyButton.setVisibility(View.INVISIBLE);
                        }
                    }
                    updateText.setVisibility(View.INVISIBLE);
                    nrCardsSelected = 0;
                    Card selectedCard = null;
                    Player spiedOnPlayer = null;
                    for (int i = 0; i < otherPlayers.size(); i++) {
                        if (getSelectedCard(otherPlayerButtonLists.get(i), otherPlayers.get(i)) != null) {
                            selectedCard = getSelectedCard(otherPlayerButtonLists.get(i), otherPlayers.get(i));
                            spiedOnPlayer = otherPlayers.get(i);
                            hideOtherPlayerCardGlows();
                        }
                    }
                    try {
                        webSocketClient.send(String.valueOf(JSON_commands.useFunctionalitySpy(selectedCard, spiedOnPlayer)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    deactivateAllOnCardClickListeners();
                    nrCardsSelected = 0;
                }
            });
        }
    }

    private void deactivateCaboPlayer() {
        if (caboplayer != null) {
            for (int i = 0; i < otherPlayers.size(); i++) {
                if (otherPlayers.get(i).getId() == caboplayer.getId()) {
                    for (ImageButton card : otherPlayerButtonLists.get(i)) {
                        card.setOnClickListener(null);
                    }
                    otherPlayersCardGlows.get(i).setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void initiatePeekAction() {
        peekButton.setVisibility(View.VISIBLE);
        updateText.setVisibility(View.VISIBLE);
        updateText.setText("Please choose 1 of your cards");
        peekButton.setEnabled(false);
        peekButton.setAlpha(0.3f);

        nrCardsSelected = 0;
        setPlayer1CardsOnClickListeners(1);
        growCardGlowAnimation(player1CardsGlow);

        peekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("-----------Peek", "button clicked");

                for (ImageButton cardButton : player1CardButtons) {
                    if (cardButton.isSelected()) {
                        animateCardTurn(cardButton);
                        setCountdownTimer(cardButton);
                        peekButton.setVisibility(View.INVISIBLE);
                        growCardGlowAnimationOut(player1CardsGlow);
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

    private Card getSelectedCard(List<ImageButton> cardButtons, Player owner) {
        for (int i = 0; i < cardButtons.size(); i++) {
            if (cardButtons.get(i).isSelected()) {
                return owner.getMyCards().get(i);
            }
        }
        return null;
    }

    private void initiateInitialCardLookUp() {
        caboButton.setEnabled(false);
        caboButton.setAlpha(0.3f);
        peekButton.setVisibility(View.VISIBLE);
        updateText.setVisibility(View.VISIBLE);
        updateText.setText("Select 2 of your cards to look at");
        peekButton.setEnabled(false);
        peekButton.setAlpha(0.3f);
        Log.d("-----------Card lookup", "initiating");
        nrCardsSelected = 0;
        deactivatePlayer1OnClickListeners();
        setPlayer1CardsOnClickListeners(2);

        ArrayList<ImageButton> selectedCards = new ArrayList<>();
        growCardGlowAnimation(player1CardsGlow);

        peekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                growCardGlowAnimationOut(player1CardsGlow);
                for (ImageButton cardButton : player1CardButtons) {
                    if (cardButton.isSelected()) {
                        selectedCards.add(cardButton);
                    }
                    for (ImageButton card : selectedCards) {
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

    private void setCountdownTimer(ImageButton cardButton) {
        timerAnimation.setVisibility(View.VISIBLE);
        timerAnimation.playAnimation();

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                updateText.setText("Please memorize the card");
                updateText.setVisibility(View.VISIBLE);
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

    private void showSpiedOnCard(Player spiedOnPlayer, Card card) {
        if (spiedOnPlayer.getId() == me.getId()) {
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
        } else {
            int playerIndex = otherPlayers.indexOf(spiedOnPlayer);
            int cardIndex = getCardIndex(spiedOnPlayer, card);
            ImageButton cardButton = otherPlayerButtonLists.get(playerIndex).get(cardIndex);
            cardButton.setImageResource(R.drawable.card_spied);
            updateText.setText(spiedOnPlayer.getName() + " is being spied on");
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    cardButton.setImageResource(R.drawable.card_button);
                }

            }.start();
        }
    }

    private void showSwappedCards(Player swappingPlayer, Card card) {
        if (swappingPlayer == me) {
            int cardIndex = me.getMyCards().indexOf(card);
            ImageButton cardButton = player1CardButtons.get(cardIndex);
            cardButton.setImageResource(R.drawable.card_swapped);

            new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    cardButton.setImageResource(R.drawable.card_button);
                }

            }.start();
        } else {
            int playerIndex = otherPlayers.indexOf(swappingPlayer);
            int cardIndex = swappingPlayer.getMyCards().indexOf(card);
            ImageButton cardButton = otherPlayerButtonLists.get(playerIndex).get(cardIndex);
            cardButton.setImageResource(R.drawable.card_swapped);
            playSwapAnimationOther();

            new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    cardButton.setImageResource(R.drawable.card_button);
                }

            }.start();
        }
    }

    private void showTwoSwappedCards(Player player1, Card card1, Player player2, Card card2) {

        ImageButton card1Button = getButtonFromCard(player1, card1);
        ImageButton card2Button = getButtonFromCard(player2, card2);
        card1Button.setImageResource(R.drawable.card_swapped);
        card2Button.setImageResource(R.drawable.card_swapped);

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                card1Button.setImageResource(R.drawable.card_button);
                card2Button.setImageResource(R.drawable.card_button);
            }

        }.start();
    }

    private ImageButton getButtonFromCard(Player owner, Card card) {
        if (owner.getId() == me.getId()) {
            return player1CardButtons.get(getCardIndex(me, card));
        } else {
            for (int i = 0; i < otherPlayers.size(); i++) {
                if (otherPlayers.get(i).getId() == owner.getId()) {
                    switch (i) {
                        case 0:
                            return player2CardButtons.get(getCardIndex(otherPlayers.get(i), card));
                        case 1:
                            return player3CardButtons.get(getCardIndex(otherPlayers.get(i), card));
                        case 2:
                            return player4CardButtons.get(getCardIndex(otherPlayers.get(i), card));
                    }
                }
            }
        }
        return null;
    }

    private void showPeekedOnCard(Player peekingPlayer, Card card) {
        int playerIndex = otherPlayers.indexOf(peekingPlayer);
        //int cardIndex = peekingPlayer.getMyCards().indexOf(card); //for some reason returns -1
        int cardIndex = getCardIndex(peekingPlayer, card);
        ImageButton cardButton = otherPlayerButtonLists.get(playerIndex).get(cardIndex);
        cardButton.setImageResource(R.drawable.card_spied);
        updateText.setText(peekingPlayer.getName() + " is peeking");

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                cardButton.setImageResource(R.drawable.card_button);
            }

        }.start();

    }

    private int getCardIndex(Player peekingPlayer, Card card) {
        for (int i = 0; i < peekingPlayer.getMyCards().size(); i++) {
            if (peekingPlayer.getMyCards().get(i).equalsCard(card)) {
                return i;
            }
        }
        return 0;
    }

    private void indicatePlayerTurn(Player player) {
        for (com.airbnb.lottie.LottieAnimationView animation : playerHighlightAnimations) {
            animation.setVisibility(View.INVISIBLE);
            animation.cancelAnimation();
        }
        if (player == me) {
            playerHighlightAnimations.get(0).setVisibility(View.VISIBLE);
            playerHighlightAnimations.get(0).playAnimation();
        } else {
            int index = otherPlayers.indexOf(player) + 1;
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

    private void displayDiscardedCard(Card card) {
        playedCardsStackButton.setImageResource(getCardResource(card));
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

        String chatText = "";

        if (jsonObject.has("chatMessage")) {
            // String chatText = jsonObject.get("chatMessage").toString();
            JSONObject js = jsonObject.getJSONObject("chatMessage");
            Player sender = null;
            if (me != null) {
                if (js.has("message")) {
                    chatText = js.get("message").toString();
                    //showText(mes);
                }
            }
            if (js.has("sender")) {
                String jsonString = js.get("sender").toString();
                Gson gson = new Gson();
                sender = gson.fromJson(jsonString, Player.class);
            }
            entireChatText = entireChatText + "\n" + chatText;
            InGameChatFragment fragment_obj = (InGameChatFragment) getSupportFragmentManager().
                    findFragmentById(R.id.fragment_chat);
            if (sender != null) {
                if (sender.getId() == me.getId()) {
                    fragment_obj.chatMessagesList.add(new ChatMessage(me.getName(), chatText, true, me.getAvatarIcon()));
                } else {
                    fragment_obj.chatMessagesList.add(new ChatMessage(sender.getName(), chatText, false, sender.getAvatarIcon()));
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment_obj.chatMessageListView.setAdapter(null);
                    fragment_obj.chatMessageListView.setAdapter(fragment_obj.adapter);
                    fragment_obj.adapter.notifyDataSetChanged();
                    fragment_obj.scrollMyListViewToBottom();
                    if (chatFragmentContainer.getVisibility() == View.INVISIBLE) {
                        chatNotificationBubble.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        if (jsonObject.has("sendMAXPlayer")) {
            int maxPlayer = (int) jsonObject.get("sendMAXPlayer");
            MAX_PLAYERS = maxPlayer;
            Log.d("----------------------MAXPLAYERS", "playerNr: " + maxPlayer);
            //visualizePlayerStats(maxPlayer);
            //deactivateAllButtons();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    visualizePlayerStats(maxPlayer);
                    deactivateAllButtons();
                }
            });
        }
        if (jsonObject.has("initialMe")) {
            JSONObject js = jsonObject.getJSONObject("initialMe");
            if (js.has("me")) {
                String jsonString = js.get("me").toString();
                Gson gson = new Gson();
                Player myself = gson.fromJson(jsonString, Player.class);
                if (me == null) {
                    me = myself;
                } else {
                    me.replacePlayerForNextRound(myself);
                }
                // hier wurde me gesetzt
                Log.d("----------------------ME", "my name: " + me.getName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playerNames.get(0).setText(me.getName());
                        playerPics.get(0).setImageResource(me.getAvatarIcon());
                        playerStats.get(0).setText("Score: " + me.getScore());
                        if (initialRound) {
                            initiateInitialCardLookUp();
                        } else {
                            round++;
                            showNewRound();
                            new CountDownTimer(3000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                }

                                public void onFinish() {
                                    hideNewRound();
                                    initiateInitialCardLookUp();
                                }

                            }.start();
                        }
                    }
                });
            }

        }

        if (jsonObject.has("initialOtherPlayer")) {
            JSONObject js = jsonObject.getJSONObject("initialOtherPlayer");
            Gson gson = new Gson();
            if (js.has("players")) {
                String jsonString = js.get("players").toString();
                List<Player> players = gson.fromJson(jsonString, new TypeToken<List<Player>>() {
                }.getType());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (initialRound) {
                            otherPlayers.addAll(players);
                            showPlayers();
                        } else {
                            nextRound(players);
                        }
                    }
                });
            }

        }

        if (jsonObject.has("nextPlayer")) {
            int nextPlayerId = jsonObject.getInt("nextPlayer");
            playingPlayerId = nextPlayerId;
            Log.d("----------------------NEXT PLAYER", "id: " + nextPlayerId);
            Log.d("----------------------MY ID", "id: " + me.getId());
            //updateText.setText("Its your turn: "+getPlayerById(nextPlayerId).getName());
            if (me.getId() == nextPlayerId) {
                //indicatePlayerTurn(me);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        indicatePlayerTurn(me);
                        if (caboplayer == null) {
                            caboButton.setEnabled(true);
                            caboButton.setAlpha(1f);
                        }
                        cardDrawCount++;
                        if (cardDrawCount == 2) {
                            hintEmoji.setVisibility(View.VISIBLE);
                            hintEmojiArrow.setVisibility(View.VISIBLE);
                        }
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
                        deactivateAllButtons();
                        Player player = getPlayerById(nextPlayerId);
                        if (player != null) {
                            hintEmoji.setVisibility(View.INVISIBLE);
                            hintEmojiArrow.setVisibility(View.INVISIBLE);
                            indicatePlayerTurn(player);
                            updateText.setVisibility(View.VISIBLE);
                            updateText.setText("It's " + getPlayerById(nextPlayerId).getName() + "'s turn");
                        }
                    }
                });
            }
        }

        if (jsonObject.has("pickedCard")) {
            JSONObject js = jsonObject.getJSONObject("pickedCard");
            if (js.has("card")) {
                String jsonString = js.get("card").toString();
                Gson gson = new Gson();
                Card card = gson.fromJson(jsonString, Card.class);
                Log.d("----------------------PICKED CARD", "card value: " + card.getValue());

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
                displayDiscardedCard(card);
                Log.d("----------------------MY STATUS", me.getStatus());

                if (me.getStatus().equals(playing)) {
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
                } else {
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

            if (me.getStatus().equals(waiting)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Player peekingPlayer = null;
                        for (Player player : otherPlayers) {
                            if (player.getId() == playingPlayerId) {
                                peekingPlayer = player;
                                Log.d("----------------------PEEK ACTION CONFIRMED BY SERVER", "peeker:" + peekingPlayer.getName());
                            }
                        }
                        showPeekedOnCard(peekingPlayer, card);
                    }
                });
            }

            if (me.getStatus().equals(playing)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    Log.d("----------------------SEND FINISH:", "after peek");
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

            //Hier das spyen anzeigen bzw erlauben
            // danach finish aufrufen
            if (me.getStatus().equals(waiting)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showSpiedOnCard(spyedPlayer, card);
                    }
                });
            }
            if (me.getStatus().equals(playing)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    Log.d("----------------------SEND FINISH:", "after spy");
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
            String json4 = js.get("player2").toString();
            Gson gson = new Gson();
            Card card1 = gson.fromJson(json1, Card.class);

            Card card2 = gson.fromJson(json2, Card.class);
            Player player1 = gson.fromJson(json3, Player.class);
            Player player2 = gson.fromJson(json4, Player.class);

            Log.d("----------------------SWAP ACTION CONFIRMED BY SERVER", me.getStatus());

            if (me.getStatus().equals(waiting)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playSwapAnimation();
                    }
                });
            }

            if (me.getStatus().equals(playing)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    Log.d("----------------------SEND FINISH:", "after swap");
                                    webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 3000);
                    }
                });
            }

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
                } else {
                    getPlayerById(player.getId()).setStatus(player.getStatus());
                }
            }
        }

        if (jsonObject.has("score")) {
            JSONObject js = jsonObject.getJSONObject("score");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                updateScores(player);
                updateScoreDisplays();
                String winner = getNameOfWinner();
                initialRound = false;
                // winner ist der Name des Gewinners
            }
        }
        if (jsonObject.has("smiley")) {
            JSONObject js = jsonObject.getJSONObject("smiley");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() == me.getId()) {
                    me.setSmiley(player.getSmiley());
                } else {
                    Player otherPlayer = getPlayerById(player.getId());
                    if (otherPlayer != null) {
                        otherPlayer.setSmiley(player.getSmiley());
                        showPlayerSmiley(otherPlayer);
                    }
                }
            }
        }
        if (jsonObject.has("calledCabo")) {
            JSONObject js = jsonObject.getJSONObject("calledCabo");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                caboplayer = player;
                fadeCaboPlayerCardsAndShowAnimation(0.3f);
            }
        }
        if (jsonObject.has("endGame")) {
            JSONObject js = jsonObject.getJSONObject("endGame");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player winnerOfGame = gson.fromJson(jsonString, Player.class);
                showEndOfGame(winnerOfGame);
                //TODO update global score in shared preferences?

            }
        }
        if (jsonObject.has("maxPoints")) {
            int maxPoints = (int) jsonObject.get("sendMAXPlayer");
            //TODO show max Points
            // vorher: communicator.sendMessage(JSON_commands.sendMaxPoints(100)); aber nur wenn firstRound==true

        }
        if (jsonObject.has("removePlayer")) {
            JSONObject js = jsonObject.getJSONObject("removePlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                //TODO show that player has removed
                //handle this: at the moment game ends
                leaveGame();
            }
        }
    }

    private void showHint() {
        hintTextOwnCards.setVisibility(View.VISIBLE);
        hintTextCardStack.setVisibility(View.VISIBLE);
        hintArrowOwnCards.setVisibility(View.VISIBLE);
        hintArrowCardStack.setVisibility(View.VISIBLE);
    }

    private void hideHint() {
        hintTextOwnCards.setVisibility(View.GONE);
        hintTextCardStack.setVisibility(View.GONE);
        hintArrowOwnCards.setVisibility(View.GONE);
        hintArrowCardStack.setVisibility(View.GONE);
    }

    public void showNewRound() {
        centerText.setVisibility(View.VISIBLE);
        centerText.setText("Round " + round);
        cardSwapBg.setVisibility(View.VISIBLE);
    }

    public void hideNewRound() {
        centerText.setVisibility(View.INVISIBLE);
        cardSwapBg.setVisibility(View.INVISIBLE);
    }

    private void showPlayerSmiley(Player player) {
        int playerIndex = otherPlayers.indexOf(player);

        if (player.getSmiley().equals(smiling)) {
            otherPlayerEmojis.get(playerIndex).setImageResource(R.drawable.emoji_happy);
        } else if (player.getSmiley().equals(laughing)) {
            otherPlayerEmojis.get(playerIndex).setImageResource(R.drawable.emoji_very_happy);
        } else if (player.getSmiley().equals(angry)) {
            otherPlayerEmojis.get(playerIndex).setImageResource(R.drawable.emoji_angry);
        } else if (player.getSmiley().equals(shocked)) {
            otherPlayerEmojis.get(playerIndex).setImageResource(R.drawable.emoji_shocked);
        } else if (player.getSmiley().equals(tongueOut)) {
            otherPlayerEmojis.get(playerIndex).setImageResource(R.drawable.emoji_tounge);
        }

    }

    public void nextRound(List<Player> players) {
        caboplayer = null;
        fadePlayerCardsRestore(1f);
        for (Player oldPlayer : otherPlayers) {
            for (Player newPlayer : players) {
                if (oldPlayer.getId() == newPlayer.getId()) {
                    oldPlayer.replacePlayerForNextRound(newPlayer);
                }
            }
        }
        for (LottieAnimationView playerHighlight : playerHighlightAnimations) {
            playerHighlight.setVisibility(View.INVISIBLE);
        }
        playedCardsStackButton.setImageResource(R.drawable.card_stack);
    }

    private void showPlayers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < otherPlayers.size(); i++) {
                    if (i + 1 < playerNames.size()) {
                        playerNames.get(i + 1).setText(otherPlayers.get(i).getName());
                        playerPics.get(i + 1).setImageResource(otherPlayers.get(i).getAvatarIcon());
                        playerStats.get(i + 1).setText("Score: " + otherPlayers.get(i).getScore());
                    } else {
                        Log.d("----------------------NAMES", "OUT OF BOUNDS");
                    }
                }

            }
        });
    }

    private void updateScoreDisplays() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerStats.get(0).setText("Score: " + me.getScore());
                for (int i = 0; i < otherPlayers.size(); i++) {
                    if (i + 1 < playerNames.size()) {
                        playerStats.get(i + 1).setText("Score: " + otherPlayers.get(i).getScore());
                    } else {
                        Log.d("----------------------NAMES", "OUT OF BOUNDS");
                    }
                }

            }
        });
    }

    private void fadeCaboPlayerCardsAndShowAnimation(float alpha) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (caboplayer.getId() == me.getId()) {
                    for (ImageButton card : player1CardButtons) {
                        card.setAlpha(alpha);
                    }
                    playerCaboAnimations.get(0).setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < otherPlayers.size(); i++) {
                    if (otherPlayers.get(i).getId() == caboplayer.getId()) {
                        for (ImageButton card : otherPlayerButtonLists.get(i)) {
                            card.setAlpha(alpha);
                        }
                        playerCaboAnimations.get(i + 1).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void fadePlayerCardsRestore(float alpha) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ImageButton card : player1CardButtons) {
                    card.setAlpha(alpha);
                }
                playerCaboAnimations.get(0).setVisibility(View.INVISIBLE);
                for (int i = 0; i < otherPlayers.size(); i++) {
                    for (ImageButton card : otherPlayerButtonLists.get(i)) {
                        card.setAlpha(alpha);
                    }
                    playerCaboAnimations.get(i + 1).setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    public void updateCards(Player updatedPlayer) {
        if (updatedPlayer.getId() == me.getId()) {
            ArrayList<Card> oldcards = me.getMyCards();
            me.updateCards(updatedPlayer);
            if (!me.getStatus().equals(playing)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < me.getMyCards().size(); i++) {
                            if (!oldcards.get(i).equalsCard(me.getMyCards().get(i))) {
                                Log.d("----------------------SWAPPED CARD", "index: " + i);
                                showSwappedCards(me, me.getMyCards().get(i));
                            }
                        }
                    }
                });
            }
        } else {
            for (Player player : otherPlayers) {
                if (player.getId() == updatedPlayer.getId()) {
                    ArrayList<Card> oldcards = player.getMyCards();
                    Log.d("-----------OLD CARDS", "cards: " + oldcards.get(0).getValue() + " " + oldcards.get(1).getValue() + " " + oldcards.get(2).getValue() + " " + oldcards.get(3).getValue() + " ");
                    player.updateCards(updatedPlayer);
                    Log.d("-----------NEW CARDS", "cards: " + updatedPlayer.getMyCards().get(0).getValue() + " " + updatedPlayer.getMyCards().get(1).getValue() + " " + updatedPlayer.getMyCards().get(2).getValue() + " " + updatedPlayer.getMyCards().get(3).getValue() + " ");
                    if (!me.getStatus().equals(playing)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < player.getMyCards().size(); i++) {
                                    if (!oldcards.get(i).equalsCard(player.getMyCards().get(i))) {
                                        Log.d("----------------------SWAPPED CARD", "index: " + i);
                                        showSwappedCards(player, player.getMyCards().get(i));
                                    }
                                }
                            }
                        });
                    }
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

    private void showEndOfGame(Player winner) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ImageButton card : player1CardButtons) {
                    card.setVisibility(View.GONE);
                }
                for (List<ImageButton> playerCards : otherPlayerButtonLists) {
                    for (ImageButton card : playerCards) {
                        card.setVisibility(View.GONE);
                    }
                }
                for (LottieAnimationView caboAnim : playerCaboAnimations) {
                    caboAnim.setVisibility(View.INVISIBLE);
                }
                for (ConstraintLayout playerLayout : playerOverviews) {
                    playerLayout.setVisibility(View.INVISIBLE);
                }
                pickCardsStackButton.setVisibility(View.GONE);
                playedCardsStackButton.setVisibility(View.GONE);
                caboButton.setVisibility(View.GONE);
                updateText.setVisibility(View.GONE);
                endGameStars.setVisibility(View.VISIBLE);
                endGameReturnButton.setVisibility(View.VISIBLE);

                cardSwapBg.setVisibility(View.VISIBLE);
                if (winner.getId() == me.getId()) {
                    centerText.setVisibility(View.VISIBLE);
                    centerText.setText("You won!");
                } else {
                    centerText.setVisibility(View.VISIBLE);
                    centerText.setText(winner.getName() + " won!");
                }

                endGameReturnButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            leaveGame();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void leaveGame() throws JSONException {
        communicator.sendMessage(JSON_commands.leaveGame());
        if (noAccount != null) {
            if (noAccount.equalsIgnoreCase("noAccount")) {
                Intent intent = new Intent(InGameActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(InGameActivity.this, MainActivity.class);
            startActivity(intent);
        }

    }

    public void readNoLogIn(Intent intent) {
        String NO_LOGIN = intent.getStringExtra("NO_LOGIN");
        noAccount = NO_LOGIN;
    }

    @Override
    protected void onDestroy () {

        super.onDestroy();

    }
}
