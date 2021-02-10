package msp.group3.caboclient.CaboView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import msp.group3.caboclient.CaboController.BackgroundSoundService;
import msp.group3.caboclient.CaboController.Communicator;
import msp.group3.caboclient.CaboController.DatabaseOperation;
import msp.group3.caboclient.CaboController.JSON_commands;
import msp.group3.caboclient.CaboModel.Card;
import msp.group3.caboclient.CaboModel.Player;
import msp.group3.caboclient.R;

import static msp.group3.caboclient.CaboController.TypeDefs.angry;
import static msp.group3.caboclient.CaboController.TypeDefs.laughing;
import static msp.group3.caboclient.CaboController.TypeDefs.playing;
import static msp.group3.caboclient.CaboController.TypeDefs.shocked;
import static msp.group3.caboclient.CaboController.TypeDefs.smiling;
import static msp.group3.caboclient.CaboController.TypeDefs.tongueOut;
import static msp.group3.caboclient.CaboController.TypeDefs.waiting;

/**
 * This is the activity of the actual gameplay. It contains in zoomable layout the player cards, the card stacks to draw from or
 * to discard cards on. The player overviews are in a static place and along with the cards generated upon a server message
 * depending on how many players are present. Interactions with the UI are illustrated via animations and the players are able
 * to communicate via a chat and setting emojis.
 */
public class InGameActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {

    /**
     ***************************** SERVER CONNECTION ******************************
     */

    /**
     * Fields needed for the client-server communication.
     */
    protected WebSocketClient webSocketClient;
    private Communicator communicator;

    /**
     ******************************* MUSIC AND SOUND *******************************
     */

    /**
     * Music service that is passed on from previous activities via an intent.
     */
    private Intent musicService;
    private MediaPlayer soundPlayer;

    /**
     ********************************** STORAGE *************************************
     */


    /**
     * sharedPrefs from which the state of for example the music preferneces (like sound playing or not) are retrieved
     */
    private SharedPreferences sharedPref;

    /**
     ************************************ LAYOUT *************************************
     */

    /**
     * General layout components
     */
    private com.otaliastudios.zoom.ZoomLayout zoomLayout;
    private ImageButton chatButton;
    private ImageView chatNotificationBubble;
    private ImageButton leaveGameButton;
    private ImageButton musicBtn;
    private ImageButton soundBtn;
    private ImageButton questionBtn;
    private Button caboButton;
    private ImageButton zoomButton;
    private Button peekButton;
    private Button spyButton;
    private Button switchButton;
    private TextView updateText;
    private TextView centerText;
    private TextView hintTextCardStack;
    private TextView hintTextOwnCards;
    private TextView hintEmoji;
    private ImageButton endGameReturnButton;
    private ImageView cardSwapBg;
    private androidx.fragment.app.FragmentContainerView chatFragmentContainer;

    /**
     * Layout components when picked card is shown
     */
    private LinearLayout pickedCardButtonContainer;
    private ImageView pickedCardBigImageview;
    private TextView pickedCardText;
    private ImageView cardContainerOverlaySwap;
    private ImageView cardContainerOverlayPeek;
    private ImageView cardContainerOverlaySpy;

    /**
     * Emoji buttons
     */
    private ImageButton ownEmojiButton;
    private LinearLayout emojiSelectionContainer;
    private ImageButton happyEmojiButton;
    private ImageButton veryHappyEmojiButton;
    private ImageButton tongueEmojiButton;
    private ImageButton shockedEmojiButton;
    private ImageButton angryEmojiButton;

    /**
     * All animations from the Lottie Plugin
     */

    private com.airbnb.lottie.LottieAnimationView cardSwapAnimation;
    private com.airbnb.lottie.LottieAnimationView tapPickCardAnimation;
    private com.airbnb.lottie.LottieAnimationView timerAnimation;
    private com.airbnb.lottie.LottieAnimationView hintArrowCardStack;
    private com.airbnb.lottie.LottieAnimationView hintArrowOwnCards;
    private com.airbnb.lottie.LottieAnimationView hintEmojiArrow;
    private com.airbnb.lottie.LottieAnimationView endGameStars;

    /**
     * Buttons for the card stacks to pick a card and play a card. Including a glow to highlight it.
     */
    private ImageButton playedCardsStackButton;
    private ImageView playedCardsStackGlow;
    private ImageButton pickCardsStackButton;

    /**
     * ArrayLists with the cardButtons for each player and one nested ArrayList containing the cardButtons
     */
    private final List<ImageButton> player1CardButtons = new ArrayList<>();
    private final List<ImageButton> player2CardButtons = new ArrayList<>();
    private final List<ImageButton> player3CardButtons = new ArrayList<>();
    private final List<ImageButton> player4CardButtons = new ArrayList<>();
    private final List<List<ImageButton>> otherPlayerButtonLists = new ArrayList<>();
    private final List<ImageView> otherPlayersCardGlows = new ArrayList<>();
    private ImageView player1CardsGlow;

    /**
     * Layout components of all the player overviews, also in the form of Lists
     */
    private final List<de.hdodenhof.circleimageview.CircleImageView> playerPics = new ArrayList<>();
    private final List<TextView> playerStats = new ArrayList<>();
    private final List<TextView> playerNames = new ArrayList<>();
    private final List<com.airbnb.lottie.LottieAnimationView> playerHighlightAnimations = new ArrayList<>();
    private final List<com.airbnb.lottie.LottieAnimationView> playerCaboAnimations = new ArrayList<>();
    private final List<ImageView> otherPlayerEmojis = new ArrayList<>();
    private final List<TextView> playerRanks = new ArrayList<>();
    private final List<ConstraintLayout> playerOverviews = new ArrayList<>();

    /**
     ********************************* GAME LOGIC ***********************************
     */

    /**
     * Players me and a list of the other players, sent and updated by the server
     */
    protected Player me;
    private ArrayList<Player> otherPlayers = new ArrayList<>();

    /**
     * Global fields of game logic
     */
    private int playingPlayerId = 0;
    private int round = 1;
    private String noAccount = "";
    private int cardDrawCount = 0;
    private boolean caboCalled=false;
    private int zoomBtnCount = 0;
    private int chatButtonCount = 0;
    private int nrCardsSelected = 0;
    protected String entireChatText = "";
    private boolean initialRound = true;
    private Player caboplayer = null;




    private AlertDialog.Builder builder = null;
    private AlertDialog ruleDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ingame_activity);

        /**
         * Setup Sounds & Music
         */
        sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);
        musicService = new Intent(this, BackgroundSoundService.class);
        musicService.putExtra("song", 2);
        musicBtn = (ImageButton) findViewById(R.id.music_button);
        soundBtn = (ImageButton) findViewById(R.id.sound_button);
        if (DatabaseOperation.getDao().getMusicPlaying(sharedPref).equals("Play")) {
            musicBtn.setImageResource(R.drawable.music_on);
            startService(musicService);
        } else {
            musicBtn.setImageResource(R.drawable.music_off);
        }
        if (DatabaseOperation.getDao().getSoundsPlaying(sharedPref).equals("Play")) {
            soundBtn.setImageResource(R.drawable.sound_on);
        } else {
            soundBtn.setImageResource(R.drawable.sound_off);
        }

        /**
         * Link layout components. Components that first need to be set up according to server messages, are initially
         * set invisible.
         */
        zoomLayout = (com.otaliastudios.zoom.ZoomLayout) findViewById(R.id.zoomlayout);
        chatButton = (ImageButton) findViewById(R.id.chat_button);
        chatNotificationBubble = (ImageView) findViewById(R.id.chat_notification_bubble);
        chatNotificationBubble.setVisibility(View.INVISIBLE);
        leaveGameButton = (ImageButton) findViewById(R.id.leave_button);
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
        pickedCardText = (TextView) findViewById(R.id.picked_card_big_text);
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
        questionBtn = (ImageButton) findViewById(R.id.question_button);
        playedCardsStackButton = (ImageButton) findViewById(R.id.played_cards_imageButton);
        playedCardsStackGlow = findViewById(R.id.card_glow_imageview);
        playedCardsStackGlow.setVisibility(View.INVISIBLE);
        player1CardsGlow = findViewById(R.id.player1_card_glow_imageview);
        player1CardsGlow.setVisibility(View.INVISIBLE);
        pickCardsStackButton = (ImageButton) findViewById(R.id.pick_card_imageButton);

        /**
         * Setting up Player Stats, all onClickListeners and hiding overlays for the action cards
         */
        setUpPlayerStats();
        setUpOnClickListeners();
        hideActionDisplay();
        pickCardsStackButton.setEnabled(false);

        /**
         * Set-up ChatFragment
         */
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


        /**
         * Set up Server-Connection
         */
        communicator = Communicator.getInstance(this);
        webSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);

        /**
         * Establish whether user is logged in via Intent
         */
        readNoLogIn(getIntent());

        /**
         * Initial message to server
         */
        try {
            communicator.sendMessage(JSON_commands.askForInitialSettings("text"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**
         * Initial card shuffle sound
         */
        playSound(R.raw.shuffle);
    }

    /**
     * Overrides the backButton
     */
    @Override
    public void onBackPressed() {
        leaveGameDialog();
    }

    /**
     * Set up all onClickListeners
     */
    private void setUpOnClickListeners() {

        /**
         * on button click, a dialog containing the game rules is shown
         */
        questionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.select_sound);
                //TODO show game rules
                makeRulesDialog();
                ruleDialog.show();
            }
        });

        /**
         *
         */
        musicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.select_sound);
                String musicState=DatabaseOperation.getDao().getMusicPlaying(sharedPref);
                if (musicState.equals("Play"))  {
                    stopService(musicService);
                    DatabaseOperation.getDao().setMusicPlaying("Stop", sharedPref);
                    musicBtn.setImageResource(R.drawable.music_off);
                } else {
                    startService(musicService);
                    DatabaseOperation.getDao().setMusicPlaying("Play", sharedPref);
                    musicBtn.setImageResource(R.drawable.music_on);
                }
            }
        });
        /**
         *
         */
        soundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.select_sound);
                String soundState=DatabaseOperation.getDao().getSoundsPlaying(sharedPref);
                if (soundState.equals("Play"))  {
                    DatabaseOperation.getDao().setSoundPlaying("Stop", sharedPref);
                    soundBtn.setImageResource(R.drawable.sound_off);
                } else {
                    DatabaseOperation.getDao().setSoundPlaying("Play", sharedPref);
                    soundBtn.setImageResource(R.drawable.sound_on);
                }
            }
        });

        /**
         * on button click the chat fragment is made visible and in case a notification bubble was visible, this is made invisible
         */
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

        /**
         * on button click a dialog is shown, asking for verification, if user really wants to leave the game
         */
        leaveGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveGameDialog();
            }
        });

        /**
         * if the layout is already zoomed in, it zooms back to 100% on button click, if not, it zooms to 130%
         */
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (zoomLayout.getZoom()==1.0f) {
                    zoomLayout.zoomTo(1.3f, true);
                    zoomButton.setImageResource(R.drawable.zoom_in);
                } else {
                    zoomLayout.zoomTo(1.0f, true);
                    zoomButton.setImageResource(R.drawable.zoom_out);
                }
                zoomBtnCount++;
            }
        });

        /**
         * Button is enabled when it's the user's (me) turn. Sends a server message, requesting "cabo" and finishing the round
         */
        caboButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.cabo);
                try {
                    webSocketClient.send(String.valueOf(JSON_commands.sendCabo("cabo")));
                    webSocketClient.send(String.valueOf(JSON_commands.sendFinishMove("finish")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * when clicked, the user picks a card and the card has a little pop animation and the underlying hint to tap
         * animation is set invisible. After it is clicked the button is disabled.
         * sends a server message with the content sendPickCard("memorized") -> requesting to receive a picked card
         */
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
        /**
         * opens the container with all emojis for player 1 (me)
         */
        ownEmojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintEmoji.setVisibility(View.INVISIBLE);
                hintEmojiArrow.setVisibility(View.INVISIBLE);
                emojiSelectionContainer.setVisibility(View.VISIBLE);
            }
        });

        /**
         * sets the emoji to smiling and forwards it to the server
         */
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

        /**
         * sets the emoji to laughing and forwards it to the server
         */
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

        /**
         * sets the emoji to tongue out and forwards it to the server
         */
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

        /**
         * sets the emoji to shocked and forwards it to the server
         */
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

        /**
         * sets the emoji to angry and forwards it to the server
         */
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

    /**
     * in this method the dialog showing the game rules is created
     * the dialog contains a scrollview with the rules in textform and can be exited via the "got it" button
     */
    private void makeRulesDialog(){
        String msg = "Normal Cards:" +
                "\n\nCards of the values -1 to 6 are normal cards. You can use these only to swap with your cards or simply discard them." +
                "\n\nAction Cards:" +
                "\nPeek: 7+8 Allow you to take a look at one of your cards" +
                "\nSpy: 9+10 Allow you to take a look at any enemy card" +
                "\nSwap: 11+12 Allow you to swap any 2 cards on the field" +
                "\n\nCard Values:" +
                "\nEvery Card is worth its indicated number." +
                "\n\nRounds:" +
                "\nIf you think, your points are low enough, you can call CABO!" +
                "\nThis will end your turn, and everyone else has 1 last turn." +
                "\nAfterwards the points of every player are summed up and a new round begins." +
                "\n\nWinning:" +
                "\nBy default the game ends as soon as the first player has reached 100 points. The player with the lowest score at this point wins." +
                "\nThe max score can also be adjusted in the settings in the waiting room.";

        builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Cheat Sheet")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Got it", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                })
        ;
        ruleDialog = builder.create();
        ruleDialog.getWindow().setBackgroundDrawableResource(R.color.beige);
    }

    /**
     * in this method the custom dialog to leave the game is created
     * the dialog is opened when the user clicks the leaveGameBtn and makes sure to verify whether the user really
     * wants to leave the game. This has to be confirmed via the accept button. Only then the method leaveGame() is called.
     */
    private void leaveGameDialog() {
        Activity activity = this;
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                RequestDialog requestDialog = new RequestDialog();
                requestDialog.showDialog(activity, null);
                requestDialog.getText().setText("Are you sure you want to leave the game?");
                requestDialog.getDialogButtonAccept().setText("Yes");
                requestDialog.getDialogButtonDecline().setText("No");
                requestDialog.getImage().setImageResource(R.drawable.exit_purple);
                requestDialog.getDialogButtonAccept().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

    /**
     * Sets up the onClickListeners for the cards of player 1 (me).
     * Depending on which other function calls this function, the user is allowed to select a different number of cards.
     * Once the user selects more than the cards that are allowed. The ones selected before are automatically deselected and
     * the selection starts anew. The global field nrCardsSelected is counted up accordingly.
     * In case multiple player card OnClickListeners are enabled, the cards of other players that are selected, are taken into account as well.
     * Once the number of cards selected have reached the limit of allowed cards, peek- and switch-button are enabled and made fully visible.
     * Once a card is clicked, the underlying glow also disappears.
     *
     * By changing the cards state to selected, the displayed card image changes, according to the card_button.xml to a displayed tickmark.
     *
     * @param cardsAllowed the number of cards that the user is allowed to select
     */
    private void setPlayer1CardsOnClickListeners(int cardsAllowed) {
        Log.d("-----------ON CLICK LISTENER PLAYER 1", "initiating");
        for (ImageButton cardButton : player1CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playSound(R.raw.select_sound);
                    hideHint();
                    playedCardsStackButton.setEnabled(false);

                    if (cardButton.isSelected()) {
                        nrCardsSelected--;
                        cardButton.setSelected(false);

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

    /**
     * Sets up the onClickListeners for the cards of player 2.
     * Depending on which other function calls this function, the user is allowed to select a different number of cards.
     * Once the user selects more than the cards that are allowed. The ones selected before are automatically deselected and
     * the selection starts anew. The global field nrCardsSelected is counted up accordingly.
     * In case multiple player card OnClickListeners are enabled, the cards of other players that are selected, are taken into account as well.
     * Once the number of cards selected have reached the limit of allowed cards, peek-, switch- and spy-button are enabled and made fully visible.
     * Once a card is clicked, the underlying glow also disappears.
     *
     * By changing the cards state to selected, the displayed card image changes, according to the card_button.xml to a displayed tickmark.
     *
     * @param cardsAllowed the number of cards that the user is allowed to select
     */
    private void setPlayer2CardsOnClickListeners(int cardsAllowed) {
        for (ImageButton cardButton : player2CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playSound(R.raw.select_sound);
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

    /**
     * Sets up the onClickListeners for the cards of player 3.
     * Depending on which other function calls this function, the user is allowed to select a different number of cards.
     * Once the user selects more than the cards that are allowed. The ones selected before are automatically deselected and
     * the selection starts anew. The global field nrCardsSelected is counted up accordingly.
     * In case multiple player card OnClickListeners are enabled, the cards of other players that are selected, are taken into account as well.
     * Once the number of cards selected have reached the limit of allowed cards, peek-, switch- and spy-button are enabled and made fully visible.
     * Once a card is clicked, the underlying glow also disappears.
     *
     * By changing the cards state to selected, the displayed card image changes, according to the card_button.xml to a displayed tickmark.
     *
     * @param cardsAllowed the number of cards that the user is allowed to select
     */
    private void setPlayer3CardsOnClickListeners(int cardsAllowed) {
        for (ImageButton cardButton : player3CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playSound(R.raw.select_sound);
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

    /**
     * Sets up the onClickListeners for the cards of player 4.
     * Depending on which other function calls this function, the user is allowed to select a different number of cards.
     * Once the user selects more than the cards that are allowed. The ones selected before are automatically deselected and
     * the selection starts anew. The global field nrCardsSelected is counted up accordingly.
     * In case multiple player card OnClickListeners are enabled, the cards of other players that are selected, are taken into account as well.
     * Once the number of cards selected have reached the limit of allowed cards, peek-, switch- and spy-button are enabled and made fully visible.
     * Once a card is clicked, the underlying glow also disappears.
     *
     * By changing the cards state to selected, the displayed card image changes, according to the card_button.xml to a displayed tickmark.
     *
     * @param cardsAllowed the number of cards that the user is allowed to select
     */
    private void setPlayer4CardsOnClickListeners(int cardsAllowed) {
        for (ImageButton cardButton : player4CardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playSound(R.raw.select_sound);

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

    /**
     * deactivates all player card onClickListeners in one go
     */
    private void deactivateAllOnCardClickListeners() {
        deactivatePlayer1OnClickListeners();
        deactivatePlayer2OnClickListeners();
        deactivatePlayer3OnClickListeners();
        deactivatePlayer4OnClickListeners();
    }

    /**
     * deactivates all buttons that the user is not allowed to click when it's not his turn
     */
    private void deactivateAllButtons() {
        deactivateAllOnCardClickListeners();
        caboButton.setEnabled(false);
        caboButton.setAlpha(0.3f);
        pickCardsStackButton.setEnabled(false);
        playedCardsStackButton.setEnabled(false);
        tapPickCardAnimation.setVisibility(View.INVISIBLE);
    }

    /**
     * deactivated all cardButtons of player 1 (me) and sets the states to not selected
     */
    private void deactivatePlayer1OnClickListeners() {
        for (ImageButton cardButton : player1CardButtons) {
            cardButton.setOnClickListener(null);
            cardButton.setSelected(false);
        }
    }

    /**
     * deactivated all cardButtons of player 2 and sets the states to not selected
     */
    private void deactivatePlayer2OnClickListeners() {
        for (ImageButton cardButton : player2CardButtons) {
            cardButton.setOnClickListener(null);
            cardButton.setSelected(false);
        }
    }

    /**
     * deactivated all cardButtons of player 3 and sets the states to not selected
     */
    private void deactivatePlayer3OnClickListeners() {
        for (ImageButton cardButton : player3CardButtons) {
            cardButton.setOnClickListener(null);
            cardButton.setSelected(false);
        }
    }

    /**
     * deactivated all cardButtons of player 4 and sets the states to not selected
     */
    private void deactivatePlayer4OnClickListeners() {
        for (ImageButton cardButton : player4CardButtons) {
            cardButton.setOnClickListener(null);
            cardButton.setSelected(false);
        }
    }

    /**
     * sets all action displays, meaning the image overlays on the big card in the container
     * to invisible
     */
    private void hideActionDisplay() {
        cardContainerOverlaySwap.setVisibility(View.INVISIBLE);
        cardContainerOverlaySpy.setVisibility(View.INVISIBLE);
        cardContainerOverlayPeek.setVisibility(View.INVISIBLE);

    }

    /**
     * Displays the overlay image on the big card in the container indicating the card action, depending on the cardValue.
     * A Countdowntimer is set to only display the image for 2 seconds and then disappear.
     *
     * @param pickedCard
     */
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
            playSound(R.raw.action_card);
        }

        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                hideActionDisplay();
            }

        }.start();
    }

    /**
     * In this function all player-visualisations are linked to their ids and the corresponding Lists are filled. The lists serve the purpose of
     * corresponding with the lists of player-objects and card-objects per player. In for-loops the corresponding button per card can be found.
     * Also in a for loop, only the players that actually exist, can later be displayed.
     * At this point all Stats are set to invisible and only later made visible upon a server message, indicating the number of players.
     */
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

        Collections.addAll(playerRanks, findViewById(R.id.player1_rank), findViewById(R.id.player2_rank), findViewById(R.id.player3_rank), findViewById(R.id.player4_rank));

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
            playerRanks.get(i).setVisibility(View.INVISIBLE);
        }

        for (ImageView glow : otherPlayersCardGlows) {
            glow.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Here the player stats are actually made visible according to the nrPlayers parameter.
     * The number of players is sent by the server and this function is called immediately as soon as this information is provided.
     * @param nrPlayers
     */
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

    /**
     * This function animates a "glow" image underlay to grow a little bigger and fade in over the duration of 2s.
     * @param glowImage
     */
    private void growCardGlowAnimation(ImageView glowImage) {
        //bounds remain the same only image changes
        AlphaAnimation fade_in = new AlphaAnimation(0f, 1f);
        fade_in.setDuration(2000);
        fade_in.setFillAfter(true);
        glowImage.startAnimation(fade_in);

        ScaleAnimation grow_in = new ScaleAnimation(0.8f, 1f, 0.8f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow_in.setDuration(1000);
        grow_in.setFillAfter(true);
        glowImage.startAnimation(grow_in);
    }

    /**
     * This function animates a "glow" image underlay to grow a little bigger and fade in over the duration of 2s.
     * @param glowImage
     */
    private void growCardGlowAnimationOut(ImageView glowImage) {
        //bounds remain the same only image changes
        AlphaAnimation fade_out = new AlphaAnimation(1f, 0f);
        fade_out.setDuration(2000);
        fade_out.setFillAfter(true);
        glowImage.startAnimation(fade_out);

        ScaleAnimation grow_out = new ScaleAnimation(1.0f, 0f, 1.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grow_out.setDuration(1000);
        grow_out.setFillAfter(true);
        glowImage.startAnimation(grow_out);
    }

    /**
     * This function shows "glow"-images behind the cards of all present other players.
     */
    private void visualizeOtherPlayerCardGlows() {
        for (int i = 0; i < otherPlayers.size(); i++) {
            if (playerOverviews.get(i + 1).getVisibility() == View.VISIBLE) {
                otherPlayersCardGlows.get(i).setVisibility(View.VISIBLE);
                growCardGlowAnimation(otherPlayersCardGlows.get(i));
            }
        }
    }

    /**
     * This function hides the "glow"-images behind the cards of all present other players.
     */
    private void hideOtherPlayerCardGlows() {
        for (int i = 0; i < otherPlayers.size(); i++) {
            if (playerOverviews.get(i + 1).getVisibility() == View.VISIBLE) {
                growCardGlowAnimationOut(otherPlayersCardGlows.get(i));
            }
        }
    }

    /**
     * This function applies a short "pop"-animation on a button, having the button grow a litte over 500ms.
     * @param button
     */
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

    /**
     * This function is responsible for animating the effect of a "card-turn" on a specific card ImageButton.
     * It makes use of two ObjectAnimators (one ofFloat scaleX 1-0; the other ofFloat scaleX 0-1)
     * The two are interpolated and the imageResource for the card front is exchanged according to the cardValue.
     * Therefore the function getCardFromButton(cardButton) is called onAnimationEnd().
     *
     * @param cardButton
     */
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

    /**
     * This is the counterpart for the animateCardTurn(cardButton) method and does the same in reverse.
     * @param cardButton
     */
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

    /**
     * This method takes a specific card-ImageButton and finds the corresponding Card object by first checking
     * to which player the cardButton belongs to and then finding the corresponding card object in the players field myCards
     * which contains an ArrayList of CardObjects. The index of the cardButton matches the index of the card-Object in the list
     * and the card-Object is finally returned.
     * @param cardButton
     * @return card object
     */
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

    /**
     * This method is called, once the picked card is sent by the server.
     * It displays the picked card, with a turn animation in the big container on the left.
     * showCardText(card) is called to indicate what kind of resource the card represents.
     * showCardAction(card) makes sure to show an indicator image on top of the card if the card is an action card.
     *
     * if it is the first card drawn in the game (verified via the global cardDrawCount==1) showHint() which indicates what
     * actions are possible is shown.
     *
     * Furthermore the cardStackButton to play/discard a card is enabled and the player1cards (own cards) to switch the cards are enabled.
     * If the player wants to switch cards, the switch onClickListener is only enabled once enough cards are selected.
     * If that is the case, on switch clicked, the selected card is sent to the server with the command swapPickedCardWithOwnCards and the swap animation is played.
     * Additionally all cardOnClickListeners and buttons are deactivated and deselected again and animations are faded.
     *
     * @param card
     */
    private void showPickedCardInContainer(Card card) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playSound(R.raw.draw_card);
                pickedCardBigImageview.setImageResource(R.drawable.card_back);
                pickedCardButtonContainer.setVisibility(View.VISIBLE);
                pickedCardText.setVisibility(View.INVISIBLE);
                final ObjectAnimator oa1 = ObjectAnimator.ofFloat(pickedCardBigImageview, "scaleX", 1f, 0f);
                final ObjectAnimator oa2 = ObjectAnimator.ofFloat(pickedCardBigImageview, "scaleX", 0f, 1f);
                oa1.setInterpolator(new DecelerateInterpolator());
                oa2.setInterpolator(new AccelerateDecelerateInterpolator());
                oa1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        pickedCardBigImageview.setImageResource(getCardResource(card));
                        showCardText(card);
                        pickedCardText.setVisibility(View.VISIBLE);
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
                                deactivateAllButtons();
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

    /**
     * This function enables the playedCardStackButton that is clicked when the user either wants to
     * @param pickedCard
     */
    private void enablePlayedCardStackButton(Card pickedCard) {
        playedCardsStackButton.setEnabled(true);
        playedCardsStackButton.setAlpha(1f);
        playedCardsStackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deactivateAllOnCardClickListeners();
                deactivateAllButtons();
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
                        playSound(R.raw.card_played);
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
                    }
                });
            }
        });
    }

    /**
     * This function hides the pickedCard Container.
     */
    private void makePickedCardContainerDisappear() {
        pickedCardButtonContainer.setVisibility(View.INVISIBLE);
        pickedCardBigImageview.setImageResource(R.drawable.card_back);
    }

    /**
     * This function is currently not functional, but it attempted to zoom in on a specific card button.
     * @param cardButton
     */
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

    /**
     * This method returns the int Resource id of a certain card, in order to replace the cardimage depending on the
     * card value.
     * @param card
     * @return
     */
    private int getCardResource(Card card) {
        switch (card.getValue()) {
            case -1:
                return R.drawable.card_minus1;
            case 0:
                return R.drawable.card_0;
            case 1:
                return R.drawable.card_1;
            case 2:
                return R.drawable.card_2;
            case 3:
                return R.drawable.card_3;
            case 4:
                return R.drawable.card_4;
            case 5:
                return R.drawable.card_5;
            case 6:
                return R.drawable.card_6;
            case 7:
                return R.drawable.card_7;
            case 8:
                return R.drawable.card_8;
            case 9:
                return R.drawable.card_9;
            case 10:
                return R.drawable.card_10;
            case 11:
                return R.drawable.card_11;
            case 12:
                return R.drawable.card_12;
            case 13:
                return R.drawable.card_13;
        }
        return 0;
    }

    /**
     * This method is called when the picked card is shown in the container and updates the displayed
     * text under the card, indicating the card type.
     * @param card
     */
    private void showCardText(Card card) {
        switch (card.getValue()) {
            case -1:
                pickedCardText.setText("Chocolate to bribe"); break;
            case 0:
                pickedCardText.setText("Collected sea shell"); break;
            case 1:
                pickedCardText.setText("Secret love letters"); break;
            case 2:
                pickedCardText.setText("Rare coffee beans"); break;
            case 3:
                pickedCardText.setText("Illegal cigarettes"); break;
            case 4:
                pickedCardText.setText("Valuable whiskey"); break;
            case 5:
                pickedCardText.setText("Stolen phone"); break;
            case 6:
                pickedCardText.setText("Stacks of cash"); break;
            case 7:
                pickedCardText.setText("Fake passports"); break;
            case 8:
                pickedCardText.setText("Gold bars"); break;
            case 9:
                pickedCardText.setText("Computer virus"); break;
            case 10:
                pickedCardText.setText("Stolen painting"); break;
            case 11:
                pickedCardText.setText("Wanted person"); break;
            case 12:
                pickedCardText.setText("Top secret documents"); break;
            case 13:
                pickedCardText.setText("Diamonds"); break;
        }
    }

    /**
     * This method is called as soon as a card is placed on the playedCardsStack and approved by the server.
     * Depending on the different values, the different actions are initiated.
     * @param pickedCard
     * @throws JSONException
     */
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

    /**
     * This method initiates the peek and swap action.
     * It is currently not in use, but can be used in later extensions of the game. It works similarly to the blind swap action, but allowing the user to look
     * at two cards first, before deciding to swap them.
     */
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

    /**
     * This method initiates the blind swap (=swap) action.
     * First it is checked, whether another player is available to swap with. This would for example not be
     * the case if there are only two players in the game and the other player called cabo (meaning their cards are blocked).
     * In that case, the round is automatically finished by sending the finish command to the server.
     *
     * If other players are available, the proceedings are similar as in all card actions. All clickable cards are highlighted
     * with a glow and their onClickListeners are enabled. Once the required number of cards is reached, the switchbutton is enabled,
     * the swap animation is played, all selected Buttons are added to a list and send to the server including their owners via the useFunctionalitySwap
     * command and the selected buttons are deselected again.
     */
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

    /**
     * This method returns the player that is the owner of a specific card in form of a cardButton
     * by finding a match in the player property myCards via the index.
     * @param cardButton
     * @return the owning Player
     */
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


    /**
     * This method only plays the swap animation when another player swaps, without sending a server message.
     */
    private void playSwapAnimationOther() {
        playSound(R.raw.card_flip);
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
    /**
     * This method only plays the swap animation when my player (me) swaps, it sends the finish message to the server
     * after the animation ended after 2s.
     */
    private void playSwapAnimation() {
        playSound(R.raw.card_flip);
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

    /**
     * This method initiates the spy action, meaning allowing the player to look at one enemy card.
     * First it is checked, whether another player is available to swap with. This would for example not be
     * the case if there are only two players in the game and the other player called cabo (meaning their cards are blocked).
     * In that case, the round is automatically finished by sending the finish command to the server.
     *
     * If other players are available, the proceedings are similar as in all card actions. All clickable cards are highlighted
     * with a glow and their onClickListeners are enabled. Once the required number of cards is reached, the peekButton is enabled,
     * the card is turned and a timer is set, the selected Button is send to the server including their owners via the useFunctionalitySpy
     * command and the selected buttons are deselected again.
     */
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

    /**
     * This method deactivates all interaction options with the cards of the player who called cabo.
     */
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

    /**
     * This method removes the player including overviews and cards from the view. It also shows a short Toast message, informing the
     * other players about the player leaving.
     * @param player who left
     */
    private void removePlayerWhoLeft(Player player) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), player.getNick() + " left the game", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < otherPlayers.size(); i++) {
                    if (otherPlayers.get(i).getId() == player.getId()) {
                        for (ImageButton card : otherPlayerButtonLists.get(i)) {
                            card.setVisibility(View.GONE);
                        }
                        playerOverviews.get(i + 1).setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    /**
     * This method enables the player to peek at one of his cards, similarly to the initial card lookup.
     */
    private void initiatePeekAction() {
        playSound(R.raw.select_sound);
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
                        playSound(R.raw.peek_action);
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

    /**
     * This method gets the selected Card-object from a specific  buttonlist and owner.
     * @param cardButtons
     * @param owner
     * @return card
     */
    private Card getSelectedCard(List<ImageButton> cardButtons, Player owner) {
        for (int i = 0; i < cardButtons.size(); i++) {
            if (cardButtons.get(i).isSelected()) {
                return owner.getMyCards().get(i);
            }
        }
        return null;
    }

    /**
     * This method initiates the initial card look-up taking place in the beginning of each round when the user is allowed to look at two cards.
     * Buttons, that the user is not allowed to click, are faded by setting down the alpha value and deactivating them.
     * The update text is updated to tell the user what to do.
     *
     * For the selection the player1OnCardListeners are set, allowing to pick two cards. Only once the required number of cards is reached
     * (when equal to the global nrCardsSelected), the peekButton is enabled within the player1CardsOnclickListener and set to full opacity.
     *
     * When the peekButton is clicked, the selected cards are turned via the cardTurnAnimation() function and the timer animation appears.
     * In parallel a different timer with the same time is running, after which is finished the "memorized" message is send to
     * the server to proceed.
     */
    private void initiateInitialCardLookUp() {
        caboButton.setEnabled(false);
        caboButton.setAlpha(0.3f);
        peekButton.setVisibility(View.VISIBLE);
        updateText.setVisibility(View.VISIBLE);
        updateText.setText("Select 2 of your cards to look at");
        peekButton.setEnabled(false);
        peekButton.setAlpha(0.3f);
        tapPickCardAnimation.setVisibility(View.INVISIBLE);
        Log.d("-----------Card lookup", "initiating");
        nrCardsSelected = 0;
        deactivatePlayer1OnClickListeners();
        setPlayer1CardsOnClickListeners(2);

        ArrayList<ImageButton> selectedCards = new ArrayList<>();
        growCardGlowAnimation(player1CardsGlow);

        peekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.peek_action);
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

    /**
     * In this method the timer anination is shown and after it is finished, the given card is animated to turn back.
     * @param cardButton
     */
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

    /**
     * This method is responsible for showing the spy symbol on the card that is currently being spied on
     * depending in a given spied on player and the spied on card-object. The symbol is shown for 10s established by a CountdownTimer.
     * At the same time the update text in the top left corner is updated to indicate which player is spying.
     * @param spiedOnPlayer
     * @param card
     */
    private void showSpiedOnCard(Player spiedOnPlayer, Card card) {
        if (spiedOnPlayer.getId() == me.getId()) {
            ImageButton cardButton = player1CardButtons.get(getCardIndex(me, card));
            updateText.setText("You are being spied on");
            playSound(R.raw.spy_action);
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
            playSound(R.raw.spy_action);
            new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    cardButton.setImageResource(R.drawable.card_button);
                }

            }.start();
        }
    }

    /**
     * This method is responsible for showing the swap symbol on the card that is currently being swapped
     * depending in a given swappingplayer and the swapped on card-object. The symbol is shown for 10s established by a CountdownTimer.
     * At the same time the update text in the top left corner is updated to indicate which player is swapping.
     *
     * If the swapping player was not me, additionally the card swap animation is played.
     * @param swappingPlayer
     * @param card
     */
    private void showSwappedCards(Player swappingPlayer, Card card) {
        if (swappingPlayer == me) {
            int cardIndex = me.getMyCards().indexOf(card);
            ImageButton cardButton = player1CardButtons.get(cardIndex);
            cardButton.setImageResource(R.drawable.card_swapped);
            playSound(R.raw.card_flip);

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
            playSound(R.raw.card_flip);
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

    /*
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
    }*/

    /**
     * This method finds and returns the corresponding ImageButton to a certain Card-object by a certain owner.
     * @param owner
     * @param card
     * @return
     */

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

    /**
     * This method is responsible for showing the peek symbol on the card that is currently being spied on
     * depending in a given peeking player and the peeked on card-object. The symbol is shown for 10s established by a CountdownTimer.
     * At the same time the update text in the top left corner is updated to indicate which player is peeking.
     * @param peekingPlayer
     * @param card
     */
    private void showPeekedOnCard(Player peekingPlayer, Card card) {
        int playerIndex = otherPlayers.indexOf(peekingPlayer);
        //int cardIndex = peekingPlayer.getMyCards().indexOf(card); //for some reason returns -1
        int cardIndex = getCardIndex(peekingPlayer, card);
        ImageButton cardButton = otherPlayerButtonLists.get(playerIndex).get(cardIndex);
        cardButton.setImageResource(R.drawable.card_spied);
        updateText.setText(peekingPlayer.getName() + " is peeking");
        playSound(R.raw.peek_action);

        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                cardButton.setImageResource(R.drawable.card_button);
            }

        }.start();

    }

    /**
     * Custom function to return the index of a card peeked on by a player.
     * @param peekingPlayer
     * @param card
     * @return int card index
     */
    private int getCardIndex(Player peekingPlayer, Card card) {
        for (int i = 0; i < peekingPlayer.getMyCards().size(); i++) {
            if (peekingPlayer.getMyCards().get(i).equalsCard(card)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * This method indicates the player's turn by showing the highlight animation behind
     * the player's avatar image. It at the same time cancels the animation of all other players.
     * @param player whose turn it is
     */
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

    /**
     * Scale view.
     *
     * @param v      the v
     * @param factor the factor
     */
    public void scaleView(View v, float factor) {
        Animation anim = new ScaleAnimation(
                1f, factor, // Start and end values for the X axis scaling
                1f, factor,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        v.startAnimation(anim);
    }

    /**
     * This method displays the discarded card on the playedCardsStackButton by replacing the imageResource.
     * @param card
     */
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


    /**
     * Process extra data.
     *
     * @throws JSONException the json exception
     */
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

    /**
     * Central method that processes all incoming server messages.
     * Depending on the method, different UI actions are initiated, it therefore regulates the gameflow.
     * @param message
     * @throws JSONException
     */
    @Override
    public void handleTextMessage(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);

        String chatText = "";

        if (jsonObject.has("chatMessage")) {
            JSONObject js = jsonObject.getJSONObject("chatMessage");
            Player sender = null;
            if (me != null) {
                if (js.has("message")) {
                    chatText = js.get("message").toString();
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
                        playSound(R.raw.notification);
                    }
                }
            });
        }

        if (jsonObject.has("sendMAXPlayer")) {
            int maxPlayer = (int) jsonObject.get("sendMAXPlayer");
            Log.d("----------------------MAXPLAYERS", "playerNr: " + maxPlayer);
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
                            nextRoundAnimation();
                            new CountDownTimer(9000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                }

                                public void onFinish() {
                                    if (me == null) {
                                        me = myself;
                                    } else {
                                        me.replacePlayerForNextRound(myself);
                                    }
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
                            new CountDownTimer(5000, 1000) {
                                public void onTick(long millisUntilFinished) {
                                }

                                public void onFinish() {
                                    nextRound(players);
                                }

                            }.start();
                            showPlayerRanks();
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
            if (me.getId() == nextPlayerId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deactivateAllButtons();
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
                        playSound(R.raw.your_turn);
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
                Log.d("----------------------MY STATUS", me.getStatus());

                if (me.getStatus().equals(playing)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayDiscardedCard(card);
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
                            displayDiscardedCard(card);
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
                        playSound(R.raw.spy_action);
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
                        playSound(R.raw.card_flip);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fadeCaboPlayerCardsAndShowAnimation(0.3f);
                        tapPickCardAnimation.setVisibility(View.INVISIBLE);
                        playSound(R.raw.cabo);
                    }
                });

            }
        }
        if (jsonObject.has("endGame")) {
            JSONObject js = jsonObject.getJSONObject("endGame");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player winnerOfGame = gson.fromJson(jsonString, Player.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showEndOfGame(winnerOfGame);
                    }
                });


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
                //leaveGame();
                if (otherPlayers.size() > 1) {
                    removePlayerWhoLeft(player);
                } else {
                    leaveGame();
                }
            }
        }
    }

    /**
     * This animation is the first step in starting a new round. First all the player's cards are revealed by playing the turn animation,
     * shown for 4s via a CountdownTimer before being turned back. CaboAnimations are hidden.
     */
    private void nextRoundAnimation() {
        updateText.setText("Revealing cards");
        for(LottieAnimationView caboAnimation : playerCaboAnimations){
            caboAnimation.setVisibility(View.INVISIBLE);
        }
        for(ImageButton card : player1CardButtons){
            card.setAlpha(1f);
            animateCardTurn(card);
        }
        for(int i=0; i<otherPlayers.size(); i++){
            for(ImageButton card : otherPlayerButtonLists.get(i)){
                card.setAlpha(1f);
                animateCardTurn(card);
            }
        }
        new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                for(ImageButton card : player1CardButtons){
                    animateCardTurnBack(card);
                }
                for(List<ImageButton> playerCards : otherPlayerButtonLists){
                    for(ImageButton card : playerCards){
                        animateCardTurnBack(card);
                    }
                }
            }

        }.start();
    }

    /**
     * This method first calculates and therm displays each player's rank after a round based on the players' points.
     */
    @SuppressLint("SetTextI18n")
    private void showPlayerRanks() {
        ArrayList<Player> tempPlayers = new ArrayList<>();
        tempPlayers.add(me);
        for (Player player : otherPlayers) {
            tempPlayers.add(player);
        }
        tempPlayers.sort(Comparator.comparing(Player::getScore));
        playerRanks.get(0).setVisibility(View.VISIBLE);
        playerRanks.get(0).setText("#" + (tempPlayers.indexOf(me) + 1));
        for (int i = 0; i < otherPlayers.size(); i++) {
            playerRanks.get(i + 1).setVisibility(View.VISIBLE);
            playerRanks.get(i + 1).setText("#" + (tempPlayers.indexOf(otherPlayers.get(i)) + 1));
        }
    }

    /**
     * This method shows the hint texts with arrows after the first drawn card, indicating to the user what to do.
     */
    private void showHint() {
        hintTextOwnCards.setVisibility(View.VISIBLE);
        hintTextCardStack.setVisibility(View.VISIBLE);
        hintArrowOwnCards.setVisibility(View.VISIBLE);
        hintArrowCardStack.setVisibility(View.VISIBLE);
    }

    /**
     * This method hides the hint texts with arrows after the first drawn card, indicating to the user what to do.
     */
    private void hideHint() {
        hintTextOwnCards.setVisibility(View.GONE);
        hintTextCardStack.setVisibility(View.GONE);
        hintArrowOwnCards.setVisibility(View.GONE);
        hintArrowCardStack.setVisibility(View.GONE);
    }

    /**
     * Show new round. Sets the center text indicating the round visible.
     */
    public void showNewRound() {
        centerText.setVisibility(View.VISIBLE);
        centerText.setText("Round " + round);
        cardSwapBg.setVisibility(View.VISIBLE);
    }

    /**
     * Hide new round.
     */
    public void hideNewRound() {
        centerText.setVisibility(View.INVISIBLE);
        cardSwapBg.setVisibility(View.INVISIBLE);
    }

    /**
     * updates the emoji image resource depending on the player's smiley property.
     * @param player
     */
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

    /**
     * Next round. This method first shows the newRound text and after a short timer replaces the player objects for the new round.
     *
     * @param players the players
     */
    public void nextRound(List<Player> players) {
        caboplayer = null;
        fadePlayerCardsRestore(1f);
        showNewRound();
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                hideNewRound();
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

        }.start();
    }

    /**
     * In this method the overviews including avatar image, names etc. are set for each of the other players in the game.
     */
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

    /**
     * This method updates the text in the score display in the player overviews.
     */
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

    /**
     * This function is called when a player called cabo. The cards are set to the given alpha value
     * and the megaphone animation is made visible.
     * @param alpha
     */
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

    /**
     * This inverses the cabo animation, setting the megaphone back to invisible and making the card fully visible again.
     * @param alpha
     */
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


    /**
     * Update cards.
     *
     * @param updatedPlayer the updated player
     */
    public void updateCards(Player updatedPlayer) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (updatedPlayer.getId() == me.getId()) {
                    ArrayList<Card> oldcards = me.getMyCards();
                    me.updateCards(updatedPlayer);
                    if (!me.getStatus().equals(playing)) {
                        for (int i = 0; i < me.getMyCards().size(); i++) {
                            if (!oldcards.get(i).equalsCard(me.getMyCards().get(i))) {
                                Log.d("----------------------SWAPPED CARD", "index: " + i);
                                showSwappedCards(me, me.getMyCards().get(i));
                            }
                        }
                    }
                } else {
                    for (Player player : otherPlayers) {
                        if (player.getId() == updatedPlayer.getId()) {
                            ArrayList<Card> oldcards = player.getMyCards();
                            Log.d("-----------OLD CARDS", "cards: " + oldcards.get(0).getValue() + " " + oldcards.get(1).getValue() + " " + oldcards.get(2).getValue() + " " + oldcards.get(3).getValue() + " ");
                            player.updateCards(updatedPlayer);
                            Log.d("-----------NEW CARDS", "cards: " + updatedPlayer.getMyCards().get(0).getValue() + " " + updatedPlayer.getMyCards().get(1).getValue() + " " + updatedPlayer.getMyCards().get(2).getValue() + " " + updatedPlayer.getMyCards().get(3).getValue() + " ");
                            if (!me.getStatus().equals(playing)) {
                                for (int i = 0; i < player.getMyCards().size(); i++) {
                                    if (!oldcards.get(i).equalsCard(player.getMyCards().get(i))) {
                                        Log.d("----------------------SWAPPED CARD", "index: " + i);
                                        showSwappedCards(player, player.getMyCards().get(i));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }


    /**
     * Contains player boolean.
     *
     * @param player the player
     * @return the boolean
     */
    public boolean containsPlayer(Player player) {
        for (Player otherPlayer : otherPlayers) {
            if (player.getId() == otherPlayer.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update scores.
     *
     * @param updatedPlayer the updated player
     */
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


    /**
     * Gets name of winner.
     *
     * @return the name of winner
     */
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

    /**
     * Gets winner.
     *
     * @param winnerScore the winner score
     * @return the winner
     */
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

    /**
     * Gets player by id.
     *
     * @param id the id
     * @return the player by id
     */
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
                    if (noAccount == null) {
                        me.won(sharedPref);
                        centerText.setVisibility(View.VISIBLE);
                        centerText.setText("You won!");
                        playSound(R.raw.champion);
                    }
                } else {
                    centerText.setVisibility(View.VISIBLE);
                    centerText.setText(winner.getName() + " won!");
                    playSound(R.raw.loser);
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

    /**
     * Leave game.
     *
     * @throws JSONException the json exception
     */
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

    /**
     * Read no log in.
     *
     * @param intent the intent
     */
    public void readNoLogIn(Intent intent) {
        String NO_LOGIN = intent.getStringExtra("NO_LOGIN");
        noAccount = NO_LOGIN;
    }

    /**
     * This function is responsible for playing sounds in this Activity
     * In case cabo was called, there is a flag that prevents the cabo sound from being interrupted
     * by the next players turn sound
     *
     * @param sound : The R.raw.*ID* of the sound you want to play
     */
    public void playSound(int sound) {
        if (DatabaseOperation.getDao().getSoundsPlaying(sharedPref).equals("Play")) {
            if (soundPlayer != null) {
                if (!caboCalled) {
                    // Make sure to release all music players, to prevent memory leakage
                    soundPlayer.stop();
                    soundPlayer.release();
                }
            }
            if (sound == R.raw.cabo) {
                caboCalled = true;
            } else {
                caboCalled = false;
            }
            soundPlayer = MediaPlayer.create(this, sound);
            soundPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    // Make sure to release all music players, to prevent memory leakage
                    soundPlayer.stop();
                    soundPlayer.release();
                }
            });
            soundPlayer.setVolume(90, 90);
            soundPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopService(musicService);
    }

    @Override
    public void onResume() {
        super.onResume();
        musicService = new Intent(this, BackgroundSoundService.class);
        musicService.putExtra("song", 2);
        if (DatabaseOperation.getDao().getMusicPlaying(sharedPref).equals("Play")) {
            //musicBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.music_on));
            startService(musicService);
        } else {
            //musicBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.music_off));
        }

    }


}
