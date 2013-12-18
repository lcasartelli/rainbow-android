package com.plasticpanda.rainbow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
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

    private Activity context;
    private SharedPreferences sharedPreferences;
    private String token;
    private String user;
    private String UUID;
    private Dao<Message, String> dao;

    private static RainbowHelper sharedInstance;

    /**
     * @param context application context
     */
    private RainbowHelper(Activity context) {
        this.context = context;
        if (this.context != null) {
            this.UUID = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
            this.sharedPreferences = this.context.getSharedPreferences("rainbow", Context.MODE_PRIVATE);
            this.token = this.sharedPreferences.getString("token", null);
            this.user = this.sharedPreferences.getString("user", null);
            try {
                this.dao = DatabaseHelper.getInstance(this.context).getDao();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Try PUBNUB
            pubNub();

        } else {
            Log.e(TAG, "Context null");
        }

    }

    /** Singleton pattern
     *
     * @param context application context
     * @return instance
     */
    public synchronized static RainbowHelper getInstance(Activity context) {
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

    private void pubNub() {
        Pubnub pn = new Pubnub(
            this.context.getString(R.string.PN_publishKey),
            this.context.getString(R.string.PN_subscribeKey)
        );
        String pn_channel = context.getString(R.string.PN_channel);

        try {
            pn.subscribe(pn_channel, new Callback() {
                @Override
                public void connectCallback(String channel, Object message) {
                    Log.d(TAG, "SUBSCRIBE : CONNECT on channel:" + channel
                        + " : " + message.getClass() + " : "
                        + message.toString());
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    Log.d(TAG, "SUBSCRIBE : DISCONNECT on channel:" + channel
                        + " : " + message.getClass() + " : "
                        + message.toString());
                }

                public void reconnectCallback(String channel, Object message) {
                    Log.d(TAG, "SUBSCRIBE : RECONNECT on channel:" + channel
                        + " : " + message.getClass() + " : "
                        + message.toString());
                }

                @Override
                public void successCallback(String channel, final Object data) {

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject obj = new JSONObject(data.toString());
                                Message msg = getMessage(obj);
                                dao.create(msg);
                                MainFragment.getInstance().refreshAdapter();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d(TAG, "SUBSCRIBE : ERROR on channel " + channel
                        + " : " + error.toString());
                }
            }
            );
        } catch (PubnubException e) {
            Log.d(TAG, e.toString());
        }
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
                    messages = parseMessages(resp);
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

    private static String getMessageContent(Message lastQueued, Message message) {
        String txt;
        if (lastQueued == null) {
            if (message.isEncrypted()) {
                txt = SecurityUtils.decrypt(message.getMessage());
            } else {
                txt = message.getMessage();
            }
        } else {
            if (lastQueued.isEncrypted()) {
                txt = SecurityUtils.decrypt(lastQueued.getMessage()) + "\n" + SecurityUtils.decrypt(message.getMessage());
            } else {
                txt = lastQueued.getMessage() + "\n" + message.getMessage();
            }
        }
        return txt;
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
                String messageContent = getMessageContent(last, queue.get(i));
                last.setMessage(messageContent);
            } else {
                Message first = queue.get(i);
                String messageContent = getMessageContent(null, queue.get(i));
                first.setMessage(messageContent);
                data.add(first);
            }
        }
        return data;
    }

    private static Message getMessage(JSONObject json) throws JSONException {
        Message message;
        String from = json.getString("from");
        boolean enc = json.getBoolean("enc");
        String messageContent = json.getString("message");
        long timestamp = json.getLong("timestamp");
        String _id = from + "-" + timestamp;
        Date date = new Date(timestamp);
        message = new Message(_id, from, messageContent, date, enc, false);
        return message;
    }

    /**
     * @param data JSON data
     * @return messages
     */
    private List<Message> parseMessages(JSONObject data) {
        List<Message> messages = new ArrayList<Message>();
        // Parse messages
        try {
            JSONArray jsonMessages = data.getJSONArray("messages");

            for (int i = 0; i < jsonMessages.length(); ++i) {
                JSONObject msg = jsonMessages.getJSONObject(i);
                Message message = getMessage(msg);
                try {
                    // Storing message
                    if (!this.dao.idExists((message.getMessageID()))) {
                        this.dao.create(message);
                    } else {
                        Log.i(TAG, "Message already exists: " + message.toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                messages.add(message);
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

    public List<Message> getMessageFromDb() throws SQLException {
        List<Message> messages = this.dao.queryForAll();
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

        String timestamp = String.valueOf(new Date().getTime());
        String messageID = this.user + "-" + timestamp;
        String cryptedMessage = SecurityUtils.encrypt(message);
        final Message msg = new Message(messageID, this.user, cryptedMessage, new Date(), true, true);

        // Storing message
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatabaseHelper.getInstance(context).getDao().create(msg);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        params.put("token", this.token);
        params.put("from", msg.getAuthor());
        params.put("did", "" + this.UUID);
        params.put("timestamp", String.valueOf(msg.getDate().getTime()));
        params.put("message", msg.getMessage());
        params.put("enc", "1");

        String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.messages_url);
        client.post(url, params, responseHandler);
    }
}
