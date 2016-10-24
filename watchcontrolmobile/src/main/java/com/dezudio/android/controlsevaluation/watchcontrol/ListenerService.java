package com.dezudio.android.controlsevaluation.watchcontrol;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;


public class ListenerService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /* Debugging Tag */
    private static final String TAG = "ListenerService";

    /* Communication from Wearable app */
    private GoogleApiClient mGoogleApiClient;
    private static final String TIMESTAMP_KEY =
            "com.dezudio.android.controlsevaluation.activity.timestamp";
    private static final String TIMESTAMP_WAS_CANCELLED_KEY =
            "com.dezudio.android.controlsevaluation.activity.timestamp_cancelled";

    /* Communication to Home app's Activity Recording Service */
    private static final String HOME_APP_PACKAGE = "com.dezudio.android.controlsevaluation.homestudy";
    private static final String LAB_APP_PACKAGE = "com.dezudio.android.controlsevaluation.labstudy";
    private static final String HOME_APP_SERVICE = HOME_APP_PACKAGE + ".ControlsReceiver";
    private static final String LAB_APP_SERVICE = LAB_APP_PACKAGE + ".RecordService";
    private static final String RECORD_ACTION = "com.dezudio.android.controlsevaluation.action.CONTROLS";


    /**
     * Initialize service
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }


    /**
     * Evaluate new DataEvents from /timestamps
     * DataEvents are limited to path prefix of /timestamps from intent-filter in manifest config
     *
     * @param dataEvents: incoming data events
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        Log.d(TAG, "onDataChanged: " + dataEvents);

        // Iterate through queue of data events
        for (DataEvent event : dataEvents) {

            // Evaluate new DataEvent payloads under /timestamps
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                // Retrieve item
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                // Log item debugging information
                Log.d(TAG, "Data Item Payload @ path " + item.getUri() + ":");
                Log.d(TAG, " * Timestamp: " +
                        dataMap.getString(TIMESTAMP_KEY));
                Log.d(TAG, " * Activity was cancelled: " +
                        dataMap.getBoolean(TIMESTAMP_WAS_CANCELLED_KEY));

                // Invoke management app intent
                Intent i = new Intent();
                i.setAction(RECORD_ACTION);
                i.setClassName(HOME_APP_PACKAGE, HOME_APP_SERVICE);
                i.putExtra(TIMESTAMP_KEY, dataMap.getString(TIMESTAMP_KEY));
                i.putExtra(TIMESTAMP_WAS_CANCELLED_KEY,
                        dataMap.getBoolean(TIMESTAMP_WAS_CANCELLED_KEY));
                //startService(i);
                sendBroadcast(i);

                i.setClassName(LAB_APP_PACKAGE, LAB_APP_SERVICE);
                startService(i);

                // Remove item after processing
                mGoogleApiClient.connect();
                final Uri itemUri = item.getUri();

                PendingResult<DataApi.DeleteDataItemsResult> pendingResult =
                        Wearable.DataApi.deleteDataItems(mGoogleApiClient, itemUri);

                // TODO: Delete not working after refactor?
                pendingResult.setResultCallback(new ResultCallback<DataApi.DeleteDataItemsResult>() {
                    @Override
                    public void onResult(DataApi.DeleteDataItemsResult deleteResult) {
                        if (deleteResult.getStatus().isSuccess()) {
                            Log.d(TAG, "Successfully deleted data item: " + itemUri);
                        } else {
                            Log.d(TAG, "Failed to delete data item:" + itemUri);
                        }
                    }
                });
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.d(TAG, "onConnected: " + bundle);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }
}
