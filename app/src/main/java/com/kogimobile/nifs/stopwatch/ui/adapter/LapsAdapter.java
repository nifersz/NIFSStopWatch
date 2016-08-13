package com.kogimobile.nifs.stopwatch.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.kogimobile.nifs.stopwatch.R;
import com.kogimobile.nifs.stopwatch.model.Lap;

/**
 * Recycler view adapter to handle the chronometer laps
 * stored while the time is running.
 *
 * @author Nelson Ivan Fernandez Suarez
 */
public class LapsAdapter extends RecyclerView.Adapter<LapsAdapter.LapViewHolder> {

    /** Layout inflater from activity context. */
    private LayoutInflater mInflater;
    /** Stores all items necessary to build the laps list. */
    private ArrayList<Lap> mLapsItems;

    /** Tag for logs shown in this class. */
    private static final String TAG = "LapsAdapter";

    /**
     * Create a new adapter for the laps stored from
     * the chronometer.
     *
     * @param ctxt Android activity context.
     */
    public LapsAdapter(Context ctxt) {
        if (ctxt == null) {
            Log.e(TAG, "There are not activity context for this adapter.");
            return;
        }

        this.mLapsItems = new ArrayList<>();
        this.mInflater = LayoutInflater.from(ctxt);
    }


    @Override
    public int getItemCount() {
        if (mLapsItems == null) {
            Log.e(TAG, "Lap items reference is null.");
            return 0;
        }
        return mLapsItems.size();
    }

    @Override
    public LapViewHolder onCreateViewHolder(ViewGroup parent,
                                            int viewType) {
        View v = mInflater.inflate(R.layout.card_lap_item, parent, false);
        LapViewHolder lapVH = new LapViewHolder(v);
        return lapVH;
    }

    @Override
    public void onBindViewHolder(LapViewHolder lapVHolder,
                                 int pos) {
        lapVHolder.lapTime.setText(mLapsItems.get(pos).getTime());
        lapVHolder.lapNumber.setText(String.valueOf(mLapsItems.get(pos).getNumber()));
    }

    /**
     * Add a new lap to this adapter, this lap appears at
     * the top of the lap list.
     *
     * @param newLap New chronometer lap to be added to this adapter.
     */
    public void addNewLap(Lap newLap) {
        if (newLap == null) {
            Log.e(TAG, "The new lap to be added is null.");
            return;
        }

        mLapsItems.add(0, newLap);
        notifyDataSetChanged();
    }

    /**
     * After an orientation change, this method restores the
     * lap list elements.
     *
     * @param lapList Lap list to restore the user interface.
     */
    public void restoreLaps(ArrayList<Lap> lapList) {
        if (lapList == null || lapList.isEmpty()) {
            return;
        }

        mLapsItems.clear();
        mLapsItems.addAll(lapList);
        notifyDataSetChanged();
    }

    /**
     * Cleans all the laps in the adapter, and updates the
     * user interface.
     */
    public void clear() {
        mLapsItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Get all the laps currently stored in this adapter.
     *
     * @return A list of chronometer laps.
     */
    public ArrayList<Lap> getItems() {
        if (mLapsItems == null) {
            return new ArrayList<>();
        }

        return mLapsItems;
    }


    /**
     * View holder related to the 'Lap' class, to
     * be associated with the 'LapsAdapter'.
     *
     * @author Nelson Ivan Fernandez Suarez
     */
    public static class LapViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_lap_number)
        TextView lapNumber;

        @BindView(R.id.text_lap_time)
        TextView lapTime;

        LapViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}