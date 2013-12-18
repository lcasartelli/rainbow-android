package com.plasticpanda.rainbow;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Luca Casartelli
 */

public class MainFragment extends ListFragment {

    private static final String TAG = MainFragment.class.getName();

    private static MainFragment sharedInstance;

    private ChatAdapter mAdapter;
    private final List<Message> messages = new ArrayList<Message>();
    private Activity context;

    private MainFragment() {
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

        this.context = getActivity();

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
                    return false;
                }
            });
        }

        getMessages();

        return rootView;
    }

    public static String getDateFormat(Date date) {
        String format = "HH:mm";
        SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat formatMonth = new SimpleDateFormat("MMM");
        SimpleDateFormat formatWeek = new SimpleDateFormat("w");
        SimpleDateFormat formatDay = new SimpleDateFormat("dd");
        Date now = new Date();
        if (formatYear.format(now).compareTo(formatYear.format(date)) != 0) {
            format = "d MMM yyyy";
        } else if (formatMonth.format(now).compareTo(formatMonth.format(date)) != 0) {
            format = "d MMM";
        } else if (formatWeek.format(now).compareTo(formatWeek.format(date)) != 0) {
            format = "E d MMM";
        } else if (formatDay.format(now).compareTo(formatDay.format(date)) != 0) {
            format = "E HH:mm";
        }

        return format;
    }

    private void sendMessage() {
        if (this.context != null && this.context.findViewById(R.id.editText) != null) {
            EditText messageView = (EditText) this.context.findViewById(R.id.editText);
            if (messageView.getText() != null) {
                String message = messageView.getText().toString();

                RainbowHelper db = RainbowHelper.getInstance(this.context);
                db.sendMessage(message, new SimpleListener() {
                    @Override
                    public void onSuccess() {
                        getMessages();
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(
                            context,
                            context.getString(R.string.send_message_error),
                            Toast.LENGTH_LONG)
                            .show();
                    }
                });
            }
        }
    }

    // DEBUG
    public void getMessages() {
        RainbowHelper db = RainbowHelper.getInstance(this.context);
        db.getMessages(new MessagesListener() {
            @Override
            public void onSuccess(List<Message> messages) {
                List<Message> compressed = RainbowHelper.compressMessages(messages);
                refreshAdapter(compressed);
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onError() {
                Toast.makeText(
                    context,
                    context.getString(R.string.messages_error),
                    Toast.LENGTH_LONG)
                    .show();
            }
        });
    }

    public synchronized void refreshAdapter(List<Message> messages) {
        this.messages.clear();
        this.messages.addAll(RainbowHelper.compressMessages(messages));
        mAdapter.notifyDataSetChanged();
    }


    class ChatAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<Message> list;
        private static final int cellID = R.layout.list_item;

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
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(cellID, null);
            }

            if ((convertView != null) &&
                (convertView.findViewById(R.id.author) != null) &&
                (convertView.findViewById(R.id.message) != null) &&
                (convertView.findViewById(R.id.message_time) != null)) {

                TextView authorView = (TextView) convertView.findViewById(R.id.author);
                TextView messageView = (TextView) convertView.findViewById(R.id.message);
                TextView messageTimeView = (TextView) convertView.findViewById(R.id.message_time);
                Message message = this.list.get(position);
                authorView.setText(message.getAuthor());
                messageView.setText(message.getMessage());
                SimpleDateFormat format = new SimpleDateFormat(getDateFormat(message.getDate()));
                String date = format.format(message.getDate());
                messageTimeView.setText(date);
            }

            return convertView;
        }
    }
}
