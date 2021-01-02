package msp.group3.caboclient;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private void setupListenerForPlayerData()    {

    }

    public Player getPlayerFromDB(String dbID, Context context) {
        // Load Player Info from firebase realtime db
        if (dbID.isEmpty()) {
            dbID = currentUser.getUid();
        }
        Player player = new Player(dbID);
        SharedPreferences sharedPref = context.getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);
        if (sharedPref.getString(String.valueOf(R.string.preference_userdbid), "None").equals("None")) {
            DatabaseReference myRef = database.getReference("cabo");
            DatabaseReference usersRef = myRef.child("users");
            String finalDbID = dbID;
            ValueEventListener readPlayerEvent = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (child.getKey().equals(finalDbID)) {
                            player.setDbID(child.child("dbID").getValue().toString());
                            player.setName(child.child("name").getValue().toString());
                            player.setMail(child.child("mail").getValue().toString());
                            player.setNick(child.child("nick").getValue().toString());

                            SharedPreferences sharedPref = context.getSharedPreferences(
                                    R.string.preference_file_key + "", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(String.valueOf(R.string.preference_userdbid),
                                    child.child("dbID").getValue().toString());
                            editor.putString(String.valueOf(R.string.preference_username),
                                    child.child("name").getValue().toString());
                            editor.putString(String.valueOf(R.string.preference_usermail),
                                    child.child("mail").getValue().toString());
                            editor.putString(String.valueOf(R.string.preference_usernick),
                                    child.child("nick").getValue().toString());
                            editor.apply();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
                usersRef.removeEventListener(readPlayerEvent);
                usersRef.addValueEventListener(readPlayerEvent);
        } else
            return readPlayerFromSharedPrefs(dbID, context);
        return player;
    }

    private Player readPlayerFromSharedPrefs(String dbID, Context context)  {
        SharedPreferences sharedPref = context.getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);
        return new Player(
                sharedPref.getString(String.valueOf(R.string.preference_userdbid), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_username), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_usermail), "None"),
                sharedPref.getString(String.valueOf(R.string.preference_usernick), "None")
        );
    }

    public void updateLastLoggedIn(String dbID) {
        DatabaseReference myRef = database.getReference("cabo/users");
        myRef.child(dbID).child("lastLoggedIn").setValue((System.currentTimeMillis() / 1000));
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
