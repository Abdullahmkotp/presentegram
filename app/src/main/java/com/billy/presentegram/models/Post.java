package com.billy.presentegram.models;

import com.google.firebase.database.Exclude;

/**
 * Created by dell on 16/11/2018.
 */

public class Post {




    private String mKey;
    private String mUserId;
    private String mVideoUrl;
    private String mDate;
    private String mVideoTitle;
    private String mUserPhoto;



    private String mUserName;

    public Post() {
    }

    public Post(String userId,String videoUrl,String date,String mVideoTitle){

        this.mUserId = userId;
        this.mVideoUrl = videoUrl;
        this.mDate = date;
        this.mVideoTitle = mVideoTitle;

    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.mVideoUrl = videoUrl;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public String getVideoTitle() {
        return mVideoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.mVideoTitle = videoTitle;
    }

    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String mKey) {
        this.mKey = mKey;
    }

    @Exclude
    public String getUserPhoto() {
        return mUserPhoto;
    }

    @Exclude
    public void setUserPhoto(String userPhoto) {
        this.mUserPhoto = userPhoto;
    }

    @Exclude
    public String getUserName() {
        return mUserName;
    }

    @Exclude
    public void setUserName(String userName) {
        this.mUserName = userName;
    }
}
