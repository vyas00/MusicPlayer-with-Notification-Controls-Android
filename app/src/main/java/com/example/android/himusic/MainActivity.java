package com.example.android.himusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

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
    private  MusicController controller;
    private boolean paused=false, playbackPaused=false;
    private  boolean firstTimePlay=false;


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

                 if(isMyMusicServiceRunning(MusicService.class)==false) {
                     playIntent = new Intent(getApplicationContext(), MusicService.class);
                     bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                     startService(playIntent);
                     /*LocalBroadcastManager.getInstance(this).registerReceiver(broadcastNotificationReceiver, new IntentFilter("TRACKS"));*/
                   /*  LocalBroadcastManager.getInstance(this).registerReceiver(broadcastBatteryReceiver, new IntentFilter("BATTERY_LOW"));*/

                 }


                setController();
    }

    private boolean isMyMusicServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


  /* private BroadcastReceiver broadcastBatteryReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
               String batteryLevel=intent.getExtras().getString("battery_low");
                pause();
                Toast.makeText(context,batteryLevel, Toast.LENGTH_LONG).show();
        }
    };
*/

/*
   private BroadcastReceiver broadcastNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");


            if(action.equals(musicService.ACTION_PRE)){
                Log.d(TAG, "pre song, notification");
                    playPrev();}
                else if(action.equals(musicService.ACTION_PLAY)){
                    if (isPlaying()){
                        Log.d(TAG, "pause song, notification");
                        pause();

                    } else {
                        start();
                        Log.d(TAG, "play song, notification");
                    }
                    }
                else if(action.equals(musicService.ACTION_NEXT)){
                Log.d(TAG, "next song, notification");
                    playNext(); }
            else if(action.equals(musicService.ACTION_DESTROY_SERVICE)) {
                Log.d(TAG, "onReceive: notification destroy");
                if (playIntent != null) stopService(playIntent);
                    musicService = null;
                try {
                    if (broadcastNotificationReceiver != null) {
                        unregisterReceiver(broadcastNotificationReceiver);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                try {
                    if (broadcastBatteryReceiver != null) {
                        unregisterReceiver(broadcastBatteryReceiver);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }            }
        };
*/


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
                musicBound=false;
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    public void songPicked(View view){

      /*  musicService.ControllerShow(controller);*/
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.songPlaying=true;
        musicService.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        firstTimePlay=true;
        Log.d(TAG, "songPicked: controller show called" + isFinishing());
        if(!isFinishing())
        controller.show();
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
        if(musicBound==false) {
            Log.d(TAG, "onStart: service binded again");
          if(playIntent!=null)  bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE); }
/*        if(isMyMusicServiceRunning(MusicService.class)==false) {
            playIntent = new Intent(getApplicationContext(), MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent); }*/
    }

        @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume invoked");
            if(paused){
                setController();
                paused=false;
             /*  if(controller!=null) musicService.ControllerShow(controller);*/

            }
      /*      controller.setEnabled(true);*/
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

        Log.d(TAG,"onStop invoked");
        controller.hide();
        controller.setEnabled(false);
        if(musicBound && musicService!=null) {unbindService(musicConnection); musicBound=false;}
        Log.d(TAG, "onStop: service unbinded here ");
        super.onStop();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart invoked");


    }
    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy invoked");
        super.onDestroy();

        if(isPlaying()==false && firstTimePlay==false)
        {
            if (playIntent != null) stopService(playIntent);
                 musicService = null;
/*            try {
                if (broadcastNotificationReceiver != null) {
                    LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastNotificationReceiver);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }*/
/*            try {
                if (broadcastBatteryReceiver != null) {

                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }*/
        }
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
        musicService.songPlaying=true;
        musicService.startNotification();
    }


    private void playPrev(){
        musicService.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        musicService.songPlaying=true;
        musicService.startNotification();
    }


    @Override
    public void start() {
        musicService.go();
        musicService.songPlaying=true;
        musicService.startNotification();
        setController();
    }


    @Override
    public void pause() {
        musicService.songPlaying=false;
        musicService.pausePlayer();
        musicService.startNotification();
        setController();
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
        {musicService.songPlaying=true;
            return musicService.isPng();
        }
      else  return false;
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

/*    @Override
    public void onBackPressed() {
        // I cannot call addToBackstack method here as there is no previous activity here
        //moveTaskToBack makes the current activity moves to the background without being destroyed
        // I implemented this so that when the user presses back button the current activity is not destroyed
        // Also I read the official android documentation page which says that "Move the task containing this activity to the back of the activity stack.....
        // ..The activity's order within the task is unchanged"
          *//*  moveTaskToBack(true);*//*
    }*/

}