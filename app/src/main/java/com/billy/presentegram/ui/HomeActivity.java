package com.billy.presentegram.ui;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.billy.presentegram.R;
import com.billy.presentegram.models.Post;
import com.billy.presentegram.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements PostsAdapter.OnPlayVideoClickListener
        , PostsAdapter.OnProfilePictureClickListener {


    private static final String VIDEO_PLAYED = "VIDEO_PLAYED";
    private static final String CURRENT_PLAYBACK_POSITION = "CURRENT_PLAYBACK_POSITION";
    private static final String VIDEO_STATE = "VIDEO_STATE";
    private static final String VIDEO_ITEM_POSITION = "VIDEO_ITEM_POSITION";
    private static final String PROFILE_OWNER = "PROFILE_OWNER";
    private static final String CURRENT_RECYCLER_POSITION = "CURRENT_RECYCLER_POSITION";

    @BindView(R.id.pb_home)
    ProgressBar homeProgressBar;

    @BindView(R.id.iv_profile_pic)
    CircleImageView mProfilePic;

    @BindView(R.id.rv_home_videos)
    RecyclerView mRecyclerView;

    @BindView(R.id.no_video_home_container)
    RelativeLayout mNoVidContainer;


    private FirebaseAuth mAuth;


    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private String mUserId;
    private ArrayList<Post> mPosts;
    private PostsAdapter mPostsAdapter;


    private Parcelable mRecyclerPosition;
    private boolean mVidPlayed;
    private boolean mVidState;
    private int mVideoItemPosition;
    private long mCurrentPlaybackPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();


        if (checkLoginStatus()) {
            mDatabase = FirebaseDatabase.getInstance();
            Query query = mDatabase.getReference().child("users").orderByChild("userId").equalTo(mUserId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        User user = snapshot.getValue(User.class);

                        if (user.getPpUrl() != null) {
                            Picasso.get().load(user.getPpUrl()).centerCrop().fit().into(mProfilePic);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }

            });


        }


        if (savedInstanceState != null) {

            mRecyclerPosition = savedInstanceState.getParcelable(CURRENT_RECYCLER_POSITION);
            mVidPlayed = savedInstanceState.getBoolean(VIDEO_PLAYED);
            mVidState = savedInstanceState.getBoolean(VIDEO_STATE);
            mVideoItemPosition = savedInstanceState.getInt(VIDEO_ITEM_POSITION);
            mCurrentPlaybackPosition = savedInstanceState.getLong(CURRENT_PLAYBACK_POSITION);

        }


        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        Query queryUsersPosts = mDatabaseRef.child("posts");
        queryUsersPosts.addValueEventListener(valueEventListener);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mPosts != null) {
            if (mRecyclerView.getLayoutManager() != null) {
                outState.putParcelable(CURRENT_RECYCLER_POSITION, mRecyclerView.getLayoutManager().onSaveInstanceState());
            }
            outState.putInt(VIDEO_ITEM_POSITION, mVideoItemPosition);
            outState.putBoolean(VIDEO_STATE, getVidState());
            outState.putBoolean(VIDEO_PLAYED, mVidPlayed);
            if (mPostsAdapter.mExoPlayer != null) {
                outState.putLong(CURRENT_PLAYBACK_POSITION, mPostsAdapter.mExoPlayer.getCurrentPosition());
            }
        }
    }

    private boolean getVidState() {
        if (mPostsAdapter.mExoPlayer != null)
            return mPostsAdapter.mExoPlayer.getPlayWhenReady();
        else return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mPosts != null) {
            if (mPostsAdapter.mExoPlayer != null) {
                mPostsAdapter.mExoPlayer.setPlayWhenReady(mVidState);

            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();

        if (mPosts != null) {
            if (mPostsAdapter.mExoPlayer != null) {
                mVidState = getVidState();
                mPostsAdapter.mExoPlayer.setPlayWhenReady(mVidState);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPosts != null) {
            if (mPostsAdapter != null) {
                if (mPostsAdapter.mExoPlayer != null) {
                    mPostsAdapter.stopVideo();
                }
            }
        }
    }


    private void recyclerInit() {
        homeProgressBar.setVisibility(View.GONE);

        if (mVidPlayed) {
            mPostsAdapter = new PostsAdapter(this, mPosts, mVideoItemPosition, mCurrentPlaybackPosition, mVidState, null);
        } else {
            mPostsAdapter = new PostsAdapter(this, mPosts);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mPostsAdapter);
        mPostsAdapter.setOnPlayVideoListener(this);
        mPostsAdapter.setOnProfilePictureListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(5);
        mRecyclerView.setDrawingCacheEnabled(true);

        if (mRecyclerPosition != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mRecyclerPosition);
        }
    }


    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                mPosts = new ArrayList<>();


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mPosts.add(snapshot.getValue(Post.class));
                }
                mDatabase = FirebaseDatabase.getInstance();
                Query queryUsersPhotos = mDatabase.getReference().child("users")
                        .orderByChild("ppUrl");
                queryUsersPhotos.addValueEventListener(usersPhotosEventListener);
            }else {
                homeProgressBar.setVisibility(View.GONE);
                mNoVidContainer.setVisibility(View.VISIBLE);

            }


        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    ValueEventListener usersPhotosEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                User user = snapshot.getValue(User.class);
                for (int i = 0; i < mPosts.size(); i++) {
                    if (user.getUserId().equals(mPosts.get(i).getUserId())) {
                        mPosts.get(i).setUserPhoto(user.getPpUrl());
                        mPosts.get(i).setUserName(user.getUsername());
                    }
                }
            }
            if (mPosts != null) {
                recyclerInit();
                mNoVidContainer.setVisibility(View.GONE);
            } else {
                homeProgressBar.setVisibility(View.GONE);
                mNoVidContainer.setVisibility(View.VISIBLE);

            }

        }


        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private boolean checkLoginStatus() {

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
            return false;
        } else {

            mUserId = mAuth.getCurrentUser().getUid();
            return true;

        }

    }


    @OnClick(R.id.iv_profile_pic)
    void openProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(PROFILE_OWNER, PROFILE_OWNER);
        intent.putExtra(getResources().getString(R.string.userId), mUserId);
        startActivity(intent);
    }


    @Override
    public void playVideo(int position) {
        mVideoItemPosition = position;
        mVidPlayed = true;
    }

    @Override
    public void openProfile(int position) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(getResources().getString(R.string.userId), mPosts.get(position).getUserId());
        startActivity(intent);

    }
}
