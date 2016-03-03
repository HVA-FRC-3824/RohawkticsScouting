package com.team3824.akmessing1.scoutingapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.team3824.akmessing1.scoutingapp.utilities.Constants;
import com.team3824.akmessing1.scoutingapp.R;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutMap;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutValue;
import com.team3824.akmessing1.scoutingapp.database_helpers.PitScoutDB;

import java.util.Map;

public class TeamPitData extends Fragment{
    public TeamPitData() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_pit_data, container, false);

        Bundle args = getArguments();
        int teamNumber = args.getInt(Constants.TEAM_NUMBER, -1);
        Activity activity = getActivity();
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.APP_DATA, Context.MODE_PRIVATE);
        String eventID = sharedPreferences.getString(Constants.EVENT_ID, "");
        PitScoutDB pitScoutDB = new PitScoutDB(activity,eventID);
        ScoutMap pitMap = pitScoutDB.getTeamMap(teamNumber);

        TextView textView = (TextView)view.findViewById(R.id.pit_robot_width);
        if(pitMap.containsKey(Constants.PIT_ROBOT_WIDTH))
        {
            textView.setText(pitMap.getString(Constants.PIT_ROBOT_WIDTH));
        }

        textView = (TextView)view.findViewById(R.id.pit_robot_length);
        if(pitMap.containsKey(Constants.PIT_ROBOT_LENGTH))
        {
            textView.setText(pitMap.getString(Constants.PIT_ROBOT_LENGTH));
        }

        textView = (TextView)view.findViewById(R.id.pit_robot_height);
        if(pitMap.containsKey(Constants.PIT_ROBOT_HEIGHT))
        {
            textView.setText(pitMap.getString(Constants.PIT_ROBOT_HEIGHT));
        }

        textView = (TextView)view.findViewById(R.id.pit_robot_weight);
        if(pitMap.containsKey(Constants.PIT_ROBOT_WEIGHT))
        {
            textView.setText(pitMap.getString(Constants.PIT_ROBOT_WEIGHT));
        }

        textView = (TextView)view.findViewById(R.id.pit_number_cims);
        if(pitMap.containsKey(Constants.PIT_NUMBER_OF_CIMS))
        {
            textView.setText(pitMap.getString(Constants.PIT_NUMBER_OF_CIMS));
        }

        textView = (TextView)view.findViewById(R.id.pit_drivetrain);
        if(pitMap.containsKey(Constants.PIT_DRIVETRAIN))
        {
            textView.setText(pitMap.getString(Constants.PIT_DRIVETRAIN));
        }

        textView = (TextView)view.findViewById(R.id.pit_programming_language);
        if(pitMap.containsKey(Constants.PIT_PROGRAMMING_LANGUAGE))
        {
            textView.setText(pitMap.getString(Constants.PIT_PROGRAMMING_LANGUAGE));
        }

        textView = (TextView)view.findViewById(R.id.pit_notes);
        if(pitMap.containsKey(Constants.PIT_NOTES))
        {
            textView.setText(pitMap.getString(Constants.PIT_NOTES));
        }

        return view;
    }

}
