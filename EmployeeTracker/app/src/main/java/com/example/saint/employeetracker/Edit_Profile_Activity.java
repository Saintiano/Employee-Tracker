package com.example.saint.employeetracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class Edit_Profile_Activity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 1;

    private EditText mUsername;
    private EditText mDisplayName;
    private EditText mWork;
    private EditText mDescription;
    private EditText mEmail;
    private EditText mPhoneNumber;

    private ProgressBar progressBar;
    private TextView pleaseWait;

    private Button saveUser;

    private ImageView uploadPhoto;
    Uri uriForProfilePhoto;
    private String profileImageUrl;

    FirebaseAuth mAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__profile_);

        mAuthentication = FirebaseAuth.getInstance();

        mUsername = (EditText) findViewById(R.id.edit_username);
        mDisplayName = (EditText) findViewById(R.id.edit_display_name);
        mWork = (EditText) findViewById(R.id.edit_work);
        mDescription = (EditText)findViewById(R.id.edit_description);
        mEmail = (EditText) findViewById(R.id.edit_email);
        mPhoneNumber = (EditText) findViewById(R.id.edit_PhoneNumber);

        pleaseWait = (TextView) findViewById(R.id.please_wait_profile);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_profile);

        saveUser =(Button) findViewById(R.id.update_profile_button);
        uploadPhoto = (ImageView) findViewById(R.id.profile_photo);



        //hiding progress bar and please wait text field at start
        progressBar.setVisibility(View.GONE);
        pleaseWait.setVisibility(View.GONE);



        saveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveUserInformtion();

            }
        });


        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //profile photo upload
                showImageChooser();
            }
        });

    }

    //method to save user information firebase cloud storage
    private void saveUserInformtion() {

        String username = mUsername.getText().toString();
        String displayName = mDisplayName.getText().toString();
        String work = mWork.getText().toString();
        String description = mDescription.getText().toString();
        String email = mEmail.getText().toString();
        String phoneNumber = mPhoneNumber.getText().toString();

        //checking if fields are empty
        if (username.isEmpty()){

            mUsername.setError("Username Required");
            mUsername.requestFocus();
            return;

        }

        //checking if fields are empty
        if (displayName.isEmpty()){

            mDisplayName.setError("Full Name Required");
            mDisplayName.requestFocus();
            return;

        }

        //checking if fields are empty
        if (work.isEmpty()){

            mWork.setError("Work Required");
            mWork.requestFocus();
            return;

        }

        //checking if fields are empty
        if (description.isEmpty()){

            mDescription.setError("Username Required");
            mDescription.requestFocus();
            return;

        }

        //checking if fields are empty
        if (email.isEmpty()){

            mEmail.setError("Username Required");
            mEmail.requestFocus();
            return;

        }

        //checking if fields are empty
        if (phoneNumber.isEmpty()){

            mPhoneNumber.setError("Username Required");
            mPhoneNumber.requestFocus();
            return;

        }

        FirebaseUser user = mAuthentication.getCurrentUser();

        if (user !=null && profileImageUrl !=null){

            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setDisplayName(description)
                    .setDisplayName(username)
                    .setDisplayName(work)
                    .setDisplayName(email)
                    .setDisplayName(phoneNumber)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                        Toast.makeText(Edit_Profile_Activity.this, "update successful", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(Edit_Profile_Activity.this, Profile_Activity.class));

                    }

                }
            });

        }


    }


    //method to get selected image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null  && data.getData() != null){

            //storing the image inside the uri object
            uriForProfilePhoto = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriForProfilePhoto);

                //using bitmap to set profile photo
                uploadPhoto.setImageBitmap(bitmap);

                //uploading to google firebase storage
                uploadImageToFirebaseStorage();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    //method to upload image to firebase storage
    private void uploadImageToFirebaseStorage() {

        //Showprogress bar and please wait text field at start
        progressBar.setVisibility(View.VISIBLE);
        pleaseWait.setVisibility(View.VISIBLE);

        StorageReference profileImageReference =
                FirebaseStorage.getInstance().getReference("profile_photo/" + System.currentTimeMillis() + ".jpg ");

        if (uriForProfilePhoto != null){

            profileImageReference.putFile(uriForProfilePhoto).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //if it is successful
                    //hiding progress bar and please wait text field at start
                    progressBar.setVisibility(View.GONE);
                    pleaseWait.setVisibility(View.GONE);

                    //storing the download url in a variable used to store the user information
                    profileImageUrl = taskSnapshot.getDownloadUrl().toString();

                    Toast.makeText(Edit_Profile_Activity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT);


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //if it fails
                    //hiding progress bar and please wait text field at start
                    progressBar.setVisibility(View.GONE);
                    pleaseWait.setVisibility(View.GONE);

                    Toast.makeText(Edit_Profile_Activity.this, "Image Upload failed", Toast.LENGTH_SHORT);

                }
            });

        }

    }

    //a method for our image chooser
    private void showImageChooser(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);

    }

    private void loadUserInformation(){



    }


    @Override
    protected void onStart() {
        super.onStart();



    }
}
