package com.example.android.himusic;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class SelectedSongsFragment extends Fragment {

    public SelectedSongsFragment() { }
    private final String TAG="SelectedSongsFragment";

    DatabaseHandler db;
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicService;
    private SongAdapter songAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: SelectedSongsFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: SelectedSongsFragment");
        View viewSelectedSongFragment= inflater.inflate(R.layout.fragment_selected_songs, container, false);
 MusicSharedPref.setContext(getContext());
        songView=viewSelectedSongFragment.findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
         db=new DatabaseHandler(getActivity());
         if(MusicSharedPref.getTableName().isEmpty()==false)
         {
             songList=db.getAllSongs(MusicSharedPref.getTableName());
             SongAdapter songAdapter=new SongAdapter(getActivity(),songList);
             songView.setAdapter(songAdapter);
         }

        return viewSelectedSongFragment ;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: SelectedSongsFragment");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: SelectedSongsFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: SelectedSongsFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: SelectedSongsFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: SelectedSongsFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: SelectedSongsFragment");
        if(songList.size()<=0 &&MusicSharedPref.getTableName().isEmpty()==false)
        {
            Log.d(TAG, "onResume: SelectedSongsFragment songlist loaded here");
            songList=db.getAllSongs(MusicSharedPref.getTableName());
            SongAdapter songAdapter=new SongAdapter(getActivity(),songList);
            songView.setAdapter(songAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: SelectedSongsFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: SelectedSongsFragment");
    }
}