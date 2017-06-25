package com.crazysunj.cardslide;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * description
 * <p>
 * Created by sunjian on 2017/6/24.
 */

public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
