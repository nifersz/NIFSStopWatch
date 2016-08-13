package com.kogimobile.nifs.stopwatch.model;

import android.os.Handler;
import android.os.SystemClock;
import android.widget.TextView;

import java.util.Locale;

/**
 * Chronometer class to be used in an android application,
 * it includes basic stopwatch functions(start, stop and
 * reset), and a set of states to ease the implementation
 * with the user interface.
 *
 * @author Nelson Ivan Fernandez Suarez
 */
public class Chronometer {

    /** Stores the current number of minutes. */
    private int mMinutes;

    /** Stores the current number of seconds. */
    private int mSeconds;

    /** Stores the current number of milliseconds. */
    private int mMilliseconds;

    /**
     * Registered time when the chronometer starts,
     * this time is represented in milliseconds.
     */
    private long mInitialTime;

    /**
     * Base time used since the chronometer was started,
     * this time is represented in milliseconds.
     */
    private long mBaseTime;

    /**
     * Current chronometer time, this time is represented
     * in milliseconds.
     */
    private long mCurrentTime;

    /**
     * Registered time when the chronometer is stopped,
     * this time is represented in milliseconds.
     */
    private long mTimeOnPause;

    /**
     * Task to update the chronometer time in the user
     * interface widget that is displaying the format
     * (MM:SS.MLS).
     */
    private Runnable mUpdateTimeDisplay;

    /**
     * Android handler for the arithmetic operations
     * related to the chronometer, and update the user
     * interface widget.
     */
    private Handler mHandler;

    /** Android widget to show the current time. */
    private TextView mUIWidget;

    /** Stores the current state of the chronometer. */
    private State mCurrentState;

    /**
     * Chronometer states, this type is useful to check conditions
     * in the code that handles the user interface changes.
     */
    public enum State {
        /** When the chronometer hasn't been started(00:00:000). */
        INITIAL,

        /** When the chronometer is working; the time is increasing. */
        RUNNING,

        /** When the chronometer is paused; it was running. */
        STOPPED
    }


    /**
     * Initializes a chronometer with the initial conditions
     * needed to get started(use 'start()' method).
     *
     * @param uiWidget Android widget to show the current time.
     */
    public Chronometer(TextView uiWidget) {
        if (uiWidget == null) {
            throw new NullPointerException("Not possible to initialize the chronometer.");
        }

        initialState();
        mUIWidget = uiWidget;
        mHandler = new Handler();
        mUpdateTimeDisplay = timerTask();
    }


    /**
     * Sets the initial state of the chronometer.
     */
    private void initialState() {
        mMinutes = 0;
        mSeconds = 0;
        mMilliseconds = 0;
        mBaseTime = 0;
        mInitialTime = 0;
        mCurrentTime = 0;
        mTimeOnPause = 0;
        mCurrentState = State.INITIAL;
    }

    /**
     * Task that calculates the new time to show in the
     * chronometer.
     *
     * @return A runnable to be executed in android handler.
     */
    private Runnable timerTask() {
        return new Runnable() {
            public void run() {
                // Calculate chronometer time
                mCurrentTime = SystemClock.uptimeMillis() - mBaseTime;
                long timeToShowInUI = mTimeOnPause + mCurrentTime;
                int totalSeconds = (int) (timeToShowInUI / 1000);

                // Get time to use format(MM:SS.MLS)
                mMinutes = totalSeconds / 60;
                mSeconds = totalSeconds % 60;
                mMilliseconds = (int) (timeToShowInUI % 1000);

                // Apply time format(MM:SS.MLS)
                String aNewTime = String.format(Locale.ENGLISH,
                                                "%02d:%02d.%03d",
                                                mMinutes,
                                                mSeconds,
                                                mMilliseconds);

                if (mUIWidget != null) {
                    mUIWidget.setText(aNewTime);
                }

                mHandler.postDelayed(this, 0);
            }
        };
    }


    /**
     * Method that starts the chronometer.
     */
    public void start() {
        mInitialTime = SystemClock.uptimeMillis();
        mBaseTime = mInitialTime;
        restart();
    }

    /**
     * After the chronometer is paused, it is resumed.
     */
    public void resume() {
        mBaseTime = SystemClock.uptimeMillis();
        restart();
    }

    /**
     * After an orientation change, it is restarted. This is
     * also used as a base method, call it only after execute
     * 'restoreTimeWhileRunning(theInitialTime)'.
     */
    public void restart() {
        mHandler.postDelayed(mUpdateTimeDisplay, 0);
        mCurrentState = State.RUNNING;
    }

    /**
     * Method that pauses the chronometer.
     */
    public void pause() {
        mTimeOnPause += mCurrentTime;
        mHandler.removeCallbacks(mUpdateTimeDisplay);
        mCurrentState = State.STOPPED;
    }

    /**
     * Method that resets(To initial state) the chronometer.
     */
    public void reset() {
        mHandler.removeCallbacks(mUpdateTimeDisplay);
        initialState();
        mUIWidget.setText("00:00.000");
    }

    /**
     * Execute this only in an android fragment
     * 'onDestroyView()' method.
     */
    public void onDestroyView() {
        if (mCurrentState == State.RUNNING) {
            reset();
        }
    }

    /**
     * Gets the current time displayed in the user interface.
     *
     * @return A time string with the format(HH:MM:MLS).
     */
    public String getDisplayedTime() {
        return String.format(Locale.ENGLISH,
                             "%02d:%02d.%03d",
                             mMinutes,
                             mSeconds,
                             mMilliseconds);
    }

    /**
     * After a device orientation change, restores this instance
     * when the chronometer was running.
     *
     * @param theInitialTime Registered time(mls) when the chronometer started.
     * @param theTimeOnPause Time(mls) when the chronometer stopped.
     */
    public void restoreTimeWhileRunning(long theInitialTime,
                                        long theTimeOnPause) {
        mInitialTime = theInitialTime;
        mBaseTime = theInitialTime;
        mTimeOnPause = theTimeOnPause;
        restart();
    }

    /**
     * After a device orientation change, restores this instance
     * when the chronometer was stopped.
     *
     * @param theInitialTime Registered time(mls) when the chronometer started.
     * @param theTimeOnPause Time(mls) when the chronometer stopped.
     * @param theChronoDisplay Last chronometer text showed in the UI.
     */
    public void restoreTimeWhileStopped(long theInitialTime,
                                        long theTimeOnPause,
                                        String theChronoDisplay) {
        mCurrentState = State.STOPPED;
        mInitialTime = theInitialTime;
        mTimeOnPause = theTimeOnPause;
        mUIWidget.setText(theChronoDisplay);
    }


    /******************* GETTERS / SETTERS *******************/

    public long getInitialTime() {
        return mBaseTime;
    }

    public long getTimeOnPause() {
        return mTimeOnPause;
    }

    public State getCurrentState() {
        return mCurrentState;
    }

}