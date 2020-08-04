package com.example.android.himusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        MusicSharedPref.setContext(context);
        String action = intent.getAction();
        if(action != null) {
            if (action.equals(Intent.ACTION_BATTERY_LOW) && MusicSharedPref.getSongPlaying() ) {
                Intent sendIntent=new Intent("BATTERY_LOW");
                sendIntent.putExtra("battery_low","Music player paused due to low battery!");
                LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent); }
        }

    }

}

