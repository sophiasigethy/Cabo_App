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

    public DatabaseReference getUserRef(String dbID)    {
        return database.getReference("cabo/users").child(dbID);
    }

    public Player readPlayerFromSharedPrefs(Context context)  {
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
        DatabaseReference myRef = database.getReference("cabo/users").child(dbID);
        myRef.child("lastLoggedIn").setValue((System.currentTimeMillis())+"");
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
