package msp.group3.caboclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class WaitingRoomActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {

    private EditText editText;
    private ImageButton sendButton;
    private WebSocketClient webSocketClient;
    private Communicator communicator;
    private TextView player1_name;
    private CircleImageView player1_image;
    private TextView player2_name;
    private CircleImageView player2_image;
    private TextView player3_name;
    private CircleImageView player3_image;
    private TextView player4_name;
    private CircleImageView player4_image;
    protected ListView messagesListView;
    protected Button cancelButton;
    private ArrayList<CircleImageView> otherPlayerImages = new ArrayList<>();
    private ArrayList<TextView> otherPlayerNamesTextViews = new ArrayList<>();
    private String noAccount = "";
    private boolean isParty = false;


    private String mMessage = "";
    //state of the game- see class Type Defs for all 3 states
    private String state = TypeDefs.MATCHING;
    // contains all other players
    private ArrayList<Player> players = new ArrayList();
    protected ArrayList<ChatMessage> messageList = new ArrayList<>();

    // player object which represent this client
    private Player me;
    private ArrayList<Player> party;
    //determines if username has already been accepted by the server
    private boolean usernameAccepted = false;
    private SharedPreferences sharedPref;
    private ChatAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);
        sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);

        editText = (EditText) findViewById(R.id.editText);
        sendButton = (ImageButton) findViewById(R.id.send_button);
        messagesListView = (ListView) findViewById(R.id.messages_listview_waiting_room);
        player1_name = (TextView) findViewById(R.id.player1_name_textview_waiting_room);
        player1_image = (CircleImageView) findViewById(R.id.player1_image_waiting_room);
        player2_name = (TextView) findViewById(R.id.player2_name_textview_waiting_room);
        player2_image = (CircleImageView) findViewById(R.id.player2_image_waiting_room);
        player3_name = (TextView) findViewById(R.id.player3_name_textview_waiting_room);
        player3_image = (CircleImageView) findViewById(R.id.player3_image_waiting_room);
        player4_name = (TextView) findViewById(R.id.player4_name_textview_waiting_room);
        player4_image = (CircleImageView) findViewById(R.id.player4_image);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    cancelAction();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Collections.addAll(otherPlayerImages, player2_image, player3_image, player4_image);
        Collections.addAll(otherPlayerNamesTextViews, player2_name, player3_name, player4_name);

        showPresentPlayers();

        readParty(getIntent());
        readNoLogIn(getIntent());

        ChatMessage welcomeMsg = new ChatMessage("BOT1", "Welcome!", true, R.drawable.robot);
        ChatMessage welcomeMsg2 = new ChatMessage("BOT2", "Welcome!", false, R.drawable.robot);

        messageList.add(welcomeMsg);
        messageList.add(welcomeMsg2);

        adapter = new ChatAdapter(getApplicationContext(), messageList);
        messagesListView.setAdapter(null);
        messagesListView.setAdapter(adapter);

        communicator = Communicator.getInstance(this);
        webSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);
        try {

          /* ArrayList<String> test= new ArrayList<>();
            test.add(me.getNick());
            test.add("ff");
            communicator.sendMessage(JSON_commands.sendStartNewGame(test));*/
            communicator.sendMessage(JSON_commands.sendStartNewGame(returnNicks()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            communicator.sendMessage(JSON_commands.sendWelcomeMessage(TypeDefs.welcomeMessage));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // startGame();
    }

    private void showPresentPlayers() {
        for (int i = 0; i < players.size(); i++) {
            otherPlayerImages.get(i).setVisibility(View.VISIBLE);
            otherPlayerNamesTextViews.get(i).setVisibility(View.VISIBLE);
        }
        for (int i = players.size(); i < 3; i++) {
            otherPlayerImages.get(i).setVisibility(View.INVISIBLE);
            otherPlayerNamesTextViews.get(i).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * this methods sends a JSON string to the server
     *
     * @param view
     * @throws JSONException
     */
    public void sendMessage(View view) throws JSONException {
        String message = editText.getText().toString();
        JSONObject jsonObject;
        if (usernameAccepted) {
            //String text = "(" + me.getNick() + "): " + message;
            jsonObject = JSON_commands.chatMessage(message, me);
        } else {
            jsonObject = JSON_commands.Username(message);
        }
        webSocketClient.send(jsonObject.toString());
        editText.setText("");
    }

    @Override
    public void onBackPressed() {
        try {
            cancelAction();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method handles how to proceed when a message from the server is received:
     * every sent message arrives right here
     * by using the key of the jsonObject the client knows what has been sent and how to deal with it
     *
     * @param message
     * @throws JSONException
     */
    @Override
    public void handleTextMessage(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);

        //this is received when client is allowed to join the game
        if (jsonObject.has("Hallo")) {
            //String mes = TypeDefs.server + jsonObject.get("Hallo").toString();
            String mes = jsonObject.get("Hallo").toString();
            if (me.getNick().equalsIgnoreCase("") || me.getNick() == null || me.getNick().equalsIgnoreCase("None")) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        showText(mes, true, null);
                    }
                });
            } else {

                webSocketClient.send(String.valueOf(JSON_commands.Username(me.getNick())));
            }

        }

        //this is received when username is accepted by the server
        //the server sent a serialized player object
        // this is then deserialized
        if (jsonObject.has("Welcome")) {
            JSONObject welcome = jsonObject.getJSONObject("Welcome");
            if (welcome.has("Player")) {
                String jsonString = welcome.get("Player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                String mes = "Hello " + player.getName() + " with id: " + player.getId();
                // String mes = "Hello " + player.getNick() + " with id: " + player.getNick();
                me = player;
                runOnUiThread(new Runnable() {
                    public void run() {
                        player1_name.setText(me.getNick());
                        usernameAccepted = true;
                        showText(mes, true, null);
                    }
                });

            }

        }

        //this is sent by the server to inform the client that another player has connected
        if (jsonObject.has("newPlayer")) {
            JSONObject welcome = jsonObject.getJSONObject("newPlayer");
            if (welcome.has("player")) {
                String jsonString = welcome.get("player").toString();
                Gson gson = new Gson();
                Player newPlayer = gson.fromJson(jsonString, Player.class);
                players.add(newPlayer);
                runOnUiThread(new Runnable() {
                    public void run() {
                        returnFreeTextView().setText(newPlayer.getNick());
                        String mes = newPlayer.getNick() + " joined the game";
                        if (isParty && (party.size() - 1) == players.size()) {
                            mes = newPlayer.getNick() + " joined the game." + "\n" + "You can start the game, all party players are now connected.";
                        }
                        showText(mes, true, null);
                        setPictureOfOtherPlayer(newPlayer);
                        showPresentPlayers();
                    }
                });
            }

        }

        //this is received when the userName is already in use
        if (jsonObject.has("usernameInUse")) {
            String name = jsonObject.get("usernameInUse").toString();
            //String mes = TypeDefs.server + name + " is already in use. Please state another username.";
            String mes = name + " is already in use. Please state another username.";
            runOnUiThread(new Runnable() {
                public void run() {
                    showText(mes, true, null);
                }
            });
        }

        //this is received when the state of the game changes


        if (jsonObject.has("statusupdateServer")) {
            String status = jsonObject.get("statusupdateServer").toString();
            checkStatus(status);
        }

        //this is sent by the server to inform the client about the other players that have been connected before
        if (jsonObject.has("sendOtherPlayer")) {
            JSONObject js = jsonObject.getJSONObject("sendOtherPlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() != me.getId()) {
                    //String mes = TypeDefs.server + player.getNick() + " with id: " + player.getId() + "has already entered the game.";
                    String mes = player.getName() + " with id: " + player.getId() + "has already entered the game.";
                    players.add(player);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            returnFreeTextView().setText(player.getNick());
                            showText(mes, true, null);
                            setPictureOfOtherPlayer(player);
                            showPresentPlayers();
                        }
                    });
                }
            }
        }
        // this is received when another client sent a chat message
        if (jsonObject.has("chatMessage")) {
            JSONObject js = jsonObject.getJSONObject("chatMessage");
            String mes = "";
            Player sender = null;
            if (me != null) {
                if (js.has("message")) {
                    mes = js.get("message").toString();
                }
            }
            if (js.has("sender")) {
                String jsonString = js.get("sender").toString();
                Gson gson = new Gson();
                sender = gson.fromJson(jsonString, Player.class);
            }
            if (sender != null) {
                showText(mes, false, sender);
            }
        }

        if (jsonObject.has("initialCards")) {
            JSONObject js = jsonObject.getJSONObject("initialCards");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() == me.getId()) {
                    me.updateStatus(player);
                }
                // nur für testzwecke sonst auskommentieren
                // showCards();
                webSocketClient.send(JSON_commands.statusupdate(TypeDefs.readyForGamestart).toString());
            }
        }
        if (jsonObject.has("statusupdatePlayer")) {
            JSONObject js = jsonObject.getJSONObject("statusupdatePlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() == me.getId()) {
                    me.updateStatus(player);
                }
            }
        }
        if (jsonObject.has("nextPlayer")) {
            int nextPlayerId = jsonObject.getInt("nextPlayer");
            //call method which shows Client who's turn it is
            //showNextPlayer(nextPlayerId);
        }
        if (jsonObject.has("startGame")) {
            if (!me.getName().equalsIgnoreCase("")) {
                startGame();
            }

        }
        if (jsonObject.has("removePlayer")) {
            JSONObject js = jsonObject.getJSONObject("removePlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                removePlayer(player);
                runOnUiThread(new Runnable() {
                    public void run() {
                        showPresentPlayers();
                        String mes = player.getName() + " disconnected";
                        showText(mes, true, null);
                    }
                });

            }
            if (party.size() > 1) {
                onBackPressed();
            }
        }
        if (jsonObject.has("notAccepted")) {
            String mes = jsonObject.get("notAccepted").toString();
            showText(mes, true, null);
        }
        if (jsonObject.has("picture")) {
            JSONObject js = jsonObject.getJSONObject("picture");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() == me.getId()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            me.setPicture(player.getPicture());
                        }
                    });
                } else {
                    Player otherPlayer = getPlayerById(player.getId());
                    if (otherPlayer != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                otherPlayer.setPicture(player.getPicture());
                            }
                        });
                    }
                }
            }
        }
        if (jsonObject.has("noStartYet")) {
            String text = "State your username first!";
            showText(text, true, null);
        }
        if (jsonObject.has("maxPoints")) {
            int maxPoints = (int) jsonObject.get("sendMAXPlayer");
            //TODO show max Points
            // vorher: communicator.sendMessage(JSON_commands.sendMaxPoints(100)); aber nur wenn firstRound==true

        }
    }

    /**
     * This method handels cancelling of WaitingRoomActivity
     * If it is called, other players in room are notified and user moves to MainActivity
     */
    public void cancelAction() throws JSONException {
        communicator.sendMessage(JSON_commands.leaveWaitingRoom());
        leaveWaitingRoom();
    }

    public void leaveWaitingRoom() throws JSONException {
        if (noAccount != null) {
            if (noAccount.equalsIgnoreCase("noAccount")) {
                Intent intent = new Intent(WaitingRoomActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(WaitingRoomActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public Player getPlayerById(int id) {
        if (players != null) {
            for (Player player : players) {
                if (player.getId() == id) {
                    return player;
                }
            }
        }
        return null;
    }

    /**
     * this method shows the received or typed in messages in the UI
     *
     * @param message
     */
    private void showText(String message, boolean serverMsg, Player sender) {

        Log.d("-----------SHOW TEXT", "trying to show text");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // mTextView.setText(mTextView.getText() + "\n" + getTextMessage());
                //mTextView.setText(mTextView.getText() + "\n" + message);
                if (sender == null && serverMsg) {
                    messageList.add(new ChatMessage("Server", message, false, R.drawable.robot));
                }
                if (sender != null) {
                    if (sender.getId() == me.getId()) {
                        messageList.add(new ChatMessage(me.getName(), message, true, me.getAvatarIcon()));
                    } else {
                        messageList.add(new ChatMessage(sender.getName(), message, false, sender.getAvatarIcon()));
                    }
                }
                messagesListView.setAdapter(null);
                messagesListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
        if (mMessage.contains("That username is already in use")) {
            //start = false;
            // mName.setText(" \n");
        }


    }

    private void setAvatarImage(int i, Player newPlayer) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (i == 0) {
                    player2_image.setImageResource(newPlayer.getAvatarIcon());
                }
                if (i == 1) {
                    player3_image.setImageResource(newPlayer.getAvatarIcon());
                }
                if (i == 2) {
                    player4_image.setImageResource(newPlayer.getAvatarIcon());
                }
            }
        });

    }


    public void showCards() {
        String c = "";
        for (int i = 0; i < me.getMyCards().size(); i++) {

            c = c + me.getMyCards().get(i) + " ";

        }
        mMessage = c;
        showText(c, true, null);
    }

    public void showNextPlayer(int id) {
        String c = "";
        if (id == me.getId()) {
            c = "MY TURN";
        } else {
            for (Player player : players) {
                if (player.getId() == id) {
                    c = player.getNick() + " turn";
                }
            }
        }
        showText(c, true, null);
    }

    /**
     * this method checks which state has been sent by the server
     *
     * @param status
     */
    private void checkStatus(String status) {
        if (status.equalsIgnoreCase(TypeDefs.MATCHING)) {
            //String mes = TypeDefs.server + "We are still waiting for other players.";
            state = TypeDefs.MATCHING;
            String mes = "";
            if (players.size() == 0) {
                if (isParty && (players.size() + 1) == party.size()) {
                    mes = "Start the game now.";
                }
                if (isParty && (players.size() + 1) != party.size()) {
                    mes = "Wait for the other party players to enter the waiting room.";
                }

                if (!isParty) {
                    mes = "Wait for other players or start the game now with a KI player.";
                }

            } else {
                if (isParty) {
                    mes = "You can start the game, all party players are now connected.";
                } else {
                    mes = "Wait for other players or start the game now.";
                }
            }

            showText(mes, true, null);
        }
        if (status.equalsIgnoreCase(TypeDefs.GAMESTART)) {
            state = TypeDefs.GAMESTART;
        }
    }


    public TextView returnFreeTextView() {
        if (getCurrentText(player1_name).equalsIgnoreCase("Player 1")) {
            return player1_name;
        }
        if (getCurrentText(player2_name).equalsIgnoreCase("Player 2")) {
            return player2_name;
        }
        if (getCurrentText(player3_name).equalsIgnoreCase("Player 3")) {
            return player3_name;
        }
        if (getCurrentText(player4_name).equalsIgnoreCase("Player 4")) {
            return player4_name;
        }
        return null;

    }

    /**
     * this method sends a request for starting the game to the server
     */
    public void changeActivity(View view) throws JSONException {

        if (state.equalsIgnoreCase(TypeDefs.GAMESTART)) {
            communicator.sendMessage(JSON_commands.startGameForAll("start"));
        } else {
            communicator.sendMessage(JSON_commands.askForStart());

        }
    }

    /**
     * this method switches to the InGame Activity to start the game
     */
    public void startGame() {
        Intent intent = new Intent(WaitingRoomActivity.this, InGameActivity.class);
        intent.putExtra("NO_LOGIN", noAccount);
        startActivity(intent);
    }

    public void removePlayer(Player removedPlayer) {
        for (Player player : players) {
            if (removedPlayer.getId() == player.getId()) {
                players.remove(player);
            }
        }
        updateTextViews(removedPlayer.getNick());
        //String text = "(Server): " + removedPlayer.getNick() + " has disconnected.";
        String text = removedPlayer.getNick() + " has disconnected.";
        showText(text, true, null);
    }

    public void updateTextViews(String name) {
        if (getCurrentText(player1_name).equalsIgnoreCase(name)) {
            player1_name.setText("Player 1");
        }
        if (getCurrentText(player2_name).equalsIgnoreCase(name)) {
            player2_name.setText("Player 2");
        }
        if (getCurrentText(player3_name).equalsIgnoreCase(name)) {
            player3_name.setText("Player 3");
        }
        if (getCurrentText(player4_name).equalsIgnoreCase(name)) {
            player4_name.setText("Player 4");
        }

    }

    private void readParty(Intent intent) {
        // Read PlayerData
        party = new ArrayList<>();
        me = DatabaseOperation.getDao().readPlayerFromSharedPrefs(sharedPref);
        party.add(me);
        if (me.getAvatarID() == 9) {
            me.setAvatarID(3);
        }
        player1_image.setImageResource(me.getAvatarIcon());
        for (int i = 0; i < 4; i++) {
            String avatarID = intent.getStringExtra("player" + i + "avatar");
            String dbID = intent.getStringExtra("player" + i + "dbid");
            if (avatarID == null || avatarID.equals(""))
                continue;
            else if (dbID.equals(me.getDbID())) {
                continue;
            } else {
                //TODO Test with unregistered User
                Player partyPlayer = new Player(dbID,
                        intent.getStringExtra("player" + i + "nick"),
                        Integer.parseInt(avatarID));
                switch (i) {
                    case 1:
                        player2_image.setImageResource(partyPlayer.getAvatarIcon());
                        break;
                    case 2:
                        player3_image.setImageResource(partyPlayer.getAvatarIcon());
                        break;
                    case 3:
                        player4_image.setImageResource(partyPlayer.getAvatarIcon());
                        break;
                }
                if (!partyPlayer.isEmpty())
                    party.add(partyPlayer);
            }


        }
        if (party.size() > 1) {
            isParty = true;
        }

    }

    public void readNoLogIn(Intent intent) {
        String NO_LOGIN = intent.getStringExtra("NO_LOGIN");
        noAccount = NO_LOGIN;
    }

    public void setPictureOfOtherPlayer(Player newPlayer) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() == newPlayer.getId()) {
                setAvatarImage(i, newPlayer);
            }
        }
    }

    public String getCurrentText(TextView textView) {
        int start = textView.getLayout().getLineStart(0);
        int end = textView.getLayout().getLineEnd(textView.getLineCount() - 1);
        String displayed = textView.getText().toString().substring(start, end);
        return displayed;
    }

    public ArrayList<String> returnNicks() {
        ArrayList<String> nicks = new ArrayList<>();
        if (party != null) {
            for (Player player : party) {
                nicks.add(player.getNick());
            }
        }
        return nicks;
    }

}
