package com.example.android.himusic;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SongSchedulerBroadcastReceiver extends BroadcastReceiver {
    private String TAG="SongSchedulerBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "song order intent has been received in SongScheduleBroadcastReceiver");

        Intent intentToService = new Intent(context,MusicService.class);
        intentToService.putExtra("song_order", "ORDER");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentToService);
            Log.d(TAG, "onReceive of SongScheduler: intent sent to service class ");
        } else {
            context.startService(intentToService);
            Log.d(TAG, "onReceive of SongScheduler: intent sent to service class");
        }

    }

}


