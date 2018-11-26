package com.billy.presentegram;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.widget.Toast;



import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;


public class UploadVideoService extends IntentService {

    private static final String VIDEO_TITLE = "VIDEO_TITLE";
    private static final String VIDEO_PATH = "VIDEO_PATH";
    private static final String VIDEO_INFO = "VIDEO_INFO";
    private DatabaseReference mDatabaseReference;
    private Resources mResources;
    private String mVideoPath;
    private String mVideoTitle;
    private String mUserId;
    private String mVideoUrl;
    private StorageReference mStorageReference;
    private boolean mVideoUploaded;
    private boolean mPostShared;


    public UploadVideoService() {
        super("UploadVideoService");

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mResources = getResources();

        Bundle bundle = intent.getBundleExtra(VIDEO_INFO);
        mVideoTitle = bundle.getString(VIDEO_TITLE);
        mUserId = bundle.getString(mResources.getString(R.string.userId), mUserId);
        mVideoPath = bundle.getString(VIDEO_PATH, mVideoPath);

        Uri uri = Uri.fromFile(new File(mVideoPath));
        StorageMetadata storageMetadata = new StorageMetadata.Builder()
                .setContentType("video/mp4").build();
        mStorageReference = FirebaseStorage.getInstance().getReference("videos").child(uri.getLastPathSegment());
        UploadTask uploadTask = mStorageReference.putFile(uri, storageMetadata);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                try {
                    throw e;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return mStorageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                mVideoUrl = task.getResult().toString();
                sharePost();
                mVideoUploaded = true;

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mVideoUploaded = false;
            }
        });

        if (!mVideoUploaded) {
            Toast.makeText(this, mResources.getString(R.string.upload_error)
                    , Toast.LENGTH_SHORT).show();
        }

    }


    private void sharePost() {
        final Calendar calendar = Calendar.getInstance();
        final String date = DateFormat.getDateFormat(this.getApplicationContext()).format(calendar.getTime());
        final String title = mVideoTitle;
        final long postTime = System.currentTimeMillis();
        final String node = String.valueOf(postTime);
        final Post post = new Post(mUserId, mVideoUrl, date, title);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("posts").child(node).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mPostShared = true;
                deleteVideo();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mPostShared = false;
                deleteVideo();
            }
        });

        if (mPostShared) {
            Toast.makeText(this, mResources.getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, mResources.getString(R.string.upload_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteVideo() {
        final Uri uri = Uri.parse(mVideoPath);
        final File file = new File(uri.getPath());
        if (file.exists()) {
            file.delete();
        } else {
            throw new RuntimeException("can not find file dir");
        }
    }
}
