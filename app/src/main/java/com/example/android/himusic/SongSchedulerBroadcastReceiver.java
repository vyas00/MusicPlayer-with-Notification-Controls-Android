package com.example.android.himusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SongSchedulerBroadcastReceiver extends BroadcastReceiver {
    private String TAG="SongSchedulerBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "song order intent has been received in SongScheduleBroadcastReceiver");

        Toast.makeText(context,"song scheduling working app is closed", Toast.LENGTH_LONG).show();
    }
}
