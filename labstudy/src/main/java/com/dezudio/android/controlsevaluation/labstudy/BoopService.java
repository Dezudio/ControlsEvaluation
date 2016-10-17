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

    private static int[] timings = {61, 29, 45, 20, 31, 54, 38, 24, 30, 68, 43, 21, 17, 29, 15, 25, 44, 87, 48, 35, 21, 62, 41, 38, 22, 25, 22, 29, 20, 15, 50, 35, 16, 20, 15, 36, 24, 46, 19, 22, 58, 66, 32, 34, 33, 65, 41, 51, 84, 27, 45, 26, 34, 44, 15, 48, 33, 44, 26, 51, 45, 59, 34, 43};

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
