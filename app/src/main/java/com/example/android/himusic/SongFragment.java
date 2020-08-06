package com.example.android.himusic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SongFragment extends Fragment {
    public SongFragment() {
    }

    private final String TAG = "SongFragment";
    private SongAdapter songAdapter;
    DatabaseHandler db;
    private ArrayList<Song> songList= new ArrayList<Song>();;
    private ListView songView;
    private MusicService musicService;
    private MainActivity instanceOfMainactivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: of SongFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewSongFragment = inflater.inflate(R.layout.fragment_song, container, false);
        Log.d(TAG, "onCreateView: of SongFragment");

        songView = viewSongFragment.findViewById(R.id.song_list);
        if(songList.size()==0) getSongList();
        db = new DatabaseHandler(getActivity());

        instanceOfMainactivity = (MainActivity) getActivity();
        if (instanceOfMainactivity.getInstanceOfService() != null) {
            musicService = instanceOfMainactivity.getInstanceOfService();
            musicService.setList(songList);
        }


        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        songAdapter = new SongAdapter(getActivity(), songList);
        songAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: from songfragment to play song");
                musicService.setSong(position);
                musicService.songPlaying = true;
                musicService.playSong();
            }
        });
        songView.setAdapter(songAdapter);

        songView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos, long id) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                builderSingle.setIcon(R.drawable.logo_music);
                builderSingle.setTitle("Select your choice: ");

                final int position = pos;
                final Song clickedsong = songList.get(pos);
                final String songName = clickedsong.getTitle();
                final String songArtist = clickedsong.getArtist();
                final long songId = clickedsong.getID();
                final String imagepath = clickedsong.getData();

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item);
                arrayAdapter.add("Schedule this song");
                /*arrayAdapter.add("Play Song");*/
                arrayAdapter.add("Add to PlayList");

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
                                                        MusicSharedPref.setScheduledTime(currentTimeinMilliSec + additionalTime);

                                                        alarmManager.set(AlarmManager.RTC_WAKEUP, currentTimeinMilliSec + additionalTime, pendingIntent);
                                                        Toast.makeText(getActivity(), songName + " has been scheduled for playing", Toast.LENGTH_LONG).show();
                                                        Log.d(TAG, "user has placed a song order ");


                                                    } else {
                                                        Toast.makeText(getActivity(), "Cannot schedule the song, please enter the time and try again ", Toast.LENGTH_LONG).show();
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
/*                        else if(strName.equals("Play Song")){
                            musicService.setSong(position);
                            musicService.songPlaying=true;
                            musicService.playSong();
                        }*/

                        else if (strName.equals("Add to PlayList")) {
                            AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                            builderSingle.setIcon(R.drawable.logo_music);
                            builderSingle.setTitle("PlayList: ");
                            ArrayList<String> playlist = new ArrayList<String>();
                            playlist = db.getPlaylistTables();
                            final ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice, playlist);

                            builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            builderSingle.setAdapter(stringArrayAdapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String tableName = stringArrayAdapter.getItem(which);
                                    db.addSong(clickedsong, tableName);
                                    Toast.makeText(getActivity(), songName + " added to " + tableName + " playlist", Toast.LENGTH_LONG).show();
                                }
                            });
                            builderSingle.show();

                        }
                    }
                });
                AlertDialog alert = builderSingle.create();
                alert.show();
                alert.getWindow().setLayout(900, 760);
                return true;
            }
        });


        return viewSongFragment;
    }

    public void getSongList() {
        ContentResolver musicResolver = getActivity().getApplicationContext().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int dataColumn=musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisData=musicCursor.getString(dataColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist,thisData));
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    public void onStart() {
        if (musicService == null && instanceOfMainactivity.getInstanceOfService() != null) {
            musicService = instanceOfMainactivity.getInstanceOfService();
            musicService.setList(songList);
        }
        if (musicService == null) Log.d(TAG, "onStart: music service is null");
        else Log.d(TAG, "onStart: Music service is not null");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: SongFragment");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: SongFragment");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: SongFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: SongFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: SongFragment");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: SongFragment");
    }

}