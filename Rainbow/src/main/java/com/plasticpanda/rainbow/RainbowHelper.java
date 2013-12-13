package com.plasticpanda.rainbow;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


public class RainbowHelper {

    private static final String TAG = RainbowHelper.class.getName();

    private String token;
    private String UUID;
    private static RainbowHelper sharedInstance;
    private Context context;

    public RainbowHelper(Context context) {
        this.context = context;
        if (this.context != null) {
            this.UUID = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
            SharedPreferences shared = this.context.getSharedPreferences("rainbow", Context.MODE_PRIVATE);
            this.token = shared.getString("token", null);
        } else {
            Log.e(TAG, "Context null");
        }
    }

    public synchronized static RainbowHelper getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new RainbowHelper(context);
        }
        return sharedInstance;
    }

    public void performLogin(String username, String code, final Command onSuccess, final Command onError) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // Initiated the request
                Log.d(TAG, "Start");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody);
                Log.d(TAG, s);
                if (onSuccess != null) {
                    try {
                        JSONObject resp = new JSONObject(s);
                        String serverToken = resp.getString("token");
                        SharedPreferences sharedPreferences = context.getSharedPreferences("rainbow", Context.MODE_PRIVATE);
                        sharedPreferences.edit()
                            .putString("token", serverToken)
                            .commit();
                        onSuccess.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Response failed :(
                String s = new String(responseBody);
                Log.e(TAG, s);
                if (onError != null) {
                    onError.execute();
                }
            }

            @Override
            public void onRetry() {
                // Request was retried;
                Log.d(TAG, "retry");
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                // Progress notification
            }

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
                Log.d(TAG, "success");
            }
        };

        if (username != null && code != null) {
            params.put("username", username);
            params.put("token", code);
            params.put("did", this.UUID);

            String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.login_url);
            client.post(url, params, responseHandler);
        } else {
            Log.e(TAG, "Params error");
        }
    }

    public void getMessages(final Command onSuccess, final Command onError) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // Initiated the request
                Log.d(TAG, "Start");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody);
                Log.d(TAG, s);
                // Save token
                token = null; // ...
                if (onSuccess != null) {
                    onSuccess.execute();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Response failed :(
                String s = new String(responseBody);
                Log.e(TAG, s);
                if (onError != null) {
                    onError.execute();
                }
            }

            @Override
            public void onRetry() {
                // Request was retried;
                Log.d(TAG, "retry");
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                // Progress notification
            }

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
                Log.d(TAG, "success");
            }
        };

        // TODO get messages
        if (true) {
            params.put("token", this.token);

            String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.messages_url);
            client.post(url, params, responseHandler);
        } else {
            Log.e(TAG, "Params error");
        }
    }
}
