package com.nychareport.backlog.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nychareport.backlog.R;
import com.nychareport.backlog.models.Problem;

/**
 * Created by Ben Rosenthal on 06/05/16.
 */
public class PostProblemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView attachedProblemImage;
    public TextView problemTitle;
    public TextView problemDescription;
    public TextView problemLocation;
    public TextView timeCreated;
    public Problem problemItem;

    protected OnClickListener onClickListener;

    public PostProblemViewHolder(View itemView, OnClickListener listener) {
        super(itemView);
        initializeViews();
        this.onClickListener = listener;
        itemView.setOnClickListener(this);
    }

    private void initializeViews() {
        attachedProblemImage = (ImageView) itemView.findViewById(R.id.iv_attached_image);
        problemTitle = (TextView) itemView.findViewById(R.id.problem_title);
        problemDescription = (TextView) itemView.findViewById(R.id.problem_description);
        problemLocation = (TextView) itemView.findViewById(R.id.problem_location);
        timeCreated = (TextView) itemView.findViewById(R.id.problem_timestamp);
    }

    public interface OnClickListener {
        void onCardClick(View view, Problem problemItem);
    }

    @Override
    public void onClick(View v) {
        if(onClickListener != null){
            onClickListener.onCardClick(v, problemItem);
        }
    }
}
