package com.kogimobile.nifs.stopwatch.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Representation of a chronometer lap defined as a lap
 * number with a string representation of the time.
 *
 * @author Nelson Ivan Fernandez Suarez
 */
public class Lap implements Parcelable {

    /** Number of the lap. */
    private int mNumber;

    /** Time of the lap with format(MM:SS:MLS). */
    private String mTime;


    protected Lap(Parcel in) {
        this.mNumber = in.readInt();
        this.mTime = in.readString();
    }

    /**
     * Initialize a new lap registered with a chronometer.
     *
     * @param number Number of the lap.
     * @param time Time of the lap(Format: MM:SS:MLS).
     */
    public Lap(int number, String time) {
        mNumber = number;
        mTime = time;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNumber);
        dest.writeString(this.mTime);
    }

    public static final Parcelable.Creator<Lap> CREATOR = new Parcelable.Creator<Lap>() {
        @Override
        public Lap createFromParcel(Parcel source) {
            return new Lap(source);
        }

        @Override
        public Lap[] newArray(int size) {
            return new Lap[size];
        }
    };


    /******************* GETTERS / SETTERS *******************/

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        this.mNumber = number;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        this.mTime = time;
    }

}