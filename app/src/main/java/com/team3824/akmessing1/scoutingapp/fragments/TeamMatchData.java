package com.team3824.akmessing1.scoutingapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.team3824.akmessing1.scoutingapp.utilities.Constants;
import com.team3824.akmessing1.scoutingapp.R;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutMap;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutValue;
import com.team3824.akmessing1.scoutingapp.database_helpers.StatsDB;
import com.team3824.akmessing1.scoutingapp.list_items.ELI_Points;

import java.util.Map;


public class TeamMatchData extends Fragment {

    public TeamMatchData() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_match_data, container, false);

        Bundle args = getArguments();
        int teamNumber = args.getInt(Constants.TEAM_NUMBER, -1);
        Activity activity = getActivity();
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.APP_DATA, Context.MODE_PRIVATE);
        String eventID = sharedPreferences.getString(Constants.EVENT_ID, "");
        StatsDB statsDB = new StatsDB(activity,eventID);
        ScoutMap statsMap = statsDB.getTeamStats(teamNumber);

        // Setup Points header row
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.points_header);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.event_teamNum).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_avg_points)).setText("Avg Points");
        ((TextView)linearLayout.findViewById(R.id.event_points)).setText("Total Points");
        ((TextView)linearLayout.findViewById(R.id.event_high_points)).setText("High Goal Points");
        ((TextView)linearLayout.findViewById(R.id.event_low_points)).setText("Low Goal Points");
        ((TextView)linearLayout.findViewById(R.id.event_defense_points)).setText("Defenses Points");
        ((TextView)linearLayout.findViewById(R.id.event_auto_points)).setText("Auto Points");
        ((TextView)linearLayout.findViewById(R.id.event_teleop_points)).setText("Teleop Points");
        ((TextView)linearLayout.findViewById(R.id.event_endgame_points)).setText("Endgame Points");
        ((TextView)linearLayout.findViewById(R.id.event_foul_points)).setText("Foul Points");


        linearLayout = (LinearLayout)view.findViewById(R.id.points);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.event_teamNum).setVisibility(View.GONE);

        ELI_Points team = new ELI_Points(teamNumber);

        boolean hasPlayed = statsMap.containsKey(Constants.TOTAL_MATCHES);

        if(hasPlayed) {
            float totalMatches = statsMap.getInt(Constants.TOTAL_MATCHES);
            ((TextView)view.findViewById(R.id.num_matches)).setText(String.valueOf((int)totalMatches));

            team.mHighPoints = statsMap.getInt(Constants.TOTAL_TELEOP_HIGH_HIT) * 5 +
                    statsMap.getInt(Constants.TOTAL_AUTO_HIGH_HIT) * 10;

            team.mLowPoints = statsMap.getInt(Constants.TOTAL_TELEOP_LOW_HIT) * 2 +
                    statsMap.getInt(Constants.TOTAL_AUTO_LOW_HIT) * 5;

            team.mEndgamePoints = statsMap.getInt(Constants.TOTAL_CHALLENGE) * 5 +
                    statsMap.getInt(Constants.TOTAL_SCALE) * 15;

            for (int i = 0; i < 9; i++) {
                team.mDefensePoints += statsMap.getInt(Constants.TOTAL_DEFENSES_AUTO_REACHED[i]) * 2;
                team.mDefensePoints += statsMap.getInt(Constants.TOTAL_DEFENSES_AUTO_CROSSED[i]) * 10;
                team.mDefensePoints += statsMap.getInt(Constants.TOTAL_DEFENSES_TELEOP_CROSSED_POINTS[i]) * 5;
            }

            team.mTeleopPoints = statsMap.getInt(Constants.TOTAL_TELEOP_HIGH_HIT) * 5 +
                    statsMap.getInt(Constants.TOTAL_TELEOP_LOW_HIT) * 2;
            for (int i = 0; i < 9; i++) {
                team.mTeleopPoints += statsMap.getInt(Constants.TOTAL_DEFENSES_TELEOP_CROSSED_POINTS[i]) * 5;
            }

            team.mAutoPoints = statsMap.getInt(Constants.TOTAL_AUTO_HIGH_HIT) * 10 +
                    statsMap.getInt(Constants.TOTAL_AUTO_LOW_HIT) * 5;
            for (int i = 0; i < 9; i++) {
                team.mAutoPoints += statsMap.getInt(Constants.TOTAL_DEFENSES_AUTO_REACHED[i]) * 2;
                team.mAutoPoints += statsMap.getInt(Constants.TOTAL_DEFENSES_AUTO_CROSSED[i]) * 10;
            }


            team.mFoulPoints = statsMap.getInt(Constants.TOTAL_FOULS) * -5 +
                    statsMap.getInt(Constants.TOTAL_TECH_FOULS) * -5;

            team.mTotalPoints = team.mEndgamePoints + team.mTeleopPoints + team.mAutoPoints + team.mFoulPoints;

            team.mAvgPoints = (totalMatches == 0.0f) ? 0.0f : (float) team.mTotalPoints / totalMatches;

            ((TextView)linearLayout.findViewById(R.id.event_avg_points)).setText(String.valueOf(team.mAvgPoints));
            ((TextView)linearLayout.findViewById(R.id.event_points)).setText(String.valueOf(team.mTotalPoints));
            ((TextView)linearLayout.findViewById(R.id.event_high_points)).setText(String.valueOf(team.mHighPoints));
            ((TextView)linearLayout.findViewById(R.id.event_low_points)).setText(String.valueOf(team.mLowPoints));
            ((TextView)linearLayout.findViewById(R.id.event_defense_points)).setText(String.valueOf(team.mDefensePoints));
            ((TextView)linearLayout.findViewById(R.id.event_auto_points)).setText(String.valueOf(team.mAutoPoints));
            ((TextView)linearLayout.findViewById(R.id.event_teleop_points)).setText(String.valueOf(team.mTeleopPoints));
            ((TextView)linearLayout.findViewById(R.id.event_endgame_points)).setText(String.valueOf(team.mEndgamePoints));
            ((TextView)linearLayout.findViewById(R.id.event_foul_points)).setText(String.valueOf(team.mFoulPoints));
        }
        else
        {
            ((TextView)view.findViewById(R.id.num_matches)).setText("0");
            ((TextView)linearLayout.findViewById(R.id.event_avg_points)).setText("0.0");
            ((TextView)linearLayout.findViewById(R.id.event_points)).setText("0");
            ((TextView)linearLayout.findViewById(R.id.event_high_points)).setText("0");
            ((TextView)linearLayout.findViewById(R.id.event_low_points)).setText("0");
            ((TextView)linearLayout.findViewById(R.id.event_defense_points)).setText("0");
            ((TextView)linearLayout.findViewById(R.id.event_auto_points)).setText("0");
            ((TextView)linearLayout.findViewById(R.id.event_teleop_points)).setText("0");
            ((TextView)linearLayout.findViewById(R.id.event_endgame_points)).setText("0");
            ((TextView)linearLayout.findViewById(R.id.event_foul_points)).setText("0");
        }


        // Setup Defenses header row
        linearLayout = (LinearLayout)view.findViewById(R.id.defense_header);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_teamNum)).setText("Defense");
        ((TextView)linearLayout.findViewById(R.id.event_cross)).setText("Auto Cross");
        ((TextView)linearLayout.findViewById(R.id.event_reach)).setText("Auto Reach");
        ((TextView)linearLayout.findViewById(R.id.event_seen)).setText("Seen");
        ((TextView)linearLayout.findViewById(R.id.event_teleop_cross)).setText("Teleop Cross");
        ((TextView)linearLayout.findViewById(R.id.event_time)).setText("Time (s)");

        // Portcullis row
        linearLayout = (LinearLayout)view.findViewById(R.id.portcullis);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.PORTCULLIS_INDEX);


        // Cheval de Frise row
        linearLayout = (LinearLayout)view.findViewById(R.id.cheval_de_frise);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.CHEVAL_DE_FRISE_INDEX);

        // Moat row
        linearLayout = (LinearLayout)view.findViewById(R.id.moat);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.MOAT_INDEX);

        // Ramparts row
        linearLayout = (LinearLayout)view.findViewById(R.id.ramparts);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.RAMPARTS_INDEX);

        // Drawbridge row
        linearLayout = (LinearLayout)view.findViewById(R.id.drawbridge);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.DRAWBRIDGE_INDEX);

        // Sally Port row
        linearLayout = (LinearLayout)view.findViewById(R.id.sally_port);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.SALLY_PORT_INDEX);

        // Rock Wall row
        linearLayout = (LinearLayout)view.findViewById(R.id.rock_wall);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.ROCK_WALL_INDEX);

        // Rough Terrain row
        linearLayout = (LinearLayout)view.findViewById(R.id.rough_terrain);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.ROUGH_TERRAIN_INDEX);

        // Low Bar row
        linearLayout = (LinearLayout)view.findViewById(R.id.low_bar);
        setupDefenseRow(linearLayout, statsMap, hasPlayed, Constants.LOW_BAR_INDEX);

        // Setup shooting header row
        linearLayout = (LinearLayout)view.findViewById(R.id.shot_header);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_teamNum)).setText("Goal");
        ((TextView)linearLayout.findViewById(R.id.event_auto_made)).setText("Auto Made");
        ((TextView)linearLayout.findViewById(R.id.event_auto_taken)).setText("Auto Taken");
        ((TextView)linearLayout.findViewById(R.id.event_auto_percentage)).setText("Auto Percentage");
        ((TextView)linearLayout.findViewById(R.id.event_teleop_made)).setText("Teleop Made");
        ((TextView)linearLayout.findViewById(R.id.event_teleop_taken)).setText("Teleop Taken");
        ((TextView)linearLayout.findViewById(R.id.event_teleop_percentage)).setText("Teleop Percentage");

        // High Goal
        linearLayout = (LinearLayout)view.findViewById(R.id.high);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_teamNum)).setText("High");
        if(hasPlayed) {
            ((TextView) linearLayout.findViewById(R.id.event_auto_made)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_AUTO_HIGH_HIT)));
            ((TextView) linearLayout.findViewById(R.id.event_auto_taken)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_AUTO_HIGH_HIT) + statsMap.getInt(Constants.TOTAL_AUTO_HIGH_MISS)));
            float percent = (statsMap.get(Constants.TOTAL_AUTO_HIGH_HIT).getInt()+statsMap.getInt(Constants.TOTAL_AUTO_HIGH_MISS) > 0) ? statsMap.getInt(Constants.TOTAL_AUTO_HIGH_HIT) / (float)(statsMap.getInt(Constants.TOTAL_AUTO_HIGH_HIT)+statsMap.getInt(Constants.TOTAL_AUTO_HIGH_MISS)) : 0.0f;
            percent *= 100.0;
            ((TextView) linearLayout.findViewById(R.id.event_auto_percentage)).setText(String.format("%.1f%%", percent));
            ((TextView) linearLayout.findViewById(R.id.event_teleop_made)).setText(String.valueOf(statsMap.get(Constants.TOTAL_TELEOP_HIGH_HIT).getInt()));
            ((TextView) linearLayout.findViewById(R.id.event_teleop_taken)).setText(String.valueOf(statsMap.get(Constants.TOTAL_TELEOP_HIGH_HIT).getInt()+statsMap.get(Constants.TOTAL_TELEOP_HIGH_MISS).getInt()));
            percent = (statsMap.getInt(Constants.TOTAL_TELEOP_HIGH_HIT)+statsMap.getInt(Constants.TOTAL_TELEOP_HIGH_MISS) > 0) ? statsMap.getFloat(Constants.TOTAL_TELEOP_HIGH_HIT) / (statsMap.getFloat(Constants.TOTAL_TELEOP_HIGH_HIT)+statsMap.getFloat(Constants.TOTAL_TELEOP_HIGH_MISS)) : 0.0f;
            percent *= 100.0;
            ((TextView) linearLayout.findViewById(R.id.event_teleop_percentage)).setText(String.format("%.1f%%",percent));
        }
        else
        {
            ((TextView) linearLayout.findViewById(R.id.event_auto_made)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_auto_taken)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_auto_percentage)).setText("0.0%");
            ((TextView) linearLayout.findViewById(R.id.event_teleop_made)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_teleop_taken)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_teleop_percentage)).setText("0.0%");
        }

        // Low Goal
        linearLayout = (LinearLayout)view.findViewById(R.id.low);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_teamNum)).setText("Low");
        if(hasPlayed) {
            ((TextView) linearLayout.findViewById(R.id.event_auto_made)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_AUTO_LOW_HIT)));
            ((TextView) linearLayout.findViewById(R.id.event_auto_taken)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_AUTO_LOW_HIT) + statsMap.getInt(Constants.TOTAL_AUTO_LOW_MISS)));
            float percent = (statsMap.getInt(Constants.TOTAL_AUTO_LOW_HIT)+statsMap.getInt(Constants.TOTAL_AUTO_LOW_MISS) > 0) ? statsMap.getFloat(Constants.TOTAL_AUTO_LOW_HIT) / (statsMap.getFloat(Constants.TOTAL_AUTO_LOW_HIT)+statsMap.getFloat(Constants.TOTAL_AUTO_LOW_MISS)) : 0.0f;
            percent *= 100.0;
            ((TextView) linearLayout.findViewById(R.id.event_auto_percentage)).setText(String.format("%.1f%%", percent));
            ((TextView) linearLayout.findViewById(R.id.event_teleop_made)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_TELEOP_LOW_HIT)));
            ((TextView) linearLayout.findViewById(R.id.event_teleop_taken)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_TELEOP_LOW_HIT)+statsMap.getInt(Constants.TOTAL_TELEOP_LOW_MISS)));
            percent = (statsMap.getInt(Constants.TOTAL_TELEOP_LOW_HIT)+statsMap.getInt(Constants.TOTAL_TELEOP_LOW_MISS) > 0) ? statsMap.getInt(Constants.TOTAL_TELEOP_LOW_HIT) / (statsMap.getFloat(Constants.TOTAL_TELEOP_LOW_HIT)+statsMap.getFloat(Constants.TOTAL_TELEOP_LOW_MISS)) : 0.0f;
            percent *= 100.0;
            ((TextView) linearLayout.findViewById(R.id.event_teleop_percentage)).setText(String.format("%.1f%%",percent));
        }
        else
        {
            ((TextView) linearLayout.findViewById(R.id.event_auto_made)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_auto_taken)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_auto_percentage)).setText("0.0%");
            ((TextView) linearLayout.findViewById(R.id.event_teleop_made)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_teleop_taken)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_teleop_percentage)).setText("0.0%");
        }

        if(hasPlayed && statsMap.containsKey(Constants.DEFENSE_ABILITY_RANKING)) {
            String ranking = statsMap.get(Constants.DEFENSE_ABILITY_RANKING).getString();
            switch(ranking.charAt(ranking.length()-1))
            {
                case '1':
                    ranking += "st";
                    break;
                case '2':
                    ranking += "nd";
                    break;
                case '3':
                    ranking += "rd";
                    break;
                default:
                    ranking += "th";
                    break;
            }
            ((TextView) view.findViewById(R.id.defense_ability)).setText(ranking);
            ranking = statsMap.getString(Constants.DRIVER_ABILITY_RANKING);
            switch(ranking.charAt(ranking.length()-1))
            {
                case '1':
                    ranking += "st";
                    break;
                case '2':
                    ranking += "nd";
                    break;
                case '3':
                    ranking += "rd";
                    break;
                default:
                    ranking += "th";
                    break;
            }
            ((TextView) view.findViewById(R.id.driver_ability)).setText(ranking);
        }


        // Setup endgame header row
        linearLayout = (LinearLayout)view.findViewById(R.id.endgame_header);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.event_teamNum).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_challenge)).setText("Challenge");
        ((TextView)linearLayout.findViewById(R.id.event_scale)).setText("Scale");

        linearLayout = (LinearLayout)view.findViewById(R.id.endgame);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.event_teamNum).setVisibility(View.GONE);
        if(hasPlayed) {
            ((TextView) linearLayout.findViewById(R.id.event_challenge)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_CHALLENGE)));
            ((TextView) linearLayout.findViewById(R.id.event_scale)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_SCALE)));
        }
        else
        {
            ((TextView) linearLayout.findViewById(R.id.event_challenge)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_scale)).setText("0");
        }

        // Setup fouls header row
        linearLayout = (LinearLayout)view.findViewById(R.id.fouls_header);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.event_teamNum).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_fouls)).setText("Fouls");
        ((TextView)linearLayout.findViewById(R.id.event_tech_fouls)).setText("Tech Fouls");
        ((TextView)linearLayout.findViewById(R.id.event_yellow_cards)).setText("Yellow Cards");
        ((TextView)linearLayout.findViewById(R.id.event_red_cards)).setText("Red Cards");

        linearLayout = (LinearLayout)view.findViewById(R.id.fouls);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.event_teamNum).setVisibility(View.GONE);
        if(hasPlayed) {
            ((TextView) linearLayout.findViewById(R.id.event_fouls)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_FOULS)));
            ((TextView) linearLayout.findViewById(R.id.event_tech_fouls)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_TECH_FOULS)));
            ((TextView) linearLayout.findViewById(R.id.event_yellow_cards)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_YELLOW_CARDS)));
            ((TextView) linearLayout.findViewById(R.id.event_red_cards)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_RED_CARDS)));
        }
        else
        {
            ((TextView) linearLayout.findViewById(R.id.event_fouls)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_tech_fouls)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_yellow_cards)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_red_cards)).setText("0");
        }

        //Setup Misc
        linearLayout = (LinearLayout)view.findViewById(R.id.misc_header);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_teamNum)).setText("DQs");
        ((TextView)linearLayout.findViewById(R.id.event_challenge)).setText("Didn't Show Up");
        ((TextView)linearLayout.findViewById(R.id.event_scale)).setText("Stopped Working");

        linearLayout = (LinearLayout)view.findViewById(R.id.misc);
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        if(hasPlayed) {
            ((TextView) linearLayout.findViewById(R.id.event_teamNum)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_DQ)));
            ((TextView) linearLayout.findViewById(R.id.event_challenge)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_DIDNT_SHOW_UP)));
            ((TextView) linearLayout.findViewById(R.id.event_scale)).setText(String.valueOf(statsMap.getInt(Constants.TOTAL_STOPPED)));
        }
        else
        {
            ((TextView) linearLayout.findViewById(R.id.event_teamNum)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_challenge)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_scale)).setText("0");
        }

        statsDB.close();
        return view;
    }


    public void setupDefenseRow(LinearLayout linearLayout, ScoutMap statsMap, boolean hasPlayed, int defense_index)
    {
        linearLayout.findViewById(R.id.event_rank).setVisibility(View.GONE);
        ((TextView)linearLayout.findViewById(R.id.event_teamNum)).setText(Constants.DEFENSES_LABEL[defense_index]);
        if(hasPlayed) {
            ((TextView) linearLayout.findViewById(R.id.event_cross)).setText(statsMap.getString(Constants.TOTAL_DEFENSES_AUTO_CROSSED[defense_index]));
            ((TextView) linearLayout.findViewById(R.id.event_reach)).setText(statsMap.getString(Constants.TOTAL_DEFENSES_AUTO_REACHED[defense_index]));
            ((TextView) linearLayout.findViewById(R.id.event_seen)).setText(statsMap.getString(Constants.TOTAL_DEFENSES_SEEN[defense_index]));
            ((TextView) linearLayout.findViewById(R.id.event_teleop_cross)).setText(statsMap.getString(Constants.TOTAL_DEFENSES_TELEOP_CROSSED[defense_index]));
            ((TextView) linearLayout.findViewById(R.id.event_time)).setText((statsMap.getInt(Constants.TOTAL_DEFENSES_TELEOP_CROSSED[defense_index]) > 0) ? statsMap.getString(Constants.TOTAL_DEFENSES_TELEOP_TIME[defense_index]) : "-1");
        }
        else
        {
            ((TextView) linearLayout.findViewById(R.id.event_cross)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_reach)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_seen)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_teleop_cross)).setText("0");
            ((TextView) linearLayout.findViewById(R.id.event_time)).setText("-1");
        }
    }
}
