package com.example.android.himusic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private final String TAG="MainActivity";


    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;
    private static MusicController controller;
    private boolean paused=false, playbackPaused=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate invoked");

        songView=findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

          getSongList();

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);


        setController();
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS"));

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");


            if(action.equals(musicService.ACTION_PRE)){
                    playPrev();}
                else if(action.equals(musicService.ACTION_PLAY)){
                    if (isPlaying()){
                        pause();
                    } else {
                        start();
                    }
                    }
                else if(action.equals(musicService.ACTION_NEXT)){
                    playNext(); }
            }
        };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end:
                stopService(playIntent);
                musicService =null;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    public void songPicked(View view){
        musicService.ControllerShow(controller);
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;

            musicService = binder.getService();
            musicService.setList(songList);
            musicBound = true;
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);


            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart invoked");

        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

    }
        @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume invoked");
            if(paused){
                setController();
                paused=false;
               if(controller!=null) musicService.ControllerShow(controller);

            }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause invoked");
        paused=true;
        playbackPaused=true;
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop invoked");
        controller.hide();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart invoked");


    }
    @Override
    protected void onDestroy() {

        Log.d(TAG,"onDestroy invoked");
        if (musicBound) unbindService(musicConnection);
        stopService(playIntent);
        musicService =null;
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void setController(){
        if(controller==null) controller = new MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);

    }


    private void playNext(){
        musicService.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
        musicService.songPlaying=true;
        musicService.startNotification();
    }


    private void playPrev(){
        musicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
        musicService.songPlaying=true;
        musicService.startNotification();
    }


    @Override
    public void start() {
        musicService.songPlaying=true;
        musicService.startNotification();
        musicService.go();
    }


    @Override
    public void pause() {
        musicService.songPlaying=false;
        musicService.startNotification();
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicService!=null && musicBound && musicService.isPng())
        return musicService.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService!=null && musicBound && musicService.isPng())
        return musicService.getPosn();
  else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService!=null && musicBound)
        return musicService.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onBackPressed() {
        // I cannot call addToBackstack method here as there is no previous activity here
        //moveTaskToBack makes the current activity moves to the background without being destroyed
        // I implemented this so that when the user presses back button the current activity is not destroyed
        // Also I read the official android documentation page which says that "Move the task containing this activity to the back of the activity stack.....
        // ..The activity's order within the task is unchanged"
            moveTaskToBack(true);
    }

}