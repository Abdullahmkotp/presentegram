package com.billy.presentegram;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ReviewVideoFragment extends Fragment {


    private static final String VIDEO_POSITION_STATE = "VIDEO_POSITION_STATE";
    private static final String VIDEO_STATE = "VIDEO_STATE";
    private static final String VIDEO_TITLE = "VIDEO_TITLE";
    private static final String VIDEO_PATH = "VIDEO_PATH";
    private static final String VIDEO_INFO = "VIDEO_INFO";
    @BindView(R.id.et_video_title)
    TextInputLayout videoTitle;

    @BindView(R.id.btn_share)
    FloatingActionButton share;

    @BindView(R.id.btn_cancel)
    Button cancel;

    @BindView(R.id.video_view)
    PlayerView playerView;

    @BindView(R.id.no_video_container)
    RelativeLayout noVidContainer;

    @BindView(R.id.video_container)
    RelativeLayout videoContainer;

    @BindView(R.id.upload_video_container)
    RelativeLayout uploadVideoContainer;


    public String mVideoPath;

    private SimpleExoPlayer mExoPlayer;

    private long mVidPosition;

    private boolean mVidStat;

    private String mUserId;




    public ReviewVideoFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_review_video, container, false);
        ButterKnife.bind(this, rootView);
        mUserId = getActivity().getIntent().getStringExtra(getActivity().getResources().getString(R.string.userId));

        if (savedInstanceState != null & mVideoPath != null) {

            mExoPlayer.setPlayWhenReady(savedInstanceState.getBoolean(VIDEO_STATE));
            mExoPlayer.seekTo(savedInstanceState.getLong(VIDEO_POSITION_STATE));
        }


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mVideoPath != null) {
            if (mExoPlayer == null) {
                setVideo();
                mExoPlayer.seekTo(mVidPosition);
                mExoPlayer.setPlayWhenReady(mVidStat);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mVideoPath != null) {
            mVidPosition = mExoPlayer.getCurrentPosition();
            mVidStat = mExoPlayer.getPlayWhenReady();
            stopVideo();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        if (mVideoPath != null) {
            outState.putLong(VIDEO_POSITION_STATE, mExoPlayer.getCurrentPosition());
            outState.putBoolean(VIDEO_STATE, getVidState());
        }

    }

    private boolean getVidState() {
        if (mVideoPath != null) {
            return mExoPlayer.getPlayWhenReady();
        } else return false;

    }

    public void setVideoPath(String videoPath) {

        this.mVideoPath = videoPath;

        if (mVideoPath != null) {
            showVideoView();
            setVideo();
        }


    }

    private void showVideoView() {
        noVidContainer.setVisibility(View.GONE);
        videoContainer.setVisibility(View.VISIBLE);

    }



    private void showNoVideoView() {
        videoContainer.setVisibility(View.GONE);
        noVidContainer.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_share)
    public void onShareClick() {
        if (videoTitleValidate()) {

            Bundle bundle = new Bundle();
            Intent uploadVideoIntentService = new Intent(getContext(), UploadVideoService.class);
            String videoTit = videoTitle.getEditText().getText().toString();
            bundle.putString(VIDEO_TITLE, videoTit);
            bundle.putString(getActivity().getResources().getString(R.string.userId), mUserId);
            bundle.putString(VIDEO_PATH, mVideoPath);
            uploadVideoIntentService.putExtra(VIDEO_INFO,bundle);
            getActivity().startService(uploadVideoIntentService);
            Toast.makeText(getContext(),getActivity().getResources().getString(R.string.sharing_post),Toast.LENGTH_SHORT).show();
            getActivity().finish();



        }else{

            return;
        }
    }

    private boolean videoTitleValidate() {

        if (videoTitle.getEditText().getText().toString().equals("") | videoTitle.getEditText().getText() == null) {

            Toast.makeText(getActivity(), getActivity().getResources()
                    .getString(R.string.video_title_null), Toast.LENGTH_SHORT).show();
            return false;

        } else if (videoTitle.getEditText().getText().length() > 20) {
            videoTitle.setError(getActivity().getResources().getString(R.string.invalid_video_title));
            return false;
        }

            return true;
    }

    @OnClick(R.id.btn_cancel)
    public void onCancelClick() {
        deleteVideo();
        showNoVideoView();

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

    private void setVideo() {

        if (mVideoPath != null) {

            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), new DefaultTrackSelector());
            playerView.setPlayer(mExoPlayer);
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getContext()
                    , Util.getUserAgent(getContext()
                    , "presentegram"));

            ExtractorMediaSource mediaSource;
            playerView.setDefaultArtwork(null);
            mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(mVideoPath));
            mExoPlayer.prepare(mediaSource);


        } else {
            return;
        }
    }

    private void stopVideo() {
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

}
