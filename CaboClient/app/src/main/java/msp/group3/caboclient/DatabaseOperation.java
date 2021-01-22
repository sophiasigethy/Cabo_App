package msp.group3.caboclient;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

public class DatabaseOperation {
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private static DatabaseOperation dao;
    private boolean isDBLoad = false;

    private DatabaseOperation() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

    }

    /**
     * Call the DatabaseOperation Singleton and use it
     */
    public static synchronized DatabaseOperation getDao() {
        if (dao == null) {
            return new DatabaseOperation();
        }
        return dao;
    }

    /**
     * This function reads the information about the player, stored in SharedPrefs
     * It returns the Player as a Player Object
     *
     * @param sharedPref: The SharedPref Object to access it
     * @return: The Player Object stored in SharedPref
     */
    public Player readPlayerFromSharedPrefs(SharedPreferences sharedPref) {
        if (!sharedPref.getString(String.valueOf(R.string.preference_useravatar), "None").equals("None")) {
            Player player = new Player(
                    sharedPref.getString(String.valueOf(R.string.preference_userdbid), "None"),
                    sharedPref.getString(String.valueOf(R.string.preference_username), "None"),
                    sharedPref.getString(String.valueOf(R.string.preference_usermail), "None"),
                    sharedPref.getString(String.valueOf(R.string.preference_usernick), "None"),
                    Integer.parseInt(sharedPref.getString(String.valueOf(R.string.preference_useravatar), "None")));
            String friends = sharedPref.getString(String.valueOf(R.string.preference_friendlist), "None");
            if (!friends.equals("None")) {
                ArrayList<Player> friendList = new ArrayList<Player>();
                ArrayList deserializedFriends = getSavedObjectFromPreference(
                        sharedPref, String.valueOf(R.string.preference_friendlist), ArrayList.class);
                if (deserializedFriends.size() > 0) {
                    for (int i = 0; i < deserializedFriends.size(); i++) {
                        String dbID = (String) ((LinkedTreeMap) deserializedFriends.get(i)).get("dbID").toString().replace("\"", "").replace("\\", "");
                        String nick = (String) ((LinkedTreeMap) deserializedFriends.get(i)).get("nick").toString().replace("\"", "").replace("\\", "");
                        //TODO Find out why double is read here
                        int avatarId = (int)Double.parseDouble((String) ((LinkedTreeMap) deserializedFriends.get(i)).get("avatarID").toString().replace("\"", "").replace("\\", ""));
                        friendList.add(new Player(dbID, nick, avatarId));
                    }
                    if (friendList != null)
                        player.setFriendList(friendList);
                    else
                        Log.e("DESERIALIZE", "Could not deserialize Friendlist");
                }
            }
            return player;
        }
        return new Player("None", "None", 9);
    }

    /**
     * This function saves a timestamp into the active user in Firebase Realtime DB
     * This is necessary, to trigger the ValueEventListener, so that data can be loaded
     * from firebase after login
     *
     * @param myDbId: My DbID to access the correct path in Firebase Realtime DB
     */
    public void updateLastLoggedIn(String myDbId) {
        getUserRef(myDbId).child("lastLoggedIn").setValue((System.currentTimeMillis()) + "");
    }

    /**
     * This function adds a new user to the Firebase Realtime DB
     * If a nickname is already in use, false is returned
     *
     * @param player:     The Player to add to the Firebase Realtime DB
     * @param sharedPref: The SharedPref Object to access it
     * @return: True, if everything is fine
     * False, if nickname already in use
     */
    public boolean addUserToDB(Player player, SharedPreferences sharedPref) {
        ArrayList<Player> allUsers = DatabaseOperation.getDao().getAllUsersList(sharedPref);
        for (Player user : allUsers) {
            if (user.getNick().toLowerCase().equals(player.getNick().toLowerCase())) {
                return false;
            }
        }
        getUserRef(player.getDbID()).setValue(player);
        return true;
    }

    /**
     * To read from Firebase Realtime database, you have to add a ValueEventListener to a DB-Reference
     * Each time values at the reference change, the Listener is called. But since we only need to read it once
     * we add a ListenerForSingleValueEvent.
     * This Listener reads every information about the user from the db and writes it to
     * SharedPrefs
     * This method should only be used to get UserData from DB if he uses LoginActivity
     *
     * @param myDbId:     My DbID to access the correct path in Firebase Realtime DB
     * @param sharedPref: The SharedPref Object to access it
     */
    public void setupDatabaseListener(String myDbId, SharedPreferences sharedPref) {
        DatabaseReference myref = getDao().getUserRef(myDbId);
        ValueEventListener readPlayerEvent = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Player> friendList = new ArrayList<>();
                for (DataSnapshot friend : snapshot.child("friendList").getChildren()) {
                    friendList.add(new Player(cleanString(friend.child("dbID").getValue().toString()),
                            cleanString(friend.child("nick").getValue().toString()),
                            Integer.parseInt(cleanString(friend.child("avatarID").getValue().toString()))));
                }
                String myDbID = cleanString(String.valueOf(snapshot.child("dbID").getValue().toString()));
                String name = cleanString(String.valueOf( snapshot.child("name").getValue().toString()));
                String mail = cleanString(String.valueOf( snapshot.child("mail").getValue().toString()));
                String nick = cleanString(String.valueOf( snapshot.child("nick").getValue().toString()));
                int avatarId = Integer.parseInt(
                        cleanString(String.valueOf(snapshot.child("avatarID").getValue().toString())));
                Player player = new Player(myDbID, name, mail, nick, avatarId);
                player.setFriendList(friendList);
                isDBLoad = writePlayerToSharedPref(player, sharedPref);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myref.addListenerForSingleValueEvent(readPlayerEvent);
    }


    public boolean writePlayerToSharedPref(Player player, SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(String.valueOf(R.string.preference_userdbid), player.getDbID());
        editor.putString(String.valueOf(R.string.preference_username), player.getName());
        editor.putString(String.valueOf(R.string.preference_usermail), player.getMail());
        editor.putString(String.valueOf(R.string.preference_usernick), player.getNick());
        editor.putString(String.valueOf(R.string.preference_useravatar), player.getAvatarID()+"");
        editor.apply();
        saveObjectToSharedPreference(
                sharedPref, String.valueOf(R.string.preference_friendlist), player.getFriendList());
        return true;
    }

    /**
     * Read all registered users from Firebase RealtimeDatabase and write them to SharedPrefs
     *
     * @param sharedPref: The SharedPref Object to access it
     */
    public void updateAllPlayers(SharedPreferences sharedPref) {
        DatabaseReference myref = getAllUsersRef();
        ValueEventListener readAllPlayersEvent = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Player> users = new ArrayList<>();
                for (DataSnapshot user : snapshot.getChildren()) {
                    String dbId = (String) user.child("dbID").getValue();
                    if (dbId != null) {
                        String nick = user.child("nick").getValue().toString();
                        int avatarID = Integer.parseInt(user.child("avatarID").getValue().toString());
                        if (nick != null)
                            users.add(new Player(dbId, nick, avatarID));
                    }
                }
                saveObjectToSharedPreference(
                        sharedPref, String.valueOf(R.string.preference_all_users), users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myref.addListenerForSingleValueEvent(readAllPlayersEvent);
        myref.child("0").setValue((System.currentTimeMillis()) + "");
    }

    /**
     * Read serialized list of all registered users
     * Maybe it´s a good idea to call updateUserList() before
     *
     * @param sharedPref: The SharedPref Object to access it
     */
    public ArrayList<Player> getAllUsersList(SharedPreferences sharedPref) {
        ArrayList<Player> allUsers = new ArrayList<>();
        String users = sharedPref.getString(String.valueOf(R.string.preference_all_users), "None");
        if (!users.equals("None")) {
            ArrayList deserializedUsers = getSavedObjectFromPreference(
                    sharedPref, String.valueOf(R.string.preference_all_users), ArrayList.class);
            if (deserializedUsers.size() > 0) {
                for (int i = 0; i < deserializedUsers.size(); i++) {
                    String dbID = cleanString((String) ((LinkedTreeMap) deserializedUsers.get(i)).get("dbID")
                            .toString());

                    String nick = cleanString((String) ((LinkedTreeMap) deserializedUsers.get(i)).get("nick")
                            .toString());
                    //TODO: Find out why we get double
                    int avatarId = (int) Double.parseDouble(cleanString((String) ((LinkedTreeMap) deserializedUsers.get(i)).get("avatarID")
                            .toString()));
                    allUsers.add(new Player(dbID, nick, avatarId));
                }
            }
        }
        return allUsers;
    }

    public boolean isNickFree(SharedPreferences sharedPref, String nick) {
        for (Player player : getAllUsersList(sharedPref))   {
            if (player.getNick().equals(nick))
                return false;
        }
        return true;
    }

    /**
     * If a friendrequest was accepted, add both users to each other´s friendList in the DB
     *
     * @param receiver: The Player who received the FriendRequestAccepted-Message
     * @param sender:   The Player who sent the FriendRequestAccepted-Message
     */
    public boolean addFriendships(Player receiver, Player sender) {

        if (receiver.getDbID() == null || receiver.getNick() == null
                || sender.getDbID() == null || sender.getNick() == null) {
            return false;
        }
        DatabaseReference myRef = database.getReference("cabo/users").
                child(receiver.getDbID()).child("friendList").child(sender.getDbID());
        myRef.child("dbID").setValue(sender.getDbID());
        myRef.child("nick").setValue(sender.getNick());
        myRef = database.getReference("cabo/users").
                child(sender.getDbID()).child("friendList").child(receiver.getDbID());
        myRef.child("dbID").setValue(receiver.getDbID());
        myRef.child("nick").setValue(receiver.getNick());
        return true;
    }

    /**
     * Serialize an object and write the string to SharedPref
     *
     * @param sharedPref:          The SharedPref Object to access it
     * @param serializedObjectKey: The key, under which the serialized object will be stored
     * @param object:              The object to be serialized and stored
     */
    public static void saveObjectToSharedPreference(
            SharedPreferences sharedPref, String serializedObjectKey, Object object) {

        SharedPreferences.Editor sharedPreferencesEditor = sharedPref.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    /**
     * Deserialize a serialized object from SharedPref
     *
     * @param sharedPref:    The SharedPref Object to access it
     * @param preferenceKey: The key, under which the serialized object can be found
     * @param classType:     The Class-Type, the deserialized object will have
     **/
    public static <GenericClass> GenericClass getSavedObjectFromPreference(
            SharedPreferences sharedPref, String preferenceKey, Class<GenericClass> classType) {

        if (sharedPref.contains(preferenceKey)) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPref.getString(preferenceKey, ""), classType);
        }
        return null;
    }

    public DatabaseReference getUserRef(String dbID) {
        return getAllUsersRef().child(dbID);
    }

    public DatabaseReference getAllUsersRef() {
        return database.getReference("cabo/users");
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public String cleanString(String string)    {
        return string
                .replace("\"", "").replace("\\", "");
    }

    public boolean isDBLoad() {
        return isDBLoad;
    }
}
