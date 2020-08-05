package com.example.android.himusic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class SelectedSongsFragment extends Fragment {

    public SelectedSongsFragment() { }
    private final String TAG="SelectedSongsFragment";

    DatabaseHandler db;
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicService;



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
         songList=db.getAllSongs(MusicSharedPref.getTableName());

            if(((MainActivity)getActivity()).getInstanceOfService()!=null) {
                musicService=((MainActivity)getActivity()).getInstanceOfService();
              musicService.setList(songList);  }

        songView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                builderSingle.setIcon(R.drawable.logo_music);
                builderSingle.setTitle("Select your choice: ");

                final int position=pos;
                final Song clickedsong= songList.get(pos);
                final String songName= clickedsong.getTitle();
                final String songArtist=clickedsong.getArtist();
                final long songId=clickedsong.getID();
                final String imagepath=clickedsong.getData();

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);
                arrayAdapter.add("Schedule this song");
                arrayAdapter.add("Play Song");
                arrayAdapter.add("Delete From Playlist");

                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        LayoutInflater li = LayoutInflater.from(getActivity());
                        View promptsView = li.inflate(R.layout.time_dialogbox, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                        alertDialogBuilder.setView(promptsView);

                        final EditText userInput = (EditText) promptsView.findViewById(R.id.et_time_dialog);

                        if (strName.equals("Schedule this song")) {
                            alertDialogBuilder
                                    .setCancelable(false)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {

                                                    long timeAtButtonClick = 0;
                                                    if (userInput.getText().length() != 0) {
                                                        MusicSharedPref.setScheduleArtistName(songArtist);
                                                        MusicSharedPref.setScheduleSongName(songName);
                                                        MusicSharedPref.setScheduleLongId(songId);
                                                        MusicSharedPref.setScheduleImagePath(imagepath);

                                                        timeAtButtonClick = Long.parseLong(userInput.getText().toString());
                                                        Intent intent = new Intent(getActivity(), SongSchedulerBroadcastReceiver.class);
                                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

                                                        long currentTimeinMilliSec = System.currentTimeMillis();
                                                        long additionalTime = timeAtButtonClick * 60 * 1000;

                                                        alarmManager.set(AlarmManager.RTC_WAKEUP, currentTimeinMilliSec + additionalTime, pendingIntent);
                                                        Toast.makeText(getActivity(), songName + " has been scheduled for playing", Toast.LENGTH_LONG).show();
                                                        Log.d(TAG, "user has placed a song order ");


                                                    } else {
                                                        Toast.makeText(getActivity(), "Canot schedule the song, please enter the time and try again ", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            })
                                    .setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                        else if(strName.equals("Play Song")){
                            if(musicService.isPng())musicService.pause();
                            musicService.setSong(position);
                            musicService.songPlaying=true;
                            musicService.playSong();
                        }

                        else if(strName.equals("Delete From Playlist"))
                        {
                            db.deleteSong(songId, MusicSharedPref.getTableName());

                            Toast.makeText(getActivity(), songName+" removed", Toast.LENGTH_LONG).show();
                            songList=db.getAllSongs(MusicSharedPref.getTableName());
                            SongAdapter songAdapter=new SongAdapter(getActivity(),songList);
                            songView.setAdapter(songAdapter);
                        }
                    }
                });
                AlertDialog alert = builderSingle.create();
                alert.show();
                alert.getWindow().setLayout(900, 760);
                return true;
            }
        });

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
        if(MusicSharedPref.getTableName().isEmpty()==false)
        {
            Log.d(TAG, "onResume: SelectedSongsFragment songlist loaded here");
            songList=db.getAllSongs(MusicSharedPref.getTableName());
            SongAdapter songAdapter=new SongAdapter(getActivity(),songList);
            songView.setAdapter(songAdapter);
        }
        if(((MainActivity)getActivity()).getInstanceOfService()!=null && musicService==null) {
            musicService=((MainActivity)getActivity()).getInstanceOfService();
            musicService.setList(songList);  }
        if(musicService==null) Log.d(TAG, "onResume of SelectedSongFragment: music service is null");
        else Log.d(TAG, "onResume of SelectedSongFragment: Music service is not null");

        Log.d(TAG, "onResume: size of list in service  "+musicService.songs.size());
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