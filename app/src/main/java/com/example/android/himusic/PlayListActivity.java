package com.example.android.himusic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

public class PlayListActivity extends AppCompatActivity {

private final String TAG="PlayListActivity";
    private ArrayList<Song> songList =new ArrayList<Song>();
    private ListView songListView;
    DatabaseHandler db;
    private SongAdapter songAdapter;
private int playListSongsCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

          db=new DatabaseHandler(PlayListActivity.this);
          songListView=findViewById(R.id.playlistsong_list);


           songList=db.getAllSongs();
           playListSongsCount=db.getSongsCount();
           songAdapter=new SongAdapter(PlayListActivity.this,songList);
        songListView.setAdapter(songAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        songAdapter.notifyDataSetChanged();
    }

    public void songPicked(View view){
        if(db.getSongsCount()!=playListSongsCount){
            songList=db.getAllSongs();
        songAdapter=new SongAdapter(PlayListActivity.this,songList);
        songListView.setAdapter(songAdapter);}

    }

}