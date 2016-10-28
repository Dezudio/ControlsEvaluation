package com.dezudio.android.controlsevaluation.homestudy;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HomeStudy";

    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1;

    public static final String START_SESSION_ACTION =
            "com.dezudio.android.controlsevaluation.action.START_SESSION";

    public static final String UPDATE_ACTION =
            "com.dezudio.android.controlsevaluation.action.UPDATE_LIST";

    public static final String ENTER_POWER_HOUR_ACTION =
            "com.dezudio.android.controlsevaluation.action.ENTER_POWER_HOUR";

    public static final String NEW_SESSION_ACTION =
            "com.dezudio.android.controlsevaluation.action.NEW_SESSION";

    private CountDownTimer timer;
    private CountDownTimer powerTimer;
    private  ArrayAdapter<String> adapter;
    public static FileWriter writer;
    public static File outputFile;

    @Override
    protected void onStart() {
        super.onStart();

        // During an active session, display a countdown When the layout is visible
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String inactive = getResources().getString(R.string.mode_inactive);
        String active = getResources().getString(R.string.mode_active);
        String studyMode = sharedPref.getString(getString(R.string.study_mode), inactive);
        String powerMode = sharedPref.getString(getString(R.string.power_mode), inactive);
        final long startTime = sharedPref.getLong(getString(R.string.study_start_time),0);
        final long powerStartTime = sharedPref.getLong(getString(R.string.power_start_time),0);


       if(studyMode.equals(active)) {
            long timerMillis = 12 * 60 * 60 * 1000 - (System.currentTimeMillis() - startTime);
            if (timerMillis > 0) {
                timer = new CountDownTimer(timerMillis, 1000 * 60) {

                    // Update the text view once/minute
                    public void onTick(long millisUntilFinished) {
                        TextView mTextField = (TextView) findViewById(R.id.session_countdown);
                        if (mTextField != null) {
                            mTextField.setText("Session Active! " +
                                    millisUntilFinished / 1000 / 60 + " min left");
                        }
                    }

                    // Display a message when there is no time remaining
                    public void onFinish() {
                        TextView mTextField = (TextView) findViewById(R.id.session_countdown);
                        mTextField.setText("Thank you for your participation!");
                    }

                }.start(); // start the session countdown timer
            }
        }

        if(powerMode.equals(active)) {
            long timerMillisPower = 30 * 60 * 1000 - (System.currentTimeMillis() - powerStartTime);
            if (timerMillisPower > 0) {
                powerTimer = new CountDownTimer(timerMillisPower, 1000 * 60) {

                    // Update the text view once/minute
                    public void onTick(long millisUntilFinished) {
                        TextView mTextField = (TextView) findViewById(R.id.power_hour_countdown);
                        if (mTextField != null) {
                            mTextField.setText("Power 30! " +
                                    millisUntilFinished / 1000 / 60 + " min left");
                        }
                    }

                    // Display a message when there is no time remaining
                    public void onFinish() {
                        TextView mTextField = (TextView) findViewById(R.id.power_hour_countdown);
                        mTextField.setText("Powered!");
                    }

                }.start(); // start the session countdown timer
            }
        }
    }

    @Override
    public void onBackPressed() {}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        doPermissionRequest();

        Intent intent = getIntent();
        String action = intent.getAction();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String inactive = getResources().getString(R.string.mode_inactive);
        String sessionMode = sharedPref.getString(getString(R.string.study_mode), inactive);


        if (START_SESSION_ACTION.equals(action)) {
            handleStart(intent);
        }
        else if(UPDATE_ACTION.equals(action)) {
            handleUpdate(intent, sharedPref);
        }
        else if(ENTER_POWER_HOUR_ACTION.equals(action)){
            handleEnterPowerHour(intent, sharedPref);
        }
        else if(NEW_SESSION_ACTION.equals(action)){
            handleRestart(intent, sharedPref);
        }
        // Not here because of an intent; just here because we're starting
        else if(sessionMode.equals(inactive)){
            setContentView(R.layout.activity_main);
        }
    }

    public void handleStart(Intent intent) {
        Log.d(TAG, "Start Study Session");

        setContentView(R.layout.activity_study);

        // Update preferences w/ study mode and study mode start time
        String active = getResources().getString(R.string.mode_active);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.study_mode), active);
        editor.putLong(getString(R.string.study_start_time),System.currentTimeMillis());
        editor.apply();

        try
        {
           File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"ControlsStudy");
            if (!root.exists()) {
                root.mkdirs();
            }
            outputFile = new File(root, "log.txt");
            outputFile.createNewFile();
            writer = new FileWriter(outputFile);
            writer.append("== LOG ==");
            writer.flush();
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        // Set an initial alert to kickstart the rest; will not notify
        long delayMins = AlarmReceiver.alertList[0];
        boolean isPowerHour = false;
        int alarmIndex = 0;
        int powerHourAlarmIndex=0;
        AlarmReceiver.setAlarm(this,delayMins*60,isPowerHour,powerHourAlarmIndex,alarmIndex);
    }

    private void fileDump() {
        try {
           File root = new File(Environment.getExternalStoragePublicDirectory(
                   Environment.DIRECTORY_DOCUMENTS),"ControlsStudy");
            if (!root.exists()) {
                root.mkdirs();
            }
            outputFile = new File(root, "log-" + System.currentTimeMillis() + ".txt");
            outputFile.createNewFile();
            writer = new FileWriter(outputFile);
            writer.append(ControlsReceiver.invocations.toString());
            writer.flush();
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void handleUpdate(Intent intent, SharedPreferences sharedPref) {
        String inactive = getResources().getString(R.string.mode_inactive);
        String active = getResources().getString(R.string.mode_active);

        String studyMode = sharedPref.getString(getString(R.string.study_mode), inactive);
        String powerMode = sharedPref.getString(getString(R.string.power_mode), inactive);
        if( studyMode.equals(active)) {
            Log.d(TAG, "Update View");
            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    ControlsReceiver.invocations);
            setContentView(R.layout.activity_study);
            ((ListView) findViewById(R.id.list)).setAdapter(adapter);

            if(powerMode.equals(active)) {
                Button mButton = (Button) findViewById(R.id.power_hour_button);
                mButton.setVisibility(Button.GONE);
            }
            fileDump();
        }
        else {
            Log.d(TAG, "Control received out of session");
            if(studyMode.equals(inactive)) {
                setContentView(R.layout.activity_main);
            }
            else {
                setContentView(R.layout.activity_done);
            }
        }
    }

    private void handleEnterPowerHour(Intent intent, SharedPreferences sharedPref){
        Log.d(TAG,"Enter Power Hour");

        String active = getResources().getString(R.string.mode_active);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.power_mode), active);
        editor.putLong(getString(R.string.power_start_time),System.currentTimeMillis());
        editor.apply();

        handleUpdate(intent, sharedPref);
    }

    private void handleRestart(Intent intent, SharedPreferences sharedPref) {
        Log.d(TAG, "Session Cleanup");
        String inactive = getResources().getString(R.string.mode_inactive);
        String active = getResources().getString(R.string.mode_active);
        String cool_down = getResources().getString(R.string.mode_cool_down);
        String studyMode = sharedPref.getString(getString(R.string.study_mode),active);
        // First time around, cool_down will be set; second time around, fully reset for new session
        if(studyMode.equals(cool_down)) {
            setContentView(R.layout.activity_main);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.study_mode), inactive);
            editor.putString(getString(R.string.power_mode), inactive);
            editor.putLong(getString(R.string.power_start_time), 0);
            editor.putLong(getString(R.string.study_start_time), 0);
            ControlsReceiver.invocations = new ArrayList();
            editor.apply();
        }
        else {
            setContentView(R.layout.activity_done);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.study_mode), cool_down);
            editor.apply();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // timer may not have been initialized yet
        if( timer != null) {timer.cancel(); }
        if( powerTimer != null) {powerTimer.cancel(); }
    }

    public void showConfirmationDialog(View view) {
        DialogFragment dialog = new SessionStartDialogFragment();
        dialog.show(getFragmentManager(),"MainActivity");
    }

    public void beginPowerHour(View view){
        DialogFragment dialog = new PowerHourStartDialogFragment();
        dialog.show(getFragmentManager(),"MainActivity");
    }


    private void doPermissionRequest() {

        requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                ASK_MULTIPLE_PERMISSION_REQUEST_CODE);

    }
}
