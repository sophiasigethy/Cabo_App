package msp.group3.caboclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {

    private Player me;
    private Communicator communicator;
    private WebSocketClient mWebSocketClient;
    private ListView friendList;
    private ArrayList<Player> party = new ArrayList<>();
    private ArrayList<Player> allUsers = new ArrayList<>();
    private TextView userNameTxt;
    private ImageButton startGameBtn;
    private ImageButton addFriendBtn;
    private SharedPreferences sharedPref;
    private Activity activity;
    private FriendListAdapter friendListAdapter;
    private CircleImageView playerImage;
    private TextView playerName;
    private TextView playerScoreTextView;
    private TextView playerPartyText;
    ImageView player1Status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        friendList = (ListView) findViewById(R.id.list_friends);
        userNameTxt = (TextView) findViewById(R.id.username);
        startGameBtn = (ImageButton) findViewById(R.id.start_game_btn);
        addFriendBtn = (ImageButton) findViewById(R.id.add_friend_btn);
        playerImage = (CircleImageView) findViewById(R.id.player1_image_main);
        playerName = (TextView) findViewById(R.id.player1_name_textview_main);
        playerScoreTextView = (TextView) findViewById(R.id.player1_score_textview_main);
        playerPartyText = (TextView) findViewById(R.id.player1_party_textview_main);
        player1Status = (ImageView) findViewById(R.id.player1_status);
        sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);

        //connects to server
        communicator = Communicator.getInstance(this);
        communicator.connectWebSocket();
        mWebSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);
        me = DatabaseOperation.getDao().readPlayerFromSharedPrefs(sharedPref);
        //userNameTxt.setText("Welcome " + me.getNick());

        playerName.setText(me.getNick());
        playerImage.setImageResource(me.getAvatarIcon());
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
                    player1Status.setImageResource(R.drawable.online);
                    Toast.makeText(activity, R.string.welcome + " " + me.getNick(), Toast.LENGTH_LONG);
                    enableButtons();
                    if (!party.contains(me))
                        party.add(me);
                    playerPartyText.setText("Party: " + party.size());
                    Log.d("---------------PARTY", "me added to party");
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
            player1Status.setImageResource(R.drawable.offline);
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
                runOnUiThread(new Runnable() {
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
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateFriendList(sender, true);
                    }
                });
                communicator.sendMessage(JSON_commands.getOnlinestatusOfNewFriend(senderNick));
            }

        }

        if (jsonObject.has("partyrequest")) {
            Gson gson = new Gson();
            Player sender = null;
            JSONObject js = jsonObject.getJSONObject("partyrequest");

            if (js.has("sender")) {
                String jsonString = js.get("sender").toString();
                sender = gson.fromJson(jsonString, Player.class);
            }

            acceptPartyInvitationDialog(sender);
        }

        if (jsonObject.has("partyaccepted")) {
            Log.d("-----------------PARTY INVITATION", "invitation accepted");
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
            Log.d("-----------------PARTY INVITATION", "sender: " + sender.getName() + " receiver: " + receiver.getName());

            showMessageForTesting(sender.getNick()+ " accepted your invitation");

            Player finalSender = sender;
            runOnUiThread(new Runnable() {
                public void run() {

                    party.add(finalSender);
                    playerPartyText.setText("Party: " + party.size());
                    updateFriendList(finalSender, false);
                    /*View v = friendList.getChildAt(
                            friendListAdapter.getPlayerIndex(finalSender) - friendList.getFirstVisiblePosition());
                    if (v != null) {
                        if (!party.contains(finalSender)) {
                            party.add(finalSender);
                            updateFriendList(finalSender, false);
                        }
                    }*/
                }
            });

        }
        if (jsonObject.has("onlinestatus")) {
            JSONObject js = jsonObject.getJSONObject("onlinestatus");
            Player player = null;
            //TODO DBID is empty
            boolean isOnline = false;
            if (js.has("isOnline")) {
                isOnline = (boolean) js.get("isOnline");
            }

            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                player = gson.fromJson(jsonString, Player.class);
            }

            Log.d("-----------------ONLINE STATUS", player.getName() + " sendStatus: " + isOnline);
            Log.d("-----------------ONLINE STATUS", player.getName() + " actualStatus: " + player.getStatus());

            if (!allUsers.contains(player)) {
                allUsers.add(player);
                DatabaseOperation.getDao().saveObjectToSharedPreference(
                        sharedPref, String.valueOf(R.string.preference_all_users), allUsers);
            }
            if (isPlayerInFriendList(player)) {
                boolean finalIsOnline = isOnline;
                Player finalPlayer = player;
                activity.runOnUiThread(new Runnable() {
                    public void run() {

                        int index = indexInFriendList(finalPlayer);
                        Player changedPlayer = me.getFriendList().get(index);
                        me.getFriendList().get(index).setOnline(finalIsOnline);
                        Log.d("-----------------ONLINE STATUS", "friendlist size: " + me.getFriendList().size());
                        Log.d("-----------------ONLINE STATUS", "friend set online: " + me.getFriendList().get(indexInFriendList(finalPlayer)).getName());

                        friendListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
        if (jsonObject.has("startPrivateParty")) {
            startMatching();
        }
        if (jsonObject.has("partyRequestFailed")) {
            String text = (String) jsonObject.get("partyRequestFailed");

            showMessageForTesting(text);

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
            String mes = player.getNick() + " joins no longer the party!.";
            showMessageForTesting(mes);
            //TODO reset party icon for this player
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
            String mes = "Party leader " + player.getNick() + " left the party, so you can start a new party now!";
            showMessageForTesting(mes);
            //TODO reset party icon for this player
        }

        if (jsonObject.has("informOthersAboutInvitation")) {
            JSONObject js = jsonObject.getJSONObject("informOthersAboutInvitation");
            Player player = null;


            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                player = gson.fromJson(jsonString, Player.class);
            }
            if (!alreadyInParty(player)) {
                party.add(player);
                String mes = player.getNick() + " was added to the party.";
                showMessageForTesting(mes);
            }
            //TODO show  that player Leader has disconnected and party is empty
        }


    }

    public int indexInFriendList(Player player) {
        for (int i = 0; i < me.getFriendList().size(); i++) {
            if (player.getNick().equalsIgnoreCase(me.getFriendList().get(i).getNick())) {
                return i;
            }
        }
        return 1000;
    }

    public boolean isFriend(Player player) {
        for (int i = 0; i < me.getFriendList().size(); i++) {
            if (player.getId() == me.getFriendList().get(i).getId()) {
                return true;
            }
        }
        return false;
    }


    public void updateFriendList(Player sender, boolean isNewFriend) {
        ArrayList<Player> friends = me.getFriendList();
        if (!isNewFriend) {
            //friends.remove(sender);
            //friends.add(0, sender);
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
        CustomSearchDialog dialog = new CustomSearchDialog();
        dialog.showDialog(activity, me, allUsers, communicator);
    }

    private void acceptFriendRequestDialog(Player sender) {
        RequestDialog requestDialog = new RequestDialog();
        requestDialog.showDialog(activity, sender);
        requestDialog.getDialogButtonAccept().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!me.getFriendsNicknames().contains(sender.getNick())) {
                    if (DatabaseOperation.getDao().addFriendships(me, sender)) {
                        updateFriendList(sender, true);
                        try {
                            communicator.sendMessage(JSON_commands.sendFriendRequestAccepted(me, sender));
                            communicator.sendMessage(JSON_commands.getOnlinestatusOfNewFriend(sender.getNick()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            player1Status.setImageResource(R.drawable.offline);
                        }
                        requestDialog.getDialog().dismiss();
                    } else {
                        requestDialog.getDialog().dismiss();
                        Toast.makeText(activity, "Error while adding friend", Toast.LENGTH_LONG);
                    }

                } else
                    Toast.makeText(activity, "User is already a friend", Toast.LENGTH_LONG);
            }
        });
    }

    private void acceptPartyInvitationDialog(Player sender) {
        activity.runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            public void run() {
                RequestDialog requestDialog = new RequestDialog();
                requestDialog.showDialog(activity, sender);
                requestDialog.getText().setText(getApplicationContext().getResources().getString(R.string.party_invitation_received)
                        + ": " + sender.getNick());
                requestDialog.getImage().setImageResource(R.drawable.party_invitation);
                requestDialog.getDialogButtonAccept().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!party.contains(sender)) {
                            // Since you accepted the request, add player to party and send confirmation
                            party.add(sender);
                            updateFriendList(sender, false);
                            try {
                                communicator.sendMessage(
                                        JSON_commands.sendPartyAccepted(me, sender));
                                requestDialog.getDialog().dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                player1Status.setImageResource(R.drawable.offline);
                            }
                        }
                    }
                });
            }
        });
    }

    public void enableButtons() {
        addFriendBtn.setEnabled(true);
        startGameBtn.setEnabled(true);
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

    public void showMessageForTesting(String text) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_LONG).show();

            }
        });
    }

    public void removePlayerFromParty(Player player) {
        for (int i = 0; i < party.size(); i++) {
            if (party.get(i).getNick().equalsIgnoreCase(player.getNick())) {
                party.remove(i);
            }
        }
    }

    public boolean alreadyInParty(Player player) {
        for (Player partyPlayer : party) {
            if (partyPlayer.getNick().equalsIgnoreCase(player.getNick())) {
                return true;
            }
        }
        return false;
    }
}
