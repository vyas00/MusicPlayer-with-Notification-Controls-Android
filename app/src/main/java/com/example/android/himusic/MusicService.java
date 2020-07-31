package com.example.android.himusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final String TAG="MusicService";

    public static final String ACTION_PRE="actionprevious";
    public static final String ACTION_PLAY="actionplay";
    public static final String ACTION_NEXT="actionpause";
    public static final String ACTION_DESTROY_SERVICE="destroy";
    public boolean songPlaying;

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition=0;
    private String songTitle;
    private   String songArtist;
    private  String songImagePath;
    private long  currSong;
    private final IBinder musicBind = new MusicBinder();
private NotificationManager notificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras()!=null) {
            if (intent.getExtras().getString("bootNotification")!=null && intent.getExtras().getString("bootNotification").equals("BOOT")) {
                Log.d(TAG, "onStartCommand: of service invoked for BOOT receiver ");
                songArtist = MusicSharedPref.getArtistName();
                songTitle = MusicSharedPref.getSongName();
                currSong = MusicSharedPref.getLongId();
                songImagePath=MusicSharedPref.getImagePath();
                ArrayList<Song> notifiSong = new ArrayList<>();
                notifiSong.add(new Song(currSong, songTitle, songArtist,songImagePath));
                setList(notifiSong);
                songPlaying = false;
                startNotification();
            }
           else if (intent.getExtras().getString("song_order")!=null &&  intent.getExtras().getString("song_order").equals("ORDER")) {
                Log.d(TAG, "onStartCommand: of service invoked for SongScheduler receiver");
                songArtist = MusicSharedPref.getScheduleArtistName();
                songTitle = MusicSharedPref.getScheduleSongName();
                currSong = MusicSharedPref.getScheduleLongId();
                songImagePath=MusicSharedPref.getScheduleImagePath();
                ArrayList<Song> notifiSong = new ArrayList<>();
                notifiSong.add(new Song(currSong, songTitle, songArtist,songImagePath));
                setList(notifiSong);
                go();
                songPlaying = true;
                startNotification();
                Toast.makeText(MusicService.this, "Your Scheduled song has been started!", Toast.LENGTH_LONG).show();
            }
        }
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition =0;
        player=new MediaPlayer();
        initMusicPlayer();
        Log.d(TAG,"service started");
         MusicSharedPref.setContext(getApplicationContext());

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastNotificationReceiver, new IntentFilter("TRACKS"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastBatteryReceiver, new IntentFilter("BATTERY_LOW"));
    }

    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    public class MusicBinder extends Binder {

        MusicService getService() {
            return MusicService.this;
        }
    }


    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return musicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startNotification();

    }

/*    public void ControllerShow(MusicController controller)
    {
       *//* this.controller=controller;
        if(this.controller!=null) this.controller.show(0);*//*
    }*/

    protected  PendingIntent getPendingIntentPrevious(){
        PendingIntent pendingIntentPrevious;

        Intent intentPrevious = new Intent(getApplicationContext(), MusicNotificationBroadcastReceiver.class).setAction(ACTION_PRE);
        pendingIntentPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
     return  pendingIntentPrevious;
    }

    protected  PendingIntent getPendingIntentPlay()
    {
        PendingIntent pendingIntentPlay;

        Intent intentPlay = new Intent(getApplicationContext(), MusicNotificationBroadcastReceiver.class).setAction(ACTION_PLAY);
        pendingIntentPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntentPlay;
    }

    protected  PendingIntent getPendingIntentNext()
    {        PendingIntent pendingIntentNext;
        Intent intentNext = new Intent(getApplicationContext(), MusicNotificationBroadcastReceiver.class).setAction(ACTION_NEXT);
        pendingIntentNext = PendingIntent.getBroadcast(getApplicationContext(), 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        return  pendingIntentNext;

    }
    protected  PendingIntent getPendingIntentDelete()
    {        PendingIntent pendingIntentDelete;
        Intent intentDelete = new Intent(getApplicationContext(), MusicNotificationBroadcastReceiver.class).setAction(ACTION_DESTROY_SERVICE);
        pendingIntentDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, intentDelete, PendingIntent.FLAG_UPDATE_CURRENT);
        return  pendingIntentDelete;
    }


    private BroadcastReceiver broadcastBatteryReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String batteryLevel=intent.getExtras().getString("battery_low");
         /*   pausePlayer();*/
            songPlaying=false;
            pausePlayer();
            startNotification();
            Toast.makeText(context,batteryLevel, Toast.LENGTH_LONG).show();
        }
    };



    private BroadcastReceiver broadcastNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");


            if(action.equals(ACTION_PRE)){
                Log.d(TAG, "pre song, notification");
               /* playPrev();*/
                playPrev();
                songPlaying=true;
                startNotification();
            }
            else if(action.equals(ACTION_PLAY)){
                if (isPng()){
                    Log.d(TAG, "pause song, notification");
                 /*   pausePlayer();*/
                    songPlaying=false;
                    pausePlayer();
                    startNotification();
                } else {
                   /* start();*/
                    go();
                    songPlaying=true;
                    startNotification();
                    Log.d(TAG, "play song, notification");
                }
            }
            else if(action.equals(ACTION_NEXT)){
                Log.d(TAG, "next song, notification");
               /* playNext();*/
                playNext();
                songPlaying=true;
                startNotification();}
            else if(action.equals(ACTION_DESTROY_SERVICE)) {
                stopSelf();
                Log.d(TAG, "onReceive: destroy from notification called");
            }
           }

    };

    public void startNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            String NOTIFICATION_CHANNEL_ID = "com.example.HiMusic";
           String channelName = "My Notification Service";

           Bitmap largeIcon=getAlbumImage(songImagePath);
           if(largeIcon==null) {largeIcon=BitmapFactory.decodeResource(getResources(), R.drawable.logo_music);}


        int drw_previous;
        drw_previous = R.drawable.icon_previous;

        int drw_play;
        if (songPlaying) drw_play = R.drawable.icon_pause;
         else drw_play = R.drawable.icon_play;

         int drw_next;
        drw_next = R.drawable.icon_next;

        MediaSessionCompat mediaSession = new MediaSessionCompat(getApplicationContext(), TAG);
        MediaSessionCompat.Token token = mediaSession.getSessionToken();

        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(false)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(songTitle)
                .setLargeIcon(largeIcon)
                .addAction(drw_previous, "Previous", getPendingIntentPrevious())
                .addAction(drw_play, "Play", getPendingIntentPlay())
                .addAction(drw_next, "Next", getPendingIntentNext())
                .setDeleteIntent(getPendingIntentDelete())
                .setTicker(songTitle)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(token))
                .setContentText("Artist: " + songArtist)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)

                .build();
        startForeground(2, notification);
         if(songPlaying==false){stopForeground(false);}
    }
        else{ startForeground(1, new Notification());}

    }
    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
    }

    public void setSong(int songIndex){
        songPosition =songIndex;
    }

    public void playSong(){
        player.reset();

        Song playSong = songs.get(songPosition);
        songTitle=playSong.getTitle();
        songArtist=playSong.getArtist();
         currSong = playSong.getID();
         songImagePath =playSong.getData();

MusicSharedPref.setArtistName(songArtist);
MusicSharedPref.setSongName(songTitle);
MusicSharedPref.setLongId(currSong);
MusicSharedPref.setImagePath(songImagePath);

        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }
    public void playPrev(){
        songPosition--;
        if(songPosition <0) songPosition =songs.size()-1;
        playSong();
    }
    public void playNext(){
        songPosition++;
        if(songPosition >=songs.size()) songPosition =0;
        playSong();
    }




    @Override
    public void onDestroy() {
        Log.d(TAG,"service destroyed");

    if(broadcastNotificationReceiver!=null)  LocalBroadcastManager.getInstance(MusicService.this).unregisterReceiver(broadcastNotificationReceiver);
    Log.d(TAG, "onDestroy: notification broadcast receiver unregistered");
       if(broadcastBatteryReceiver!=null) LocalBroadcastManager.getInstance(MusicService.this).unregisterReceiver(broadcastBatteryReceiver);
        Log.d(TAG, "onDestroy: battery broadcast receiver unregistered");
        player.stop();
        player.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager!=null){
            stopForeground(true);
            notificationManager.cancelAll();
        }

        super.onDestroy();
    }

}
