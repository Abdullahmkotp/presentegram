package com.billy.presentegram;



class Following {


    private String mUserId;

    public Following() {
    }

    public Following(String userId) {
        this.mUserId = userId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }
}
