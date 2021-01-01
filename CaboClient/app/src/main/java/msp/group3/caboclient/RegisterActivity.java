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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
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
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
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
        mAuth.createUserWithEmailAndPassword(player.getMail(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(player.getName())
                                            .build());
                            if (player.getDbID().equals(""))
                                player.setDbID(user.getUid());
                            addUserToDB();
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
    }

    private void moveToMainActivity() {
        //add user to firebase db
        Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
        myIntent.putExtra("dbid", player.getDbID());
        RegisterActivity.this.startActivity(myIntent);
    }

    private void addUserToDB() {
        //TODO: Add check for username already in use
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("cabo/users");
        myRef.child(player.getDbID()).setValue(player);
    }
}