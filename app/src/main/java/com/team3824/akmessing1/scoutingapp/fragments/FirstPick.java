package com.team3824.akmessing1.scoutingapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobeta.android.dslv.DragSortListView;
import com.team3824.akmessing1.scoutingapp.database_helpers.PitScoutDB;
import com.team3824.akmessing1.scoutingapp.R;
import com.team3824.akmessing1.scoutingapp.ScoutValue;
import com.team3824.akmessing1.scoutingapp.database_helpers.StatsDB;
import com.team3824.akmessing1.scoutingapp.Team;
import com.team3824.akmessing1.scoutingapp.adapters.PickListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class FirstPick extends ScoutFragment{

    DragSortListView list;
    PickListAdapter adapter;

    public FirstPick() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first_pick, container, false);
        list = (DragSortListView)view.findViewById(R.id.first_pick_list);

        final Comparator<Team> compare = new Comparator<Team>(){
            public int compare(Team a, Team b)
            {
                int rankA = a.getMapElement(StatsDB.KEY_FIRST_PICK_RANK).getInt();
                int rankB = b.getMapElement(StatsDB.KEY_FIRST_PICK_RANK).getInt();
                if(a.getMapElement(StatsDB.KEY_PICKED).getInt() > 0 && b.getMapElement(StatsDB.KEY_PICKED).getInt() > 0)
                {
                    return rankA - rankB;
                }
                else if( a.getMapElement(StatsDB.KEY_PICKED).getInt() > 0 )
                {
                    return 1;
                }
                else if(b.getMapElement(StatsDB.KEY_PICKED).getInt() > 0)
                {
                    return -1;
                }
                else {
                    return rankA - rankB;
                }
            }
        };

        ArrayList<Team> teams = new ArrayList<>();
        SharedPreferences sharedPref = getActivity().getSharedPreferences("appData", Context.MODE_PRIVATE);
        final StatsDB statsDB = new StatsDB(getContext(),sharedPref.getString("event_id",""));
        Cursor statsCursor = statsDB.getStats();
        PitScoutDB pitScoutDB = new PitScoutDB(getContext(),sharedPref.getString("event_id",""));
        do{
            int teamNumber = statsCursor.getInt(statsCursor.getColumnIndex(StatsDB.KEY_TEAM_NUMBER));
            Cursor pitCursor = pitScoutDB.getTeamInfo(teamNumber);

            Team team = new Team(teamNumber,
                    pitCursor.getString(pitCursor.getColumnIndex(PitScoutDB.KEY_NICKNAME)));

            HashMap<String, ScoutValue> map = new HashMap<>();

            if(pitCursor.getColumnIndex("robotPicture") != -1) {
                map.put("robotPicture", new ScoutValue(pitCursor.getString(pitCursor.getColumnIndex("robotPicture"))));
            }

            int firstRank = statsCursor.getInt(statsCursor.getColumnIndex(StatsDB.KEY_FIRST_PICK_RANK));
            int computedFirstPickRank = statsCursor.getInt(statsCursor.getColumnIndex(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK));
            int picked = statsCursor.getInt(statsCursor.getColumnIndex(StatsDB.KEY_PICKED));
            map.put(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(firstRank));
            map.put(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK,new ScoutValue(computedFirstPickRank));
            map.put(StatsDB.KEY_PICKED, new ScoutValue(picked));

            String bottomText = "Pick Rank: "+String.valueOf(map.get(StatsDB.KEY_FIRST_PICK_RANK).getInt())+" Computed Pick Rank: "+String.valueOf(map.get(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK).getInt());
            map.put("first_pick_bottom_text", new ScoutValue(bottomText));

            team.setValueMap(map);
            teams.add(team);

            statsCursor.moveToNext();
        }while(!statsCursor.isAfterLast());
        Collections.sort(teams, compare);

        adapter = new PickListAdapter(getContext(),R.id.first_pick_list,teams,1);
        list.setAdapter(adapter);

        list.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from < to) {
                    HashMap<String, ScoutValue> map = new HashMap<String, ScoutValue>();
                    Team team = adapter.getItem(from);
                    team.setMapElement(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(to + 1));
                    int teamNumber = team.getTeamNumber();
                    int firstPick = to + 1;
                    int computedFirstPick = team.getMapElement(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK).getInt();
                    String bottomText = "Pick Rank: " + String.valueOf(firstPick) + " Computed Pick Rank: " + String.valueOf(computedFirstPick);
                    team.setMapElement("first_pick_bottom_text", new ScoutValue(bottomText));
                    adapter.remove(team);
                    adapter.add(to, team);
                    map.put(StatsDB.KEY_TEAM_NUMBER, new ScoutValue(teamNumber));
                    map.put(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(firstPick));
                    map.put(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK, new ScoutValue(computedFirstPick));
                    statsDB.updateStats(map);
                    for (int i = from; i < to; i++) {
                        team = adapter.getItem(i);
                        team.setMapElement(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(i + 1));
                        teamNumber = team.getTeamNumber();
                        firstPick = i + 1;
                        computedFirstPick = team.getMapElement(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK).getInt();
                        bottomText = "Pick Rank: " + String.valueOf(firstPick) + " Computed Pick Rank: " + String.valueOf(computedFirstPick);
                        team.setMapElement("first_pick_bottom_text", new ScoutValue(bottomText));
                        map.put(StatsDB.KEY_TEAM_NUMBER, new ScoutValue(teamNumber));
                        map.put(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(firstPick));
                        map.put(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK, new ScoutValue(computedFirstPick));
                        statsDB.updateStats(map);
                    }
                } else if (from > to) {
                    HashMap<String, ScoutValue> map = new HashMap<String, ScoutValue>();
                    Team team = adapter.getItem(from);
                    int teamNumber = team.getTeamNumber();
                    int firstPick = to + 1;
                    int computedFirstPick = team.getMapElement(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK).getInt();
                    team.setMapElement(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(to + 1));
                    String bottomText = "Pick Rank: " + String.valueOf(firstPick) + " Computed Pick Rank: " + String.valueOf(computedFirstPick);
                    team.setMapElement("first_pick_bottom_text", new ScoutValue(bottomText));
                    adapter.remove(team);
                    adapter.add(to, team);
                    map.put(StatsDB.KEY_TEAM_NUMBER, new ScoutValue(teamNumber));
                    map.put(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(firstPick));
                    map.put(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK, new ScoutValue(computedFirstPick));
                    statsDB.updateStats(map);
                    for (int i = to + 1; i <= from; i++) {
                        team = adapter.getItem(i);
                        team.setMapElement(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(i + 1));
                        teamNumber = team.getTeamNumber();
                        firstPick = i + 1;
                        computedFirstPick = team.getMapElement(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK).getInt();
                        bottomText = "Pick Rank: " + String.valueOf(firstPick) + " Computed Pick Rank: " + String.valueOf(computedFirstPick);
                        team.setMapElement("first_pick_bottom_text", new ScoutValue(bottomText));
                        team.setMapElement("first_pick_bottom_text", new ScoutValue(bottomText));
                        map.put(StatsDB.KEY_TEAM_NUMBER, new ScoutValue(teamNumber));
                        map.put(StatsDB.KEY_FIRST_PICK_RANK, new ScoutValue(firstPick));
                        map.put(StatsDB.KEY_COMPUTED_FIRST_PICK_RANK, new ScoutValue(computedFirstPick));
                        statsDB.updateStats(map);
                    }
                }

                adapter.notifyDataSetChanged();

            }
        });

        return view;
    }


}