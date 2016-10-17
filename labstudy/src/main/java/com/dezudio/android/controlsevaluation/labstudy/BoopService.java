package com.dezudio.android.controlsevaluation.labstudy;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BoopService extends IntentService {
    public static final int TIMING_STOPPED = 0;
    public static final int TIMING_PAUSED = 1;
    public static final int TIMING_HAPPENING = 2;
    public static int timingState = TIMING_PAUSED;
    public static int timeIndex = 0;

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_START = "com.dezudio.android.controlsevaluation.labstudy.action.START_TIMINGS";
    private static final String ACTION_PAUSE = "com.dezudio.android.controlsevaluation.labstudy.action.STOP_TIMINGS";
    private static final String ACTION_STOP = "com.dezudio.android.controlsevaluation.labstudy.action.STOP_TIMINGS";

    private static int[] timings = {90, 53, 50, 85, 30, 24, 68, 43, 67, 84, 87, 83, 21, 62, 41, 38, 98, 20, 65, 35, 49, 62, 75, 25, 45, 66, 66, 33, 106, 51, 84, 176, 87, 79, 51, 45, 59, 41, 19, 15};

    public MediaPlayer mp;

    public BoopService() {
        super("BoopService");
        handler = new Handler();
    }

    static ToneGenerator toneG;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startTiming(Context context) {
        Intent intent = new Intent(context, BoopService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM,500);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void pauseTiming(Context context) {
        Intent intent = new Intent(context, BoopService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }

    public static void stopTiming(Context context) {
        Intent intent = new Intent(context, BoopService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                handleActionStart();
            } else if (ACTION_STOP.equals(action)) {
                handleActionStop();
            } else if (ACTION_PAUSE.equals(action)) {
                handleActionPause();
            }
        }
    }

    /**
     * Handle action start in the provided background thread with the provided
     * parameters.
     */
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d("ALARM", "BOOOOOOOOOOP");

            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

            timeIndex++;
            if (timeIndex < timings.length)
                handler.postDelayed(this, timings[timeIndex] * 1000);
            else {
                Log.d("ALARM", "SESSION OVER");
                timingState = TIMING_PAUSED;
            }

        }
    };

    private void handleActionStart() {
        timingState = TIMING_HAPPENING;

        timeIndex = 0;
        handler.postDelayed(runnable, timings[timeIndex]);

    }


    /**
     * Handle action stop in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStop() {
        timingState = TIMING_STOPPED;


    }

    private void handleActionPause() {
        timingState = TIMING_PAUSED;
        timeIndex = timings.length;


    }

}