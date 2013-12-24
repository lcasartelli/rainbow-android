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

package com.plasticpanda.rainbow.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Service;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.core.RainbowHelper;
import com.plasticpanda.rainbow.utils.SimpleListener;


public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getName();
    private static LoginFragment sharedInstance;

    private LoginFragment() {
        super();
    }

    public static synchronized LoginFragment getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new LoginFragment();
        }
        return sharedInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Activity context = getActivity();
        if (context != null) {
            final RainbowHelper dbHelper = RainbowHelper.getInstance(context);
            Button btn = (Button) view.findViewById(R.id.login);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View clickView) {
                    TextView userTextView = (TextView) view.findViewById(R.id.user);
                    TextView codeTextView = (TextView) view.findViewById(R.id.code);

                    // Hide keyboard
                    InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
                    keyboard.hideSoftInputFromWindow(userTextView.getWindowToken(), 0);
                    keyboard.hideSoftInputFromWindow(codeTextView.getWindowToken(), 0);


                    if (userTextView != null &&
                        userTextView.getText() != null &&
                        codeTextView != null &&
                        codeTextView.getText() != null) {
                        dbHelper.performLogin(
                            userTextView.getText().toString(),
                            codeTextView.getText().toString(),
                            new SimpleListener() {
                                @Override
                                public void onSuccess() {
                                    if (getFragmentManager() != null) {
                                        getFragmentManager()
                                            .beginTransaction()
                                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                            .replace(R.id.container, MainFragment.getInstance())
                                                //.addToBackStack(null)
                                            .commit();
                                    }
                                }

                                @Override
                                public void onError() {
                                    Log.d(TAG, "Login error");
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.login_error),
                                        Toast.LENGTH_LONG)
                                        .show();
                                }
                            });
                    } else {
                        Log.e(TAG, "Error with login input");
                    }
                }
            });
        }
    }
}
