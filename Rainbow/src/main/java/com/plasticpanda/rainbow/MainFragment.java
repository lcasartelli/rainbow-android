package com.plasticpanda.rainbow;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends ListFragment {

    private static MainFragment sharedInstance;
    private BaseAdapter mAdapter;
    private ArrayList<String> values;
    private Activity activity;

    public MainFragment() {
    }

    public static synchronized MainFragment getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new MainFragment();
        }
        return sharedInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        this.values = new ArrayList<String>();
        this.activity = getActivity();

        // TODO: remove!
        this.values.add("");
        this.values.add("");
        this.values.add("");
        this.values.add("");
        this.values.add("");
        this.values.add("");
        this.values.add("");
        this.values.add("");
        this.values.add("");
        this.values.add("");

        if (this.activity != null) {
            mAdapter = new ChatArrayAdapter(this.activity, R.layout.list_item, values);
            setListAdapter(mAdapter);
        }
        return rootView;
    }

    class ChatArrayAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<String> list;
        private int cellID;

        public ChatArrayAdapter(Context context, int resource, List<String> list) {
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
        public String getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(cellID, null);
                // do something ...
            }

            return convertView;
        }
    }
}