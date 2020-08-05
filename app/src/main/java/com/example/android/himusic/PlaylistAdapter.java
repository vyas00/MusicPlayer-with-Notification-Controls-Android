package com.example.android.himusic;


import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PlaylistAdapter extends BaseAdapter {
    private final String TAG="PlaylistAdapter";
    Context context;
DatabaseHandler db;
    private ArrayList<String> playlist;
    private LayoutInflater songInflate;

    public PlaylistAdapter(Context c, ArrayList<String> play){
        this.context=c;
        playlist=play;
        songInflate =LayoutInflater.from(c);
        db=new DatabaseHandler(c);
    }

    @Override
    public int getCount() {
        return playlist.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {

        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LinearLayout linearLayout = (LinearLayout) songInflate.inflate(R.layout.playlist_view, parent, false);

        final String currplay = playlist.get(position);

        TextView playlistView = (TextView)linearLayout.findViewById(R.id.tv_playlist);
        TextView songcount = (TextView)linearLayout.findViewById(R.id.tv_totalsongs);

        songcount.setText("Total songs: "+ Integer.toString(db.getSongsCount(currplay)));
        playlistView.setText(currplay);

        linearLayout.setTag(position);

        return linearLayout;

    }



}