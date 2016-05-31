package com.nychareport.backlog.activities;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nychareport.backlog.Constants;
import com.nychareport.backlog.R;
import com.nychareport.backlog.fragments.DetailedProblemFragment;
import com.nychareport.backlog.misc.Utils;
import com.nychareport.backlog.adapter.ProblemPostAdapter;
import com.nychareport.backlog.models.Problem;
import com.nychareport.backlog.viewholder.PostProblemViewHolder;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PostProblemViewHolder.OnClickListener {

    private static final String TAG = PostProblemActivity.class.getSimpleName();

    private RecyclerView postsRecyclerView;
    private ProblemPostAdapter postsAdapter;
    private List<Problem> postsList;

    private Firebase postsRef;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        postsList = new ArrayList<>();
        setSupportActionBar(toolbar);
        setTitle("Home Feed");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        postsRef = new Firebase(Constants.FIREBASE_URL_POSTS);

        ImageView fab = (ImageView) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePageActivity.this, PostProblemActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        postsRecyclerView = (RecyclerView) findViewById(R.id.posts);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.addItemDecoration(new ItemSpacingDecoration(10));

        postsAdapter = new ProblemPostAdapter(postsList, this);
        postsRecyclerView.setAdapter(postsAdapter);
        setupFirebaseDataListners();
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_user_preferences:
                Intent intent = new Intent(HomePageActivity.this, UserOnboardingActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_share:

                break;
            case R.id.nav_logout:
                new Firebase(Constants.FIREBASE_URL).unauth();
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onCardClick(View view, Problem problemItem) {
        Log.d("GGGGG", "Clicked card");/*
        Bundle bundle = new Bundle();
        bundle.putString("card_id", problemItem.getProblemID());
        mFirebaseAnalytics.logEvent("card_click_from_feed", bundle);*/
        // Create and show the dialog.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
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
