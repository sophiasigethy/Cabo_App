package msp.group3.caboclient;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

/**
 * Created by sophiasigethy
 */

public class Communicator {

    private WebSocketClient mWebSocketClient;
    private Gamestate gamestate;



    public Communicator(URI uri, Gamestate gamestate) {
        this.gamestate = gamestate;
        connectWebSocket(uri);
    }

    private void connectWebSocket(URI uri) {

        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                try {
                    gamestate.handleTextMessage(getMessage(s));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }

        };
        mWebSocketClient.connect();
    }

    public void sendMessage(JSONObject jsonObject) {
        mWebSocketClient.send(jsonObject.toString());
    }

    private JSONObject getMessage(String message) throws JSONException {
        return new JSONObject(message);
    }
}
