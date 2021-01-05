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

    private DatabaseOperation() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

    }

    public static synchronized DatabaseOperation getDao() {
        /**
         * Call the DatabaseOperation Singleton and use it
         * */
        if (dao == null) {
            return new DatabaseOperation();
        }
        return dao;
    }

    public Player readPlayerFromSharedPrefs(SharedPreferences sharedPref) {
        /**
         * This function reads the information about the player, stored in SharedPrefs
         * It returns the Player as a Player Object
         * @param sharedPref: The SharedPref Object to access it
         * @return: The Player Object stored in SharedPref
         * */
        Player player = new Player(
                sharedPref.getString(String.valueOf(R.string.preference_userdbid), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_username), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_usermail), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_usernick), "None")
        );
        String friends = sharedPref.getString(String.valueOf(R.string.preference_friendlist), "None");
        if (!friends.equals("None")) {
            ArrayList<Player> friendList = new ArrayList<Player>();
            ArrayList deserializedFriends = getSavedObjectFromPreference(
                    sharedPref, String.valueOf(R.string.preference_friendlist), ArrayList.class);
            if (deserializedFriends.size() > 0) {
                for (int i = 0; i < deserializedFriends.size(); i++) {
                    String dbID = (String) ((LinkedTreeMap) deserializedFriends.get(i)).get("dbID");
                    String nick = (String) ((LinkedTreeMap) deserializedFriends.get(i)).get("nick");
                    friendList.add(new Player(dbID, nick));
                }
                if (friendList != null)
                    player.setFriendList(friendList);
                else
                    Log.e("DESERIALIZE", "Could not deserialize Friendlist");
            }
        }
        return player;
    }

    public void updateLastLoggedIn(String myDbId) {
        /**
         * This function saves a timestamp into the active user in Firebase Realtime DB
         * This is necessary, to trigger the ValueEventListener, so that data can be loaded
         * from firebase after login
         * @param myDbId: My DbID to access the correct path in Firebase Realtime DB
         * */
        getUserRef(myDbId).child("lastLoggedIn").setValue((System.currentTimeMillis()) + "");
    }

    public boolean addUserToDB(Player player, SharedPreferences sharedPref) {
        /**
         * This function adds a new user to the Firebase Realtime DB
         * If a nickname is already in use, false is returned
         * @param player: The Player to add to the Firebase Realtime DB
         * @param sharedPref: The SharedPref Object to access it
         * @return: True, if everything is fine
         *          False, if nickname already in use
         * */
        ArrayList<Player> allUsers = DatabaseOperation.getDao().getAllUsersList(sharedPref);
        for (Player user : allUsers) {
            if (user.getNick().toLowerCase().equals(player.getNick().toLowerCase())) {
                return false;
            }
        }
        getUserRef(player.getDbID()).setValue(player);
        return true;
    }

    public void setupDatabaseListener(String myDbId, SharedPreferences sharedPref) {
        /**
         * To read from Firebase Realtime database, you have to add a ValueEventListener to a DB-Reference
         * Each time values at the reference change, the Listener is called
         * This Listener reads every information about the user from the db and writes it to
         * SharedPrefs
         * To avoid multiple calls of the listener, it is setup once after Login/Registration
         * @param myDbId: My DbID to access the correct path in Firebase Realtime DB
         * @param sharedPref: The SharedPref Object to access it
         */
        DatabaseReference myref = getDao().getUserRef(myDbId);
        ValueEventListener readPlayerEvent = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Player> friendList = new ArrayList<>();
                for (DataSnapshot friend : snapshot.child("friendList").getChildren()) {
                    friendList.add(new Player(friend.child("dbID").getValue() + "",
                            friend.child("nick").getValue() + ""));
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(String.valueOf(R.string.preference_userdbid),
                        snapshot.child("dbID").getValue().toString());
                editor.putString(String.valueOf(R.string.preference_username),
                        snapshot.child("name").getValue().toString());
                editor.putString(String.valueOf(R.string.preference_usermail),
                        snapshot.child("mail").getValue().toString());
                editor.putString(String.valueOf(R.string.preference_usernick),
                        snapshot.child("nick").getValue().toString());
                editor.apply();
                saveObjectToSharedPreference(
                        sharedPref, String.valueOf(R.string.preference_friendlist), friendList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myref.addValueEventListener(readPlayerEvent);
    }

    public void updateUserList(SharedPreferences sharedPref) {
        /**
         * Read all registered users from Firebase RealtimeDatabase and write them to SharedPrefs
         * @param sharedPref: The SharedPref Object to access it
         */
        DatabaseReference myref = getAllUsersRef();
        ValueEventListener readAllPlayersEvent = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Player> users = new ArrayList<>();
                for (DataSnapshot user : snapshot.getChildren()) {
                    String dbId = (String) user.child("dbID").getValue();
                    if (dbId != null) {
                        String nick = user.child("nick").getValue().toString();
                        if (nick != null)
                            users.add(new Player(dbId, nick));
                    }
                }
                saveObjectToSharedPreference(
                        sharedPref, String.valueOf(R.string.preference_users), users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myref.addListenerForSingleValueEvent(readAllPlayersEvent);
        myref.child("0").setValue((System.currentTimeMillis()) + "");
    }

    public ArrayList<Player> getAllUsersList(SharedPreferences sharedPref) {
        /**
         * Read serialized list of all registered users
         * Maybe it´s a good idea to call updateUserList() before
         * @param sharedPref: The SharedPref Object to access it
         */
        ArrayList<Player> allUsers = new ArrayList<>();
        String users = sharedPref.getString(String.valueOf(R.string.preference_users), "None");
        if (!users.equals("None")) {
            ArrayList deserializedUsers = getSavedObjectFromPreference(
                    sharedPref, String.valueOf(R.string.preference_users), ArrayList.class);
            if (deserializedUsers.size() > 0) {
                for (int i = 0; i < deserializedUsers.size(); i++) {
                    String dbID = (String) ((LinkedTreeMap) deserializedUsers.get(i)).get("dbID");
                    String nick = (String) ((LinkedTreeMap) deserializedUsers.get(i)).get("nick");
                    allUsers.add(new Player(dbID, nick));
                }
            }
        }
        return allUsers;
    }

    public void addFriendships(Player receiver, Player sender) {
        /**
         * If a friendrequest was accepted, add both users to each other´s friendList in the DB
         * @param receiver: The Player who received the FriendRequestAccepted-Message
         * @param sender: The Player who sent the FriendRequestAccepted-Message
         */
        DatabaseReference myRef = database.getReference("cabo/users").
                child(receiver.getDbID()).child("friendList").child(sender.getDbID());
        myRef.child("dbID").setValue(sender.getDbID());
        myRef.child("nick").setValue(sender.getNick());
        myRef = database.getReference("cabo/users").
                child(sender.getDbID()).child("friendList").child(receiver.getDbID());
        myRef.child("dbID").setValue(receiver.getDbID());
        myRef.child("nick").setValue(receiver.getNick());
    }

    public static void saveObjectToSharedPreference(
            SharedPreferences sharedPref, String serializedObjectKey, Object object) {
        /**
         * Serialize an object and write the string to SharedPref
         * @param sharedPref: The SharedPref Object to access it
         * @param serializedObjectKey: The key, under which the serialized object will be stored
         * @param object: The object to be serialized and stored
         * */
        SharedPreferences.Editor sharedPreferencesEditor = sharedPref.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    public static <GenericClass> GenericClass getSavedObjectFromPreference(
            SharedPreferences sharedPref, String preferenceKey, Class<GenericClass> classType) {
        /**
         * Deserialize a serialized object from SharedPref
         * @param sharedPref: The SharedPref Object to access it
         * @param preferenceKey: The key, under which the serialized object can be found
         * @param classType: The Class-Type, the deserialized object will have
         **/
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
}
