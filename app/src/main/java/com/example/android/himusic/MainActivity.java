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
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity /*implements MediaController.MediaPlayerControl*/ {

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

   public TabLayout tabLayout;

   public int tabPosition=0;
   public ListView songLView;
   private Fragment controllerFragmet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      /*  setController();*/
        controllerFragmet=new ControllerFragment();
        setContentView(R.layout.activity_main);
        songLView = findViewById(R.id.song_list);
        Log.d(TAG, "onCreate invoked :");
        db = new DatabaseHandler(MainActivity.this);
        db.createPlaylistTable("LikedSongs");
        MusicSharedPref.setContext(getApplicationContext());

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
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
                        if (MusicSharedPref.getScheduleSongName().equals("null") == false) {
                            LayoutInflater lI = LayoutInflater.from(MainActivity.this);
                            View v = lI.inflate(R.layout.scheduled_dialogbox, null);
                            AlertDialog.Builder alertDialogBuilderforAlarm = new AlertDialog.Builder(MainActivity.this);
                            alertDialogBuilderforAlarm.setView(v);
                            final TextView alarmsongname = (TextView) v.findViewById(R.id.tv_alarm_song_name);
                            final TextView alarmsongtime = (TextView) v.findViewById(R.id.tv_alarm_song_time);
                            alarmsongname.setText("Song Name: " + MusicSharedPref.getScheduleSongName());
                            alarmsongtime.setText("Scheduled time: " + getTimeStamp(MusicSharedPref.getScheduleTimeLong()));
                            alertDialogBuilderforAlarm
                                    .setCancelable(false)
                                    .setPositiveButton("Re-Schedule",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    AlarmManager alarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                                                    Intent intent = new Intent(MainActivity.this, SongSchedulerBroadcastReceiver.class);
                                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                    if (pendingIntent != null) {
                                                        alarmManager.cancel(pendingIntent);
                                                        pendingIntent.cancel();
                                                    }
                                                    LayoutInflater li = LayoutInflater.from(MainActivity.this);
                                                    View promptsView = li.inflate(R.layout.time_dialogbox, null);

                                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

                                                    alertDialogBuilder.setView(promptsView);
                                                    final EditText userInput = (EditText) promptsView.findViewById(R.id.et_time_dialog);
                                                    alertDialogBuilder
                                                            .setCancelable(false)
                                                            .setPositiveButton("OK",
                                                                    new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int id) {

                                                                            long timeAtButtonClick = 0;
                                                                            if (userInput.getText().length() != 0) {

                                                                                timeAtButtonClick = Long.parseLong(userInput.getText().toString());
                                                                                Intent intent = new Intent(MainActivity.this, SongSchedulerBroadcastReceiver.class);
                                                                                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                                                                AlarmManager alarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);

                                                                                long currentTimeinMilliSec = System.currentTimeMillis();
                                                                                long additionalTime = timeAtButtonClick * 60 * 1000;
                                                                                MusicSharedPref.setScheduledTime(currentTimeinMilliSec + additionalTime);

                                                                                alarmManager.set(AlarmManager.RTC_WAKEUP, currentTimeinMilliSec + additionalTime, pendingIntent);
                                                                                Toast.makeText(MainActivity.this, MusicSharedPref.getScheduleSongName() + " has been Re-scheduled for playing", Toast.LENGTH_LONG).show();
                                                                                Log.d(TAG, "user has placed a song order ");


                                                                            } else {
                                                                                Toast.makeText(MainActivity.this, "Cannot schedule the song, please enter the time and try again ", Toast.LENGTH_LONG).show();
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
                                            })
                                    .setNegativeButton("Cancel Schedule",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    AlarmManager alarmManager = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                                                    Intent intent = new Intent(MainActivity.this, SongSchedulerBroadcastReceiver.class);
                                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                    if (pendingIntent != null) {
                                                        alarmManager.cancel(pendingIntent);
                                                        pendingIntent.cancel();
                                                        Toast.makeText(MainActivity.this, "Scheduled song canceled ", Toast.LENGTH_LONG).show();
                                                        MusicSharedPref.setScheduleSongName("null");
                                                    }
                                                    dialog.cancel();
                                                }
                                            })

                                    .setNeutralButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });


                            AlertDialog alertDialogalarm = alertDialogBuilderforAlarm.create();
                            alertDialogalarm.show();
                        } else {
                            Toast.makeText(MainActivity.this, "No Song added for Scheduled Play ", Toast.LENGTH_LONG).show();
                        }

                    default:
                        return true;
                }
                return true;

            }
        });

        if (isMyMusicServiceRunning(MusicService.class) == false) {
            playIntent = new Intent(getApplicationContext(), MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
            Log.d(TAG, "onCreate: service created in oncreate ");
        } else {
            firstTimePlay = true;
        }
        /*     setController();*/


        tabLayout = findViewById(R.id.music_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Card Songs").setIcon(R.drawable.ic_sdcardstorage));
        tabLayout.addTab(tabLayout.newTab().setText("Playlist").setIcon(R.drawable.ic_playlist_play));
        if (MusicSharedPref.getTableName().isEmpty() == false) {
            tabLayout.addTab(tabLayout.newTab().setText(MusicSharedPref.getTableName()).setIcon(R.drawable.ic_selected_playlist));
        } else {
            tabLayout.addTab(tabLayout.newTab().setText("Songs").setIcon(R.drawable.ic_selected_playlist));
        }

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabPosition = tab.getPosition();
                Log.d(TAG, "onTabSelected: " + tabPosition);
                if (tabPosition == 0)
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new SongFragment()).commit();
                if (tabPosition == 1)
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new PlaylistFragment()).commit();
                if (tabPosition == 2)
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new SelectedSongsFragment()).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
    public String getTimeStamp(long timeinMillies) {
        String date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = formatter.format(new Date(timeinMillies));
        System.out.println("Today is " + date);

        return date;
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



    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService = binder.getService();
            musicBound = true;
            Log.d(TAG, "onServiceConnected: invoked");
            if(tabPosition==0) getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new SongFragment()).commit();
            if(tabPosition==1)  getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new PlaylistFragment()).commit();
            if(tabPosition==2) getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new SelectedSongsFragment()).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.music_controller_container,controllerFragmet).commit();
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
            Log.d(TAG, "onStart MainActivity: service started and binded again ");
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
            if(musicService!=null) Log.d(TAG, " onResume: music service is not null ");
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
        if(musicBound && musicService!=null) { Log.d(TAG, "onStop: service unbinded here ");
        unbindService(musicConnection); musicBound=false;}

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
    }

/*    public void setController(){
        if(controller==null) controller = new MusicController(MainActivity.this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
        Log.d(TAG, "setController: called");
    }

    public void songPicked(View v){
        controller.show();
        Log.d(TAG, "songPicked: controller showing: "+ controller.isShowing());
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
    }*/

}