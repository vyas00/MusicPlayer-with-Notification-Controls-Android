package com.example.android.himusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryLevel=(int)(((float)level / (float)scale) * 100.0f);
        Intent sendIntent=new Intent("BATTERY_LOW");
        sendIntent.putExtra("battery_low",batteryLevel);
        LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
    }
}
