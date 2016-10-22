package com.dezudio.android.controlsevaluation.homestudy;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import java.util.Date;

public class PowerHourStartDialogFragment extends DialogFragment {

    private static final String START_POWER_INTENT =
            "com.dezudio.android.controlsevaluation.homestudy.START_POWER";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.power_start_confirmation_instructions)
                .setPositiveButton(R.string.start_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(START_POWER_INTENT);
                        startActivity(sendIntent);

                        // Begin study session
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.power_mode), "active");
                        editor.putLong(getString(R.string.power_start_time), System.currentTimeMillis());
                        editor.apply();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        return builder.create();
    }
}
