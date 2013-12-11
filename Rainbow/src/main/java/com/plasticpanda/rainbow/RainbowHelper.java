package com.plasticpanda.rainbow;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class RainbowHelper {

    private static final String TAG = RainbowHelper.class.getName();

    private String token;
    private String UUID;
    private Context context;
    private static RainbowHelper sharedInstance;

    public RainbowHelper(Context context) {
        this.context = context;
        this.UUID = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);

        this.performLogin("ciao", "ciao");
    }

    public synchronized static RainbowHelper getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new RainbowHelper(context);
        }
        return sharedInstance;
    }

    public void performLogin(String username, String code) {
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("username", "luca");
        params.put("token", "00000");
        params.put("did", this.UUID);

        client.post("http://fast.plasticpanda.com:23002/login", params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // Initiated the request
                Log.d(TAG, "Start");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Successfully got a response
                String s = new String(responseBody);
                Log.d(TAG, s);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Response failed :(
                String s = new String(responseBody);
                Log.e(TAG, s);
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
        });
    }

    public String getUUID() {
        return UUID;
    }
}
