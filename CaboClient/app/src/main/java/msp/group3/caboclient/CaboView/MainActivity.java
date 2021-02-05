package msp.group3.caboclient.CaboView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;
import msp.group3.caboclient.CaboController.BackgroundSoundService;
import msp.group3.caboclient.CaboController.Communicator;
import msp.group3.caboclient.CaboController.DatabaseOperation;
import msp.group3.caboclient.CaboController.JSON_commands;
import msp.group3.caboclient.CaboModel.Player;
import msp.group3.caboclient.R;
import msp.group3.caboclient.CaboController.TypeDefs;

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
    private ImageButton musicBtn;
    private ImageButton soundBtn;
    private ImageButton settingsBtn;
    private SharedPreferences sharedPref;
    private Activity activity;
    private FriendListAdapter friendListAdapter;
    private CircleImageView playerImage;
    private TextView playerName;
    //private TextView playerScoreTextView;
    ImageView partySymbol;
    ArrayList<TextView> partyMemberTextviews = new ArrayList<>();
    ImageView player1Status;
    Intent musicService;
    private MediaPlayer soundPlayer;

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
        //playerScoreTextView = (TextView) findViewById(R.id.player1_score_textview_main);
        player1Status = (ImageView) findViewById(R.id.player1_status);
        partySymbol = (ImageView) findViewById(R.id.player1_party_side);
        musicBtn = (ImageButton) findViewById(R.id.music_button);
        soundBtn = (ImageButton) findViewById(R.id.sound_button);
        settingsBtn = (ImageButton) findViewById(R.id.settings_button);
        partySymbol.setVisibility(View.INVISIBLE);
        Collections.addAll(partyMemberTextviews, findViewById(R.id.player1_party_member1), findViewById(R.id.player1_party_member2), findViewById(R.id.player1_party_member3));

        for(TextView partymembertext: partyMemberTextviews){
            partymembertext.setVisibility(View.INVISIBLE);
        }
        sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);
        musicService = new Intent(this, BackgroundSoundService.class);
        musicService.putExtra("song", 1);
        if (DatabaseOperation.getDao().getMusicPlaying(sharedPref).equals("Play"))  {
            musicBtn.setImageResource(R.drawable.music_on);
            startService(musicService);
            //bindService(musicService, mServerConn, Context.BIND_AUTO_CREATE);
        } else {
            musicBtn.setImageResource(R.drawable.music_off);
        }
        if (DatabaseOperation.getDao().getSoundsPlaying(sharedPref).equals("Play"))  {
            soundBtn.setImageResource(R.drawable.sound_on);
            //startService(musicService);
        } else {
            soundBtn.setImageResource(R.drawable.sound_off);
        }

        //connects to server
        communicator = Communicator.getInstance(this);
        communicator.connectWebSocket();
        mWebSocketClient = communicator.getmWebSocketClient();
        communicator.setActivity(this);
        me = DatabaseOperation.getDao().readPlayerFromSharedPrefs(sharedPref);
        //userNameTxt.setText("Welcome " + me.getNick());

        playerName.setText(me.getNick()+" | "+me.getGlobalScore());
        playerImage.setImageResource(me.getAvatarIcon());
        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.select_sound);
                startMatching();
            }
        });
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.select_sound);
                searchFriendDialog();
            }
        });
        musicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.select_sound);
                String musicState=DatabaseOperation.getDao().getMusicPlaying(sharedPref);
                if (musicState.equals("Play"))  {
                    stopService(musicService);
                    DatabaseOperation.getDao().setMusicPlaying("Stop", sharedPref);
                    musicBtn.setImageResource(R.drawable.music_off);
                } else {
                    startService(musicService);
                    DatabaseOperation.getDao().setMusicPlaying("Play", sharedPref);
                    musicBtn.setImageResource(R.drawable.music_on);
                }
            }
        });
        soundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.select_sound);
                String soundState=DatabaseOperation.getDao().getSoundsPlaying(sharedPref);
                if (soundState.equals("Play"))  {
                    DatabaseOperation.getDao().setSoundPlaying("Stop", sharedPref);
                    soundBtn.setImageResource(R.drawable.sound_off);
                } else {
                    DatabaseOperation.getDao().setSoundPlaying("Play", sharedPref);
                    soundBtn.setImageResource(R.drawable.sound_on);
                }
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSound(R.raw.select_sound);
                Intent intent = new Intent(activity, LicenseActivity.class);
                startActivity(intent);
            }
        });
        addFriendBtn.setEnabled(false);
        addFriendBtn.setAlpha(0.5f);
        startGameBtn.setEnabled(false);
        startGameBtn.setAlpha(0.5f);
        allUsers = DatabaseOperation.getDao().getAllUsersList(sharedPref);
        showPartyMembers();
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
                    Log.d("---------------PARTY", "me added to party");
                    friendListAdapter = new FriendListAdapter(activity, me, party, communicator);
                    friendList.setAdapter(friendListAdapter);
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
            Log.d("-----------------FRIEND REQUEST", "FriendRequest sent");
            JSONObject friendrequest = (JSONObject) jsonObject.get("friendrequest");
            Player sender = null;
            Player receiver = null;
            Gson gson = new Gson();
            if (friendrequest.has("receiver")) {
                String jsonString = friendrequest.get("receiver").toString();
                receiver = gson.fromJson(jsonString, Player.class);
            }
            if (friendrequest.has("sender")) {
                String jsonString = friendrequest.get("sender").toString();
                sender = gson.fromJson(jsonString, Player.class);
            }
            Log.d("-----------------FRIEND REQUEST", "sender: " + sender.getName() + " receiver: " + receiver.getName());
            if (receiver.getDbID().equals(me.getDbID())) {
                Player finalSender = sender;
                runOnUiThread(new Runnable() {
                    public void run() {
                        acceptFriendRequestDialog(finalSender);
                    }
                });
            }
        }

        if (jsonObject.has("friendAccepted")) {
            Log.d("-----------------FRIEND ACCEPTED", "FriendRequest accepted");
            JSONObject friendAccepted = (JSONObject) jsonObject.get("friendAccepted");
            Player sender = null;
            Player receiver = null;
            Gson gson = new Gson();
            if (friendAccepted.has("receiver")) {
                String jsonString = friendAccepted.get("receiver").toString();
                receiver = gson.fromJson(jsonString, Player.class);
            }
            if (friendAccepted.has("sender")) {
                String jsonString = friendAccepted.get("sender").toString();
                sender = gson.fromJson(jsonString, Player.class);
            }
            Log.d("-----------------FRIEND ACCEPTED", "sender: " + sender.getName() + " receiver: " + receiver.getName());
            if (receiver != null && receiver.getDbID().equals(me.getDbID())) {
                Player finalSender = sender;
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateFriendList(finalSender, true);
                    }
                });
                communicator.sendMessage(JSON_commands.getOnlinestatusOfNewFriend(sender.getNick()));
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

            showMessageForTesting(sender.getNick() + " accepted your invitation");

            Player finalSender = sender;
            runOnUiThread(new Runnable() {
                public void run() {
                    party.add(finalSender);
                    updateFriendList(finalSender, false);
                    showPartyMembers();
                    playSound(R.raw.party_joined);
                }
            });

        }
        if (jsonObject.has("onlinestatus")) {
            JSONObject js = jsonObject.getJSONObject("onlinestatus");
            Player player = null;
            //TODO DBID is empty -> noch aktuell?
            boolean isOnline = false;
            if (js.has("isOnline")) {
                isOnline = (boolean) js.get("isOnline");
            }

            if (js.has("player")) {
                String jsonString = js.get("player").toString();
                Gson gson = new Gson();
                player = gson.fromJson(jsonString, Player.class);
                player.setOnline(isOnline);
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
                        if (index != 1000) {
                            me.getFriendList().set(index, finalPlayer);
                            Log.d("-----------------ONLINE STATUS", "friendlist size: " + me.getFriendList().size());
                            Log.d("-----------------ONLINE STATUS", "friend set online: " + me.getFriendList().get(indexInFriendList(finalPlayer)).getName());
                            friendListAdapter.notifyDataSetChanged();
                            DatabaseOperation.getDao().updatePlayer(me);
                            DatabaseOperation.getDao().writePlayerToSharedPref(me, sharedPref);
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
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    friendListAdapter.notifyDataSetChanged();
                    showPartyMembers();
                }
            });
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
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    friendListAdapter.notifyDataSetChanged();
                    showPartyMembers();
                }
            });
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
                updateFriendList(player, false);
                String mes = player.getNick() + " was added to the party.";
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        showMessageForTesting(mes);
                        friendListAdapter.notifyDataSetChanged();
                        showPartyMembers();
                    }
                });
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
        activity.runOnUiThread(new Runnable() {
            public void run() {
                ArrayList<Player> friends = me.getFriendList();
                if (!isNewFriend) {
                    //friends.remove(sender);
                    //friends.add(0, sender);
                } else {
                    me.addNewFriend(sender, sharedPref);
                }
                me.setFriendList(friends);
                friendListAdapter.notifyDataSetChanged();
            }
        });
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
            intent.putExtra("player" + i + "globalscore", party.get(i).getGlobalScore() + "");
        }
        while (i < 4) {
            intent.putExtra("player" + i + "dbid", "");
            intent.putExtra("player" + i + "nick", "");
            intent.putExtra("player" + i + "avatar", "");
            intent.putExtra("player" + i + "globalscore", "");
            i++;
        }
        // TODO wait for ServerMessage with GameState-ID -> noch aktuell?

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
                            showPartyMembers();
                            try {
                                communicator.sendMessage(
                                        JSON_commands.sendPartyAccepted(me, sender));
                                requestDialog.getDialog().dismiss();
                                playSound(R.raw.party_joined);
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
        addFriendBtn.setAlpha(1f);
        startGameBtn.setAlpha(1f);
    }

    public void showPartyMembers(){
        Log.d("-----------------------SHOW PARTY", "partymembers: "+party.size());
        if(party.size()>1){
            partySymbol.setVisibility(View.VISIBLE);
        }
        for(TextView partymember : partyMemberTextviews){
            partymember.setVisibility(View.INVISIBLE);
        }
        if(party.size()<=1){
            partySymbol.setVisibility(View.INVISIBLE);
        }
        for(int i=0; i<party.size()-1; i++){
            partyMemberTextviews.get(i).setVisibility(View.VISIBLE);
            partyMemberTextviews.get(i).setText(party.get(i+1).getNick());
        }
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
        stopService(musicService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        musicService = new Intent(this, BackgroundSoundService.class);
        musicService.putExtra("song", 1);
        if (DatabaseOperation.getDao().getMusicPlaying(sharedPref).equals("Play"))  {
            //musicBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.music_on));
            startService(musicService);
        } else {
            //musicBtn.setBackground(ContextCompat.getDrawable(activity, R.drawable.music_off));
        }

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

    /**
     * This function is responsible for playing sounds in this Activity
     * @param sound: The R.raw.*ID* of the sound you want to play
     * */
    public void playSound(int sound) {
        if (DatabaseOperation.getDao().getSoundsPlaying(sharedPref).equals("Play")) {
            if (soundPlayer != null) {
                soundPlayer.stop();
                soundPlayer.release();
            }
            soundPlayer = MediaPlayer.create(this, sound);
            soundPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    soundPlayer.stop();
                    soundPlayer.release();
                }
            });
            soundPlayer.setVolume(90, 90);
            soundPlayer.start();
        }
    }
}
