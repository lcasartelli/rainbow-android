/*
 * Copyright (C) 2013 Luca Casartelli luca@plasticpanda.com, Plastic Panda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.plasticpanda.rainbow.core;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.ui.LoginFragment;
import com.plasticpanda.rainbow.ui.MainFragment;
import com.plasticpanda.rainbow.utils.BackListener;


public class MainActivity extends Activity {

    private boolean defaultBackAction;
    private BackListener backListener;


    public MainActivity() {
        super();
        this.defaultBackAction = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences(RainbowConst.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        Fragment fragment;
        if (token != null) {
            fragment = MainFragment.getInstance();
        } else {
            fragment = LoginFragment.getInstance();
        }

        if (savedInstanceState == null) {
            getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.container, fragment)
                .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setBackListener(BackListener backListener) {
        this.backListener = backListener;
    }

    public void setDefaultBackAction(boolean defaultBackAction) {
        this.defaultBackAction = defaultBackAction;
    }

    @Override
    public void onBackPressed() {
        if (defaultBackAction) {
            super.onBackPressed();
        } else {
            backListener.goBack();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        RainbowApp.activityPaused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RainbowApp.activityResumed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        RainbowApp.activityPaused();
    }
}
