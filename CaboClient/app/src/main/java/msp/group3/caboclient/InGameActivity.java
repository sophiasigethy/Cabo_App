package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;


import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private int zoomBtnCount = 0;
    private int chatButtonCount = 0;

    private ImageButton playedCardsStackButton;
    private ImageButton pickCardsStackButton;

    private androidx.fragment.app.FragmentContainerView chatFragmentContainer;

    private final List<ImageButton> playerCardButtons = new ArrayList<>();
    private final List<de.hdodenhof.circleimageview.CircleImageView> playerPics = new ArrayList<>();
    private final List<TextView> playerStats = new ArrayList<>();
    private final List<TextView> playerNames = new ArrayList<>();
    private final List<Integer> cardClickCounts = new ArrayList<>();

    private Player me;
    private ArrayList<Player> otherPlayers = new ArrayList<>();

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

        //TODO insert Number of Players here
        setUpPlayerStats(4);

        setUpOnClickListeners();

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

        setAllCardsOnClickListeners();

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

    private void setAllCardsOnClickListeners() {
        for (ImageButton cardButton : playerCardButtons) {
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(),
                            "Card clicked: " + getResources().getResourceEntryName(cardButton.getId()), Toast.LENGTH_SHORT).show();
                    //growCardAnimation(cardButton);
                    int index = playerCardButtons.indexOf(cardButton);
                    cardClickCounts.set(index, cardClickCounts.get(index) + 1);
                    if (cardClickCounts.get(index) % 2 == 0) {
                        cardButton.setSelected(false);
                    } else {
                        cardButton.setSelected(true);
                    }
                }
            });
        }
    }

    private void setUpPlayerStats(int nrPlayers) {

        Collections.addAll(playerCardButtons, findViewById(R.id.player1_card1_imageButton), findViewById(R.id.player1_card2_imageButton), findViewById(R.id.player1_card3_imageButton), findViewById(R.id.player1_card4_imageButton),
                findViewById(R.id.player2_card1_imageButton), findViewById(R.id.player2_card2_imageButton), findViewById(R.id.player2_card3_imageButton), findViewById(R.id.player2_card4_imageButton),
                findViewById(R.id.player3_card1_imageButton), findViewById(R.id.player3_card2_imageButton), findViewById(R.id.player3_card3_imageButton), findViewById(R.id.player3_card4_imageButton),
                findViewById(R.id.player4_card1_imageButton), findViewById(R.id.player4_card2_imageButton), findViewById(R.id.player4_card3_imageButton), findViewById(R.id.player4_card4_imageButton));

        Collections.addAll(playerPics, findViewById(R.id.player1_image_game), findViewById(R.id.player2_image_game), findViewById(R.id.player3_image_game), findViewById(R.id.player4_image_game));

        Collections.addAll(playerStats, findViewById(R.id.player1_info_game), findViewById(R.id.player2_info_game), findViewById(R.id.player3_info_game), findViewById(R.id.player4_info_game));

        Collections.addAll(playerNames, findViewById(R.id.player1_name_game), findViewById(R.id.player2_name_game), findViewById(R.id.player3_name_game), findViewById(R.id.player4_name_game));

        Collections.addAll(cardClickCounts, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        for (int i = nrPlayers; i < 4; i++) {
            playerPics.get(i).setVisibility(View.INVISIBLE);
            playerStats.get(i).setVisibility(View.INVISIBLE);
            playerNames.get(i).setVisibility(View.INVISIBLE);
        }

        for (int i = nrPlayers * 4; i < 16; i++) {
            playerCardButtons.get(i).setVisibility(View.INVISIBLE);
        }
    }

    private void growCardAnimation(ImageButton card) {
        //bounds remain the same only image changes
        ObjectAnimator.ofFloat(card, "scaleX", 1.0f, 1.3f).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "scaleY", 1.0f, 1.3f).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "x", -15).setDuration(600).start();
        ObjectAnimator.ofFloat(card, "y", -15).setDuration(600).start();
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
    public void handelTextMessage(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);

        if (jsonObject.has("chatMessage")) {
            String chatText = jsonObject.get("chatMessage").toString();
            // TODO pauline: den String chatText einfach nur anzeigen :)
        }

        if (jsonObject.has("sendMAXPlayer")) {
            int maxPlayer = (int) jsonObject.get("sendMAXPlayer");
            // TODO pauline: hier wurde die Anzahl Spieler geschickt
        }
        if (jsonObject.has("initialMe")) {
            JSONObject js = jsonObject.getJSONObject("initialMe");
            if (js.has("me")) {
                String jsonString = js.get("me").toString();
                Gson gson = new Gson();
                me = gson.fromJson(jsonString, Player.class);
                // hier wurde me gesetzt

                webSocketClient.send(String.valueOf(JSON_commands.sendMemorized("memorized")));
            }
        }
        if (jsonObject.has("initialOtherPlayer")) {
            JSONObject js = jsonObject.getJSONObject("initialOtherPlayer");
            if (js.has("otherPlayer")) {
                String jsonString = js.get("otherPlayer").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() != me.getId()) {
                    otherPlayers.add(player);
                }
            }
            //webSocketClient.send(String.valueOf(JSON_commands.sendMemorized("memorized")));
        }

        //TODO: wenn alles angezeigt wurde und der Spieler seine Karten angeschaut hat, muss folgendes gesendet werden:
        // webSocketClient.send(String.valueOf(JSON_commands.sendMemorized("memorized")));

        if (jsonObject.has("nextPlayer")) {
            int nextPlayerId = jsonObject.getInt("nextPlayer");
            //TODO pauline: call method which shows Client who's turn it is
            // dann muss der player auf den nachziehstapel tippen, um eine karte zu ziehen
            // du kannst immer über me.getStatus() überprüfen, ob der Client wirklich spielen darf -> der Stautus muss gleich "playing" siehe typeDefs sein
            webSocketClient.send(String.valueOf(JSON_commands.sendPickCard("memorized")));
        }

        if (jsonObject.has("pickedCard")) {
            JSONObject js = jsonObject.getJSONObject("pickedCard");
            if (js.has("card")) {
                String jsonString = js.get("card").toString();
                Gson gson = new Gson();
                Card card = gson.fromJson(jsonString, Card.class);
                //TODO pauline: das ist die Karte, die der Spieler von Nachziehstapel gezogen hat
                //danach hat er folgende Möglichkeiten:
                //1. karte mit eigner Karte tauschen: webSocketClient.send(String.valueOf(JSON_commands.swapPickedCardWithOwnCards(card)));
                //2. karte ablegen und die Funktionalität nutzen:  webSocketClient.send(String.valueOf(JSON_commands.playPickedCard(card)));
                webSocketClient.send(String.valueOf(JSON_commands.swapPickedCardWithOwnCards(card)));
                webSocketClient.send(String.valueOf(JSON_commands.playPickedCard(card)));
            }

        }
        if (jsonObject.has("discardedCard")) {
            JSONObject js = jsonObject.getJSONObject("discardedCard");
            if (js.has("card")) {
                String jsonString = js.get("card").toString();
                Gson gson = new Gson();
                Card card = gson.fromJson(jsonString, Card.class);
                //TODO Pauline: dies ist die Karte, die der Spieler (der gerade an der Reihe ist) abgelegt (auf den Ablegestapel),
                //Indem er die gezogene Karte mit seiner eigenen Karte tauscht
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
                //danach kann er die Funktionalität nutzen:
                // je nachdem muss dann an den Server dies gesandt werden:
                //webSocketClient.send(String.valueOf(JSON_commands.useFunctionalityPeek(card)));
                //webSocketClient.send(String.valueOf(JSON_commands.useFunctionalitySpy(card)));
                //webSocketClient.send(String.valueOf(JSON_commands.useFunctionalitySwap(card)));
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
                    me.replacePlayer(player);
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
                String winner= getNameOfWinner();
                //TODO Pauline: die Scores sind jetzt in allen Spielern upgedated : player.getScore(); und können somit angezeigt werden
                // winner ist der Name des Gewinners
            }
        }
    }

    public void updateCards(Player updatedPlayer){
        if (updatedPlayer.getId() == me.getId()) {
            me.updateCards(updatedPlayer);
        }else{
            for (Player player: otherPlayers){
                if (player.getId()==updatedPlayer.getId()){
                    player.updateCards(updatedPlayer);
                }
            }
        }
    }

    public void updateScores(Player updatedPlayer){
        if (updatedPlayer.getId() == me.getId()) {
            me.updateScore(updatedPlayer);
        }else{
            for (Player player: otherPlayers){
                if (player.getId()==updatedPlayer.getId()){
                    player.updateScore(updatedPlayer);
                }
            }
        }
    }

    public String getNameOfWinner(){
        ArrayList<Integer> scores= new ArrayList<>();
        scores.add(me.getScore());
        for (Player player: otherPlayers){
            scores.add(player.getScore());
        }
        Collections.sort(scores);
        int winnerScore= scores.get(scores.size()-1);
        return getWinner(winnerScore);
    }

    public String getWinner(int winnerScore) {
        if (me.getScore()==winnerScore){
            return me.getName();
        }else{
            for (Player player: otherPlayers){
                if (player.getScore()==winnerScore){
                    return player.getName();
                }
            }
        }
        return "";
    }

}
