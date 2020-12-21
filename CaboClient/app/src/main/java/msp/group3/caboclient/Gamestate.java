package msp.group3.caboclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;


public class Gamestate {

    private Communicator communicator;
    // contains all other players
    private ArrayList<Player> players = new ArrayList();
    // player object which represent this client
    private Player me;
    //determines if username has already been accepted by the server
    private boolean usernameAccepted = false;
    private AppCompatActivity activity;

    /*private static Gamestate instance;
    public static Gamestate getInstance(URI uri) throws JSONException {
        if (instance==null){
            instance = new Gamestate(uri);
        }
        return instance;
    }*/

    public Gamestate(URI uri, AppCompatActivity activity) throws JSONException {
        this.activity = activity;
        this.communicator = new Communicator(uri, this);

    }
    public void handleTextMessage(JSONObject jsonObject) throws JSONException {

        //this is received when client is allowed to join the game
        if (jsonObject.has("Hallo")) {
            String mes = TypeDefs.server + jsonObject.get("Hallo").toString();

            Intent intent = new Intent(this.activity, MainActivity.class);
            intent.putExtra("key", "myID");
            intent.putExtra("id", mes);
            //Intent intent = new Intent(MainActivity.this,WaitingRoomActivity.class);


            this.activity.startActivity(intent);
            //showText(mes);
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
               // me = new Player(player.getId(), player.getName());
                //usernameAccepted = true;
                //showText(mes);
            }
        }

        //this is sent by the server to inform the client that another player has connected
        if (jsonObject.has("newPlayer")) {
            JSONObject welcome = jsonObject.getJSONObject("newPlayer");
            if (welcome.has("player")) {
                String jsonString = welcome.get("player").toString();
                Gson gson = new Gson();
                Player newPlayer = gson.fromJson(jsonString, Player.class);
                //players.add(newPlayer);
                String mes = "(Server): " + newPlayer.getName() + " joined the game";
               // showText(mes);
            }

        }

        //this is received when the userName is already in use
        if (jsonObject.has("usernameInUse")) {
            String name = jsonObject.get("usernameInUse").toString();
            String mes = TypeDefs.server + name + " is already in use. Please state another username.";
            //showText(mes);
        }

        //this is received when the state of the game changes
        if (jsonObject.has("statusupdateServer")) {
            String status = jsonObject.get("statusupdateServer").toString();
            //checkStatus(status);
        }

        //this is sent by the server to inform the client about the other players that have been connected before
        if (jsonObject.has("sendOtherPlayer")) {
            JSONObject js = jsonObject.getJSONObject("sendOtherPlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
               // if (player.getId() != me.getId()) {
                   // String mes = TypeDefs.server + player.getName() + " with id: " + player.getId() + "has already entered the game.";
                   // players.add(player);
                   // showText(mes);
               // }
            }
        }
        // this is received when another client sent a chat message
        if (jsonObject.has("chatMessage")) {
            //if (me != null) {
                String mes = jsonObject.get("chatMessage").toString();
             //   showText(mes);
           // }
        }

        if (jsonObject.has("initialCards")) {
            JSONObject js = jsonObject.getJSONObject("initialCards");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
                /*if (player.getId() == me.getId()) {
                    me.replacePlayer(player);
                }
                //TODO nur für testzwecke sonst auskommentieren
                showCards();
                mWebSocketClient.send(JSON_commands.statusupdate(TypeDefs.readyForGamestart).toString());*/
            }
        }
        if (jsonObject.has("statusupdatePlayer")) {
            JSONObject js = jsonObject.getJSONObject("statusupdatePlayer");
            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                Player player = gson.fromJson(jsonString, Player.class);
               /* if (player.getId() == me.getId()) {
                    me.replacePlayer(player);
                }*/
            }

        }
        if (jsonObject.has("nextPlayer")) {
            int nextPlayerId = jsonObject.getInt("nextPlayer");
            //call method which showas Client who's turn it is
           // showNextPlayer(nextPlayerId);
        }
    }
}
