package msp.group3.caboclient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
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
    private Player player;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final SignInButton googleLoginButton = findViewById(R.id.google_login);

        /*
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
                    signInOrRegister("", "");
                signInOrRegister(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    @Override
    protected void onStart() {
        // Check if user is logged in
        super.onStart();
        if (DatabaseOperation.getDao().getCurrentUser() != null) {
            player = DatabaseOperation.getDao().getPlayerFromDB(
                    "", getApplicationContext());
            moveToMainActivity();
        }
    }

    private void signInOrRegister(String email, String password) {
        // Check with firebase db is user is registered
        if (!(email.isEmpty() && password.isEmpty())) {
            DatabaseOperation.getDao().getmAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                player = DatabaseOperation.getDao().getPlayerFromDB("", getApplicationContext());
                                moveToMainActivity();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "No such user found",
                                        Toast.LENGTH_SHORT).show();
                                Intent registerIntent = new Intent(
                                        LoginActivity.this, RegisterActivity.class);
                                LoginActivity.this.startActivity(registerIntent);
                            }
                        }
                    });
        } else {
            Intent registerIntent = new Intent(
                    LoginActivity.this, RegisterActivity.class);
            LoginActivity.this.startActivity(registerIntent);
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
            player = new Player(account);
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
        if (player.getName().isEmpty()) {
            player = DatabaseOperation.getDao().getPlayerFromDB(player.getDbID(), getApplicationContext());
        }
        if (player == null) {
            Toast.makeText(LoginActivity.this, "Error reading DB", Toast.LENGTH_LONG);
            return;
        }
        DatabaseOperation.getDao().updateLastLoggedIn(player.getDbID());
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
        myIntent.putExtra("dbid", player.getDbID());
        LoginActivity.this.startActivity(myIntent);
    }
}