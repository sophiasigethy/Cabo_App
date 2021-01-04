package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {

    private Player me;
    private Communicator communicator;
    private WebSocketClient mWebSocketClient;
    private ListView friendList;
    private TextView userNameTxt;
    private Button startGameBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        friendList = (ListView) findViewById(R.id.list_friends);
        userNameTxt = (TextView) findViewById(R.id.username);
        startGameBtn = (Button) findViewById(R.id.start_game_btn);

        //connects to server
        communicator = Communicator.getInstance(this);
        communicator.connectWebSocket();
        mWebSocketClient = communicator.getmWebSocketClient();
        me = DatabaseOperation.getDao().readPlayerFromSharedPrefs(getApplicationContext());
        if (me.getNick().equals("None")) {
            me.setNick(getIntent().getStringExtra("nick"));
        }
        Toast.makeText(MainActivity.this, R.string.welcome + " " + me.getNick(), Toast.LENGTH_LONG);
        userNameTxt.setText("Welcome " + me.getNick());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, me.getFriendsNicknames());
        friendList.setAdapter(adapter);
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMatching();
            }
        });
    }



    /**
     * this method handles how to proceed when a message from the server is received:
     * every sent message arrives right here
     * by using the key of the jsonObject the client knows what has been sent and how to deal with it
     *
     * @param message
     */
    public void handleTextMessage(String message) throws JSONException {
        JSONObject jsonObject = new JSONObject(message);
//        if (message.equalsIgnoreCase("startMatching")) {
//            startMatching();
//        }
        if (jsonObject.has("accepted")) {
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG);
        }
        if (jsonObject.has("notAccepted")) {
            String mes = TypeDefs.server + jsonObject.get("notAccepted").toString();
        }
    }


    public void startMatching() {
        Intent intent = new Intent(MainActivity.this, WaitingRoomActivity.class);
        startActivity(intent);
    }
}
