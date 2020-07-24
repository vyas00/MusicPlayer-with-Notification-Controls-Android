package com.example.android.himusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final String TAG="MusicService";

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private String songTitle;
    private  String songArtist;
    private final IBinder musicBind = new MusicBinder();

    private  MusicController controller;

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition =0;
        player=new MediaPlayer();
        initMusicPlayer();
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

        ControllerShow(controller);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startNotification();
        else
            startForeground(1, new Notification());

    }

    public void ControllerShow(MusicController controller)
    {
        this.controller=controller;
        if(this.controller!=null) this.controller.show(0);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startNotification(){
        String NOTIFICATION_CHANNEL_ID = "com.example.HiMusic";
        String channelName = "My Notification Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.icon_play)
                .setContentTitle(songTitle)
                .setOngoing(true)
                .setTicker(songTitle)
                .setContentText("Artist: "+songArtist)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    public void setSong(int songIndex){
        songPosition =songIndex;
    }
    public void playSong(){
        player.reset();

        Song playSong = songs.get(songPosition);
        songTitle=playSong.getTitle();
        songArtist=playSong.getArtist();

        long currSong = playSong.getID();

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

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
        player.stop();
        player.release();
        stopForeground(true);
        super.onDestroy();
    }

}
