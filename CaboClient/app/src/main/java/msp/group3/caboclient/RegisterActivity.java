package msp.group3.caboclient;

import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    private Player player;

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
                            DatabaseOperation.getDao().addUserToDB(player);
                            DatabaseOperation.getDao().getPlayerFromDB(player.getDbID(),
                                    getApplicationContext());
                            DatabaseOperation.getDao().updateLastLoggedIn(user.getUid());
                            moveToMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        /*
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(String.valueOf(R.string.preference_userdbid), DatabaseOperation.getDao().getCurrentUser().getUid());
        editor.putString(String.valueOf(R.string.preference_username), player.getName());
        editor.putString(String.valueOf(R.string.preference_usermail), player.getMail());
        editor.putString(String.valueOf(R.string.preference_usernick), player.getNick());
        editor.apply();
        */
    }

    private void moveToMainActivity() {
        //add user to firebase db
        Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
        myIntent.putExtra("dbid", player.getDbID());
        RegisterActivity.this.startActivity(myIntent);
    }
}