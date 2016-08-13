package com.kogimobile.nifs.stopwatch.ui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.kogimobile.nifs.stopwatch.R;
import com.kogimobile.nifs.stopwatch.model.Chronometer;
import com.kogimobile.nifs.stopwatch.model.Lap;
import com.kogimobile.nifs.stopwatch.ui.adapter.LapsAdapter;

import java.util.ArrayList;

public class MainFragment extends Fragment {

    @BindView(R.id.text_chronometer)
    TextView mTxtvChronoDisplay;

    @BindView(R.id.button_add_lap)
    ImageButton mBtnAddLap;

    @BindView(R.id.fabutton_start_stop)
    FloatingActionButton mBtnStartStop;

    @BindView(R.id.button_reset)
    ImageButton mBtnReset;

    @Nullable
    @BindView(R.id.layout_laps)
    LinearLayout mLayoutLaps;

    @Nullable
    @BindView(R.id.layout_initial_hint)
    FrameLayout mLayoutInitialHint;

    @Nullable
    @BindView(R.id.text_initial_hint)
    TextView mTxtvInitialHint;

    @Nullable
    @BindView(R.id.text_laps_label)
    TextView mTxtvLapsLabel;

    @BindView(R.id.rcvw_laps_list)
    RecyclerView mRecyclerView;

    @BindColor(R.color.chronometerInitial)
    int chronoInitialColor;

    @BindColor(R.color.chronometerRunning)
    int chronoRunningColor;

    @BindColor(R.color.chronometerStopped)
    int chronoStoppedColor;

    @BindDrawable(android.R.drawable.ic_media_play)
    Drawable mPlayIcon;

    @BindDrawable(android.R.drawable.ic_media_pause)
    Drawable mPauseIcon;

    private Animation mBlinkingAnimation;

    private Unbinder mUnbinder;

    private Chronometer mChronometer;

    private LapsAdapter mLapsAdapter;

    private static final String STATE_LAPS_PANEL = "panelLaps";
    private static final String STATE_INITIAL_TIME = "chronoInitialTime";
    private static final String STATE_TIME_ON_PAUSE = "chronoTimeOnPause";
    private static final String STATE_CHRONO_DISPLAY = "chronoDisplay";


    public MainFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mChronometer = new Chronometer(mTxtvChronoDisplay);
        prepareBlinking();

        // Chronometer control buttons
        mBtnAddLap.setOnClickListener((View view) -> {
            addLap();
        });
        mBtnStartStop.setOnClickListener((View view) -> {
            startStopChronometer();
        });
        mBtnReset.setOnClickListener((View view) -> {
            resetChronometer();
        });

        // Laps list RecyclerView/Adapter
        mLapsAdapter = new LapsAdapter(getActivity());
        mRecyclerView.setAdapter(mLapsAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String theChronoDisplay;
        ArrayList<Lap> theLapsList;
        Long theInitialTime, theTimeOnPause;

        if (savedInstanceState != null) {
            theInitialTime = savedInstanceState.getLong(STATE_INITIAL_TIME);
            theTimeOnPause = savedInstanceState.getLong(STATE_TIME_ON_PAUSE);
            theChronoDisplay = savedInstanceState.getString(STATE_CHRONO_DISPLAY);
            theLapsList = savedInstanceState.getParcelableArrayList(STATE_LAPS_PANEL);

            if (theChronoDisplay != null) { // STOPPED
                mLapsAdapter.restoreLaps(theLapsList);
                if ( !(theLapsList == null || theLapsList.isEmpty()) ) {
                    showLapsPanel();
                }

                mBtnStartStop.setImageDrawable(mPlayIcon);
                mChronometer.restoreTimeWhileStopped(theInitialTime,
                                                     theTimeOnPause,
                                                     theChronoDisplay);
                mTxtvChronoDisplay.setTextColor(chronoStoppedColor);
                mTxtvChronoDisplay.startAnimation(mBlinkingAnimation);
                return;
            }
            if (theInitialTime != 0) { // RUNNING
                mLapsAdapter.restoreLaps(theLapsList);
                if ( !(theLapsList == null || theLapsList.isEmpty()) ) {
                    showLapsPanel();
                }

                mBtnStartStop.setImageDrawable(mPauseIcon);
                mTxtvChronoDisplay.setTextColor(chronoRunningColor);
                mChronometer.restoreTimeWhileRunning(theInitialTime,
                                                     theTimeOnPause);
                return;
            }

            // INITIAL
            hideLapsPanel();
            mTxtvChronoDisplay.setTextColor(chronoInitialColor);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        switch (mChronometer.getCurrentState()) {
            case INITIAL:
                saveChronoState(null, 0, 0, outState);
                break;
            case RUNNING:
                saveChronoState(null,
                                mChronometer.getInitialTime(),
                                mChronometer.getTimeOnPause(), outState);
                savePanelLapsState(outState);
                break;
            case STOPPED:
                String displayTxt = mTxtvChronoDisplay.getText().toString();
                saveChronoState(displayTxt,
                                mChronometer.getInitialTime(),
                                mChronometer.getTimeOnPause(), outState);
                savePanelLapsState(outState);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mChronometer.onDestroyView();
        mUnbinder.unbind();
    }

    /**
     * Checks the current device orientation.
     *
     * @return If device is in portrait, true; otherwise false.
     */
    private boolean isDeviceInPortrait() {
        return getActivity().getResources()
                            .getBoolean(R.bool.is_portrait);
    }

    /**
     * Initialization of a blinking animation for the
     * chronometer display.
     */
    private void prepareBlinking() {
        mBlinkingAnimation = new AlphaAnimation(0.0f, 1.0f);
        mBlinkingAnimation.setDuration(200);
        mBlinkingAnimation.setStartOffset(20);
        mBlinkingAnimation.setRepeatMode(Animation.REVERSE);
        mBlinkingAnimation.setRepeatCount(Animation.INFINITE);
    }

    /**
     * Takes the current time(Format MM:SS:MLS) and creates
     * a new object to be added in the laps list.
     */
    private void addLap() {
        if (mChronometer.getCurrentState() != Chronometer.State.RUNNING) {
            return;
        }

        int lapNum = mLapsAdapter.getItemCount() + 1;
        String lapTime = mChronometer.getDisplayedTime();

        mLapsAdapter.addNewLap(new Lap(lapNum, lapTime));

        // Should the lap list be shown?
        if (mLapsAdapter.getItemCount() == 1) {
            showLapsPanel();
        }
    }

    /**
     * Double operation to start or stop a chronometer.
     * The user interface button is updated accordingly.
     */
    private void startStopChronometer() {
        switch (mChronometer.getCurrentState()) {
            case INITIAL:
                mChronometer.start();
                mBtnStartStop.setImageDrawable(mPauseIcon);
                mTxtvChronoDisplay.setTextColor(chronoRunningColor);
                break;
            case RUNNING:
                mChronometer.pause();
                mBtnStartStop.setImageDrawable(mPlayIcon);
                mTxtvChronoDisplay.setTextColor(chronoStoppedColor);
                mTxtvChronoDisplay.startAnimation(mBlinkingAnimation);
                break;
            case STOPPED:
                mChronometer.resume();
                mBtnStartStop.setImageDrawable(mPauseIcon);
                mTxtvChronoDisplay.setTextColor(chronoRunningColor);
                mTxtvChronoDisplay.clearAnimation();
                break;
        }
    }

    /**
     * Resets the chronometer to its original state, all the
     * laps stored are deleted,
     */
    private void resetChronometer() {
        if (mChronometer.getCurrentState() == Chronometer.State.INITIAL) {
            return;
        }
        // Only show the hint panel if they were laps stored.
        if (mLapsAdapter.getItemCount() > 0) {
            hideLapsPanel();
        }

        mChronometer.reset();
        mLapsAdapter.clear();
        mBtnStartStop.setImageDrawable(mPlayIcon);
        mTxtvChronoDisplay.setTextColor(chronoInitialColor);
        mTxtvChronoDisplay.clearAnimation();
    }

    /**
     * Updates the user interface to show the laps list layout.
     */
    private void showLapsPanel() {
        if (isDeviceInPortrait()) {
            mLayoutLaps.setVisibility(View.VISIBLE);
            mLayoutInitialHint.setVisibility(View.GONE);
        } else {
            mTxtvInitialHint.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mTxtvLapsLabel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates the user interface to hide the laps list layout.
     */
    private void hideLapsPanel() {
        if (isDeviceInPortrait()) {
            mLayoutLaps.setVisibility(View.GONE);
            mLayoutInitialHint.setVisibility(View.VISIBLE);
        } else {
            mTxtvInitialHint.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mTxtvLapsLabel.setVisibility(View.GONE);
        }
    }

    private void saveChronoState(String displayTxt,
                                 long initialTime,
                                 long timeOnPause,
                                 Bundle outState) {
        outState.putLong(STATE_INITIAL_TIME, initialTime);
        outState.putLong(STATE_TIME_ON_PAUSE, timeOnPause);
        outState.putString(STATE_CHRONO_DISPLAY, displayTxt);
    }

    private void savePanelLapsState(Bundle outState) {
        ArrayList<Lap> lapsList = mLapsAdapter.getItems();

        if (lapsList.isEmpty()) {
            return;
        }

        outState.putParcelableArrayList(STATE_LAPS_PANEL, lapsList);
    }

}