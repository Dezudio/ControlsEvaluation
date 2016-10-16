package com.dezudio.android.controlsevaluation.labstudy;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

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

                handleActionRecord(timestamp, wasCancelled);
            }
        }
    }


    /**
     * Handle action Record by committing the data to storage and displaying it
     */
    private void handleActionRecord(String timestamp, boolean wasCancelled) {
        Log.d(TAG, "Data Item Payload:");
        Log.d(TAG, " * Timestamp: " + timestamp);
        Log.d(TAG, " * Activity was cancelled: " + wasCancelled);
        //40 minutes
        //O(1.30)
        // Running total of blue rows
    }
}

