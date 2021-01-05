package msp.group3.caboclient;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {

    private Player me;
    private Communicator communicator;
    private WebSocketClient mWebSocketClient;
    private ListView friendList;
    private TextView userNameTxt;
    private Button startGameBtn;
    private Button addFriendBtn;
    private SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        friendList = (ListView) findViewById(R.id.list_friends);
        userNameTxt = (TextView) findViewById(R.id.username);
        startGameBtn = (Button) findViewById(R.id.start_game_btn);
        addFriendBtn = (Button) findViewById(R.id.add_friend_btn);
        sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);

        //connects to server
        communicator = Communicator.getInstance(this);
        communicator.connectWebSocket();
        mWebSocketClient = communicator.getmWebSocketClient();
        me = DatabaseOperation.getDao().readPlayerFromSharedPrefs(sharedPref);
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
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseOperation.getDao().updateUserList(sharedPref);
                showAddFriendDialog();
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
            //TODO: Make message more detailed, to differ what was accepted (friendrequested or game invite?)
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG);
        }
        if (jsonObject.has("notAccepted")) {
            //TODO: Make message more detailed, to differ what was accepted (friendrequested or game invite?)
            String mes = TypeDefs.server + jsonObject.get("notAccepted").toString();
        }
        if (jsonObject.has("friendrequestaccepted")) {
            //TODO: Receive dbID and nick from accepting Player, and edit the following accordingly
            //String receivedDbID = "";
            //String receivedNick = "";
            //Player sender = new Player(receivedDbID, receivedNick);
            //DatabaseOperation.getDao().addFriendships(me, sender);
        }
    }


    public void startMatching() {
        Intent intent = new Intent(MainActivity.this, WaitingRoomActivity.class);
        startActivity(intent);
    }


    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.search_friend);
        // Set up the input
        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText searchNick = new EditText(MainActivity.this);
        searchNick.setHint(R.string.enter_nick);
        searchNick.setWidth(70);
        linearLayout.addView(searchNick);
        // Set up the buttons
        builder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setView(linearLayout);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchNick.getText().toString().isEmpty()) {
                    ArrayList<Player> allUsers = DatabaseOperation.getDao().getAllUsersList(sharedPref);
                    if (allUsers.size() > 0) {
                        for (Player user : allUsers) {
                            if (user.getNick().toLowerCase().equals(
                                    searchNick.getText().toString().toLowerCase())) {
                                Toast.makeText(MainActivity.this,
                                        "Friendrequest sent to " + user.getDbID(), Toast.LENGTH_LONG);
                                //TODO Send FriendRequest
                                dialog.dismiss();
                                break;
                            }
                        }
                        Toast.makeText(MainActivity.this,
                                "No such user found: " + searchNick.getText().toString(), Toast.LENGTH_LONG);
                        dialog.dismiss();
                    }
                }
                Toast.makeText(MainActivity.this,
                        "Friendrequest aborted", Toast.LENGTH_LONG);
                dialog.cancel();
            }
        });
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {
        //TODO Display Dialog if user wants to exit
    }
}
