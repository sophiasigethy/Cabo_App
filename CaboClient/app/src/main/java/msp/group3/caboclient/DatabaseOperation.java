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
        if (dao == null) {
            return new DatabaseOperation();
        }
        return dao;
    }

    private void setupListenerForPlayerData() {

    }

    public DatabaseReference getUserRef(String dbID) {
        return database.getReference("cabo/users").child(dbID);
    }

    public DatabaseReference getUsersRef() {
        return database.getReference("cabo/users");
    }

    public Player readPlayerFromSharedPrefs(SharedPreferences sharedPref) {
        Player player = new Player(
                sharedPref.getString(String.valueOf(R.string.preference_userdbid), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_username), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_usermail), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_usernick), "None")
        );
        String friends = sharedPref.getString(String.valueOf(R.string.preference_friendlist),
                "None");
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

    public void updateLastLoggedIn(String dbID) {
        DatabaseReference myRef = database.getReference("cabo/users").child(dbID);
        myRef.child("lastLoggedIn").setValue((System.currentTimeMillis()) + "");
    }

    public void addUserToDB(Player player) {
        //TODO: Add check for username already in use
        DatabaseReference myRef = database.getReference("cabo/users");
        myRef.child(player.getDbID()).setValue(player);
    }

    public void addFriend(String myDbId, String friendsDbId) {
        DatabaseReference myRef = database.getReference("cabo/friendships");
        myRef.child(myDbId).child(friendsDbId).setValue("1");
    }

    public void setupDatabaseListener(String myDbId, SharedPreferences sharedPref) {
        DatabaseReference myref = getDao().getUserRef(myDbId);
        //if (!sharedPref.getString(String.valueOf(R.string.preference_userdbid), "None").equals(myDbId)) {
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

    public static void saveObjectToSharedPreference(
            SharedPreferences sharedPreferences, String serializedObjectKey, Object object) {
        // Serialize an object and write the string to sharedpref
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(object);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    public void updateUserList(SharedPreferences sharedPref) {
        //Read all registered users from Firebase RealtimeDatabase and write them to SharedPrefs
        DatabaseReference myref = getUsersRef();
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
        // Read serialized list of all registered users
        // Maybe it´s a good idea to call updateUserList() before
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


    public static <GenericClass> GenericClass getSavedObjectFromPreference(
            SharedPreferences sharedPreferences, String preferenceKey, Class<GenericClass> classType) {
        // Deserialize an object from sharedpref
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType);
        }
        return null;
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
