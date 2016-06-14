package com.nychareport.backlog.fragments;

import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nychareport.backlog.Constants;
import com.nychareport.backlog.R;
import com.nychareport.backlog.adapter.ProblemPostAdapter;
import com.nychareport.backlog.models.Problem;
import com.nychareport.backlog.viewholder.PostProblemViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaurav.kaushik on 14/06/16.
 */
public class FeedFragment extends Fragment implements PostProblemViewHolder.OnClickListener {

    private static final String TAG = FeedFragment.class.getSimpleName();

    private RecyclerView postsRecyclerView;
    private ProblemPostAdapter postsAdapter;
    private List<Problem> postsList;

    private Firebase postsRef;
    private FirebaseAnalytics mFirebaseAnalytics;

    public FeedFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postsList = new ArrayList<>();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        postsRef = new Firebase(Constants.FIREBASE_URL_POSTS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        postsRecyclerView = (RecyclerView) rootView.findViewById(R.id.posts);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postsRecyclerView.addItemDecoration(new ItemSpacingDecoration(10));

        postsAdapter = new ProblemPostAdapter(postsList, this);
        postsRecyclerView.setAdapter(postsAdapter);
        setupFirebaseDataListners();
        return rootView;
    }

    private void setupFirebaseDataListners() {
        /**
         * Firebase - Receives message
         */
        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    try {
                        Problem model = dataSnapshot.getValue(Problem.class);
                        model.setProblemID(dataSnapshot.getKey());
                        postsList.add(model);
                        postsAdapter.notifyItemInserted(postsList.size() - 1);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onCardClick(View view, Problem problemItem) {
        /*
        Bundle bundle = new Bundle();
        bundle.putString("card_id", problemItem.getProblemID());
        mFirebaseAnalytics.logEvent("card_click_from_feed", bundle);*/
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        DialogFragment newFragment = DetailedProblemFragment.newInstance(problemItem);
        newFragment.show(ft, "dialog");
    }

    private class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public ItemSpacingDecoration(int space) {
            int density = (int) getResources().getDisplayMetrics().density;
            this.space = space * density;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.top = space;
            outRect.bottom = space;
            outRect.left = space - 2;
            outRect.right = space - 2;
        }
    }
}
