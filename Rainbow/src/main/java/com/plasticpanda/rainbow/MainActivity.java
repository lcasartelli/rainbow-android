package com.plasticpanda.rainbow;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

/**
 * @author Luca Casartelli
 */

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("rainbow", Context.MODE_PRIVATE);
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
}
