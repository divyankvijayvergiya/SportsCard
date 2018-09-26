package io.assignment.sportcard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private LoginButton loginButton;
    public CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    GoogleApiClient mGoogleApiClient;
    private final static String TAG = "Login Activity";
    private static final int RC_SIGN_IN = 1000;
    private GoogleSignInButton googleSignInButton;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        loginButton = findViewById(R.id.login_fb);
        googleSignInButton = findViewById(R.id.sign_in_button);
        mCallbackManager = CallbackManager.Factory.create();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        loginButton.setText("Continue with Facebook");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestProfile()
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        facebookLogin();
    }

    private void facebookLogin() {
        loginButton.setReadPermissions(Arrays.asList("public_profile, email"));
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.d("MainAcitvity", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                GraphRequest graphRequest = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
//                                progressDialog.show();
//                                progressDialog.setMessage("Please wait...");

                                Log.d("Main", response.toString());
                                try {
                                    final String name = object.getString("name");
                                    Log.d("---->name", name);
                                    final String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    Log.d("---->profile", profilePicUrl);
                                    String id = object.getString("id");

                                    new Handler().postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            // This method will be executed once the timer is over
                                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                                databaseReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").setValue(name);
                                                databaseReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profileUrl").setValue(profilePicUrl);
                                                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                            }
                                            progressDialog.dismiss();
                                        }
                                    }, 5000);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,picture.type(large)");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    public void handleFacebookAccessToken(AccessToken token) {


        Log.d("MainActivity", "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Login Activity", "signInWithCredential:success");
                            loginButton.setText("Continue with Facebook");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {

                                loginButton.setText("Continue with Facebook");

                            } else {
                                Log.d("uidFacebook", "Not generated");
                            }


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Login Activity", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "You are already registered with same email via google",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        progressDialog.show();
        progressDialog.setMessage("Please wait...");

        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                                if (account != null) {
                                    final String personName = account.getDisplayName();
                                    databaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).child("Name").setValue(personName);
                                    Log.d("gname", personName);
                                    Uri personPhoto = account.getPhotoUrl();
                                    if (personPhoto != null) {
                                        databaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).child("profileUrl").setValue(personPhoto.toString());
                                        Log.d("googlephoto", personPhoto.toString());
                                    } else {
                                        Log.d("photo", "null");
                                    }

                                } else {
                                    Log.d("acct", "null");
                                }

                                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                progressDialog.dismiss();
                            } else {
                                Log.d("uidGoogle", "Not generated");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


}
