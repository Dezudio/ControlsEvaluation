package com.dezudio.android.controlsevaluation.homestudy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import java.text.SimpleDateFormat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    public static final String ALARM_ACTION =
            "com.dezudio.android.controlsevaluation.action.START_ALARM";
    public static final String ALARM_STOP_ACTION =
            "com.dezudio.android.controlsevaluation.action.STOP_ALARM";
    public static final String START_POWER_HOUR_ACTION =
            "com.dezudio.android.controlsevaluation.action.START_POWER_HOUR";
    public static final String RESET_SESSION_ACTION =
            "com.dezudio.android.controlsevaluation.action.RESET_SESSION";

    /* Intent Extras Keys */
    private static final String POWER_HOUR_KEY =
            "com.dezudio.android.controlsevaluation.key.power_hour";
    private static final String POWER_HOUR_ALARM_INDEX_KEY =
            "com.dezudio.android.controlsevaluation.key.power_hour_alarm_index";
    private static final String ALARM_INDEX_KEY =
            "com.dezudio.android.controlsevaluation.key.alarm_index";
    private static final String SECOND_RESET_KEY =
            "com.dezudio.android.controlsevaluation.key.second_reset";

    public static final long[] alertList = {1}; // List of alert intervals (minutes)
    public static final long[] powerHourAlertList = {15, 15}; // List of alert intervals (seconds)

    public static PendingIntent pendingAlarmIntent;
    public static Vibrator v;
    public static Ringtone r;
    private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if (ALARM_ACTION.equals(action)) {
            handleAlarm(context, intent);
        } else if (ALARM_STOP_ACTION.equals(action)) {
            handleAlarmStop(context, intent);
        } else if (START_POWER_HOUR_ACTION.equals(action)) {
            handleStartPowerHour(context, intent);
        } else if (RESET_SESSION_ACTION.equals(action)) {
            handleReset(context, intent);
        }
    }

    /**
     * Cleanup and reset for new session
     * First called shortly after last alarm to thank the user, then call ourselves again
     * 3 hours later following cool down to reset for a new session.
     */
    public void handleReset(Context context, Intent intent) {
        Log.d(TAG, "handleReset");

        boolean secondTime = intent.getBooleanExtra(SECOND_RESET_KEY,false);

        Intent sendIntent = new Intent();
        sendIntent.setAction(MainActivity.NEW_SESSION_ACTION);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendIntent);

        if(!secondTime) {
            Log.d(TAG, " * Reset phase I");
            AlarmManager alarmMgr = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.setAction(RESET_SESSION_ACTION);
            alarmIntent.putExtra(SECOND_RESET_KEY, true);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            pendingAlarmIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            // Wait 30 seconds for the alarm to finish or for them to respond.
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            30 * 1000, pendingAlarmIntent);
        }
        else {
            // The intent was already dispatched to the activity above; log for clarity
            Log.d(TAG, " * Reset phase II");
        }
    }

    /**
     * Initiate power hour and cancel pending regular session alarm
     */
    private void handleStartPowerHour(Context context, Intent intent) {
        Log.d(TAG, "handleStartPowerHour");

        int alarmIndex = intent.getIntExtra(ALARM_INDEX_KEY, 0);
        int powerHourAlarmIndex = 0;
        boolean isPowerHour = true;
        long delaySecs = powerHourAlertList[powerHourAlarmIndex];


        ControlsReceiver.invocations.add("PH: " +
                timestampFormat.format(System.currentTimeMillis()));

        cancelAlarm(context);
        setAlarm(context, delaySecs, isPowerHour, powerHourAlarmIndex, alarmIndex);

        Intent sendIntent = new Intent();
        sendIntent.setAction(MainActivity.ENTER_POWER_HOUR_ACTION);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendIntent);
    }

    /**
     * Stop alarm noise after control device is triggered
     */
    private void handleAlarmStop(Context context, Intent intent) {
        Log.d(TAG, "handleAlarmStop");

        if (r != null) {
            r.stop();
        }
        if( v!= null){
            v.cancel();
        }
    }

    /**
     * Sound alarm to force control device to trigger
     * Deals with both power hour and regular session alarms
     */
    private void handleAlarm(Context context, Intent intent) {

        boolean isPowerHour = intent.getBooleanExtra(POWER_HOUR_KEY, false);
        int powerHourAlarmIndex = intent.getIntExtra(POWER_HOUR_ALARM_INDEX_KEY, 0);
        int alarmIndex = intent.getIntExtra(ALARM_INDEX_KEY, 0);

        /* This alarm occurred during a power hour */
        if (isPowerHour) {
            Log.d(TAG, "Power Hour Alarm");

            if (powerHourAlarmIndex < powerHourAlertList.length) {
                // Add alarm entry to list
                Log.d(TAG, "* power hour alarm #" + Integer.valueOf(powerHourAlarmIndex).toString());
                ControlsReceiver.invocations.add("T!: " +
                        timestampFormat.format(System.currentTimeMillis()));
                // Make noise
                soundAlarm(context);

                // Next alarm index
                powerHourAlarmIndex++;

                // If there is a next alarm, set it
                if (powerHourAlarmIndex < powerHourAlertList.length) {
                    long delaySecs = powerHourAlertList[powerHourAlarmIndex];
                    setAlarm(context, delaySecs, isPowerHour, powerHourAlarmIndex, alarmIndex);
                }
                // Otherwise resume regular session alarms
                else {
                    long delayMins = alertList[alarmIndex];
                    isPowerHour = false;
                    setAlarm(context, delayMins * 60, isPowerHour, powerHourAlarmIndex, alarmIndex);
                }
            }
        }

        /* This was a regular session alarm */
        else {
            Log.d(TAG, "Regular Session Alarm");

            if (alarmIndex < alertList.length) {
                // Add alarm entry to list
                Log.d(TAG, "* alarm #" + Integer.valueOf(alarmIndex).toString());
                ControlsReceiver.invocations.add("T: " +
                        timestampFormat.format(System.currentTimeMillis()));

                // Make noise
                soundAlarm(context);

                // Next alarm index
                alarmIndex++;

                // If there is a next alarm, set it
                if (alarmIndex < alertList.length) {
                    long delayMins = alertList[alarmIndex];
                    setAlarm(context, delayMins * 60, isPowerHour, powerHourAlarmIndex, alarmIndex);

                }
                // That was the last session alarm; fire off an alarm to close up shop
                else {
                    Log.d(TAG, "No more alarms. Shut it down T-30.");

                    AlarmManager alarmMgr = (AlarmManager)
                            context.getSystemService(Context.ALARM_SERVICE);
                    Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                    alarmIntent.setAction(RESET_SESSION_ACTION);
                    alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    pendingAlarmIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    // Wait 30 seconds for the alarm to finish or for them to respond.
                    alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() +
                                    30 * 1000, pendingAlarmIntent);
                }
            }
        }

        /* Update list view after alarm */
        Intent sendIntent = new Intent();
        sendIntent.setAction(MainActivity.UPDATE_ACTION);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendIntent);
    }

    /**
     * Have alarm make noise
     */
    private void soundAlarm(Context context) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Vibrate
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = { 0, 400, 200, 400, 200, 400, 200, 400 };
        v.vibrate(pattern, -1);
    }

    /**
     * Set an alarm for some time in the future.
     * Handles both power hour and regular session alarm setting
     * @param seconds How many seconds to delay
     * @param isPowerHour If this is a power hour alarm
     * @param powerHourAlarmIndex Position in the power hour alarm index
     * @param alarmIndex Position in the normal session alarm index
     */
    public static void setAlarm(Context context, long seconds, boolean isPowerHour,
                                int powerHourAlarmIndex, int alarmIndex) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.setAction(ALARM_ACTION);
        alarmIntent.putExtra(POWER_HOUR_KEY, isPowerHour);
        alarmIntent.putExtra(POWER_HOUR_ALARM_INDEX_KEY, powerHourAlarmIndex);
        alarmIntent.putExtra(ALARM_INDEX_KEY, alarmIndex);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        pendingAlarmIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        seconds * 1000, pendingAlarmIntent);
    }

    private void cancelAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendingAlarmIntent);
    }
}