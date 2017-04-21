package com.fc.materialbomb;

import android.app.Application;
import android.os.Handler;

/**
 * class description here
 *
 * @author fucong
 * @version 1.0.0
 * @see ""
 */

public class FCApplication extends Application {

    private static FCApplication mFCApplication;
    private Handler mHandler;

    public static FCApplication getInstance() {
        return mFCApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    private void init(){

        mFCApplication = this;
        mHandler = new Handler();
    }

    public void runOnUIThread(final Runnable r){
        mHandler.post(r);
    }

    public void runOnUIThreadDelay(final Runnable r, long delayMillis) {

        mHandler.postDelayed(r, delayMillis);
    }




}
