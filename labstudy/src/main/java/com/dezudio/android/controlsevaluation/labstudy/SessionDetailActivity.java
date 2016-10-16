package com.dezudio.android.controlsevaluation.labstudy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * An activity representing a single Session detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link SessionListActivity}.
 */
public class SessionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (BoopService.timingState == BoopService.TIMING_PAUSED) {
                    fab.setImageDrawable(getDrawable(R.drawable.ic_stop));
                    BoopService.stopTiming(getApplicationContext());
                    Snackbar.make(view, "Stopping timing activity", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    //fab.setEnabled(false);
                }
                return true;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (BoopService.timingState == BoopService.TIMING_HAPPENING) {
                    BoopService.pauseTiming(getApplicationContext());
                    //fab.setImageDrawable(getResources().getDrawable(R.id.fab, getTheme())."@android:drawable/ic_");
                    Snackbar.make(view, "Pausing timing activity", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    fab.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));
                } else if ((BoopService.timingState == BoopService.TIMING_PAUSED) ||
                        (BoopService.timingState == BoopService.TIMING_STOPPED)) {
                    BoopService.startTiming(getApplicationContext());
                    Snackbar.make(view, "Starting timing activity", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    fab.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
                }
            }
        });




        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)

        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null)

        {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(SessionDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(SessionDetailFragment.ARG_ITEM_ID));
            SessionDetailFragment fragment = new SessionDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.session_detail_container, fragment)
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, SessionListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
