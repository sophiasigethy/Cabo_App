package msp.group3.caboclient;

import android.app.Activity;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by sophiasigethy
 */

public class Communicator {


    private WebSocketClient mWebSocketClient;

    private CommunicatorCallback loginActivity;
    private Activity activity;


    private static Communicator instance;
    private boolean started= false;

    public interface CommunicatorCallback extends Serializable {
       public void handelTextMessage(String message) throws JSONException;
    }


    public static Communicator getInstance(Activity activity){
        if (instance==null){
            instance= new Communicator(activity);
        }

        return instance;
    }


    public Communicator(Activity activity) {

            loginActivity = (CommunicatorCallback)activity;
            this.activity=activity;

    }

    public void connectWebSocket() {
        URI uri;
        try {
            // change variable url in strings to your own ip adress
            uri = new URI("ws://192.168.178.27:8080/chat");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {

                    if (loginActivity instanceof MainActivity){
                        try {
                            loginActivity.handelTextMessage(s);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        if (loginActivity instanceof WaitingRoomActivity){
                            try {
                                loginActivity.handelTextMessage(s);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

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


    public WebSocketClient getmWebSocketClient() {
        return mWebSocketClient;
    }


    public void setLoginActivity(CommunicatorCallback loginActivity) {
        this.loginActivity = loginActivity;
    }




}
