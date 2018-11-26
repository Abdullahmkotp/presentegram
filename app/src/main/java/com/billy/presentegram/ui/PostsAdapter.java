package com.billy.presentegram.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.billy.presentegram.R;
import com.billy.presentegram.models.Post;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.VideoViewHolder> {


    private Context mContext;
    private ArrayList<Post> mPosts;
    private String mPpUrl;
    private String mUsername;
    private boolean mProfileOwner;
    private OnDeleteClickListener mDeleteListener;
    private OnPlayVideoClickListener mPlayVideoListener;
    private OnProfilePictureClickListener mProfilePictureListener;
    public ExoPlayer mExoPlayer;
    private int mPlayedPosition = -1;
    private long mPlaybackPosition;
    private boolean mVideoState;


    interface OnPlayVideoClickListener {
        void playVideo(int position);
    }


    interface OnDeleteClickListener {
        void deletePost(int position);
    }

    interface OnProfilePictureClickListener {
        void openProfile(int position);
    }


    //profile constructor
    public PostsAdapter(Context context, ArrayList<Post> posts, String ppUrl, String userName, boolean profileOwner) {
        mPosts = new ArrayList<>();
        mPosts = posts;
        mPpUrl = ppUrl;
        mUsername = userName;
        mContext = context;
        mProfileOwner = profileOwner;

    }


    //home constructor
    public PostsAdapter(Context context, ArrayList<Post> posts) {
        mPosts = new ArrayList<>();
        mPosts = posts;
        mContext = context;

    }

    //recreate adapter
    public PostsAdapter(Context context, ArrayList<Post> posts, int position, long playbackPosition, boolean playOrPause, String ppUrl) {
        mPosts = new ArrayList<>();
        mPosts = posts;
        mContext = context;
        mPlayedPosition = position;
        mPlaybackPosition = playbackPosition;
        mVideoState = playOrPause;
        mPpUrl = ppUrl;
    }

    public void setOnDeleteListener(OnDeleteClickListener listener) {
        mDeleteListener = listener;
    }

    public void setOnPlayVideoListener(OnPlayVideoClickListener listener) {mPlayVideoListener = listener;}

    public void setOnProfilePictureListener(OnProfilePictureClickListener listener ){ mProfilePictureListener =listener;}


    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new VideoViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {

        if (mContext instanceof ProfileActivity) {

            holder.profileBind(position);
            if (mPlayedPosition == position) {
                resetVideo(mPosts.get(position).getVideoUrl(), holder.mPlayerView, mPlaybackPosition, mVideoState);
                holder.playButton.setVisibility(View.GONE);
            }

        } else if (mContext instanceof HomeActivity) {
            holder.homeBind(position);
            if (mPlayedPosition == position) {
                resetVideo(mPosts.get(position).getVideoUrl(), holder.mPlayerView, mPlaybackPosition, mVideoState);
                holder.playButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }


    public class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView videoTitle;
        TextView videoDate;
        CircleImageView pp;
        TextView userName;
        Button deleteButton;
        Button playButton;
        PlayerView mPlayerView;


        public VideoViewHolder(View itemView) {
            super(itemView);

            videoTitle = itemView.findViewById(R.id.tv_post_video_title);
            videoDate = itemView.findViewById(R.id.tv_post_video_date);
            pp = itemView.findViewById(R.id.iv_post_personal_pic);
            userName = itemView.findViewById(R.id.tv_post_user_name);
            deleteButton = itemView.findViewById(R.id.iv_post_video_delete);
            playButton = itemView.findViewById(R.id.btn_post_play_video);
            mPlayerView = itemView.findViewById(R.id.player_view_post_video);


            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mDeleteListener != null) {
                        int position = getAdapterPosition();
                        mDeleteListener.deletePost(position);
                    }
                }
            });


            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mPlayVideoListener != null) {
                        int position = getAdapterPosition();
                        mPlayVideoListener.playVideo(position);
                        setVideo(mPosts.get(position).getVideoUrl(), mPlayerView);
                        playButton.setVisibility(View.GONE);
                    }
                }
            });

            pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mProfilePictureListener != null) {
                        int position = getAdapterPosition();
                        mProfilePictureListener.openProfile(position);
                    }

                }
            });
        }

        public void profileBind(int position) {

            if (!mProfileOwner) {
                deleteButton.setVisibility(View.GONE);
            }
            videoTitle.setText(mPosts.get(position).getVideoTitle());
            videoDate.setText(mPosts.get(position).getDate());
            userName.setText(mUsername);
            if ( mPpUrl != null) {
                Picasso.get().load(mPpUrl).fit().centerCrop().into(pp);
            } else Picasso.get().load(R.drawable.default_pp).fit().centerCrop().into(pp);

        }

        public void homeBind(int position) {
            deleteButton.setVisibility(View.GONE);
            videoTitle.setText(mPosts.get(position).getVideoTitle());
            videoDate.setText(mPosts.get(position).getDate());
            userName.setText(mPosts.get(position).getUserName());
            if (mPosts.get(position).getUserPhoto() != null) {
                Picasso.get().load(mPosts.get(position).getUserPhoto()).fit().centerCrop().into(pp);
            } else Picasso.get().load(R.drawable.default_pp).fit().centerCrop().into(pp);
        }


    }


    public void setVideo(String url, PlayerView mPlayerView) {

        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
        }


        mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
        mPlayerView.setPlayer(mExoPlayer);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext
                , Util.getUserAgent(mContext
                , "presentegram"));

        ExtractorMediaSource mediaSource;
        if (url != null) {
            mPlayerView.setDefaultArtwork(null);
            mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(url));
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        } else {

            mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_video_available));

        }

    }

    public void stopVideo() {
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }


    private void resetVideo(String url, PlayerView playerView, long playbackPosition, boolean vidState) {

        mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
        playerView.setPlayer(mExoPlayer);
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext
                , Util.getUserAgent(mContext
                , "presentegram"));

        ExtractorMediaSource mediaSource;
        if (url != null) {
            playerView.setDefaultArtwork(null);
            mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(url));
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(vidState);
            mExoPlayer.seekTo(playbackPosition);
        } else {
            playerView.setDefaultArtwork(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_video_available));
        }
    }
}
