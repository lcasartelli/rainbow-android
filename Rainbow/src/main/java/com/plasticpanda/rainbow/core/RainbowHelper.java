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

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.db.DatabaseHelper;
import com.plasticpanda.rainbow.db.Message;
import com.plasticpanda.rainbow.utils.MessagesListener;
import com.plasticpanda.rainbow.utils.PNListener;
import com.plasticpanda.rainbow.utils.SecurityUtils;
import com.plasticpanda.rainbow.utils.SimpleListener;
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


public class RainbowHelper {

    private static final String TAG = RainbowHelper.class.getName();

    private Context context;
    private SharedPreferences sharedPreferences;
    private String token;
    private String user;
    private String UUID;
    private Dao<Message, String> dao;

    private static final String SHARED_TOKEN_KEY = "token";
    private static final String SHARED_USER_KEY = "user";

    private static final String JSON_FROM_KEY = "from";
    private static final String JSON_USER_KEY = "username";
    private static final String JSON_TOKEN_KEY = "token";
    private static final String JSON_MESSAGE_KEY = "message";
    private static final String JSON_TIMESTAMP_KEY = "timestamp";
    private static final String JSON_TYPE_KEY = "message_t";
    private static final String JSON_DID_KEY = "did";
    private static final String JSON_ENCRYPTION_KEY = "enc";
    private static final String JSON_LIMIT_KEY = "limit";
    private static final String JSON_SINCE_KEY = "since";
    private static final String JSON_MESSAGES_KEY = "messages";

    private static RainbowHelper sharedInstance;

    /**
     * @param context application context
     */
    private RainbowHelper(Context context) {
        this.context = context;
        if (this.context != null) {
            this.UUID = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);
            this.sharedPreferences = this.context.getSharedPreferences(RainbowConst.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            this.token = this.sharedPreferences.getString(SHARED_TOKEN_KEY, null);
            this.user = this.sharedPreferences.getString(SHARED_USER_KEY, null);
            try {
                this.dao = DatabaseHelper.getInstance(this.context).getMessagesDao();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //pubNub();

        } else {
            Log.e(TAG, "Context null");
        }

    }

    /**
     * Singleton pattern
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
            .putString(SHARED_TOKEN_KEY, t)
            .commit();
        this.token = t;
    }

    private void saveUser(String u) {
        this.sharedPreferences.edit()
            .putString(SHARED_USER_KEY, u)
            .commit();
        this.user = u;
    }

    public void pubNub(final PNListener listener) {
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject obj = new JSONObject(data.toString());
                                Message msg = getMessage(obj);
                                dao.create(msg);
                                listener.onReceiveMessage(msg);
                                //MainFragment.getInstance().refreshAdapter();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
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
     * @param username      username
     * @param code          authy code
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
                if (loginListener != null) {
                    try {
                        JSONObject resp = new JSONObject(s);
                        String serverToken = resp.getString(JSON_TOKEN_KEY);
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

            params.put(JSON_USER_KEY, username);
            params.put(JSON_TOKEN_KEY, code);
            params.put(JSON_DID_KEY, this.UUID);
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
        params.put(JSON_TOKEN_KEY, this.token);
        params.put(JSON_SINCE_KEY, "0");
        params.put(JSON_FROM_KEY, this.user);
        params.put(JSON_DID_KEY, "" + this.UUID);
        params.put(JSON_LIMIT_KEY, limit);

        String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.messages_url);
        client.get(url, params, responseHandler);
    }

    private static String concatMessageContent(Message lastQueued, Message message) {
        String txt;
        if (lastQueued == null) {
            txt = message.getMessage();
        } else {
            txt = lastQueued.getMessage() + "\n" + message.getMessage();
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
            queue.get(i).setMessage(SecurityUtils.decrypt(queue.get(i).getMessage()));
            if (i > 0 &&
                (data.get(data.size() - 1).getAuthor().compareTo(queue.get(i).getAuthor()) == 0) &&
                !(
                    data.get(data.size() - 1).getMessage().matches("^(http|https)(://).+(.png|.jpeg|.jpg)(/)*$") ||
                        queue.get(i).getMessage().matches("^(http|https)(://).+(.png|.jpeg|.jpg)(/)*$")
                )) {
                Message last = data.get(data.size() - 1);
                String messageContent = concatMessageContent(last, queue.get(i));
                last.setMessage(messageContent);
            } else {
                Message first = queue.get(i);
                String messageContent = concatMessageContent(null, queue.get(i));
                first.setMessage(messageContent);
                data.add(first);
            }
        }
        return data;
    }

    private static Message getMessage(JSONObject json) throws JSONException {
        Message message;
        String from = json.getString(JSON_FROM_KEY);
        boolean enc = json.getBoolean(JSON_ENCRYPTION_KEY);
        String messageContent = json.getString(JSON_MESSAGE_KEY);
        long timestamp = json.getLong(JSON_TIMESTAMP_KEY);
        String _id = from + "-" + timestamp;
        Date date = new Date(timestamp);
        char type;
        try {
            type = json.getString(JSON_TYPE_KEY).charAt(0);
        } catch (JSONException e) {
            type = Message.TEXT_MESSAGE;
        }
        message = new Message(_id, from, messageContent, date, enc, false, type);
        return message;
    }

    private void downloadAttachment(Message msg) {
        ImagesHelper.getInstance(context).retrieveImage(msg);
    }

    /**
     * @param data JSON data
     * @return messages
     */
    private List<Message> parseMessages(JSONObject data) {
        List<Message> messages = new ArrayList<Message>();
        // Parse messages
        JSONArray jsonMessages;
        try {
            jsonMessages = data.getJSONArray(JSON_MESSAGES_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
            jsonMessages = new JSONArray();
        }

        for (int i = 0; i < jsonMessages.length(); ++i) {
            try {
                JSONObject msg = jsonMessages.getJSONObject(i);
                Message message = getMessage(msg);
                // Storing message
                if (!this.dao.idExists((message.getMessageID()))) {
                    this.dao.create(message);
                } else {
                    Log.i(TAG, "Message already exists: " + message.toString());
                }

                String msg_decrypted;
                if (message.isEncrypted()) {
                    msg_decrypted = SecurityUtils.decrypt(message.getMessage());
                } else {
                    msg_decrypted = message.getMessage();
                }

                // download attachment
                if (
                    message.getType() == Message.IMAGE_MESSAGE ||
                        msg_decrypted.matches("^(http|https)(://).+(.png|.jpeg|.jpg)(/)*$")
                    ) {
                    Log.d(TAG, "download attachment");
                    downloadAttachment(message);
                }

                messages.add(message);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatabaseHelper.getInstance(context).getMessagesDao().create(msg);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        params.put(JSON_TOKEN_KEY, this.token);
        params.put(JSON_FROM_KEY, msg.getAuthor());
        params.put(JSON_DID_KEY, "" + this.UUID);
        params.put(JSON_TIMESTAMP_KEY, timestamp);
        params.put(JSON_MESSAGE_KEY, msg.getMessage());
        params.put(JSON_ENCRYPTION_KEY, "1");
        // DEBUG
        params.put(JSON_TYPE_KEY, "" + Message.TEXT_MESSAGE);

        String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.messages_url);
        client.post(url, params, responseHandler);
    }

    /*public void uploadImage() {
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
                if (responseBody != null) {
                    Log.i(TAG, new String(responseBody));
                }

                try {
                    JSONObject json = new JSONObject(responseBody.toString());
                    String url = json.getString("url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Upload file error: " + statusCode);
                //TODO: onFailure upload
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
                Log.d(TAG, "Finish upload request");
            }
        };

        File file = null;
        if (context.getCacheDir() != null) {
            file = new File(context.getCacheDir(), "Rainbow").listFiles()[1];
        }

        try {
            params.put("token", this.token);
            params.put(JSON_FROM_KEY, "luca");
            params.put("did", "" + this.UUID);
            params.put("filename", "test.jpeg");
            params.put("upload", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String url = this.context.getString(R.string.rainbow_base_url) + this.context.getString(R.string.attachment_url);
        client.post(url, params, responseHandler);
    }*/
}
