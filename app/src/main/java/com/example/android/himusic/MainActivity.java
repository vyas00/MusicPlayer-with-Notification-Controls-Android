package com.example.android.himusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity  {

    private final String TAG="MainActivity";


    public MusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;
    private  MusicController controller;
    private boolean paused=false, playbackPaused=false;
    private  boolean firstTimePlay=false;
    DatabaseHandler db;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

   private TabLayout tabLayout;

   private  SongFragment songFragment;
   private  PlaylistFragment playlistFragment;
   private SelectedSongsFragment selectedSongsFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songFragment=new SongFragment();
        playlistFragment=new PlaylistFragment();
        selectedSongsFragment=new SelectedSongsFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.frame_container, songFragment).commit();
        Log.d(TAG,"onCreate invoked :");
        db=new DatabaseHandler(MainActivity.this);
        db.createPlaylistTable("LikedSongs");
MusicSharedPref.setContext(getApplicationContext());



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
                        LayoutInflater li = LayoutInflater.from(MainActivity.this);
                        View promptsView = li.inflate(R.layout.playlist_dialogbox, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setView(promptsView);
                        final EditText userInput = (EditText) promptsView.findViewById(R.id.et_playlist_dialog);
                        alertDialogBuilder
                                .setCancelable(false)
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                if (userInput.getText().length() != 0) {
                                                    db.createPlaylistTable(userInput.getText().toString());
                                                    Toast.makeText(MainActivity.this, userInput.getText().toString() + " playlist Created", Toast.LENGTH_LONG).show();


                                                } else {
                                                    Toast.makeText(MainActivity.this, "Unable to create Playlist, enter valid name ", Toast.LENGTH_LONG).show();
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
                        break;
                    case R.id.scheduled_dongs:
                        Toast.makeText(MainActivity.this, "Scheduled songs",Toast.LENGTH_SHORT).show();break;
                    default:
                        return true;
                }
                return true;

            }
        });

                 if(isMyMusicServiceRunning(MusicService.class)==false) {
                     playIntent = new Intent(getApplicationContext(), MusicService.class);
                  /*   bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);*/
                     startService(playIntent);
                     Log.d(TAG, "onCreate: service created in oncreate ");
                 }
                 else{firstTimePlay=true;}
           /*     setController();*/


        tabLayout = findViewById(R.id.music_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Card Songs").setIcon(R.drawable.ic_sdcardstorage));
        tabLayout.addTab(tabLayout.newTab().setText("Playlist").setIcon(R.drawable.ic_playlist_play));
        if(MusicSharedPref.getTableName().isEmpty()==false)
        {tabLayout.addTab(tabLayout.newTab().setText(MusicSharedPref.getTableName()).setIcon(R.drawable.ic_selected_playlist));}
        else{
            tabLayout.addTab(tabLayout.newTab().setText("Songs").setIcon(R.drawable.ic_selected_playlist));
        }

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos= tab.getPosition();
                if(pos==0) getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, songFragment).commit();
                if(pos==1)  getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, playlistFragment).commit();
                if(pos==2) getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, selectedSongsFragment).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
/*                int pos= tab.getPosition();
                if(pos==0)getSupportFragmentManager().beginTransaction().remove(songFragment).commit();
                if(pos==1)  getSupportFragmentManager().beginTransaction().remove(playlistFragment).commit();
                if(pos==2) getSupportFragmentManager().beginTransaction().add(R.id.frame_container, selectedSongsFragment).commit();*/
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
/*                int pos= tab.getPosition();
                if(pos==0)getSupportFragmentManager().beginTransaction().add(R.id.frame_container, songFragment).commit();
                if(pos==1)  getSupportFragmentManager().beginTransaction().add(R.id.frame_container, playlistFragment).commit();
                if(pos==2) getSupportFragmentManager().beginTransaction().add(R.id.frame_container, selectedSongsFragment).commit();*/

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

public MusicService getInstanceOfService()
{
    return musicService;
}

public Fragment getSongFragmentInstance()
{
    return  this.songFragment;
}
    public Fragment getPlaylistFragmentInstance()
    {
        return  this.playlistFragment;
    }

    public Fragment getselectedSongFragmentInstance()
    {
        return  this.selectedSongsFragment;
    }

    public void selectTabText(int position, String settext){
        tabLayout.getTabAt(position).setText(settext);
    }



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

/*    public void songPicked(View view){

        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.songPlaying=true;
        musicService.playSong();
        if(playbackPaused){
        *//*           setController();*//*
            playbackPaused=false;
        }
        Log.d(TAG, "songPicked: controller show called  " + isFinishing());
*//*          controller.show();*//*
    }*/

    public  void songPicked(View view)
    {
      if(musicService!=null)  Log.d(TAG, "songPicked: music service isnot null");
      else Log.d(TAG, "songPicked: music service is null");
    }

    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();
            musicBound = true;
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;

        }
    };



    @Override
    protected void onStart() {

        super.onStart();
        Log.d(TAG, "onStart invoked");
        if(isMyMusicServiceRunning(MusicService.class)==false){
            playIntent = new Intent(getApplicationContext(), MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        if(musicBound==false && isMyMusicServiceRunning(MusicService.class)) {
            playIntent = new Intent(getApplicationContext(), MusicService.class);
            Log.d(TAG, "onStart: service binded again");
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
           }
    }

        @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume invoked");
            if(paused){
             /*   setController();*/
                paused=false;
            }
            if(musicService!=null) Log.d(TAG, "music service is not null ");
            else if(musicService==null) Log.d(TAG, "onResume: music service is null");

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
    /*     if(controller.isShowing())controller.hide();*/
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

/*        if(isPlaying()==false && firstTimePlay==false)
        {
            if (playIntent != null) stopService(playIntent);
                 musicService = null;
            try {
                if (broadcastNotificationReceiver != null) {
                    LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(broadcastNotificationReceiver);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            try {
                if (broadcastBatteryReceiver != null) {

                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }*/
    }

/*    private void setController(){
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
    }*/

/*    private void playNext(){
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
    }*/


/*
    @Override
    public void start() {
        musicService.go();
        musicService.songPlaying=true;
        musicService.startNotification();
        setController();
    }

*/

/*
    @Override
    public void pause() {
        musicService.songPlaying=false;
        musicService.pausePlayer();
        musicService.startNotification();
        setController();
    }
*/

/*
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
*/

}