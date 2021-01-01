package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.os.Build;
//import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {


    private TextView mTextView;

    private Communicator communicator;
    private WebSocketClient mWebSocketClient;
    private String myDbID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDbID = getIntent().getStringExtra("dbid");

        mTextView = (TextView) findViewById(R.id.connecting);

        //connects to server
        communicator = Communicator.getInstance(this);
        communicator.connectWebSocket();
        mWebSocketClient = communicator.getmWebSocketClient();

        //startGame();
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
//        if (message.equalsIgnoreCase("startMatching")) {
//            startMatching();
//        }
        if (jsonObject.has("accepted")) {
            startMatching();
        }
        if (jsonObject.has("notAccepted")) {
            String mes = TypeDefs.server + jsonObject.get("notAccepted").toString();
            mTextView.setText(mes);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        }
    }


    public void startMatching() {
        Intent intent = new Intent(MainActivity.this, WaitingRoomActivity.class);
        startActivity(intent);

    }
}
