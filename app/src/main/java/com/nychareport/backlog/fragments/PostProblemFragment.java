package com.nychareport.backlog.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.nychareport.backlog.BackLogApplication;
import com.nychareport.backlog.Constants;
import com.nychareport.backlog.R;
import com.nychareport.backlog.misc.AWSUtils;
import com.nychareport.backlog.models.Problem;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Created by gaurav.kaushik on 14/06/16.
 */
public class PostProblemFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = PostProblemFragment.class.getSimpleName();

    private View postButton;
    private View loadFromGalleryButton;
    private View loadFromCameraButton;
    private EditText problemTitle;
    private EditText problemLocation;
    private EditText problemDescription;
    private ImageView attachedImage;
    private String attachedImagePath;

    /* References to the Firebase */
    private Firebase mFirebaseRef;
    private SharedPreferences sharedPreferences;

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;
    public PostProblemFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

        // Initializes TransferUtility, always do this before using it.
        transferUtility = AWSUtils.getTransferUtility(getActivity());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BackLogApplication.getCurrentInstance());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_problem, container, false);
        initalizeViews(rootView);

        return rootView;
    }
    private void initalizeViews(View rootView) {
        postButton = (View) rootView.findViewById(R.id.btn_done);
        loadFromCameraButton = (View) rootView.findViewById(R.id.btn_camera);
        loadFromGalleryButton = (View) rootView.findViewById(R.id.btn_gallery);
        problemTitle = (EditText) rootView.findViewById(R.id.et_problem_title);
        problemLocation = (EditText) rootView.findViewById(R.id.et_problem_location);
        problemDescription = (EditText) rootView.findViewById(R.id.et_problem_description);
        attachedImage = (ImageView) rootView.findViewById(R.id.iv_attached_image);

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
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= 19) {
                    // For Android versions of KitKat or later, we use a
                    // different intent to ensure
                    // we can get the file path from the returned intent URI
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                } else {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                }

                intent.setType("image/*");
                startActivityForResult(intent, Constants.REQUEST_CODE_LOAD_FROM_GALLERY);
                break;
            case R.id.btn_done:
                String userEmail = sharedPreferences.getString(Constants.KEY_ENCODED_EMAIL, null);
                Firebase postsRef = mFirebaseRef.child(Constants.FIREBASE_LOCATION_POSTS);
                Firebase uploadRef = postsRef.push();
                /**
                 * Set raw version of date to the ServerValue.TIMESTAMP value and save into
                 * timestampCreatedMap
                 */
                HashMap<String, Object> timestampCreated = new HashMap<>();
                String fileName = uploadRef.getKey();
                File file = null;
                if (attachedImagePath != null) {
                    file = new File(attachedImagePath);
                    fileName += "-" + file.getName();
                }
                timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
                Problem problem = new Problem(
                        problemTitle.getText().toString(),
                        problemDescription.getText().toString(),
                        problemLocation.getText().toString(),
                        fileName,
                        userEmail,
                        timestampCreated);
                if (file != null) {
                    TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, fileName, file);
                    observer.setTransferListener(new UploadListener());
                }
                Log.d(LOG_TAG, "Problem: " + problem.toString());
                uploadRef.setValue(problem, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "You just posted an issue", Snackbar.LENGTH_LONG)
                                .setActionTextColor(BackLogApplication.getCurrentInstance().getResources().getColor(R.color.fluorescent_green))
                                .show();


                    }
                });

                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case Constants.REQUEST_CODE_LOAD_FROM_CAMERA:
                if(resultCode == Activity.RESULT_OK){
                    Bitmap bp = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    attachedImage.setImageBitmap(bp);
                }
                break;
            case Constants.REQUEST_CODE_LOAD_FROM_GALLERY:
                if(resultCode == Activity.RESULT_OK){
                    Uri uri = imageReturnedIntent.getData();
                    attachedImage.setImageURI(uri);
                    try {
                        attachedImagePath = getPath(uri);
                    } catch (URISyntaxException e) {
                        Toast.makeText(getActivity(),
                                "Unable to get the file from the given URI.  See error log for details",
                                Toast.LENGTH_LONG).show();
                        Log.e(LOG_TAG, "Unable to upload file from the given uri", e);
                    }
                }
                break;
        }
    }

    /*
         * Begins to upload the file specified by the file path.
         */
    private void beginUpload(String filePath) {
        if (filePath == null) {
            Toast.makeText(getActivity(), "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, file.getName(),
                file);
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        observer.setTransferListener(new UploadListener());
    }

    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getActivity().getApplicationContext(), uri)) {
            if (AWSUtils.isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (AWSUtils.isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (AWSUtils.isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] {
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */
    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(LOG_TAG, "Error during upload: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(LOG_TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(LOG_TAG, "onStateChanged: " + id + ", " + newState);
        }
    }

}
