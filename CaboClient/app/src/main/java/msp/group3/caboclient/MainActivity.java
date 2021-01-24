package msp.group3.caboclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    private ArrayList<Player> party = new ArrayList<>();
    private ArrayList<Player> allUsers = new ArrayList<>();
    private TextView userNameTxt;
    private Button startGameBtn;
    private Button findGameBtn;
    private ImageButton addFriendBtn;
    private SharedPreferences sharedPref;
    private Activity activity;
    private FriendListAdapter friendListAdapter;
    private boolean accepted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        friendList = (ListView) findViewById(R.id.list_friends);
        userNameTxt = (TextView) findViewById(R.id.username);
        startGameBtn = (Button) findViewById(R.id.start_game_btn);
        findGameBtn = (Button) findViewById(R.id.find_game_btn);
        addFriendBtn = (ImageButton) findViewById(R.id.add_friend_btn);
        sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);

        //connects to server
        communicator = Communicator.getInstance(this);
        communicator.connectWebSocket();
        mWebSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);
        me = DatabaseOperation.getDao().readPlayerFromSharedPrefs(sharedPref);
        userNameTxt.setText("Welcome " + me.getNick());
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMatching();
            }
        });
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchFriendDialog();
            }
        });
        allUsers = DatabaseOperation.getDao().getAllUsersList(sharedPref);
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
        if (jsonObject.has("connectionAccepted")) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, R.string.welcome + " " + me.getNick(), Toast.LENGTH_LONG);
                    enableButtons();
                    if (!party.contains(me))
                        party.add(me);
                    friendListAdapter = new FriendListAdapter(activity, me, party, communicator);
                    friendList.setAdapter(friendListAdapter);
                    //updateFriendList();
                }
            });
            try {
                communicator.sendMessage(JSON_commands.sendUserLogin(me));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (jsonObject.has("connectionNotAccepted")) {
            String mes = TypeDefs.server + jsonObject.get("connectionNotAccepted").toString();
        }

        if (jsonObject.has("friendrequest")) {
            JSONObject friendrequest = (JSONObject) jsonObject.get("friendrequest");
            String destinationDbID = friendrequest.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
            if (destinationDbID.equals(me.getDbID())) {
                String senderDbID = friendrequest.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = friendrequest.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int senderAvatar = Integer.parseInt(friendrequest.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                Player sender = new Player(senderDbID, senderNick, senderAvatar);
                //TODO: Find better way to display this Dialog
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        acceptFriendRequestDialog(sender);
                    }
                });
            }
        }

        if (jsonObject.has("friendAccepted")) {
            JSONObject friendrequest = (JSONObject) jsonObject.get("friendAccepted");
            String destinationDbID = friendrequest.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
            if (destinationDbID.equals(me.getDbID())) {
                String senderDbID = friendrequest.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = friendrequest.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int senderAvatar = Integer.parseInt(friendrequest.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                Player sender = new Player(senderDbID, senderNick, senderAvatar);
                //TODO: Find better way to display this Dialog
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        updateFriendList(sender, true);
                    }
                });
            }
        }

        if (jsonObject.has("partyrequest")) {
            // Invite a Player to join your party
           /* JSONObject partyRequest = (JSONObject) jsonObject.get("partyrequest");
            String destinationDbID = partyRequest.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
            if (destinationDbID.equals(me.getDbID())) {
                String senderDbID = partyRequest.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = partyRequest.get("senderNick").toString().replace("\"", "").replace("\\", "");

                int senderAvatar = Integer.parseInt(partyRequest.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                Player sender = new Player(senderDbID, senderNick, senderAvatar);*/
            Gson gson = new Gson();
            Player sender = null;
            JSONObject js = jsonObject.getJSONObject("partyrequest");

            if (js.has("sender")) {
                String jsonString = js.get("sender").toString();
                sender = gson.fromJson(jsonString, Player.class);
            }
            //TODO only for testing
            showMessageForTesting("Friendrequest von"+sender.getNick());
            communicator.sendMessage(JSON_commands.sendPartyAccepted(me, sender));

            View v = friendList.getChildAt(friendListAdapter.getPlayerIndex(sender) - friendList.getFirstVisiblePosition());
            if (v != null) {
                @SuppressLint("WrongViewCast") Button inviteButton = (Button) v.findViewById(R.id.friendlist_invite);
                inviteButton.setBackgroundResource(R.drawable.party_invitation);
                inviteButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
                Player finalSender = sender;
                inviteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View v = friendList.getChildAt(friendListAdapter.getPlayerIndex(finalSender) - friendList.getFirstVisiblePosition());
                        if (v != null) {
                            if (!party.contains(finalSender)) {
                                // Since you accepted the request, add player to party and send confirmation
                                party.add(finalSender);
                                updateFriendList(finalSender, false);
                                try {
                                    communicator.sendMessage(
                                            JSON_commands.sendPartyAccepted(me, finalSender));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }

        }

        if (jsonObject.has("partyaccepted")) {
            // If a player has accepted your party invitation, you receive this
            /*JSONObject partyAccepted = (JSONObject) jsonObject.get("partyaccepted");
            String destinationDbID = partyAccepted.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
            if (destinationDbID.equals(me.getDbID())) {
                String senderDbID = partyAccepted.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = partyAccepted.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int senderAvatar = Integer.parseInt(partyAccepted.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                Player sender = new Player(senderDbID, senderNick, senderAvatar);*/
            Gson gson = new Gson();
            Player sender = null;
            Player receiver = null;
            JSONObject js = jsonObject.getJSONObject("partyaccepted");

            if (js.has("sender")) {
                String jsonString = js.get("sender").toString();
                sender = gson.fromJson(jsonString, Player.class);
            }
            if (js.has("receiver")) {
                String jsonString = js.get("sender").toString();
                receiver = gson.fromJson(jsonString, Player.class);
            }
            showMessageForTesting("Einladung angenommen");

            Player finalSender = sender;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    View v = friendList.getChildAt(
                            friendListAdapter.getPlayerIndex(finalSender) - friendList.getFirstVisiblePosition());
                    if (v != null) {
                        if (!party.contains(finalSender)) {
                            party.add(finalSender);
                            updateFriendList(finalSender, false);
                        }
                    }
                }
            });

        }
        if (jsonObject.has("onlinestatus")) {
            JSONObject js = jsonObject.getJSONObject("onlinestatus");
            Player player = null;
            boolean isOnline = false;
            if (js.has("isOnline")) {
                isOnline = (boolean) js.get("isOnline");
            }

            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                player = gson.fromJson(jsonString, Player.class);
            }

           /* JSONObject onlineRequest = (JSONObject) jsonObject.get("onlinestatus");
            Boolean isOnline = Boolean.parseBoolean(onlineRequest.get("isonline").toString().replace("\"", "").replace("\\", ""));
            String playerDbID = onlineRequest.get("senderDbID").toString().replace("\"", "").replace("\\", "");
            String playerNick = onlineRequest.get("senderNick").toString().replace("\"", "").replace("\\", "");
            int playerAvatar = Integer.parseInt(onlineRequest.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
            Player player = new Player(playerDbID, playerNick, playerAvatar);*/
            if (!allUsers.contains(player)) {
                allUsers.add(player);
                DatabaseOperation.getDao().saveObjectToSharedPreference(
                        sharedPref, String.valueOf(R.string.preference_all_users), allUsers);
            }
            if (isPlayerInFriendList(player)) {
                boolean finalIsOnline = isOnline;
                Player finalPlayer = player;
                Player finalPlayer1 = player;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        View v = friendList.getChildAt(
                                friendListAdapter.getPlayerIndex(finalPlayer) - friendList.getFirstVisiblePosition());
                        if (v != null) {
                            ImageView friendlistStatus = (ImageView) v.findViewById(R.id.friendlist_status);
                            if (finalIsOnline) {
                                //friendlistStatus.setBackgroundColor(Color.GREEN);
                                //friendlistStatus.setBackground(ContextCompat.getDrawable(activity, R.drawable.circle_green));
                                friendlistStatus.setImageResource(R.drawable.online);
                                me.getFriendList().get(me.getFriendList().indexOf(finalPlayer)).setOnline(true);
                            } else {
                                //friendlistStatus.setBackgroundColor(Color.RED);
                                //friendlistStatus.setBackground(ContextCompat.getDrawable(activity, R.drawable.circle_red));
                                friendlistStatus.setImageResource(R.drawable.offline);
                                me.getFriendList().get(me.getFriendList().indexOf(finalPlayer)).setOnline(true);
                            }
                            //TODO Check if this is enough
                            updateFriendList(finalPlayer, false);
                            friendListAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
        if (jsonObject.has("startPrivateParty")) {
            startMatching();
        }
        if (jsonObject.has("partyRequestFailed")) {
            String text = (String) jsonObject.get("partyRequestFailed");
            //TODO show text why party request failed
        }

        if (jsonObject.has("playerRemovedFromParty")) {
            JSONObject js = jsonObject.getJSONObject("playerRemovedFromParty");
            Player player = null;

            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                player = gson.fromJson(jsonString, Player.class);
            }
            removePlayerFromParty(player);
            //TODO show  that player has disconnected and is no longer part of party
        }
        if (jsonObject.has("leaderRemovedFromParty")) {
            JSONObject js = jsonObject.getJSONObject("leaderRemovedFromParty");
            Player player = null;

            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                player = gson.fromJson(jsonString, Player.class);
            }
            party.clear();
            party.add(me);
            //TODO show  that player Leader has disconnected and party is empty
        }


    }


    private void updateFriendList(Player sender, boolean isNewFriend) {
        ArrayList<Player> friends = me.getFriendList();
        if (!isNewFriend) {
            friends.remove(sender);
            friends.add(0, sender);
        } else {
            me.addNewFriend(sender, sharedPref);
        }
        //TODO Check if Adapter also has to get new list
        me.setFriendList(friends);
        friendListAdapter.notifyDataSetChanged();
    }

    public void startMatching() {
        Intent intent = new Intent(activity, WaitingRoomActivity.class);
        int i;
        for (i = 0; i < party.size(); i++) {
            if (party.get(i).equals(me))
                continue;
            intent.putExtra("player" + i + "dbid", party.get(i).getDbID());
            intent.putExtra("player" + i + "nick", party.get(i).getNick());
            intent.putExtra("player" + i + "avatar", party.get(i).getAvatarID() + "");
        }
        while (i < 4) {
            intent.putExtra("player" + i + "dbid", "");
            intent.putExtra("player" + i + "nick", "");
            intent.putExtra("player" + i + "avatar", "");
            i++;
        }
        // TODO wait for ServerMessage with GameState-ID

        startActivity(intent);
    }


    private void searchFriendDialog() {
        DatabaseOperation.getDao().updateAllPlayers(sharedPref);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.search_friend);
        // Set up the input
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText searchNick = new EditText(activity);
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
                    if (!(me.getFriendsNicknames().contains(searchNick.getText().toString()))) {
                        if (allUsers.size() > 0) {
                            for (Player user : allUsers) {
                                if (user.getNick().toLowerCase().equals(
                                        searchNick.getText().toString().toLowerCase().trim())) {
                                    Toast.makeText(activity,
                                            "Friendrequest sent to " + user.getDbID(), Toast.LENGTH_LONG);
                                    try {
                                        communicator.sendMessage(JSON_commands.sendFriendRequest(me, user));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                    break;
                                }
                            }
                            Toast.makeText(activity,
                                    "No such user found: " + searchNick.getText().toString(),
                                    Toast.LENGTH_LONG);
                            dialog.dismiss();
                        }
                    } else
                        Toast.makeText(activity,
                                searchNick.getText().toString() + " is already your friend",
                                Toast.LENGTH_LONG);
                }
                Toast.makeText(activity,
                        "You have to enter a Name", Toast.LENGTH_LONG);
                dialog.cancel();
            }
        });
    }

    private void acceptFriendRequestDialog(Player sender) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(
                getApplicationContext().getResources().getString(R.string.friend_request_received)
                        + ": " + sender.getNick());
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!me.getFriendsNicknames().contains(sender.getNick())) {
                    if (DatabaseOperation.getDao().addFriendships(me, sender)) {
                        updateFriendList(sender, true);
                        try {
                            communicator.sendMessage(JSON_commands.sendFriendRequestAccepted(me, sender));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(activity, "Error while adding friend", Toast.LENGTH_LONG);
                    }

                } else
                    Toast.makeText(activity, "User is already a friend", Toast.LENGTH_LONG);
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Return declinefriendrequest message to sender
            }
        });

    }

    public void enableButtons() {
        addFriendBtn.setEnabled(true);
        startGameBtn.setEnabled(true);
        findGameBtn.setEnabled(true);
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

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            communicator.sendMessage(JSON_commands.sendLogout());
        } catch (JSONException e) {
            Log.e("LOGOUT", "Could not send logout to server");
        }
        //TODO: Remove Connection
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: Establish Connection
    }

    public boolean isPlayerInFriendList(Player player) {
        for (Player friend : me.getFriendList()) {
            if (friend.getNick().equalsIgnoreCase(player.getNick())) {
                return true;
            }
        }
        return false;
    }



    public Player getPlayerByNickInFriendlist(String nick) {
        for (Player friend : me.getFriendList()) {
            if (friend.getNick().equalsIgnoreCase(nick)) {
                return friend;
            }
        }
        return null;
    }

    public void showMessageForTesting(String text){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG);

            }
        });
    }

    public void removePlayerFromParty(Player player){
        for (int i=0; i<party.size(); i++){
            if (party.get(i).getNick().equalsIgnoreCase(player.getNick())){
                party.remove(i);
            }
        }
    }
}
