package com.example.saint.employeetracker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private Context mContext = HomeActivity.this;

    private Button editProfile;

    //firebase setup
    private FirebaseAuth mAuthentication;
    private FirebaseAuth.AuthStateListener mAuthenticationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        editProfile = (Button) findViewById(R.id.profile);

        setUpFirebaseAuth();


        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(HomeActivity.this, Edit_Profile_Activity.class));

            }
        });


    }


    //Setting up firebase methods and fields.......................................................

    private void checkCurrentUser(FirebaseUser user){

        if (user == null){

            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);

        }

    }

    private void setUpFirebaseAuth(){

        //app wide, can be accessed anywhere in fragments and activities
        mAuthentication = FirebaseAuth.getInstance();

        mAuthenticationListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                //calling method to check if user is logged in
                checkCurrentUser(user);

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

        checkCurrentUser(mAuthentication.getCurrentUser());


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
