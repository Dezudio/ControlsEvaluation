package com.dezudio.android.controlsevaluation.homestudy;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;


/**
 * Receive intents from user study clients and record activity
 */
public class RecordService extends IntentService {

    private static final String TAG = "RecordService";

    private static final String TIMESTAMP_KEY =
            "com.dezudio.android.controlsevaluation.activity.timestamp";
    private static final String TIMESTAMP_WAS_CANCELLED_KEY =
            "com.dezudio.android.controlsevaluation.activity.timestamp_cancelled";
    private static final String RECORD_ACTION = "com.dezudio.android.controlsevaluation.action.RECORD";
    private static final String RECORD_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.RECORD";
    private static final String TIMER_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.TIMER";
    private static final String POWER_HOUR_FINISHED_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.POWER_HOUR_FINISHED";

    public static ArrayList timeList = new ArrayList<String>();

    public static int alertIndex = 0;
    public static final long[] alertList = {1, 1, 2, 3, 5, 8, 13, 21}; // List of alert intervals (minutes)

    public static Ringtone r;

    public RecordService() {
        super("RecordService");
    }


    /**
     * Manage received intents to the Record service
     *
     * @param intent: Should just be limited to .RECORD
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        if (intent != null) {

            final String action = intent.getAction();

            if (RECORD_ACTION.equals(action)) {

                final String timestamp = intent.getStringExtra(TIMESTAMP_KEY);
                final boolean wasCancelled = intent.getBooleanExtra(
                        TIMESTAMP_WAS_CANCELLED_KEY, false);
                if(r != null) { r.stop(); }
                handleActionRecord(timestamp, wasCancelled);
            }

            else if(TIMER_INTENT.equals(action)){
                Log.d(TAG, "Advance to next alert");
                if( alertIndex < alertList.length) {
                    AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Intent i2 = new Intent(this,AlarmReceiver.class);

                    PendingIntent alarmIntent = PendingIntent.getBroadcast(this,0,i2,0);

                    alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() +
                                    alertList[alertIndex] * 60 * 1000, alarmIntent);
                    alertIndex++;
                    timeList.add("T: " + intent.getStringExtra(TIMESTAMP_KEY));

                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(RECORD_INTENT);
                    sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(sendIntent);
                }
            }
            else if(POWER_HOUR_FINISHED_INTENT.equals(action)){
                Log.d(TAG,"Power Half Hour Finished");

            }
        }
    }


    /**
     * Handle action Record by committing the data to storage and displaying it
     */
    private void handleActionRecord(String timestamp, boolean wasCancelled) {

        timeList.add("R: " + timestamp);

        Intent sendIntent = new Intent();
        sendIntent.putExtra(TIMESTAMP_KEY,timestamp);
        sendIntent.setAction(RECORD_INTENT);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sendIntent);

        Log.d(TAG, "Data Item Payload:");
        Log.d(TAG, " * Timestamp: " + timestamp);
        Log.d(TAG, " * Activity was cancelled: " + wasCancelled);
    }
}

