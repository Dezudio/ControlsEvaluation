package com.dezudio.android.controlsevaluation.homestudy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class PowerHourReceiver extends BroadcastReceiver {

    private static final String POWER_HOUR_FINISHED_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.POWER_HOUR_FINISHED";

    @Override
    public void onReceive(Context context, Intent intent) {


        Intent i = new Intent(context, RecordService.class);
        i.setAction(POWER_HOUR_FINISHED_INTENT);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startService(i);
        Log.d("HI","THERE");
    }
}