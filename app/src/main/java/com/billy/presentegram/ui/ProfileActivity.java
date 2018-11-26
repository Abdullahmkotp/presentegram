package com.billy.presentegram.ui;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.billy.presentegram.widget.FollowingWidget;
import com.billy.presentegram.R;
import com.billy.presentegram.models.Following;
import com.billy.presentegram.models.Post;
import com.billy.presentegram.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
        implements PostsAdapter.OnDeleteClickListener, PostsAdapter.OnPlayVideoClickListener {


    private final String TAG = ProfileActivity.this.getClass().getSimpleName();
    private static final int PICK_IMAGE = 255;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 256;
    private static final String VIDEO_PLAYED = "VIDEO_PLAYED";
    private static final String CURRENT_PLAYBACK_POSITION = "CURRENT_PLAYBACK_POSITION";
    private static final String VIDEO_STATE = "VIDEO_STATE";
    private static final String VIDEO_ITEM_POSITION = "VIDEO_ITEM_POSITION";
    private static final String CURRENT_RECYCLER_POSITION = "CURRENT_RECYCLER_POSITION";

    @BindView(R.id.tv_username_profile)
    TextView tv_username;

    @BindView(R.id.tv_gender_profile)
    TextView tv_gender;

    @BindView(R.id.tv_dob_profile)
    TextView tv_dob;

    @BindView(R.id.appBar)
    AppBarLayout appBarLayout;

    @BindView(R.id.pb_profile)
    ProgressBar progressBar;

    @BindView(R.id.pb_profile_pic)
    ProgressBar progressBarProfilePic;

    @BindView(R.id.iv_main_profile_pic)
    CircleImageView mProfilePic;

    @BindView(R.id.btn_following)
    ImageView mBtnFollowing;

    @BindView(R.id.no_video_profile_container)
    RelativeLayout mNoVidContainer;

    @BindView(R.id.rv_profile_videos)
    RecyclerView mRecyclerView;


    private User mUser;
    private String mUserId;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private boolean mFollowingState;
    private PostsAdapter mPostsAdapter;
    private ArrayList<Post> mPosts;
    private boolean mProfileOwner;
    private String mFollowingKey;

    private Parcelable mRecyclerPosition;
    private boolean mVidPlayed;
    private boolean mVidState;
    private int mVideoItemPosition;
    private long mCurrentPlaybackPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        mDatabase = FirebaseDatabase.getInstance();

        if (savedInstanceState != null) {

            mRecyclerPosition = savedInstanceState.getParcelable(CURRENT_RECYCLER_POSITION);
            mVidPlayed = savedInstanceState.getBoolean(VIDEO_PLAYED);
            mVidState = savedInstanceState.getBoolean(VIDEO_STATE);
            mVideoItemPosition = savedInstanceState.getInt(VIDEO_ITEM_POSITION);
            mCurrentPlaybackPosition = savedInstanceState.getLong(CURRENT_PLAYBACK_POSITION);

        }


        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        mUserId = getIntent().getStringExtra(getResources().getString(R.string.userId));
        if (mUserId.equals(currentUserId)) {
            mBtnFollowing.setVisibility(View.INVISIBLE);
            mProfileOwner = true;
        } else mProfileOwner = false;


        if (!mProfileOwner) {

            Query queryFollowing = FirebaseDatabase.getInstance().getReference().child("following")
                    .child(currentUserId)
                    .orderByChild("userId")
                    .equalTo(mUserId);
            queryFollowing.addValueEventListener(followEventListener);
        }
        Query queryUser = FirebaseDatabase.getInstance().getReference("users").orderByChild("userId").equalTo(mUserId);
        queryUser.addListenerForSingleValueEvent(valueUserListener);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mPosts != null) {
            outState.putParcelable(CURRENT_RECYCLER_POSITION, mRecyclerView.getLayoutManager().onSaveInstanceState());
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
            if (mPostsAdapter.mExoPlayer != null) {
                mPostsAdapter.stopVideo();
            }
        }
    }

    ValueEventListener followEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String followingId = snapshot.getValue(Following.class).getUserId();
                    mFollowingKey  = snapshot.getKey();
                    if (mUserId.equals(followingId)) {
                        mFollowingState = true;
                        mBtnFollowing.setBackground(getResources().getDrawable(R.drawable.unfollow2));

                    } else {
                        mFollowingState = false;
                        mBtnFollowing.setBackground(getResources().getDrawable(R.drawable.unfollow2));
                    }
                }
            } else {
                mFollowingState = false;
                mBtnFollowing.setBackground(getResources().getDrawable(R.drawable.follow2));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

            try {
                throw databaseError.toException();
            } catch (DatabaseException e) {
                e.printStackTrace();

            }


        }
    };

    ValueEventListener valueUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (dataSnapshot.exists()) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    mUser = snapshot.getValue(User.class);

                    updateAppBar(mUser);

                    Query queryVideos = FirebaseDatabase.getInstance().getReference("posts").orderByChild("userId").equalTo(mUserId);
                    queryVideos.addValueEventListener(valueVideosListener);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {


        }


    };

    ValueEventListener valueVideosListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (dataSnapshot.exists()) {
                mPosts = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    post.setKey(snapshot.getKey());
                    mPosts.add(post);
                }
            }
            if (mPosts != null) {
                recyclerInit();
                mNoVidContainer.setVisibility(View.GONE);

            } else {

                progressBar.setVisibility(View.INVISIBLE);
                mNoVidContainer.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {


        }
    };

    private void recyclerInit() {

        progressBar.setVisibility(View.INVISIBLE);

        if (mVidPlayed) {
            mPostsAdapter = new PostsAdapter(this, mPosts, mVideoItemPosition, mCurrentPlaybackPosition, mVidState, mUser.getPpUrl());
        } else {
            mPostsAdapter = new PostsAdapter(this, mPosts, mUser.getPpUrl(), mUser.getUsername(), mProfileOwner);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mPostsAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(5);
        mRecyclerView.setDrawingCacheEnabled(true);
        if (mRecyclerPosition != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mRecyclerPosition);
        }
        mPostsAdapter.setOnDeleteListener(this);
        mPostsAdapter.setOnPlayVideoListener(this);

    }


    public void updateAppBar(User user) {

        if (user != null) {

            tv_username.setText(mUser.getUsername());
            tv_dob.setText(mUser.getDateOfBirth());
            tv_gender.setText(mUser.getGender());

            if (user.getPpUrl() != null) {

                Picasso.get().load(user.getPpUrl()).centerCrop().fit().into(mProfilePic);
            }

            appBarLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void deletePost(int position) {

        final Post selectedPost = mPosts.get(position);


        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("posts");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = mFirebaseStorage.getReferenceFromUrl(selectedPost.getVideoUrl());
        mStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedPost.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.postdelete), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        mPostsAdapter.notifyDataSetChanged();


    }

    @Override
    public void playVideo(int position) {

        mVideoItemPosition = position;
        mVidPlayed = true;

    }

    @OnClick(R.id.fab_record)
    void recordNewVideo() {

        Intent intent = new Intent(this, VideoRecorderActivity.class);
        intent.putExtra(getResources().getString(R.string.userId), mUserId);
        startActivity(intent);
    }

    @OnClick(R.id.iv_main_profile_pic)
    void selectProfilePic() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), PICK_IMAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    @OnClick(R.id.btn_following)
    public void onFollowClick() {
        Following following = new Following(mUserId);
        String currentUserId = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mDatabase.getReference();

        if (!mFollowingState) {
            mDatabaseRef.child("following").child(currentUserId).push().setValue(following);
            mBtnFollowing.setBackground(getResources().getDrawable(R.drawable.unfollow2));
            mFollowingState = true;
            Toast.makeText(this,getResources().getString(R.string.following),Toast.LENGTH_SHORT).show();
            updateWidget();
        } else {

            mDatabaseRef.child("following").child(currentUserId).child(mFollowingKey).removeValue();
            mBtnFollowing.setBackground(getResources().getDrawable(R.drawable.follow2));
            mFollowingState = false;
            Toast.makeText(this,getResources().getString(R.string.unfollowing),Toast.LENGTH_SHORT).show();
            updateWidget();
        }

    }

    private void updateWidget() {
        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, FollowingWidget.class));
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.notifyAppWidgetViewDataChanged(ids,R.id.following_list_view_widget);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestImagePermission();

                return;

            } else {
                if (data != null) {
                    Uri imageUri = data.getData();
                    try {

                        final Bitmap imageBitmab = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imageBitmab.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageToByte = baos.toByteArray();

                        mFirebaseStorage = FirebaseStorage.getInstance();
                        mStorageRef = mFirebaseStorage.getReference("profilePic");
                        mStorageRef = mStorageRef.child(mUserId + ".jpg");


                    /*Upload Profile Pic*/


                        UploadTask uploadTask = mStorageRef.putBytes(imageToByte);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                try {
                                    throw exception;
                                } catch (Exception e) {
                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                } finally {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.upload_error)
                                            , Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(getApplicationContext(), R.string.profile_picture_updated, Toast.LENGTH_SHORT).show();


                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                progressBarProfilePic.setVisibility(View.VISIBLE);

                            }
                        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                progressBarProfilePic.setVisibility(View.INVISIBLE);
                                mProfilePic.setImageBitmap(imageBitmab);

                            }
                        });



                    /*Save Url to database*/

                        uploadTask = mStorageRef.putFile(imageUri);

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return mStorageRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    mDatabaseRef = mDatabase.getReference().child("users").child(mUserId);

                                    mDatabaseRef.child("ppUrl").setValue(downloadUri.toString().trim());

                                } else {
                                    try {
                                        throw new Exception();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    return;
                }
            }
        }
    }


    private void requestImagePermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length >= 0) {

                    boolean readStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (readStorageAccepted) {
                        Toast.makeText(this, R.string.storage_accessed, Toast.LENGTH_SHORT).show();
                    } else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                showMessageOKCancel(
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestImagePermission();
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                    break;
                }


        }
    }


    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ProfileActivity.this)
                .setMessage(R.string.storage_permission)
                .setPositiveButton(getResources().getString(R.string.ok), okListener)
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .create()
                .show();
    }


}

