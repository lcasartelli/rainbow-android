package com.plasticpanda.rainbow;

import android.app.Application;

import com.testflightapp.lib.TestFlight;

public class RainbowApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TestFlight.takeOff(this, getString(R.string.testflight_token));
    }
}