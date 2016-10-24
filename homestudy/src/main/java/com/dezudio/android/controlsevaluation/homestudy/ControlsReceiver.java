package com.dezudio.android.controlsevaluation.homestudy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class ControlsReceiver extends BroadcastReceiver {
    private static final String TAG = "ControlsReceiver";

    private static final String CONTROLS_ACTION = "com.dezudio.android.controlsevaluation.action.CONTROLS";
    private static final String TIMESTAMP_KEY =
            "com.dezudio.android.controlsevaluation.activity.timestamp";


    public static ArrayList invocations = new ArrayList<String>();


    public ControlsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(CONTROLS_ACTION.equals(action)) {
            String timestamp = intent.getStringExtra(TIMESTAMP_KEY);
            invocations.add("R: " + timestamp);

            Intent stopAlarm = new Intent(context,AlarmReceiver.class);
            stopAlarm.setAction(AlarmReceiver.ALARM_STOP_ACTION);
            context.sendBroadcast(stopAlarm);

            Intent sendIntent = new Intent();
            sendIntent.putExtra(TIMESTAMP_KEY,timestamp);
            sendIntent.setAction(MainActivity.UPDATE_ACTION);
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sendIntent);

            Log.d(TAG, "Data Item Payload:");
            Log.d(TAG, " * Timestamp: " + timestamp);
        }
    }
}
