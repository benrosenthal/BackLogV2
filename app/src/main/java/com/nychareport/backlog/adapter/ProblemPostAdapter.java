package com.nychareport.backlog.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.nychareport.backlog.BackLogApplication;
import com.nychareport.backlog.Constants;
import com.nychareport.backlog.R;
import com.nychareport.backlog.misc.AWSUtils;
import com.nychareport.backlog.misc.Utils;
import com.nychareport.backlog.models.Problem;
import com.nychareport.backlog.viewholder.PostProblemViewHolder;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.List;

/**
 * Created by Ben Rosenthal on 5/06/16.
 */
public class ProblemPostAdapter extends RecyclerView.Adapter<PostProblemViewHolder> {

    private List<Problem> mDataSet;
    /**
     * Called when a view has been clicked.
     *
     * @param problemSet Dataset of posts made by users
     */
    public ProblemPostAdapter(List<Problem> problemSet) {
        mDataSet = problemSet;
    }

    @Override
    public PostProblemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PostProblemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.problem_post, parent, false));
    }

    @Override
    public void onBindViewHolder(final PostProblemViewHolder holder, int position) {
        Problem problem = mDataSet.get(position);
        holder.problemTitle.setText(problem.getProblem());
        holder.problemDescription.setText(problem.getProblemDescription());
        holder.problemLocation.setText(problem.getProblemLocation());
        if (problem.getTimestampCreatedLong() != 0) {
            CharSequence timeCreate = Utils.prepareCreatorTimestamp(String.valueOf(problem.getTimestampCreatedLong()), BackLogApplication.getCurrentInstance());
            holder.timeCreated.setText(timeCreate);
        } else {
            holder.timeCreated.setVisibility(View.GONE);
        }
        holder.problemDescription.setText(problem.getProblemDescription());
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
                        holder.attachedProblemImage.setImageBitmap(bitmap);
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
            holder.attachedProblemImage.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
