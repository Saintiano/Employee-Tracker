package com.example.saint.employeetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private ProgressBar progressBar;
    private EditText mEmail, mPassword;
    private Button loginButton, signUp;
    private TextView pleaseWait, passwordRecovery;

    // Request sing in code. Could be anything as you required.
    public static final int RequestSignInCode = 7;

    // Google API Client object.
    public GoogleApiClient googleApiClient;

    //google sign button
    private Button googleButton;

    //firebase setup
    private FirebaseAuth mAuthentication;
    private FirebaseAuth.AuthStateListener mAuthenticationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //initialization of fields
        progressBar = (ProgressBar) findViewById(R.id.progressBar_login);
        mEmail = (EditText) findViewById(R.id.login_email);
        mPassword = (EditText) findViewById(R.id.login_password);
        signUp = (Button) findViewById(R.id.btnSignup);
        googleButton = (Button) findViewById(R.id.google_signin);

        passwordRecovery = (TextView) findViewById(R.id.passRecover);
        pleaseWait = (TextView) findViewById(R.id.please_wait);
        mContext = MainActivity.this;



        //hiding progress bar and please wait text field at start
        progressBar.setVisibility(View.GONE);
        pleaseWait.setVisibility(View.GONE);


        //setting up firebase
        setUpFirebaseAuth();

        //calling initialize login method
        initializeLogin();


        // Creating and Configuring Google Sign In object.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Creating and Configuring Google Api Client.
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .enableAutoManage(MainActivity.this , new GoogleApiClient.OnConnectionFailedListener() {
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

        //setting up signup onclick listener
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);

            }
        });


        //setting up password recovery onclick listener
        passwordRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, PassWord_Recovery_Activity.class);
                startActivity(intent);

            }
        });

    }


    //Google Sign In function Starts From Here.
    public void UserSignInMethod(){

        progressBar.setVisibility(View.VISIBLE);
        pleaseWait.setVisibility(View.VISIBLE);

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

        Toast.makeText(MainActivity.this,""+ authCredential.getProvider(),Toast.LENGTH_LONG).show();

        mAuthentication.signInWithCredential(authCredential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> AuthResultTask) {

                        if (AuthResultTask.isSuccessful()){

                            // Getting Current Login user details.
                            FirebaseUser firebaseUser = mAuthentication.getCurrentUser();

                            progressBar.setVisibility(View.GONE);
                            pleaseWait.setVisibility(View.GONE);


                            Toast.makeText(MainActivity.this,"Google Sign in Successful",Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(MainActivity.this, HomeActivity.class));


                        }else {

                            progressBar.setVisibility(View.GONE);
                            pleaseWait.setVisibility(View.GONE);

                            Toast.makeText(MainActivity.this,"Something Went Wrong",Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }





    //Setting up firebase methods and fields.......................................................

    //a method checking if email and password is null, empty
    private Boolean isStringNull(String string){

        if (string.equals("")){

            return true;

        }else{

            return false;

        }

    }

    //A method to handle the login
    private  void initializeLogin(){

        //initialize button for login
        loginButton = (Button) findViewById(R.id.btnSignin);

        //setting on click listener to the button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getting email and passwords and storing in a variable
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                //checking if null with method
                if (isStringNull(email) && isStringNull(password)){

                    Toast.makeText(mContext, "Please fill up the fields", Toast.LENGTH_SHORT).show();

                }else{

                    //try logging in since all fields are filled
                    //show progress bar and please wait fields first
                    progressBar.setVisibility(View.VISIBLE);
                    pleaseWait.setVisibility(View.VISIBLE);

                    mAuthentication.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (!task.isSuccessful()){

                                        Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                        //remove progress bar and please wait fields first
                                        progressBar.setVisibility(View.GONE);
                                        pleaseWait.setVisibility(View.GONE);

                                    }else{

                                        Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                        //remove progress bar and please wait fields first
                                        progressBar.setVisibility(View.GONE);
                                        pleaseWait.setVisibility(View.GONE);

                                        //taking user to the next home activity if successfully logged in
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }

                                }
                            });

                }

            }
        });


    }

    private void setUpFirebaseAuth(){

        //app wide, can be accessed anywhere in fragments and activities
        mAuthentication = FirebaseAuth.getInstance();

        mAuthenticationListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                //calling method to check if user is logged in

                if (user != null){

                    //user is signed in


                }else {

                    //user is signed out


                }


            }
        };

    }

    //create firebase on start and stop methods
    @Override
    public void onStart() {
        super.onStart();

        //add user (non Null)
        mAuthentication.addAuthStateListener(mAuthenticationListener);


    }

    @Override
    public void onStop() {
        super.onStop();

        //remove user
        if (mAuthenticationListener != null){

            mAuthentication.removeAuthStateListener(mAuthenticationListener);

        }

    }
}
