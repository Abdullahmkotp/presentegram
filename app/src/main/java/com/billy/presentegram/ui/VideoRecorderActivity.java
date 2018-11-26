package com.billy.presentegram.ui;


import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;


import com.billy.presentegram.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoRecorderActivity extends FragmentActivity implements
        PreparePrompterFragment.SetPrompterTextProperties,VideoRecorderFragment.OnVideoRecorded {

    @BindView(R.id.vp_vra)
    ViewPager mPager;

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    private PrepareVideoSlideAdapter mAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recorder);
        ButterKnife.bind(this);
        mAdapter = new PrepareVideoSlideAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(mPager);
        setupTapIcon();



    }

    private void setupTapIcon() {
        tabLayout.getTabAt(0).setIcon(R.drawable.txt);
        tabLayout.getTabAt(1).setIcon(R.drawable.vidcam);
        tabLayout.getTabAt(2).setIcon(R.drawable.share);
    }


    @Override
    public void setPrompterText(String text, float textSize, int color) {

        mAdapter.videoRecorderFragment.getPrompterTextProperties(text,textSize,color);

    }

    @Override
    public void sendPath(String path) {
        mAdapter.reviewVideoFragment.setVideoPath(path);
    }
}
