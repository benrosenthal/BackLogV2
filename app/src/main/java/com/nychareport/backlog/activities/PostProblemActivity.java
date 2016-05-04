package com.nychareport.backlog.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.nychareport.backlog.BackLogApplication;
import com.nychareport.backlog.Constants;
import com.nychareport.backlog.R;
import com.nychareport.backlog.Utils;
import com.nychareport.backlog.models.Problem;


public class PostProblemActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = PostProblemActivity.class.getSimpleName();

    private View postButton;
    private View loadFromGalleryButton;
    private View loadFromCameraButton;
    private EditText problemTitle;
    private EditText problemLocation;
    private EditText problemDescription;
    private ImageView attachedImage;

    /* References to the Firebase */
    private Firebase mFirebaseRef;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_problem);
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BackLogApplication.getCurrentInstance());
        String development = sharedPreferences.getString(Constants.KEY_HOUSING_DEVELOPMENT, null);
        initalizeViews();
        problemLocation.setText(development);
    }

    private void initalizeViews() {
        postButton = (View) findViewById(R.id.btn_done);
        loadFromCameraButton = (View) findViewById(R.id.btn_camera);
        loadFromGalleryButton = (View) findViewById(R.id.btn_gallery);
        problemTitle = (EditText) findViewById(R.id.et_problem_title);
        problemLocation = (EditText) findViewById(R.id.et_problem_location);
        problemDescription = (EditText) findViewById(R.id.et_problem_description);
        attachedImage = (ImageView) findViewById(R.id.iv_attached_image);

        loadFromCameraButton.setOnClickListener(this);
        loadFromGalleryButton.setOnClickListener(this);

        postButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, Constants.REQUEST_CODE_LOAD_FROM_CAMERA);
                break;
            case R.id.btn_gallery:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , Constants.REQUEST_CODE_LOAD_FROM_GALLERY);
                break;
            case R.id.btn_done:
                String userEmail = sharedPreferences.getString(Constants.KEY_ENCODED_EMAIL, null);
                Firebase postsRef = mFirebaseRef.child(Constants.FIREBASE_LOCATION_POSTS);
                Problem problem = new Problem(
                        problemTitle.getText().toString(),
                        problemDescription.getText().toString(),
                        problemLocation.getText().toString(),
                        "String decoded bitmap of image",
                        userEmail);
                Log.d(LOG_TAG, "Problem: " + problem.toString());
                postsRef.push().setValue(problem, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        Log.d(LOG_TAG, "Uploaded!");
                    }
                });
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    attachedImage.setImageURI(selectedImage);
                }
                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    attachedImage.setImageURI(selectedImage);
                }
                break;
        }
    }
}
