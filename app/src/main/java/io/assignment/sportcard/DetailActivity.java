package io.assignment.sportcard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    EditText etName, etContact, etAge, etHeight, etWeight, etSportsName;
    Button btSave;
    ImageView profileImage;
    DatabaseReference databaseReference;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        etName = findViewById(R.id.et_name);
        etContact = findViewById(R.id.et_number);
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        etSportsName = findViewById(R.id.et_sports_name);
        btSave = findViewById(R.id.save_profile);
        profileImage = findViewById(R.id.profile_pic_user);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        databaseReference.keepSynced(true);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserDetails();
            }
        });


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("Name").getValue(String.class);
                String age = dataSnapshot.child("Age").getValue(String.class);
                String height = dataSnapshot.child("Height").getValue(String.class);
                String weight = dataSnapshot.child("Weight").getValue(String.class);
                String sportName = dataSnapshot.child("SportsName").getValue(String.class);
                String image = dataSnapshot.child("profileUrl").getValue(String.class);

                if (name != null) {
                    etName.setText(name);
                } else {
                    Log.d("name", "null");
                }
                if (age != null) {
                    etAge.setText(age);
                } else {
                    Log.d("age", "null");
                }
                if (height != null) {
                    etHeight.setText(height);
                } else {
                    Log.d("height", "null");
                }
                if (weight != null) {
                    etWeight.setText(weight);
                } else {
                    Log.d("weight", "null");
                }
                if (sportName != null) {
                    etSportsName.setText(sportName);
                } else {
                    Log.d("sportName", "null");
                }

                Picasso.get().load(image).into(profileImage);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserDetails() {
        final String name = etName.getText().toString().trim();
        final String age = etAge.getText().toString().trim();
        final String height = etHeight.getText().toString().trim();
        final String weight = etWeight.getText().toString().trim();
        final String sportsName = etSportsName.getText().toString().trim();

        if (!name.isEmpty() && !age.isEmpty() && !height.isEmpty() && !weight.isEmpty() && !sportsName.isEmpty()) {

            AppExecuter.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    databaseReference.child("Name").setValue(name);
                    databaseReference.child("Age").setValue(age);
                    databaseReference.child("Weight").setValue(weight);
                    databaseReference.child("Height").setValue(height);
                    databaseReference.child("SportsName").setValue(sportsName);
                }
            });
            Toast.makeText(DetailActivity.this, "Data saved", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(DetailActivity.this, "Please fill all the details", Toast.LENGTH_SHORT).show();
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setMessage("Please wait");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CardDialog cardDialog = new CardDialog();
                cardDialog.showDialog(DetailActivity.this, "", getApplicationContext());
                progressDialog.dismiss();

            }
        }, 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.logout:

                if (AccessToken.getCurrentAccessToken() != null) {
                    mAuth.signOut();
                    LoginManager.getInstance().logOut();
                    Intent intent1 = new Intent(DetailActivity.this, MainActivity.class);
                    startActivity(intent1);
                } else {
                    mAuth.signOut();
                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent1 = new Intent(DetailActivity.this, MainActivity.class);
                            startActivity(intent1);
                        }
                    });

                }


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
