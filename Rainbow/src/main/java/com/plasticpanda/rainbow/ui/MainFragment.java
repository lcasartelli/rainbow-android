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

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.core.ImagesHelper;
import com.plasticpanda.rainbow.core.MainActivity;
import com.plasticpanda.rainbow.core.RainbowHelper;
import com.plasticpanda.rainbow.db.Message;
import com.plasticpanda.rainbow.utils.BackListener;
import com.plasticpanda.rainbow.utils.ImageListener;
import com.plasticpanda.rainbow.utils.MessagesListener;
import com.plasticpanda.rainbow.utils.SimpleListener;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainFragment extends ListFragment {

    private static final String TAG = MainFragment.class.getName();

    private static MainFragment sharedInstance;

    private ChatAdapter mAdapter;
    private final List<Message> messages = new ArrayList<Message>();
    private MainActivity context;
    private GalleryHelper galleryHelper;

    public MainFragment() {
        super();
    }

    public static synchronized MainFragment getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new MainFragment();
        }
        return sharedInstance;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        this.context = (MainActivity) getActivity();
        this.galleryHelper = GalleryHelper.getInstance(this.context);

        if (this.context != null) {
            this.mAdapter = new ChatAdapter(this.context, messages);
            setListAdapter(this.mAdapter);
        }

        EditText messageView;
        if (rootView != null) {
            messageView = (EditText) rootView.findViewById(R.id.editText);
            messageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_SEND) {
                        sendMessage();
                    }
                    return true;
                }
            });
        }

        // get messages from database
        refreshAdapter();
        // get messages from remote database
        getMessages();

        this.context.setDefaultBackAction(true);
        this.context.setBackListener(new BackListener() {
            @Override
            public void goBack() {
                galleryHelper.popImageView();
            }
        });

        LocalBroadcastManager.getInstance(this.context).registerReceiver(this.broadcastReceiver, new IntentFilter("new-message"));

        return rootView;
    }

    private static String getDateFormat(Date date) {
        String format;
        Date now = new Date();
        SimpleDateFormat formatYear = new SimpleDateFormat("yyyy"),
            formatMonth = new SimpleDateFormat("MMM"),
            formatWeek = new SimpleDateFormat("w"),
            formatDay = new SimpleDateFormat("dd");

        if (formatYear.format(now).compareTo(formatYear.format(date)) != 0) {
            format = "d MMM yyyy";
        } else if (formatMonth.format(now).compareTo(formatMonth.format(date)) != 0) {
            format = "d MMM";
        } else if (formatWeek.format(now).compareTo(formatWeek.format(date)) != 0) {
            format = "E d MMM";
        } else if (formatDay.format(now).compareTo(formatDay.format(date)) != 0) {
            format = "E HH:mm";
        } else {
            format = "HH:mm";
        }

        return format;
    }

    private void sendMessage() {
        if (this.context != null && this.context.findViewById(R.id.editText) != null) {
            EditText messageView = (EditText) this.context.findViewById(R.id.editText);
            if (messageView.getText() != null && messageView.getText().toString().length() > 0) {
                final String message = messageView.getText().toString();

                messageView.setText("".toCharArray(), 0, 0);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RainbowHelper.getInstance(context).sendMessage(message, new SimpleListener() {
                            @Override
                            public void onSuccess() {
                                refreshAdapter();
                            }

                            @Override
                            public void onError() {
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.send_message_error),
                                            Toast.LENGTH_LONG)
                                            .show();
                                    }
                                });
                            }
                        });
                    }
                }).start();
            }
        }
    }

    // DEBUG
    void getMessages() {
        final RainbowHelper db = RainbowHelper.getInstance(this.context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getMessages(new MessagesListener() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        refreshAdapter();
                    }

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {

                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.messages_error),
                                    Toast.LENGTH_LONG)
                                    .show();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    public synchronized void refreshAdapter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Message> data = RainbowHelper.getInstance(context).getMessageFromDb();
                    List<Message> compressed = RainbowHelper.compressMessages(data);
                    messages.clear();
                    messages.addAll(compressed);
                    Log.i(TAG, "refreshed");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshAdapter();
        }
    };


    class ChatAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<Message> list;

        public ChatAdapter(Context context, List<Message> list) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Message getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // use timestamp
            return list.get(position).getDate().getTime();
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = list.get(position);
            int type = 0;
            if (message.getType() == Message.IMAGE_MESSAGE || message.getMessage().matches("^(http|https)(://).+(.png|.jpeg|.jpg)(/)*$")) {
                type = 1;
            }
            return type;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = this.list.get(position);

            if (convertView == null) {
                if (message.getType() == Message.IMAGE_MESSAGE || message.getMessage().matches("^(http|https)(://).+(.png|.jpeg|.jpg)(/)*$")) {
                    convertView = mInflater.inflate(R.layout.list_item_img, null);
                } else {
                    convertView = mInflater.inflate(R.layout.list_item_text, null);
                }
            }

            if (message.getType() == Message.IMAGE_MESSAGE || message.getMessage().matches("^(http|https)(://).+(.png|.jpeg|.jpg)(/)*$")) {
                loadImageCell(message, convertView);
            } else {
                loadTextCell(message, convertView);
            }

            return convertView;
        }

        private void loadTextCell(Message message, View view) {
            if ((view != null) &&
                (view.findViewById(R.id.author_text) != null) &&
                (view.findViewById(R.id.message_text) != null) &&
                (view.findViewById(R.id.message_time_text) != null)) {

                TextView authorView = (TextView) view.findViewById(R.id.author_text);
                TextView messageView = (TextView) view.findViewById(R.id.message_text);
                TextView messageTimeView = (TextView) view.findViewById(R.id.message_time_text);

                loadBaseCell(message, authorView, messageTimeView);

                messageView.setText(message.getMessage());
            }
        }

        private void loadImageCell(final Message message, View view) {
            if ((view != null) &&
                (view.findViewById(R.id.author_img) != null) &&
                (view.findViewById(R.id.image_img) != null) &&
                (view.findViewById(R.id.message_time_img) != null)) {

                TextView authorView = (TextView) view.findViewById(R.id.author_img);
                // DEBUG
                final ImageView imageView = (ImageView) view.findViewById(R.id.image_img);
                TextView messageTimeView = (TextView) view.findViewById(R.id.message_time_img);
                loadBaseCell(message, authorView, messageTimeView);

                imageView.setImageBitmap(null);

                ImagesHelper imgHelper = ImagesHelper.getInstance(context);
                imgHelper.retrieveImageThumb(message, new ImageListener() {
                    @Override
                    public void onSuccess(final Bitmap b) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(b);
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        ImageFragment frag = new ImageFragment(context);
                                        frag.setMessage(message);

                                        galleryHelper.pushImageView(imageView, message);
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onSuccess() { /* Don't use! */ }

                    @Override
                    public void onError() {
                    }
                });
            }
        }

        private void loadBaseCell(Message message, TextView authorView, TextView messageTimeView) {
            // author
            authorView.setText(message.getAuthor());
            // date
            SimpleDateFormat format = new SimpleDateFormat(getDateFormat(message.getDate()));
            String date = format.format(message.getDate());
            messageTimeView.setText(date);
        }
    }
}
