package com.example.saint.employeetracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "Sign_Up_Activity";

    // Request sing in code. Could be anything as you required.
    public static final int RequestSignInCode = 7;

    // Google API Client object.
    public GoogleApiClient googleApiClient;

    //google sign button
    private Button googleButton;


    private ProgressBar progressBar;
    private TextView pleaseWaitSignUp, sign_in;

    private EditText mEmail, mPassword, mUsername;
    private Button sign_Up;

    private String emailSignUp, passwordSignUp, usernameSignUp;

    //firebase setup
    private FirebaseAuth mAuthentication;
    private FirebaseAuth.AuthStateListener mAuthenticationListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        //initialization of fields
        progressBar = (ProgressBar) findViewById(R.id.progressBar_signup);
        mEmail = (EditText) findViewById(R.id.email_signup);
        mPassword = (EditText) findViewById(R.id.password_signup);
        mUsername = (EditText) findViewById(R.id.username_signup);
        sign_in = (TextView) findViewById(R.id.sign_in);
        sign_Up = (Button) findViewById(R.id.btnSignup);
        googleButton = (Button) findViewById(R.id.google_signup);

        pleaseWaitSignUp = (TextView) findViewById(R.id.please_wait_signup);


        mAuthentication = FirebaseAuth.getInstance();

        //hiding progress bar and please wait text field at start
        progressBar.setVisibility(View.GONE);
        pleaseWaitSignUp.setVisibility(View.GONE);


        // Creating and Configuring Google Sign In object.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Creating and Configuring Google Api Client.
        googleApiClient = new GoogleApiClient.Builder(SignUpActivity.this)
                .enableAutoManage(SignUpActivity.this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();


        // Adding Click listener to User Sign in Google button.
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserSignInMethod();

            }
        });



        sign_Up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerNewUser();

            }
        });




        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

    }


    //Google Sign In function Starts From Here.
    public void UserSignInMethod(){

        progressBar.setVisibility(View.VISIBLE);
        pleaseWaitSignUp.setVisibility(View.VISIBLE);

// Passing Google Api Client into Intent.
        Intent AuthIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);

        startActivityForResult(AuthIntent, RequestSignInCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestSignInCode){

            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (googleSignInResult.isSuccess()){

                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();

                FirebaseUserAuth(googleSignInAccount);
            }

        }
    }

    public void FirebaseUserAuth(GoogleSignInAccount googleSignInAccount) {

        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        Toast.makeText(SignUpActivity.this,""+ authCredential.getProvider(),Toast.LENGTH_LONG).show();

        mAuthentication.signInWithCredential(authCredential)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> AuthResultTask) {

                        if (AuthResultTask.isSuccessful()){

                            // Getting Current Login user details.
                            FirebaseUser firebaseUser = mAuthentication.getCurrentUser();

                            progressBar.setVisibility(View.GONE);
                            pleaseWaitSignUp.setVisibility(View.GONE);


                            Toast.makeText(SignUpActivity.this,"Google Sign in Successful",Toast.LENGTH_LONG).show();

                            startActivity(new Intent(SignUpActivity.this, HomeActivity.class));


                        }else {

                            progressBar.setVisibility(View.GONE);
                            pleaseWaitSignUp.setVisibility(View.GONE);

                            Toast.makeText(SignUpActivity.this,"Something Went Wrong",Toast.LENGTH_LONG).show();


                        }
                    }
                });
    }



    //register new users
    private void registerNewUser(){

        emailSignUp = mEmail.getText().toString().trim();
        usernameSignUp = mUsername.getText().toString().trim();
        passwordSignUp = mPassword.getText().toString().trim();

        //Show progress bar and please wait text field at start
        progressBar.setVisibility(View.VISIBLE);
        pleaseWaitSignUp.setVisibility(View.VISIBLE);


        if (emailSignUp.isEmpty()){

            mEmail.setError("Email Is Required");
            mEmail.requestFocus();
            return;

        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailSignUp).matches()){

            mEmail.setError("Please enter a valid Email");
            mEmail.requestFocus();
            return;

        }

        if (passwordSignUp.isEmpty()){

            mPassword.setError("Password Is Required");
            mPassword.requestFocus();
            return;

        }

        if (passwordSignUp.length() < 6){

            mPassword.setError("Minimum password characters is 6");
            mPassword.requestFocus();
            return;

        }

        mAuthentication.createUserWithEmailAndPassword(emailSignUp, passwordSignUp).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            //hiding progress bar and please wait text field at start
                            progressBar.setVisibility(View.GONE);
                            pleaseWaitSignUp.setVisibility(View.GONE);

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            Toast.makeText(SignUpActivity.this, "Registration Successful.",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(SignUpActivity.this, HomeActivity.class));


                        } else {

                            //hiding progress bar and please wait text field at start
                            progressBar.setVisibility(View.GONE);
                            pleaseWaitSignUp.setVisibility(View.GONE);

                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Registration failed, Existing Email Address.",
                                    Toast.LENGTH_SHORT).show();



                        }


                    }
                });



    }


}
