package msp.group3.caboclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    final int NO_AVATAR_CHOSEN = 9;
    final int TRANSPARENT = 0x00000000;
    private static final String TAG = "LOGIN";
    private Player player;
    private String nick;
    private int avatarId = NO_AVATAR_CHOSEN;
    private SharedPreferences sharedPref;
    private LinearLayout avatar0Border;
    private LinearLayout avatar1Border;
    private LinearLayout avatar2Border;
    private LinearLayout avatar3Border;
    private LinearLayout avatar4Border;
    private LinearLayout avatar5Border;
    private TextView errorTv;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText mail = findViewById(R.id.register_mail);
        final EditText name = findViewById(R.id.register_name);
        final EditText nick = findViewById(R.id.register_nick);
        final EditText password = findViewById(R.id.register_password);
        sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);
        DatabaseOperation.getDao().updateAllPlayers(sharedPref);
        avatar0Border = (LinearLayout) this.findViewById(R.id.borderAvatar0);
        ImageView avatar0Button = findViewById(R.id.avatar0);
        avatar0Button.setOnClickListener(this);
        avatar1Border = (LinearLayout) this.findViewById(R.id.borderAvatar1);
        ImageView avatar1Button = findViewById(R.id.avatar1);
        avatar1Button.setOnClickListener(this);
        avatar2Border = (LinearLayout) this.findViewById(R.id.borderAvatar2);
        ImageView avatar2Button = findViewById(R.id.avatar2);
        avatar2Button.setOnClickListener(this);
        avatar3Border = (LinearLayout) this.findViewById(R.id.borderAvatar3);
        ImageView avatar3Button = findViewById(R.id.avatar3);
        avatar3Button.setOnClickListener(this);
        avatar4Border = (LinearLayout) this.findViewById(R.id.borderAvatar4);
        ImageView avatar4Button = findViewById(R.id.avatar4);
        avatar4Button.setOnClickListener(this);
        avatar5Border = (LinearLayout) this.findViewById(R.id.borderAvatar5);
        ImageView avatar5Button = findViewById(R.id.avatar5);
        avatar5Button.setOnClickListener(this);
        errorTv = (TextView) this.findViewById(R.id.register_error_txt);

        final Button registerButton = findViewById(R.id.register_btn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mail.getText().toString().isEmpty()
                        || name.getText().toString().isEmpty()
                        || nick.getText().toString().isEmpty()
                        || password.getText().toString().isEmpty()
                        || avatarId == NO_AVATAR_CHOSEN) {
                    Toast.makeText(RegisterActivity.this,
                            "All fields are mandatory", Toast.LENGTH_LONG);
                    errorTv.setText("All fields are mandatory and you must choose an avatar");
                    errorTv.setTextColor(Color.RED);
                } else {
                    if (DatabaseOperation.getDao().isNickFree(sharedPref, nick.getText().toString())) {
                        player = new Player("", name.getText().toString(),
                                mail.getText().toString(), nick.getText().toString(), avatarId, 0);
                        register(password.getText().toString());
                    } else {
                        Toast.makeText(RegisterActivity.this, R.string.duplicate_user, Toast.LENGTH_LONG);
                        errorTv.setText(R.string.duplicate_user);
                        errorTv.setTextColor(Color.RED);
                    }
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
                            DatabaseOperation.getDao().updateAllPlayers(sharedPref);
                            FirebaseUser user = DatabaseOperation.getDao().getCurrentUser();
                            user.updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(player.getName())
                                            .build());
                            if (player.getDbID().equals(""))
                                player.setDbID(user.getUid());
                            if (DatabaseOperation.getDao().addUserToDB(player, sharedPref)) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(String.valueOf(R.string.preference_userdbid), player.getDbID());
                                editor.putString(String.valueOf(R.string.preference_username), player.getName());
                                editor.putString(String.valueOf(R.string.preference_usermail), player.getMail());
                                editor.putString(String.valueOf(R.string.preference_usernick), player.getNick());
                                editor.putString(String.valueOf(R.string.preference_useravatar), player.getAvatarID() + "");
                                editor.putString(String.valueOf(R.string.preference_global_score), player.getGlobalScore() + "");
                                editor.apply();
                                moveToMainActivity();
                            } else {
                                // This can only fail, if during registering, another user registered with the same name
                                // So the created db user has to be deleted
                                Toast.makeText(RegisterActivity.this, R.string.duplicate_user, Toast.LENGTH_LONG);
                                errorTv.setText(R.string.duplicate_user);
                                errorTv.setTextColor(Color.RED);
                                user.delete();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Registration Failed: "
                                            + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            errorTv.setText("Registration Failed: " + task.getException().getMessage());
                            errorTv.setTextColor(Color.RED);
                        }
                    }
                });
    }

    private void moveToMainActivity() {
        // Read player from db and move to MainActivity
        Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
        myIntent.putExtra("dbid", player.getDbID());
        myIntent.putExtra("mail", player.getMail());
        myIntent.putExtra("name", player.getName());
        myIntent.putExtra("nick", player.getNick());
        myIntent.putExtra("avatarid", player.getAvatarID());
        myIntent.putExtra("globalscore", player.getGlobalScore());
        RegisterActivity.this.startActivity(myIntent);
    }

    public void clearSelection() {
        avatar0Border.setBackgroundColor(TRANSPARENT);
        avatar1Border.setBackgroundColor(TRANSPARENT);
        avatar2Border.setBackgroundColor(TRANSPARENT);
        avatar3Border.setBackgroundColor(TRANSPARENT);
        avatar4Border.setBackgroundColor(TRANSPARENT);
        avatar5Border.setBackgroundColor(TRANSPARENT);
    }

    @Override
    public void onClick(View v) {
        clearSelection();
        switch (v.getId()) {
            case R.id.avatar0:
                if (avatarId == 0) {
                    avatarId = NO_AVATAR_CHOSEN;
                    avatar0Border.setBackgroundColor(TRANSPARENT);
                } else {
                    avatarId = 0;
                    avatar0Border.setBackgroundColor(Color.CYAN);
                }
                break;
            case R.id.avatar1:
                if (avatarId == 1) {
                    avatarId = NO_AVATAR_CHOSEN;
                    avatar1Border.setBackgroundColor(TRANSPARENT);
                } else {
                    avatarId = 1;
                    avatar1Border.setBackgroundColor(Color.CYAN);
                }
                break;
            case R.id.avatar2:
                if (avatarId == 2) {
                    avatarId = NO_AVATAR_CHOSEN;
                    avatar2Border.setBackgroundColor(TRANSPARENT);
                } else {
                    avatarId = 2;
                    avatar2Border.setBackgroundColor(Color.CYAN);
                }
                break;
            case R.id.avatar3:
                if (avatarId == 3) {
                    avatarId = NO_AVATAR_CHOSEN;
                    avatar3Border.setBackgroundColor(TRANSPARENT);
                } else {
                    avatarId = 3;
                    avatar3Border.setBackgroundColor(Color.CYAN);
                }
                break;
            case R.id.avatar4:
                if (avatarId == 4) {
                    avatarId = NO_AVATAR_CHOSEN;
                    avatar4Border.setBackgroundColor(TRANSPARENT);
                } else {
                    avatarId = 4;
                    avatar4Border.setBackgroundColor(Color.CYAN);
                }
                break;
            case R.id.avatar5:
                if (avatarId == 5) {
                    avatarId = NO_AVATAR_CHOSEN;
                    avatar5Border.setBackgroundColor(TRANSPARENT);
                } else {
                    avatarId = 5;
                    avatar5Border.setBackgroundColor(Color.CYAN);
                }
                break;
        }
    }

}