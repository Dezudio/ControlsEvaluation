package com.dezudio.android.controlsevaluation.watchcontrol;


import android.app.Activity;
import android.view.View;

import android.os.Bundle;
import android.os.Vibrator;

import android.content.Intent;

import android.util.Log;

import java.util.Date;
import java.text.SimpleDateFormat;

import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


/**
 * Watch-side of vision study
 */
public class MainActivity extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks {

    /* Debugging Tag */
    private static final String TAG = "WatchControlWear";


    /* Haptic Feedback Details */
    private Vibrator vibrator;

    // start delay, length on, length off, ...
    private long[] initialVibrationPattern = {0, 300};
    private long[] cancelledVibrationPattern = {0, 300, 50, 300, 50, 300};
    private long[] confirmedVibrationPattern = {0, 700};

    //-1 - don't repeat vibration pattern
    private final int indexInPatternToRepeat = -1;


    /* Delay duration before recording activity */
    private static final int NUM_SECONDS = 2.25;


    /* Communication to Mobile app */
    private GoogleApiClient mGoogleApiClient;
    private static final String TIMESTAMP_KEY =
            "com.dezudio.android.controlsevaluation.activity.timestamp";
    private static final String TIMESTAMP_WAS_CANCELLED_KEY =
            "com.dezudio.android.controlsevaluation.activity.timestamp_cancelled";
    private PutDataMapRequest putDataMapReq;
    private SimpleDateFormat timestampFormat;


    /**
     * Initialize activity
     * <p>
     * Immediately begin a countdown timer to record activity
     */
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        setContentView(R.layout.activity_main);

        // Vibrate initialization pattern
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(initialVibrationPattern, indexInPatternToRepeat);

        // Begin countdown to record activity trigger
        DelayedConfirmationView delayedConfirmationView;
        delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
        delayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);

        delayedConfirmationView.start();
        delayedConfirmationView.setListener(this);

        // Mobile phone data passing
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        // Create a DataMapItem at /timestamps/[:id]
        if (putDataMapReq == null) {
            putDataMapReq = PutDataMapRequest.createWithAutoAppendedId("/timestamps");
        }

        // Initialize timestamp string format
        timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }


    /**
     * Cancel recording activity because cancel button was pressed
     */
    @Override
    public void onTimerSelected(View v) {

        // Cancel button looks pressed
        v.setPressed(true);

        // TODO: Record activity but flag it as cancelled

        // Display confirmation of cancelled activity trigger
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.activity_cancelled));
        startActivity(intent);

        // Vibrate cancellation pattern
        vibrator.vibrate(cancelledVibrationPattern, indexInPatternToRepeat);

        // Prevent onTimerFinished from being heard.
        ((DelayedConfirmationView) v).setListener(null);

        finish();
    }


    /**
     * Record activity after cancellation window has passed
     */
    @Override
    public void onTimerFinished(View v) {

        // Set timestamp payload to current wall-clock time
        putDataMapReq.getDataMap().putString(TIMESTAMP_KEY,
                timestampFormat.format(new Date()));

        // Set cancellation payload to false (user did not cancel activity)
        putDataMapReq.getDataMap().putBoolean(TIMESTAMP_WAS_CANCELLED_KEY, false);

        // Create request with DataMap payloads from putDataMapReq
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();

        // Submit API request, handle result
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if (result.getStatus().isSuccess()) {
                    Log.d(TAG, "Data Item Payload @ path " + result.getDataItem().getUri() + ":");
                    Log.d(TAG, " * Timestamp: " +
                            putDataMapReq.getDataMap().getString(TIMESTAMP_KEY));
                    Log.d(TAG, " * Activity was cancelled: " +
                            putDataMapReq.getDataMap().getBoolean(TIMESTAMP_WAS_CANCELLED_KEY));
                }
            }
        });


        // Display confirmation of recorded activity trigger
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.activity_recorded));
        startActivity(intent);

        // Vibrate confirmation pattern
        vibrator.vibrate(confirmedVibrationPattern, indexInPatternToRepeat);

        finish();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.d(TAG, "onConnected: " + bundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }
}