package com.dezudio.android.controlsevaluation.homestudy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import java.text.SimpleDateFormat;


public class AlarmReceiver extends BroadcastReceiver {

    private static final String TIMER_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.TIMER";
    private static final String TIMESTAMP_KEY =
            "com.dezudio.android.controlsevaluation.activity.timestamp";

    private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onReceive(Context context, Intent intent) {


        Intent i = new Intent(context, RecordService.class);
        i.setAction(TIMER_INTENT);
        i.putExtra( TIMESTAMP_KEY, timestampFormat.format(System.currentTimeMillis()));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startService(i);
    }
}