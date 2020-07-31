package com.example.android.himusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

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

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate invoked");

        drawerLayout = (DrawerLayout)findViewById(R.id.activity_drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.Open, R.string.Close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.my_playlist:
                        Toast.makeText(MainActivity.this, "My Playlist",Toast.LENGTH_SHORT).show();break;
                    case R.id.scheduled_dongs:
                        Toast.makeText(MainActivity.this, "Scheduled songs",Toast.LENGTH_SHORT).show();break;
                    default:
                        return true;
                }
                return true;

            }
        });

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
                     Log.d(TAG, "onCreate: service created in oncreate ");
                     /*LocalBroadcastManager.getInstance(this).registerReceiver(broadcastNotificationReceiver, new IntentFilter("TRACKS"));*/
                   /*  LocalBroadcastManager.getInstance(this).registerReceiver(broadcastBatteryReceiver, new IntentFilter("BATTERY_LOW"));*/
                 }
                 else{firstTimePlay=true;}


                setController();

        songView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
                builderSingle.setIcon(R.drawable.logo_music);
                builderSingle.setTitle("Select your choice: ");

                final Song clickedsong= songList.get(pos);
                final String songName= clickedsong.getTitle();
                final String songArtist=clickedsong.getArtist();
                final long songId=clickedsong.getID();
                final String imagepath=clickedsong.getData();

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_item);
                arrayAdapter.add("Schedule this song");
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
                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View promptsView = li.inflate(R.layout.time_dialogbox, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

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
                                                        Intent intent = new Intent(MainActivity.this, SongSchedulerBroadcastReceiver.class);
                                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                                                        long currentTimeinMilliSec = System.currentTimeMillis();
                                                        long additionalTime = timeAtButtonClick * 60 * 1000;

                                                        alarmManager.set(AlarmManager.RTC_WAKEUP, currentTimeinMilliSec + additionalTime, pendingIntent);
                                                        Toast.makeText(MainActivity.this, songName + " has been scheduled for playing", Toast.LENGTH_LONG).show();
                                                        Log.d(TAG, "user has placed a song order ");
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Canot schedule the song, please enter the time and try again ", Toast.LENGTH_LONG).show();
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
                        else if(strName.equals("Add to PlayList")){
                            Toast.makeText(MainActivity.this, "yet to be added! ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                AlertDialog alert = builderSingle.create();
                alert.show();
                alert.getWindow().setLayout(900, 760);
                return true;
            }
        });


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
            case R.id.action_controller:
/*                stopService(playIntent);
                musicService =null;
                musicBound=false;
                finish();*/
if(isMyMusicServiceRunning(MusicService.class)) controller.show();break;
        }

        if(actionBarDrawerToggle.onOptionsItemSelected(item)) return true;
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
        Log.d(TAG, "songPicked: controller show called  " + isFinishing());
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
    protected void onStart() {

        super.onStart();
        Log.d(TAG, "onStart invoked");
        if(musicBound==false && isMyMusicServiceRunning(MusicService.class)) {
            playIntent = new Intent(getApplicationContext(), MusicService.class);
            Log.d(TAG, "onStart: service binded again");
            if(playIntent!=null)  bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
           }
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
         if(controller.isShowing())controller.hide();
        if(musicBound && musicService!=null) {unbindService(musicConnection); musicBound=false;}
        Log.d(TAG, "onStop: service unbinded here ");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart invoked");
    }

/*
    @Override
    public void onBackPressed() {
        finish();
    }
*/

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