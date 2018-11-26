package com.billy.presentegram;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by dell on 21/11/2018.
 */

public class FollowingRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FollowingRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
