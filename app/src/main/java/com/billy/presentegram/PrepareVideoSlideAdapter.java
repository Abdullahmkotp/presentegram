package com.billy.presentegram;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/**
 * Created by dell on 11/11/2018.
 */

public class PrepareVideoSlideAdapter extends FragmentStatePagerAdapter  {

    PreparePrompterFragment preparePrompterFragment;
    VideoRecorderFragment videoRecorderFragment;
    ReviewVideoFragment reviewVideoFragment;






    public PrepareVideoSlideAdapter(FragmentManager fm, Context context) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                preparePrompterFragment = new PreparePrompterFragment();

                return preparePrompterFragment;

            case 1:
                videoRecorderFragment = new VideoRecorderFragment();


                return videoRecorderFragment;

            case 2:
                reviewVideoFragment = new ReviewVideoFragment();


                return reviewVideoFragment;


            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return 3;
    }




}
