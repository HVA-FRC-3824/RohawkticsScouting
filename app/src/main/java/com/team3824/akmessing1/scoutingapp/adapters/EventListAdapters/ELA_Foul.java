package com.team3824.akmessing1.scoutingapp.adapters.EventListAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.team3824.akmessing1.scoutingapp.R;
import com.team3824.akmessing1.scoutingapp.list_items.event_list_items.ELI_Fouls;

import java.util.ArrayList;

/**
 * Adapter for the Event List when fouls are used to compare teams
 */

public class ELA_Foul extends ArrayAdapter<ELI_Fouls> {

    private final String TAG = "ELA_Foul";

    private ArrayList<ELI_Fouls> mTeams;

    /**
     * @param context
     * @param teams
     */
    public ELA_Foul(Context context, ArrayList<ELI_Fouls> teams) {
        super(context, R.layout.list_item_event_fouls, teams);
        mTeams = teams;
    }

    /**
     * @param position
     * @param convertView
     * @param parentView
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_event_fouls, null);
        }

        ELI_Fouls team = mTeams.get(position);

        team.mRank = position;
        TextView textView;

        //Header row
        if (team.mTeamNumber == -1) {
            textView = (TextView) convertView.findViewById(R.id.event_rank);
            textView.setText("Rank");
            textView = (TextView) convertView.findViewById(R.id.event_teamNum);
            textView.setText("Team Number");
            textView = (TextView) convertView.findViewById(R.id.event_fouls);
            textView.setText("Fouls");
            textView = (TextView) convertView.findViewById(R.id.event_tech_fouls);
            textView.setText("Tech Fouls");
            textView = (TextView) convertView.findViewById(R.id.event_yellow_cards);
            textView.setText("Yellow Cards");
            textView = (TextView) convertView.findViewById(R.id.event_red_cards);
            textView.setText("Red Cards");
        } else {
            textView = (TextView) convertView.findViewById(R.id.event_rank);
            textView.setText(String.valueOf(team.mRank));
            textView = (TextView) convertView.findViewById(R.id.event_teamNum);
            textView.setText(String.valueOf(team.mTeamNumber));
            textView = (TextView) convertView.findViewById(R.id.event_fouls);
            textView.setText(String.valueOf(team.mFouls));
            textView = (TextView) convertView.findViewById(R.id.event_tech_fouls);
            textView.setText(String.valueOf(team.mTechFouls));
            textView = (TextView) convertView.findViewById(R.id.event_yellow_cards);
            textView.setText(String.valueOf(team.mYellowCards));
            textView = (TextView) convertView.findViewById(R.id.event_red_cards);
            textView.setText(String.valueOf(team.mRedCards));
        }

        return convertView;
    }

}
