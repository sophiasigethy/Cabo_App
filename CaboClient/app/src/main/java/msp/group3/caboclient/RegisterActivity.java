package msp.group3.caboclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    private Player player;
    private String nick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText mail = findViewById(R.id.register_mail);
        final EditText name = findViewById(R.id.register_name);
        final EditText nick = findViewById(R.id.register_nick);
        final EditText password = findViewById(R.id.register_password);
        final Button loginButton = findViewById(R.id.register_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mail.getText().toString().isEmpty()
                        || name.getText().toString().isEmpty()
                        || nick.getText().toString().isEmpty()
                        || password.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this,
                            "All fields are mandatory", Toast.LENGTH_LONG);
                } else {
                    player = new Player("", name.getText().toString(),
                            mail.getText().toString(), nick.getText().toString());
                    register(password.getText().toString());
                }
            }
        });
    }

    private void register(String password) {
        DatabaseOperation.getDao().getmAuth().createUserWithEmailAndPassword(player.getMail(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = DatabaseOperation.getDao().getCurrentUser();
                            user.updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(player.getName())
                                            .build());
                            if (player.getDbID().equals(""))
                                player.setDbID(user.getUid());
                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(String.valueOf(R.string.preference_userdbid), player.getDbID());
                            editor.putString(String.valueOf(R.string.preference_username), player.getName());
                            editor.putString(String.valueOf(R.string.preference_usermail), player.getMail());
                            editor.putString(String.valueOf(R.string.preference_usernick), player.getNick());
                            editor.apply();
                            DatabaseOperation.getDao().addUserToDB(player);
                            moveToMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Registration Failed: "
                                            + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void moveToMainActivity() {
        // Read player from db and move to MainActivity
        String myDbId = DatabaseOperation.getDao().getCurrentUser().getUid();
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);
        DatabaseReference myref = DatabaseOperation.getDao().getUserRef(myDbId);
        //if (!sharedPref.getString(String.valueOf(R.string.preference_userdbid), "None").equals(myDbId)) {
        ValueEventListener readPlayerEvent = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                nick = snapshot.child("nick").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        myref.addValueEventListener(readPlayerEvent);
        //}
        DatabaseOperation.getDao().updateLastLoggedIn(myDbId);
        Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
        myIntent.putExtra("dbid", myDbId);
        myIntent.putExtra("nick", nick);
        RegisterActivity.this.startActivity(myIntent);
    }
}