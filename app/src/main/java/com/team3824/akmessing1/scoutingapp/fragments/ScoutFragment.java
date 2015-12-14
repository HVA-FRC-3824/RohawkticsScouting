package com.team3824.akmessing1.scoutingapp.fragments;

import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.team3824.akmessing1.scoutingapp.ScoutValue;
import com.team3824.akmessing1.scoutingapp.views.CustomScoutView;

import java.util.Map;

// abstract base class for each of the match scouting fragments
public abstract class ScoutFragment extends Fragment {

    private String TAG = "ScoutFragment";
    protected Map<String, ScoutValue> valueMap;

    public ScoutFragment()
    {
    }

    public void setValuesMap(Map<String, ScoutValue> map)
    {
        valueMap = map;
    }

    // Recursive functions to get all the values and store them in a map
    public void writeContentsToMap(Map<String, ScoutValue> map)
    {
        // Get the ViewGroup holding all of the widgets
        ViewGroup vg = (ViewGroup) getView();
        if (vg == null) {
            Log.d(TAG,"Null view");
            // If the view has been destroyed, state should already be saved
            // to parent activity
            return;
        }
        int childCount = vg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = vg.getChildAt(i);
            if (view instanceof CustomScoutView) {
                ((CustomScoutView) view).writeToMap(map);
            } else if (view instanceof ViewGroup) {
                writeContentsToMap(map, (ViewGroup) view);
            }
        }
    }

    public void writeContentsToMap(Map<String, ScoutValue> map, ViewGroup viewGroup)
    {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof CustomScoutView) {
                ((CustomScoutView) view).writeToMap(map);
            } else if (view instanceof ViewGroup) {
                writeContentsToMap(map, (ViewGroup) view);
            }
        }
    }

    // Recursive function to get all the values from a map and populate the fields
    public void restoreContentsFromMap(Map<String, ScoutValue> map) {
        // Get the ViewGroup holding all of the widgets
        ViewGroup vg = (ViewGroup) getView();
        if (vg == null) {
            Log.d(TAG,"Null view");
            // If the view has been destroyed, state should already be saved
            // to parent activity
            return;
        }
        int childCount = vg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = vg.getChildAt(i);
            if (view instanceof CustomScoutView) {
                ((CustomScoutView) view).restoreFromMap(map);
            } else if (view instanceof ViewGroup) {
                restoreContentsFromMap(map, (ViewGroup) view);
            }
        }
    }

    public void restoreContentsFromMap(Map<String, ScoutValue> map, ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof CustomScoutView) {
                ((CustomScoutView) view).restoreFromMap(map);
            } else if (view instanceof ViewGroup) {
                restoreContentsFromMap(map, (ViewGroup) view);
            }
        }
    }
}
