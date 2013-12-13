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
import java.util.Date;
import java.util.List;

/**
 * @author Luca Casartelli
 */

// TODO scroll to bottom with keyboard up

public class MainFragment extends ListFragment {

    private static final String TAG = MainFragment.class.getName();

    private static MainFragment sharedInstance;

    public MainFragment() {
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
        ArrayList<Message> values = new ArrayList<Message>();
        Activity activity = getActivity();

        //TODO: remove debug queue
        ArrayList<Message> queue = new ArrayList<Message>();
        queue.add(new Message("0", "Hermione", "Ciao Ron", new Date()));
        queue.add(new Message("0", "Hermione", "Come stai?", new Date()));
        queue.add(new Message("0", "Ron", "Ciao Hermione!", new Date()));
        queue.add(new Message("0", "Ron", "Bene, tu?", new Date()));
        queue.add(new Message("0", "Hermione", "Tutto bene", new Date()));
        queue.add(new Message("0", "Hermione", "Grazie!", new Date()));
        queue.add(new Message("0", "Hermione", ":)", new Date()));
        queue.add(new Message("0", "Ron", ":)", new Date()));


        for (int i = 0; i < queue.size(); ++i) {
            Log.d(TAG, queue.get(i).getMessage());
            if ((i > 0) &&
                (values.get(values.size() - 1).getAuthor().compareTo(queue.get(i).getAuthor()) == 0)) {
                Message last = values.get(values.size() - 1);
                String message = last.getMessage() + "\n" + queue.get(i).getMessage();
                last.setMessage(message);
            } else {
                values.add(queue.get(i));
            }
        }

        Log.d(TAG, values.toString());


        if (activity != null) {
            BaseAdapter mAdapter = new ChatAdapter(activity, R.layout.list_item, values);
            setListAdapter(mAdapter);
        }
        return rootView;
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
