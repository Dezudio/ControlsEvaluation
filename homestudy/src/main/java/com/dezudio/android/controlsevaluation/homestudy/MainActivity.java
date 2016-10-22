package com.dezudio.android.controlsevaluation.homestudy;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HomeStudy";
    private static final String START_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.START";

    private static final String START_POWER_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.START_POWER";

    private static final String RECORD_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.RECORD";

    private CountDownTimer timer;
    private  ArrayAdapter<String> adapter;

    @Override
    protected void onStart() {
        super.onStart();

        // During an active session, display a countdown When the layout is visible
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String studyModeDefault = getResources().getString(R.string.study_mode_default);
        String studyMode = sharedPref.getString(getString(R.string.study_mode), studyModeDefault);
        final long startTime = sharedPref.getLong(getString(R.string.study_start_time),0);

        if (!studyMode.equals(studyModeDefault)) {
            long timerMillis = 12 * 60 * 60 * 1000 - (System.currentTimeMillis() - startTime);
            if (timerMillis > 0) {
                timer = new CountDownTimer(timerMillis, 1000 * 60) {

                    // Update the text view once/minute
                    public void onTick(long millisUntilFinished) {
                        TextView mTextField = (TextView) findViewById(R.id.session_countdown);
                        if (mTextField != null) {
                            mTextField.setText(millisUntilFinished / 1000 / 60 + " Minutes Remain");
                        }
                    }

                    // Display a message when there is no time remaining
                    public void onFinish() {
                        TextView mTextField = (TextView) findViewById(R.id.session_countdown);
                        mTextField.setText("Thank you for your participation!");
                    }

                }.start(); // start the session countdown timer
            } else {
                Log.d(TAG, "Come back tomorrow");
            }
        }

        String powerModeDefault = getString(R.string.power_mode_default);
        String powerMode = sharedPref.getString(getString(R.string.power_mode),powerModeDefault);
        final long powerStartTime = sharedPref.getLong(getString(R.string.power_start_time),0);

        if (!powerMode.equals(powerModeDefault)) {
            long timerMillisPower = 30 * 60 * 1000 - (System.currentTimeMillis() - powerStartTime);
            if (timerMillisPower > 0) {
                timer = new CountDownTimer(timerMillisPower, 1000 * 60) {

                    // Update the text view once/minute
                    public void onTick(long millisUntilFinished) {
                        TextView mTextField = (TextView) findViewById(R.id.power_hour_countdown);
                        if (mTextField != null) {
                            mTextField.setText(millisUntilFinished / 1000 / 60 + " min");
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
        else {
            Log.d(TAG, "Power half hour is over");
        }



    }

    @Override
    public void onBackPressed() {}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (START_INTENT.equals(action)) {
            setContentView(R.layout.activity_study);
            handleStart(intent);
        }

        else if (START_POWER_INTENT.equals(action)) {
            setContentView(R.layout.activity_study);
            handlePowerStart(intent);
            adapter=new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    com.dezudio.android.controlsevaluation.homestudy.RecordService.timeList);
        }

        else if(RECORD_INTENT.equals(action)) {
                   adapter=new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    com.dezudio.android.controlsevaluation.homestudy.RecordService.timeList);

        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String studyModeDefault = getResources().getString(R.string.study_mode_default);
        String studyMode = sharedPref.getString(getString(R.string.study_mode), studyModeDefault);

        if (studyMode.equals(studyModeDefault)) {
            Log.d(TAG, "Study not in session");
            setContentView(R.layout.activity_main);
        } else {
            Log.d(TAG, "Study active");
            setContentView(R.layout.activity_study);

            ((ListView) findViewById(R.id.list)).setAdapter(adapter);
        }

    }


    public void showConfirmationDialog(View view) {
        DialogFragment dialog = new SessionStartDialogFragment();
        dialog.show(getFragmentManager(),"MainActivity");
    }

    public void beginPowerHour(View view){
        DialogFragment dialog = new PowerHourStartDialogFragment();
        dialog.show(getFragmentManager(),"MainActivity");
    }
    public void handleStart(Intent intent) {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.study_mode), "active");
        editor.putLong(getString(R.string.study_start_time),System.currentTimeMillis());
        editor.apply();

        // Set an initial alert to kickstart the rest; will not notify
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent i2 = new Intent(this,AlarmReceiver.class);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(this,0,i2,0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        1 * 1000, alarmIntent);

    }

    public void handlePowerStart(Intent intent) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.power_mode), "active");
        editor.putLong(getString(R.string.power_start_time),System.currentTimeMillis());
        editor.apply();

        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent i2 = new Intent(this,PowerHourReceiver.class);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(this,0,i2,0);

        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        10 * 1000, alarmIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // timer may not have been initialized yet
        if( timer != null) {timer.cancel(); }
    }

}
