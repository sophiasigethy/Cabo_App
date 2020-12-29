package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.os.Build;
//import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {
    private WebSocketClient mWebSocketClient;
    private String name = Build.MANUFACTURER + " " + Build.MODEL;
    private boolean start = false;

    private TextView mTextView;
    private TextView mName;
    private EditText mEditText;
    //current message that was sent or typed in
    private String mMessage = "";
    //state of the game- see class Type Defs for all 3 states
    private String state = TypeDefs.MATCHING;
    // contains all other players
    private ArrayList<Player> players = new ArrayList();
    // player object which represent this client
    private Player me;
    //determines if username has already been accepted by the server
    private boolean usernameAccepted = false;

    private Communicator communicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.messages);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mName = (TextView) findViewById(R.id.name);
        mEditText = (EditText) findViewById(R.id.editText);

        //connects to server
        communicator= Communicator.getInstance(this);
        communicator.connectWebSocket();
        mWebSocketClient= communicator.getmWebSocketClient();

        startGame();
    }



    /**
     * this method handles how to proceed when a message from the server is received:
     * every sent message arrives right here
     * by using the key of the jsonObject the client knows what has been sent and how to deal with it
     *
     * @param message
     */
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
                showCards();
                mWebSocketClient.send(JSON_commands.statusupdate(TypeDefs.readyForGamestart).toString());
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
            //call method which showas Client who's turn it is
            showNextPlayer(nextPlayerId);
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
     * this methods sends a JSON string to the server
     *
     * @param view
     * @throws JSONException
     */
    public void sendMessage(View view) throws JSONException {
        String message = mEditText.getText().toString();
        JSONObject jsonObject;
        if (usernameAccepted) {
            String text = "(" + me.getName() + "): " + message;
            jsonObject = JSON_commands.chatMessage(text);
        } else {
            jsonObject = JSON_commands.Username(message);
        }
        mWebSocketClient.send(jsonObject.toString());
        mEditText.setText("");
    }


    /**
     * this method shows the received or typed in messages in the UI
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
            start = false;
            mName.setText(" \n");
        }
    }

    /**
     * this method checks which state has been sent by the server
     *
     * @param status
     */
    private void checkStatus(String status) {
        if (status.equalsIgnoreCase(TypeDefs.MATCHING)) {
            mMessage = TypeDefs.server + "We are still waiting for other players.";
        }
        if (status.equalsIgnoreCase(TypeDefs.GAMESTART)) {
            startGame();
        }
    }

    /**
     * this method switches to the InGame Activity to start the game
     */
    public void startGame() {
        state = TypeDefs.GAMESTART;
        // TODO an Pauline: hier auf andere Activity/Layout weiterleiten
        Intent intent = new Intent(MainActivity.this,InGameActivity.class);
        //Intent intent = new Intent(MainActivity.this,WaitingRoomActivity.class);

        startActivity(intent);

    }
}
