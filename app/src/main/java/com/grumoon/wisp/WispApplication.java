package com.grumoon.wisp;

import android.app.Application;

import com.grumoon.wisp.base.manager.cast.CastManager;
import com.grumoon.wisp.base.manager.log.LogManager;

public class WispApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CastManager.getInstance().init(this);
        LogManager.init();
    }
}
