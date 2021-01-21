package msp.group3.caboclient;

import android.content.Intent;
import android.os.Bundle;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

public class ConnectionActivity extends AppCompatActivity implements Communicator.CommunicatorCallback{
    private Communicator communicator;
    private WebSocketClient mWebSocketClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_activity);

        communicator = Communicator.getInstance(this);
        communicator.connectWebSocket();
        mWebSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);

    }


    @Override
    public void handleTextMessage(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);
        if (jsonObject.has("connectionAccepted")) {
            communicator.sendMessage(JSON_commands.noAccount());
        }
        if (jsonObject.has("allowedToMove")) {
           moveToWaitingRoomActivity();
        }

    }

    public void moveToWaitingRoomActivity() throws JSONException {
        Intent intent = new Intent(ConnectionActivity.this, WaitingRoomActivity.class);
        ConnectionActivity.this.startActivity(intent);
    }
}