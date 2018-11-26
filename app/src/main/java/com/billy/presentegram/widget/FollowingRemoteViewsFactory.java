package com.billy.presentegram.widget;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.billy.presentegram.R;
import com.billy.presentegram.models.Following;
import com.billy.presentegram.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by dell on 21/11/2018.
 */

class FollowingRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private ArrayList<User> mUsers;
    private ArrayList<Following> mFollowingUsers;
    private boolean mDataAvailable = false;

    FollowingRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;

    }

    @Override
    public void onCreate() {



    }

    @Override
    public void onDataSetChanged() {
        try {
            mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }catch (NullPointerException e){
            e.printStackTrace();

            return;

        }







        mReference = FirebaseDatabase.getInstance().getReference();
        mReference.child("following").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mFollowingUsers = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        mFollowingUsers.add(snapshot.getValue(Following.class));
                    }
                    mReference.child("users").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mUsers = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                String userId = snapshot.getValue(User.class).getUserId();

                                for (int i = 0; i < mFollowingUsers.size(); i++) {
                                    if (mFollowingUsers.get(i).getUserId().equals(userId)) {
                                        mUsers.add(snapshot.getValue(User.class));
                                    }
                                }
                            }
                            mDataAvailable = true;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        while (!mDataAvailable) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mFollowingUsers.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.following_widget_list_item_view);
        rv.setTextViewText(R.id.tv_widget_username, mUsers.get(i).getUsername());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(mContext.getResources().getString(R.string.userId), mUsers.get(i).getUserId());
        rv.setOnClickFillInIntent(R.id.ll_user_row, fillInIntent);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
