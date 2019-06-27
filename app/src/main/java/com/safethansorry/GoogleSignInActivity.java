package com.safethansorry;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.quickstart.auth.R;

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
public class GoogleSignInActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    DatabaseHelper mDatabaseHelper;
    FirebaseDatabase database;
    DatabaseReference databaseRef;

    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private TextView mDetailTextView;

    private String source, srcLatLng, destination, destLatLng, waypoints;
    private boolean val2,val3;
    private float rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);

        // Button listeners
        SignInButton signin_btn = (SignInButton) findViewById(R.id.signInButton);
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        Button signout_btn = (Button) findViewById(R.id.signOutButton);
        signout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        Button feedback_btn = (Button) findViewById(R.id.feedbackButton);
        feedback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveFeedback();
            }
        });

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void giveFeedback()
    {
        setContentView(R.layout.feedback_form);
        mDatabaseHelper = new DatabaseHelper(this);
        Cursor cursor=mDatabaseHelper.getLatestItem();

        String time;
        if (cursor.moveToFirst()){
                //time = cursor.getString(cursor.getColumnIndex("time"));
                source = cursor.getString(cursor.getColumnIndex("origin"));
                srcLatLng = cursor.getString(cursor.getColumnIndex("originLatLng"));
                destination = cursor.getString(cursor.getColumnIndex("destination"));
                destLatLng = cursor.getString(cursor.getColumnIndex("destinationLatLng"));
                waypoints = cursor.getString(cursor.getColumnIndex("waypoints"));
                Toast.makeText(GoogleSignInActivity.this,
                        "Rate your trip from "+source+" to "+destination,
                        Toast.LENGTH_LONG).show();
                time = "";

        }
        cursor.close();

        final RatingBar ratingbar=(RatingBar)findViewById(R.id.ratingBar);
        Button submitbtn=(Button)findViewById(R.id.submit_btn);
        //Performing action on Button Click
        submitbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                //Question 1
                String rtng=String.valueOf(ratingbar.getRating());
                rating=Float.parseFloat(rtng);

                //Question 2
                RadioGroup radioGroup2 = (RadioGroup)findViewById(R.id.groupradio2);
                radioGroup2.clearCheck();
                radioGroup2.setOnCheckedChangeListener(
                        new RadioGroup
                                .OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group,
                                                         int checkedId)
                            {
                                RadioButton
                                        radioButton
                                        = (RadioButton)group
                                        .findViewById(checkedId);
                            }
                        });
                int selectedId = radioGroup2.getCheckedRadioButtonId();
                if (selectedId == -1) { }
                else {
                    RadioButton radioButton
                            = (RadioButton)radioGroup2
                            .findViewById(selectedId);
                    if (Boolean.parseBoolean(String.valueOf(radioButton.getText()))==true){
                        val2=true;
                    }else{
                        val2=false;
                    }
                }

                //Question 3
                RadioGroup radioGroup3 = (RadioGroup)findViewById(R.id.groupradio3);
                radioGroup3.clearCheck();
                radioGroup3.setOnCheckedChangeListener(
                        new RadioGroup
                                .OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group,
                                                         int checkedId)
                            {
                                RadioButton
                                        radioButton1
                                        = (RadioButton)group
                                        .findViewById(checkedId);
                            }
                        });
                selectedId = radioGroup3.getCheckedRadioButtonId();
                if (selectedId == -1) { }
                else {
                    RadioButton radioButton1
                            = (RadioButton)radioGroup3
                            .findViewById(selectedId);
                    if (Boolean.parseBoolean(String.valueOf(radioButton1.getText()))==true){
                        val3=true;
                    }else{
                        val3=false;
                    }
                }

                database = FirebaseDatabase.getInstance();
                final long timemillis = System.currentTimeMillis();
                databaseRef = database.getReference();
                Feedback feedback = new Feedback(timemillis, source, srcLatLng, destination, destLatLng, waypoints, rating,
                        val2,val3,true, false);
                databaseRef.push().setValue(feedback);

                Toast.makeText(GoogleSignInActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(GoogleSignInActivity.this, RoutingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.signInButton).setVisibility(View.GONE);
            findViewById(R.id.signOutAndDisconnect).setVisibility(View.VISIBLE);
            findViewById(R.id.feedbackButton).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.signInButton).setVisibility(View.VISIBLE);
            findViewById(R.id.signOutAndDisconnect).setVisibility(View.GONE);
            findViewById(R.id.feedbackButton).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signInButton) {
            signIn();
        } else if (i == R.id.signOutButton) {
            signOut();
        }
    }
}