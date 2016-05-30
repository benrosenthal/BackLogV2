package com.nychareport.backlog.fragments;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.nychareport.backlog.BackLogApplication;
import com.nychareport.backlog.Constants;
import com.nychareport.backlog.R;
import com.nychareport.backlog.misc.AWSUtils;
import com.nychareport.backlog.misc.Utils;
import com.nychareport.backlog.models.Problem;

import java.io.File;

/**
 * Created by 11148 on 30/05/16.
 */
public class DetailedProblemFragment extends DialogFragment {

    private ImageView attachedProblemImage;
    private TextView problemTitle;
    private TextView problemDescription;
    private TextView problemLocation;
    private TextView timeCreated;

    private static final String PROBLEM_KEY = "problem-key";
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static DetailedProblemFragment newInstance(Problem problem) {
        DetailedProblemFragment f = new DetailedProblemFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable(PROBLEM_KEY, problem);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detailed_problem, container, false);
        getDialog().setTitle("Problem Details");
        Problem problem = getArguments().getParcelable(PROBLEM_KEY);
        initializeViews(v, problem);

        // Watch for button clicks.
        Button button = (Button)v.findViewById(R.id.back);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                dismiss();
            }
        });

        return v;
    }

    private void initializeViews(View v, Problem problem) {
        problemTitle = (TextView) v.findViewById(R.id.problem_title);
        problemDescription = (TextView) v.findViewById(R.id.problem_description);
        problemLocation = (TextView) v.findViewById(R.id.problem_location);
        timeCreated = (TextView) v.findViewById(R.id.problem_timestamp);
        attachedProblemImage = (ImageView) v.findViewById(R.id.iv_attached_image);
        problemTitle.setText(problem.getProblem());
        problemDescription.setText(problem.getProblemDescription());
        problemLocation.setText(problem.getProblemLocation());
        if (problem.getTimestampCreatedLong() != 0) {
            CharSequence timeCreate = Utils.prepareCreatorTimestamp(String.valueOf(problem.getTimestampCreatedLong()), BackLogApplication.getCurrentInstance());
            timeCreated.setText(timeCreate);
        } else {
            timeCreated.setVisibility(View.GONE);
        }
        final File file = new File(
                Environment.getExternalStorageDirectory().toString() + "/" + problem.getProblemPic());
        if (!file.exists()) {
            // Initiate the download
            TransferObserver observer = AWSUtils.getTransferUtility(BackLogApplication.getCurrentInstance())
                    .download(Constants.BUCKET_NAME, problem.getProblemPic(), file);
            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (state == TransferState.COMPLETED) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        attachedProblemImage.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                }

                @Override
                public void onError(int id, Exception ex) {

                }
            });
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            attachedProblemImage.setImageBitmap(bitmap);
        }
    }

}