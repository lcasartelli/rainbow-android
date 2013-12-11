package com.plasticpanda.rainbow;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getName();

    public LoginFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getActivity();
        if (context != null) {
            final RainbowHelper dbHelper = RainbowHelper.getInstance(context);
            Button btn = (Button) view.findViewById(R.id.login);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView userTextView = (TextView) view.findViewById(R.id.user);
                    TextView codeTextView = (TextView) view.findViewById(R.id.code);

                    if (userTextView != null && userTextView.getText() != null && codeTextView != null && codeTextView.getText() != null) {
                        dbHelper.performLogin(userTextView.getText().toString(), codeTextView.getText().toString(), new Command() {
                            @Override
                            public void execute() {
                                if (getFragmentManager() != null) {
                                    getFragmentManager().beginTransaction()
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                        .replace(R.id.container, new MainFragment())
                                            //.addToBackStack(null)
                                        .commit();
                                }
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
