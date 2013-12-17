package com.plasticpanda.rainbow;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Luca Casartelli
 */

// TODO scroll to bottom with keyboard up

public class MainFragment extends ListFragment {

    private static final String TAG = MainFragment.class.getName();

    private static MainFragment sharedInstance;

    private ChatAdapter mAdapter;
    private List<Message> messages;

    public MainFragment() {
        this.messages = new ArrayList<Message>();
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
        Activity activity = getActivity();

        if (activity != null) {
            this.mAdapter = new ChatAdapter(activity, R.layout.list_item, messages);
            setListAdapter(this.mAdapter);
        }

        RainbowHelper db = RainbowHelper.getInstance(this.getActivity());
        db.getMessages(new Command() {
            @Override
            public void execute(List<Message> messages) {
                List<Message> compressed = RainbowHelper.compressMessages(messages);
                refreshAdapter(compressed);
            }
        }, null);


        return rootView;
    }

    public synchronized void refreshAdapter(List<Message> messages) {
        this.messages.clear();
        this.messages.addAll(RainbowHelper.compressMessages(messages));
        Log.d(TAG, this.messages.toString());
        mAdapter.notifyDataSetChanged();
    }


    class ChatAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<Message> list;
        private int cellID;

        public ChatAdapter(Context context, int resource, List<Message> list) {
            this.cellID = resource;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.list = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Message getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
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
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String date = format.format(message.getDate());
                messageTimeView.setText(date);
            }

            return convertView;
        }
    }
}
