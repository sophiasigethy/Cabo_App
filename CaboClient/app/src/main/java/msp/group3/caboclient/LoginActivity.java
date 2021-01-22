package msp.group3.caboclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private GoogleSignInAccount account;
    private static final int RC_SIGN_IN = 666;
    private SharedPreferences sharedPref;
    private TextView loginErr;
    private Handler workerHandler;
    private HandlerThread handlerThread;
    private int counter = 0;
    private String nick;
    private Button noLogIn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getApplicationContext().getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);
        DatabaseOperation.getDao().updateAllPlayers(sharedPref);
        // Check if LoginData is already available
        if (DatabaseOperation.getDao().getCurrentUser() != null) {
            String mAuthUser = DatabaseOperation.getDao().getCurrentUser().getUid();
            String myDbId = sharedPref.getString(String.valueOf(R.string.preference_userdbid), "None");
            if (!myDbId.equals("None")) {
                if (myDbId.equals(mAuthUser)) {
                    moveToMainActivity();
                }
            }
        }
        setContentView(R.layout.activity_login);
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.move_to_register);
        loginErr = findViewById(R.id.login_error_txt);
        /*
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final SignInButton googleLoginButton = findViewById(R.id.google_login);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null || DatabaseOperation.getDao().getCurrentUser() != null) {
            //moveToMainActivity();
        }
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
        */

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usernameEditText.getText().toString() == null
                        || passwordEditText.getText().toString() == null)
                    signIn("", "");
                signIn(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        /*noLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToWaitingRoomActivity();
            }
        });*/


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(
                        LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });
    }

    private void signIn(String email, String password) {
        // Check with firebase db is user is registered
        if (!(email.isEmpty() && password.isEmpty())) {
            DatabaseOperation.getDao().getmAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                String myDbId = DatabaseOperation.getDao().getCurrentUser().getUid();

                                // Create a Thread that waits until
                                // Data from DB was written to sharedPrefs
                                handlerThread = new HandlerThread("UpdateSharedPref");
                                handlerThread.start();
                                workerHandler = new Handler(handlerThread.getLooper());
                                workerHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        DatabaseOperation.getDao().setupDatabaseListener(myDbId, sharedPref);
                                        DatabaseOperation.getDao().updateLastLoggedIn(myDbId);
                                    }
                                });
                                moveToMainActivity();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this,
                                        "User Authentication Failed: "
                                                + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                                loginErr.setText(R.string.invalid_credentials);
                                loginErr.setTextColor(Color.RED);
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Enter Mail and Password!", Toast.LENGTH_SHORT);
        }
    }

    private void signInWithGoogle() {
        // handle logging in via google
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // Signed in successfully, show authenticated UI.
            account = completedTask.getResult(ApiException.class);
            moveToMainActivity();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Login Failed - " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void moveToMainActivity() {
        // Read player from db and move to MainActivity
        Player me = DatabaseOperation.getDao().readPlayerFromSharedPrefs(sharedPref);
        if (!me.getDbID().equals("None")) {
            if (handlerThread != null)
                handlerThread.quit();
            Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
            myIntent.putExtra("dbid", me.getDbID());
            myIntent.putExtra("nick", me.getNick());
            myIntent.putExtra("avatarid", me.getAvatarID());
            LoginActivity.this.startActivity(myIntent);
        } else {
            //TODO: Replace with real Animation
            String animation = counter++ % 2 == 0 ? "." : "";
            loginErr.setText(getResources().getString(R.string.loading) + animation);
            loginErr.setTextColor(Color.BLACK);
            (new Handler()).postDelayed(this::moveToMainActivity, 350);
        }
    }

    private void moveToWaitingRoomActivity(){
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(myIntent);

    }
}
