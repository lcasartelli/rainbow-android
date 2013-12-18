package com.plasticpanda.rainbow;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Luca Casartelli
 */


public class RainbowHelper {

    private static final String TAG = RainbowHelper.class.getName();

    private Context context;
    private SharedPreferences sharedPreferences;
    private String token;
    private String user;
    private String UUID;

    private static RainbowHelper sharedInstance;

    /**
     * @param context application context
     */
    private RainbowHelper(Context context) {
        this.context = context;
        if (this.context != null) {
            this.UUID = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
            this.sharedPreferences = this.context.getSharedPreferences("rainbow", Context.MODE_PRIVATE);
            this.token = this.sharedPreferences.getString("token", null);
            this.user = this.sharedPreferences.getString("user", null);
        } else {
            Log.e(TAG, "Context null");
        }
    }

    /** Singleton pattern
     *
     * @param context application context
     * @return instance
     */
    public synchronized static RainbowHelper getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new RainbowHelper(context);
        }
        return sharedInstance;
    }

    private void saveToken(String t) {
        this.sharedPreferences.edit()
            .putString("token", t)
            .commit();
        this.token = t;
    }

    private void saveUser(String u) {
        this.sharedPreferences.edit()
            .putString("user", u)
            .commit();
        this.user = u;
    }

    /**
     *
     * @param username username
     * @param code Authy code
     * @param loginListener login listener
     */
    public void performLogin(String username, String code, final SimpleListener loginListener) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        RequestParams params = new RequestParams();


        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // Initiated the request
                Log.d(TAG, "Start login");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody);
                Log.d(TAG, s);
                if (loginListener != null) {
                    try {
                        JSONObject resp = new JSONObject(s);
                        String serverToken = resp.getString("token");
                        saveToken(serverToken);
                        saveUser(user);
                        loginListener.onSuccess();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (loginListener != null) {
                    loginListener.onError();
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
            this.user = username;

            String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.login_url);
            client.post(url, params, responseHandler);
        } else {
            Log.e(TAG, "Params error");
        }
    }

    public void getMessages(final MessagesListener messagesListener) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        RequestParams params = new RequestParams();

        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // Initiated the request
                Log.d(TAG, "Start getting messages");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody);
                List<Message> messages = null;
                try {
                    JSONObject resp = new JSONObject(s);
                    messages = RainbowHelper.parseMessages(resp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (messagesListener != null) {
                    messagesListener.onSuccess(messages);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Response failed
                Log.e(TAG, "" + statusCode);
                if (messagesListener != null) {
                    messagesListener.onError();
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

        String limit = "500";
        params.put("token", this.token);
        params.put("since", "0");
        params.put("from", this.user);
        params.put("did", "" + this.UUID);
        params.put("limit", limit);

        String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.messages_url);
        client.get(url, params, responseHandler);
    }

    /**
     * @param queue message
     * @return compressed list
     */
    public static List<Message> compressMessages(List<Message> queue) {
        List<Message> data = new ArrayList<Message>();
        for (int i = 0; i < queue.size(); ++i) {
            if ((i > 0) &&
                (data.get(data.size() - 1).getAuthor().compareTo(queue.get(i).getAuthor()) == 0)) {
                Message last = data.get(data.size() - 1);
                String message;
                if (last.isEncrypted()) {
                    message = last.getClearMessage() + "\n" + queue.get(i).getClearMessage();
                } else {
                    message = last.getMessage() + "\n" + queue.get(i).getMessage();
                }

                last.setMessage(message);
            } else {
                Message first = queue.get(i);
                if (first.isEncrypted()) {
                    first.setMessage(first.getClearMessage());
                } else {
                    first.setMessage(first.getMessage());
                }
                data.add(first);
            }
        }
        return data;
    }

    /**
     * @param data JSON data
     * @return messages
     */
    private static List<Message> parseMessages(JSONObject data) {
        List<Message> messages = new ArrayList<Message>();
        // Parse messages
        try {
            JSONArray jsonMessages = data.getJSONArray("messages");

            for (int i = 0; i < jsonMessages.length(); ++i) {
                JSONObject msg = jsonMessages.getJSONObject(i);
                String _id = msg.getString("_id");
                String from = msg.getString("from");
                boolean enc = msg.getBoolean("enc");
                String message = msg.getString("message");
                long timestamp = msg.getLong("timestamp");
                Date date = new Date(timestamp);
                messages.add(new Message(_id, from, message, date, enc));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Sort by timestamp
        Comparator<Message> comparator = new Comparator<Message>() {
            public int compare(Message c1, Message c2) {
                return c1.getDate().compareTo(c2.getDate());
            }
        };

        Collections.sort(messages, comparator);

        return messages;
    }

    public void sendMessage(String message, final SimpleListener messageListener) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        RequestParams params = new RequestParams();

        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.d(TAG, "Start sending message...");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (messageListener != null) {
                    messageListener.onSuccess();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "" + statusCode);
                if (messageListener != null) {
                    messageListener.onError();
                }
            }

            @Override
            public void onRetry() {
                Log.d(TAG, "retry");
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                // Progress notification
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "End send message request");
            }
        };

        params.put("token", this.token);
        params.put("from", this.user);
        params.put("did", "" + this.UUID);
        params.put("timestamp", String.valueOf(new Date().getTime()));
        params.put("message", SecurityUtils.encrypt(message));
        params.put("enc", "1");

        String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.messages_url);
        client.post(url, params, responseHandler);
    }
}
