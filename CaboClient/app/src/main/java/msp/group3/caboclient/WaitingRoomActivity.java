package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WaitingRoomActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {

    private EditText editText;
    private Button button;
    private WebSocketClient webSocketClient;
    private Communicator communicator;
    private TextView mTextView;
    private TextView player1_name;
    private TextView player2_name;
    private TextView player3_name;
    private TextView player4_name;
    private TextView name;

    private String mMessage = "";
    //state of the game- see class Type Defs for all 3 states
    private String state = TypeDefs.MATCHING;
    // contains all other players
    private ArrayList<Player> players = new ArrayList();
    // player object which represent this client
    private Player me;
    //determines if username has already been accepted by the server
    private boolean usernameAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        mTextView = (TextView) findViewById(R.id.messages);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        player1_name = (TextView) findViewById(R.id.player1_name_textview_waiting_room);
        player2_name = (TextView) findViewById(R.id.player2_name_textview_waiting_room);
        player3_name = (TextView) findViewById(R.id.player3_name_textview_waiting_room);
        player4_name = (TextView) findViewById(R.id.player4_name_textview_waiting_room);
        name= (TextView) findViewById(R.id.name);


        communicator = Communicator.getInstance(this);
        webSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);
        try {
            communicator.sendMessage(JSON_commands.sendWelcomeMessage(TypeDefs.welcomeMessage));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //startGame();

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
            String text = "(" + me.getName() + "): " + message;
            jsonObject = JSON_commands.chatMessage(text);
        } else {
            jsonObject = JSON_commands.Username(message);
        }
        webSocketClient.send(jsonObject.toString());
        editText.setText("");
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
    public void handelTextMessage(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);

        //this is received when client is allowed to join the game
        if (jsonObject.has("Hallo")) {
            String mes = TypeDefs.server + jsonObject.get("Hallo").toString();
            showText(mes);
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
                String mes = TypeDefs.server + "Hello " + player.getName() + " with id: " + player.getId();
                me = new Player(player.getId(), player.getName());
                name.setText(player.getName());
                player1_name.setText(me.getName());
                usernameAccepted = true;
                showText(mes);
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
                returnFreeTextView().setText(newPlayer.getName());
                String mes = "(Server): " + newPlayer.getName() + " joined the game";
                showText(mes);
            }

        }

        //this is received when the userName is already in use
        if (jsonObject.has("usernameInUse")) {
            String name = jsonObject.get("usernameInUse").toString();
            String mes = TypeDefs.server + name + " is already in use. Please state another username.";
            showText(mes);
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
                    String mes = TypeDefs.server + player.getName() + " with id: " + player.getId() + "has already entered the game.";
                    players.add(player);
                    returnFreeTextView().setText(player.getName());
                    showText(mes);
                }
            }
        }
        // this is received when another client sent a chat message
        if (jsonObject.has("chatMessage")) {
            if (me != null) {
                String mes = jsonObject.get("chatMessage").toString();
                showText(mes);
            }
        }

        if (jsonObject.has("initialCards")) {
            JSONObject js = jsonObject.getJSONObject("initialCards");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                if (player.getId() == me.getId()) {
                    me.replacePlayer(player);
                }
                //TODO nur für testzwecke sonst auskommentieren
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
                    me.replacePlayer(player);
                }
            }
        }
        if (jsonObject.has("nextPlayer")) {
            int nextPlayerId = jsonObject.getInt("nextPlayer");
            //call method which shows Client who's turn it is
            //showNextPlayer(nextPlayerId);
        }
        if (jsonObject.has("startGame")) {
            startGame();
        }
        if (jsonObject.has("removePlayer")) {
            JSONObject js = jsonObject.getJSONObject("removePlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                removePlayer(player);
            }
        }
        if (jsonObject.has("notAccepted")) {
            String mes = TypeDefs.server + jsonObject.get("notAccepted").toString();
            showText(mes);
        }

    }

    /**
     * this method shows the received or typed in messages in the UI
     *
     * @param message
     */
    private void showText(String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // mTextView.setText(mTextView.getText() + "\n" + getTextMessage());
                mTextView.setText(mTextView.getText() + "\n" + message);
                if (mTextView.getLayout() != null) {
                    final int scrollAmount = mTextView.getLayout().getLineTop(mTextView.getLineCount()) - mTextView.getHeight();
                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0)
                        mTextView.scrollTo(0, scrollAmount);
                    else
                        mTextView.scrollTo(0, 0);
                }
            }
        });
        if (mMessage.contains("That username is already in use")) {
            //start = false;
            // mName.setText(" \n");
        }


    }


    public void showCards() {
        String c = "";
        for (int i = 0; i < me.getMyCards().size(); i++) {

            c = c + me.getMyCards().get(i) + " ";

        }
        mMessage = c;
        showText(c);
    }

    public void showNextPlayer(int id) {
        String c = "";
        if (id == me.getId()) {
            c = "MY TURN";
        } else {
            for (Player player : players) {
                if (player.getId() == id) {
                    c = player.getName() + " turn";
                }
            }
        }
        showText(c);
    }

    /**
     * this method checks which state has been sent by the server
     *
     * @param status
     */
    private void checkStatus(String status) {
        if (status.equalsIgnoreCase(TypeDefs.MATCHING)) {
            String mes = TypeDefs.server + "We are still waiting for other players.";
            showText(mes);
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
            Toast.makeText(WaitingRoomActivity.this,
                    "There are not enough player yet!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * this method switches to the InGame Activity to start the game
     */
    public void startGame() {
        Intent intent = new Intent(WaitingRoomActivity.this, InGameActivity.class);
        startActivity(intent);
    }

    public void removePlayer(Player removedPlayer) {
        for (Player player : players) {
            if (removedPlayer.getId() == player.getId()) {
                players.remove(player);
            }
        }
        updateTextViews(removedPlayer.getName());
        String text= "(Server): "+ removedPlayer.getName()+ " has disconnected.";
        showText(text);
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

    public String getCurrentText(TextView textView) {
        int start = textView.getLayout().getLineStart(0);
        int end = textView.getLayout().getLineEnd(textView.getLineCount() - 1);
        String displayed = textView.getText().toString().substring(start, end);
        return displayed;
    }

}