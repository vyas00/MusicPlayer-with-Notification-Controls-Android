package com.example.android.himusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
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
   private ViewPager viewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate invoked :");
        db=new DatabaseHandler(MainActivity.this);


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
                        Log.d(TAG, "onNavigationItemSelected: " + db.getSongsCount()+ " songs");
                        if(db.getSongsCount()>0){
                        Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                        startActivity(intent); break;}
                        else {
                            Toast.makeText(MainActivity.this, "No songs in the playlist! Check the Box to add the song",Toast.LENGTH_LONG).show();
                            break;}
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
                     bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                     startService(playIntent);
                     Log.d(TAG, "onCreate: service created in oncreate ");
                 }
                 else{firstTimePlay=true;}
           /*     setController();*/


        tabLayout = findViewById(R.id.music_tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("Songs").setIcon(R.drawable.ic_sdcardstorage));
        tabLayout.addTab(tabLayout.newTab().setText("Playlist").setIcon(R.drawable.ic_playlist_play));
        tabLayout.addTab(tabLayout.newTab().setText("Songs").setIcon(R.drawable.ic_schedule_songs));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final CategoryAdapter cadapter = new CategoryAdapter(this,getSupportFragmentManager(),
                tabLayout.getTabCount());
        viewPager.setAdapter(cadapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
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

public MusicService getInstanceOfService()
{
    return musicService;
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
            if(playIntent!=null)  bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
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