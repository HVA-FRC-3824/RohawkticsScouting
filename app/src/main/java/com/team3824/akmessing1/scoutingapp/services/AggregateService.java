package com.team3824.akmessing1.scoutingapp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.team3824.akmessing1.scoutingapp.utilities.Constants;
import com.team3824.akmessing1.scoutingapp.database_helpers.MatchScoutDB;
import com.team3824.akmessing1.scoutingapp.utilities.SchulzeMethod;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutValue;
import com.team3824.akmessing1.scoutingapp.database_helpers.PitScoutDB;
import com.team3824.akmessing1.scoutingapp.database_helpers.ScheduleDB;
import com.team3824.akmessing1.scoutingapp.database_helpers.StatsDB;
import com.team3824.akmessing1.scoutingapp.database_helpers.SuperScoutDB;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class AggregateService extends IntentService {
    private String TAG ="AggregateService";
    private Handler toastHandler;

    public AggregateService()
    {
        super("AggregateService");
        toastHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(TAG, "Aggregate Service Started");
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.APP_DATA, Context.MODE_PRIVATE);
        String eventID = sharedPreferences.getString(Constants.EVENT_ID, "");
        PitScoutDB pitScoutDB = new PitScoutDB(context,eventID);
        SuperScoutDB superScoutDB = new SuperScoutDB(context, eventID);
        MatchScoutDB matchScoutDB = new MatchScoutDB(context, eventID);
        ScheduleDB scheduleDB = new ScheduleDB(context,eventID);
        StatsDB statsDB = new StatsDB(context, eventID);

        String lastUpdated;
        boolean update = intent.getBooleanExtra(Constants.UPDATE, false);
        if(update)
        {
            lastUpdated = statsDB.getLastUpdatedTime();
        }
        else {
            lastUpdated = "";
        }
        Log.d(TAG, "Last Time Updated: "+lastUpdated);
        ArrayList<Integer> matchesUpdated;

        matchesUpdated = superScoutDB.getMatchesUpdatedSince(lastUpdated);


        if(matchesUpdated == null || matchesUpdated.size() > 0)
        {
            Cursor matchCursor;
            matchCursor = superScoutDB.getAllMatches();

            ArrayList<Integer> teamNumbers = pitScoutDB.getTeamNumbers();
            if(matchCursor.getCount() > 0) {

                // Normal updateStats method does not seem to give the table enough time to update
                // the columns for the following loop
                if(!statsDB.hasColumn(Constants.DRIVE_ABILITY_RANKING))
                {
                    statsDB.addColumn(Constants.DRIVE_ABILITY_RANKING,"TEXT");
                }
                if(!statsDB.hasColumn(Constants.DEFENSE_ABILITY_RANKING))
                {
                    statsDB.addColumn(Constants.DEFENSE_ABILITY_RANKING,"TEXT");
                }

                String[] driveAbilityRanking = SchulzeMethod.CardinalRankCalc(teamNumbers, matchCursor, eventID, Constants.SUPER_DRIVE_ABILITY, this);
                HashMap<String, ScoutValue> map;
                for (int i = 0; i < teamNumbers.size(); i++) {
                    map = new HashMap<>();
                    map.put(StatsDB.KEY_TEAM_NUMBER, new ScoutValue(teamNumbers.get(i)));
                    map.put(Constants.DRIVE_ABILITY_RANKING, new ScoutValue(driveAbilityRanking[i]));
                    statsDB.updateStats(map);
                }

                matchCursor.moveToFirst();

                String[] defenseAbilityRanking = SchulzeMethod.CardinalRankCalc(teamNumbers, matchCursor, eventID, Constants.SUPER_DEFENSE_ABILITY, this);
                for (int i = 0; i < teamNumbers.size(); i++) {
                    map = new HashMap<>();
                    map.put(StatsDB.KEY_TEAM_NUMBER, new ScoutValue(teamNumbers.get(i)));
                    map.put(Constants.DEFENSE_ABILITY_RANKING, new ScoutValue(defenseAbilityRanking[i]));
                    statsDB.updateStats(map);
                }
            }
        }

        ArrayList<Integer> teamsUpdated = matchScoutDB.getTeamsUpdatedSince(lastUpdated);
        for(int i = 0; i < teamsUpdated.size(); i++)
        {
            Cursor teamCursor = matchScoutDB.getTeamInfoSince(teamsUpdated.get(i), lastUpdated);
            Map<String, ScoutValue> teamStats = (update)?statsDB.getTeamStats(teamsUpdated.get(i)):
                    new HashMap<String,ScoutValue>();

            // Calculate metrics and insert in map

            //Auto
            int[] totalStartPosition = set_start_array(teamStats,"start");
            int[] totalDefenseReaches = set_start_array(teamStats, "reach");
            int[] totalDefenseCrosses = set_start_array(teamStats, "cross");
            int totalAutoHighGoalHit = set_start(teamStats,Constants.TOTAL_AUTO_HIGH_HIT);
            int totalAutoHighGoalMiss = set_start(teamStats,Constants.TOTAL_AUTO_HIGH_MISS);
            int totalAutoLowGoalHit = set_start(teamStats, Constants.TOTAL_AUTO_LOW_HIT);
            int totalAutoLowGoalMiss = set_start(teamStats, Constants.TOTAL_AUTO_LOW_MISS);


            //Teleop
            int[] totalTeleopDefenses = set_start_array(teamStats, "teleop");
            int[] totalTeleopDefensesPoints = set_start_array(teamStats, "teleop_points");
            int[] totalDefensesSeen = set_start_array(teamStats, "seen");
            int[] totalDefensesTime = set_start_array(teamStats, "time");
            int totalTeleopHighGoalHit = set_start(teamStats,Constants.TOTAL_TELEOP_HIGH_HIT);
            int totalTeleopHighGoalMiss = set_start(teamStats,Constants.TOTAL_TELEOP_HIGH_MISS);
            int totalTeleopLowGoalHit = set_start(teamStats,Constants.TOTAL_TELEOP_LOW_HIT);
            int totalTeleopLowGoalMiss = set_start(teamStats,Constants.TOTAL_TELEOP_LOW_MISS);

            //Endgame
            int totalChallenge = set_start(teamStats,Constants.TOTAL_CHALLENGE);
            int totalScale = set_start(teamStats,Constants.TOTAL_SCALE);

            //Post
            int totalDQ = set_start(teamStats,Constants.TOTAL_DQ);
            int totalStopped = set_start(teamStats,Constants.TOTAL_STOPPED);
            int totalDidntShowUp = set_start(teamStats,Constants.TOTAL_DIDNT_SHOW_UP);

            //Fouls
            int totalFouls = set_start(teamStats,Constants.TOTAL_FOULS);
            int totalTechFouls = set_start(teamStats,Constants.TOTAL_TECH_FOULS);
            int totalYellowCards = set_start(teamStats,Constants.TOTAL_YELLOW_CARDS);
            int totalRedCards = set_start(teamStats,Constants.TOTAL_RED_CARDS);


            int totalMatches = set_start(teamStats,Constants.TOTAL_MATCHES);

            for(teamCursor.moveToFirst(); !teamCursor.isAfterLast(); teamCursor.moveToNext())
            {
                int matchNumber = teamCursor.getInt(teamCursor.getColumnIndex(MatchScoutDB.KEY_MATCH_NUMBER));
                Map<String, ScoutValue> superMatch = superScoutDB.getMatchInfo(matchNumber);
                // If superscout has not yet recorded the match change the update time so that this
                // match can be aggregated later
                if(superMatch == null) {
                    Map<String, ScoutValue> matchTeam = new HashMap<>();
                    matchTeam.put(MatchScoutDB.KEY_ID,new ScoutValue(teamCursor.getString(teamCursor.getColumnIndex(MatchScoutDB.KEY_ID))));
                    matchTeam.put(MatchScoutDB.KEY_TEAM_NUMBER,new ScoutValue(teamCursor.getInt(teamCursor.getColumnIndex(MatchScoutDB.KEY_TEAM_NUMBER))));
                    matchTeam.put(MatchScoutDB.KEY_MATCH_NUMBER,new ScoutValue(teamCursor.getInt(teamCursor.getColumnIndex(MatchScoutDB.KEY_MATCH_NUMBER))));
                    matchScoutDB.updateMatch(matchTeam);
                }
                else{
                    totalMatches++;

                    Cursor match = scheduleDB.getMatch(matchNumber);

                    String defense2, defense3, defense4, defense5;
                    defense3 = superMatch.get(Constants.DEFENSE_3).getString().toLowerCase().replace(" ", "_");
                    if (match.getInt(match.getColumnIndex(ScheduleDB.KEY_BLUE1)) == teamsUpdated.get(i) ||
                            match.getInt(match.getColumnIndex(ScheduleDB.KEY_BLUE2)) == teamsUpdated.get(i) ||
                            match.getInt(match.getColumnIndex(ScheduleDB.KEY_BLUE3)) == teamsUpdated.get(i)) {
                        defense2 = superMatch.get(Constants.BLUE_DEFENSE_2).getString().toLowerCase().replace(" ", "_");
                        defense4 = superMatch.get(Constants.BLUE_DEFENSE_4).getString().toLowerCase().replace(" ", "_");
                        defense5 = superMatch.get(Constants.BLUE_DEFENSE_5).getString().toLowerCase().replace(" ", "_");
                    } else {
                        defense2 = superMatch.get(Constants.RED_DEFENSE_2).getString().toLowerCase().replace(" ", "_");
                        defense4 = superMatch.get(Constants.RED_DEFENSE_4).getString().toLowerCase().replace(" ", "_");
                        defense5 = superMatch.get(Constants.RED_DEFENSE_5).getString().toLowerCase().replace(" ", "_");
                    }

                    increment_array(teamCursor, totalDefensesSeen, defense2, defense3, defense4, defense5, "seen");

                    //Auto
                    increment_array(teamCursor, totalStartPosition, defense2, defense3, defense4, defense5, "start");
                    increment_array(teamCursor, totalDefenseReaches, defense2, defense3, defense4, defense5, "reach");
                    increment_array(teamCursor, totalDefenseCrosses, defense2, defense3, defense4, defense5, "cross");
                    totalAutoHighGoalHit += teamCursor.getInt(teamCursor.getColumnIndex(Constants.AUTO_HIGH_HIT));
                    totalAutoHighGoalMiss += teamCursor.getInt(teamCursor.getColumnIndex(Constants.AUTO_HIGH_MISS));
                    totalAutoLowGoalHit += teamCursor.getInt(teamCursor.getColumnIndex(Constants.AUTO_LOW_HIT));
                    totalAutoLowGoalMiss += teamCursor.getInt(teamCursor.getColumnIndex(Constants.AUTO_LOW_MISS));

                    //Teleop
                    increment_array(teamCursor, totalTeleopDefenses, defense2, defense3, defense4, defense5, "teleop");
                    increment_array(teamCursor, totalTeleopDefensesPoints, defense2, defense3, defense4, defense5, "teleop_points");
                    increment_array(teamCursor, totalDefensesTime, defense2, defense3, defense4, defense5, "time");
                    totalTeleopHighGoalHit += teamCursor.getInt(teamCursor.getColumnIndex(Constants.TELEOP_HIGH_HIT));
                    totalTeleopHighGoalMiss += teamCursor.getInt(teamCursor.getColumnIndex(Constants.TELEOP_HIGH_MISS));
                    totalTeleopLowGoalHit += teamCursor.getInt(teamCursor.getColumnIndex(Constants.TELEOP_LOW_HIT));
                    totalTeleopLowGoalMiss += teamCursor.getInt(teamCursor.getColumnIndex(Constants.TELEOP_LOW_MISS));

                    //Endgame
                    totalChallenge += (teamCursor.getString(teamCursor.getColumnIndex(Constants.ENDGAME_CHALLENGE_SCALE)).equals("Challenge")) ? 1 : 0;
                    totalScale += (teamCursor.getString(teamCursor.getColumnIndex(Constants.ENDGAME_CHALLENGE_SCALE)).equals("Scale")) ? 1 : 0;

                    //Post
                    totalDQ += teamCursor.getInt(teamCursor.getColumnIndex(Constants.POST_DQ));
                    totalStopped += teamCursor.getInt(teamCursor.getColumnIndex(Constants.POST_STOPPED));
                    totalDidntShowUp += teamCursor.getInt(teamCursor.getColumnIndex(Constants.POST_DIDNT_SHOW_UP));

                    //Fouls
                    totalFouls += teamCursor.getInt(teamCursor.getColumnIndex(Constants.FOUL_STANDARD));
                    totalTechFouls += teamCursor.getInt(teamCursor.getColumnIndex(Constants.FOUL_TECH));
                    totalYellowCards += teamCursor.getInt(teamCursor.getColumnIndex(Constants.FOUL_YELLOW_CARD));
                    totalRedCards += teamCursor.getInt(teamCursor.getColumnIndex(Constants.FOUL_RED_CARD));
                }
            }

            HashMap<String, ScoutValue> newTeamStats = new HashMap<>();

            for(int j = 0; j < 9; j++)
            {
                newTeamStats.put(Constants.TOTAL_DEFENSES_STARTED[j], new ScoutValue(totalStartPosition[j]));
                newTeamStats.put(Constants.TOTAL_DEFENSES_SEEN[j], new ScoutValue(totalDefensesSeen[j]));
                newTeamStats.put(Constants.TOTAL_DEFENSES_AUTO_REACHED[j], new ScoutValue(totalDefenseReaches[j]));
                newTeamStats.put(Constants.TOTAL_DEFENSES_AUTO_CROSSED[j], new ScoutValue(totalDefenseCrosses[j]));
                newTeamStats.put(Constants.TOTAL_DEFENSES_TELEOP_CROSSED[j], new ScoutValue(totalTeleopDefenses[j]));
                newTeamStats.put(Constants.TOTAL_DEFENSES_TELEOP_CROSSED_POINTS[j], new ScoutValue(totalTeleopDefensesPoints[j]));
                newTeamStats.put(Constants.TOTAL_DEFENSES_TELEOP_TIME[j], new ScoutValue(totalDefensesTime[j]));
            }
            newTeamStats.put(Constants.TOTAL_DEFENSES_STARTED[9], new ScoutValue(totalStartPosition[9]));
            newTeamStats.put(Constants.TOTAL_DEFENSES_STARTED[10], new ScoutValue(totalStartPosition[10]));

            newTeamStats.put(Constants.TOTAL_AUTO_HIGH_HIT, new ScoutValue(totalAutoHighGoalHit));
            newTeamStats.put(Constants.TOTAL_AUTO_HIGH_MISS, new ScoutValue(totalAutoHighGoalMiss));
            newTeamStats.put(Constants.TOTAL_AUTO_LOW_HIT, new ScoutValue(totalAutoLowGoalHit));
            newTeamStats.put(Constants.TOTAL_AUTO_LOW_MISS, new ScoutValue(totalAutoLowGoalMiss));

            newTeamStats.put(Constants.TOTAL_TELEOP_HIGH_HIT, new ScoutValue(totalTeleopHighGoalHit));
            newTeamStats.put(Constants.TOTAL_TELEOP_HIGH_MISS, new ScoutValue(totalTeleopHighGoalMiss));
            newTeamStats.put(Constants.TOTAL_TELEOP_LOW_HIT, new ScoutValue(totalTeleopLowGoalHit));
            newTeamStats.put(Constants.TOTAL_TELEOP_LOW_MISS, new ScoutValue(totalTeleopLowGoalMiss));

            newTeamStats.put(Constants.TOTAL_CHALLENGE, new ScoutValue(totalChallenge));
            newTeamStats.put(Constants.TOTAL_SCALE, new ScoutValue(totalScale));

            newTeamStats.put(Constants.TOTAL_DQ, new ScoutValue(totalDQ));
            newTeamStats.put(Constants.TOTAL_STOPPED, new ScoutValue(totalStopped));
            newTeamStats.put(Constants.TOTAL_DIDNT_SHOW_UP, new ScoutValue(totalDidntShowUp));

            newTeamStats.put(Constants.TOTAL_FOULS, new ScoutValue(totalFouls));
            newTeamStats.put(Constants.TOTAL_TECH_FOULS, new ScoutValue(totalTechFouls));
            newTeamStats.put(Constants.TOTAL_YELLOW_CARDS, new ScoutValue(totalYellowCards));
            newTeamStats.put(Constants.TOTAL_RED_CARDS, new ScoutValue(totalRedCards));

            newTeamStats.put(Constants.TOTAL_MATCHES, new ScoutValue(totalMatches));

            newTeamStats.put(StatsDB.KEY_TEAM_NUMBER, new ScoutValue(teamsUpdated.get(i)));

            statsDB.updateStats(newTeamStats);
        }

        statsDB.close();
        matchScoutDB.close();
        superScoutDB.close();
        Log.d(TAG, "Aggregate Service Finished");
        writeToast("Data aggregated");
    }

    private int set_start(Map<String, ScoutValue> teamStats, String key)
    {
        if(teamStats.containsKey(key))
            return teamStats.get(key).getInt();
        else
            return 0;
    }

    private int[] set_start_array(Map<String, ScoutValue> teamStats, String type)
    {
        int[] array;
        if(type.equals("start"))
            array = new int[11];
        else
            array = new int[9];
        for(int i = 0; i < 9; i++)
        {
            switch(type)
            {
                case "start":
                    array[i] = set_start(teamStats,Constants.TOTAL_DEFENSES_STARTED[i]);
                    break;
                case "reach":
                    array[i] = set_start(teamStats,Constants.TOTAL_DEFENSES_AUTO_REACHED[i]);
                    break;
                case "cross":
                    array[i] = set_start(teamStats,Constants.TOTAL_DEFENSES_AUTO_CROSSED[i]);
                    break;
                case "teleop":
                    array[i] = set_start(teamStats,Constants.TOTAL_DEFENSES_TELEOP_CROSSED[i]);
                    break;
                case "teleop_points":
                    array[i] = set_start(teamStats, Constants.TOTAL_DEFENSES_TELEOP_CROSSED_POINTS[i]);
                    break;
                case "seen":
                    array[i] = set_start(teamStats,Constants.TOTAL_DEFENSES_SEEN[i]);
                    break;
                case "time":
                    array[i] = set_start(teamStats,Constants.TOTAL_DEFENSES_TELEOP_TIME[i]);
                    break;
            }
        }
        if(type.equals("start"))
        {
            array[9] = set_start(teamStats,"total_start_spy");
            array[10] = set_start(teamStats,"total_start_secret_passage");
        }
        return array;
    }

    private void increment_array(Cursor cursor, int[] array, String defense2, String defense3, String defense4, String defense5, String type)
    {
        Log.d(TAG,"increment");

        List defensesList = Arrays.asList(Constants.DEFENSES);

        int index;

        switch(type)
        {
            case "start":
                switch(cursor.getString(cursor.getColumnIndex(Constants.AUTO_START_POSITION)))
                {
                    case "Defense 1 (Low bar)":
                        index = Constants.LOW_BAR_INDEX;
                        array[index]++;
                        break;
                    case "Defense 2":
                        index = defensesList.indexOf(defense2);
                        array[index]++;
                        break;
                    case "Defense 3":
                        index = defensesList.indexOf(defense3);
                        array[index]++;
                        break;
                    case "Defense 4":
                        index = defensesList.indexOf(defense4);
                        array[index]++;
                        break;
                    case "Defense 5":
                        index = defensesList.indexOf(defense5);
                        array[index]++;
                        break;
                    case "Spybox":
                        array[9]++;
                        break;
                    case "Secret Passage":
                        array[10]++;
                        break;
                }
                break;
            case "reach":
                if(cursor.getString(cursor.getColumnIndex(Constants.AUTO_REACH_CROSS)).equals("Reach"))
                {
                    switch(cursor.getString(cursor.getColumnIndex(Constants.AUTO_START_POSITION)))
                    {
                        case "Defense 1 (Low bar)":
                            index = Constants.LOW_BAR_INDEX;
                            array[index]++;
                            break;
                        case "Defense 2":
                            index = defensesList.indexOf(defense2);
                            array[index]++;
                            break;
                        case "Defense 3":
                            index = defensesList.indexOf(defense3);
                            array[index]++;
                            break;
                        case "Defense 4":
                            index = defensesList.indexOf(defense4);
                            array[index]++;
                            break;
                        case "Defense 5":
                            index = defensesList.indexOf(defense5);
                            array[index]++;
                            break;
                    }
                }
                break;
            case "cross":
                Log.d(TAG,cursor.getString(cursor.getColumnIndex(Constants.AUTO_REACH_CROSS)));
                if(cursor.getString(cursor.getColumnIndex(Constants.AUTO_REACH_CROSS)).equals("Cross"))
                {
                    switch(cursor.getString(cursor.getColumnIndex(Constants.AUTO_START_POSITION)))
                    {
                        case "Defense 1 (Low bar)":
                            index = Constants.LOW_BAR_INDEX;
                            array[index]++;
                            break;
                        case "Defense 2":
                            index = defensesList.indexOf(defense2);
                            array[index]++;
                            break;
                        case "Defense 3":
                            index = defensesList.indexOf(defense3);
                            array[index]++;
                            break;
                        case "Defense 4":
                            index = defensesList.indexOf(defense4);
                            array[index]++;
                            break;
                        case "Defense 5":
                            index = defensesList.indexOf(defense5);
                            array[index]++;
                            break;
                    }
                }
                break;
            case "teleop":
                index = Constants.LOW_BAR_INDEX;
                array[index] += stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_1)));

                index = defensesList.indexOf(defense2);
                array[index] += stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_2)));

                index = defensesList.indexOf(defense3);
                array[index] += stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_3)));

                index = defensesList.indexOf(defense4);
                array[index] += stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_4)));

                index = defensesList.indexOf(defense5);
                array[index] += stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_5)));
                break;
            case "teleop_points":
                if(cursor.getString(cursor.getColumnIndex(Constants.AUTO_REACH_CROSS)).equals("Cross"))
                {
                    int numCross = 0;
                    switch (cursor.getString(cursor.getColumnIndex(Constants.AUTO_START_POSITION)))
                    {
                        case "Defense 1 (Low bar)":
                            index = Constants.LOW_BAR_INDEX;
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_1)));
                            array[index] += (numCross > 1) ? 1 : numCross;

                            index = defensesList.indexOf(defense2);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_2)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense3);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_3)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense4);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_4)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense5);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_5)));
                            array[index] += (numCross > 2) ? 2 : numCross;
                            break;
                        case "Defense 2":
                            index = Constants.LOW_BAR_INDEX;
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_1)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense2);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_2)));
                            array[index] += (numCross > 1) ? 1 : numCross;

                            index = defensesList.indexOf(defense3);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_3)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense4);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_4)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense5);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_5)));
                            array[index] += (numCross > 2) ? 2 : numCross;
                            break;
                        case "Defense 3":
                            index = Constants.LOW_BAR_INDEX;
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_1)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense2);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_2)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense3);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_3)));
                            array[index] += (numCross > 1) ? 1 : numCross;

                            index = defensesList.indexOf(defense4);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_4)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense5);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_5)));
                            array[index] += (numCross > 2) ? 2 : numCross;
                            break;
                        case "Defense 4":
                            index = Constants.LOW_BAR_INDEX;
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_1)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense2);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_2)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense3);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_3)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense4);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_4)));
                            array[index] += (numCross > 1) ? 1 : numCross;

                            index = defensesList.indexOf(defense5);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_5)));
                            array[index] += (numCross > 2) ? 2 : numCross;
                            break;
                        case "Defense 5":
                            index = Constants.LOW_BAR_INDEX;
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_1)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense2);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_2)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense3);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_3)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense4);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_4)));
                            array[index] += (numCross > 2) ? 2 : numCross;

                            index = defensesList.indexOf(defense5);
                            numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_5)));
                            array[index] += (numCross > 1) ? 1 : numCross;
                            break;
                    }
                }
                else
                {
                    index = Constants.LOW_BAR_INDEX;
                    int numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_1)));
                    array[index] += (numCross > 2) ? 2 : numCross;

                    index = defensesList.indexOf(defense2);
                    numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_2)));
                    array[index] += (numCross > 2) ? 2 : numCross;

                    index = defensesList.indexOf(defense3);
                    numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_3)));
                    array[index] += (numCross > 2) ? 2 : numCross;

                    index = defensesList.indexOf(defense4);
                    numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_4)));
                    array[index] += (numCross > 2) ? 2 : numCross;

                    index = defensesList.indexOf(defense5);
                    numCross = stringToCount(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_5)));
                    array[index] += (numCross > 2) ? 2 : numCross;
                }
                break;
            case "seen":
                index = Constants.LOW_BAR_INDEX;
                array[index] ++;

                index = defensesList.indexOf(defense2);
                array[index] ++;

                index = defensesList.indexOf(defense3);
                array[index] ++;

                index = defensesList.indexOf(defense4);
                array[index] ++;

                index = defensesList.indexOf(defense5);
                array[index] ++;
                break;
            case "time":
                index = Constants.LOW_BAR_INDEX;
                array[index] += stringToTime(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_1)));

                index = defensesList.indexOf(defense2);
                array[index] += stringToTime(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_2)));

                index = defensesList.indexOf(defense3);
                array[index] += stringToTime(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_3)));

                index = defensesList.indexOf(defense4);
                array[index] += stringToTime(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_4)));

                index = defensesList.indexOf(defense5);
                array[index] += stringToTime(cursor.getString(cursor.getColumnIndex(Constants.TELEOP_DEFENSE_5)));
                break;
        }
    }

    private int stringToCount(String jsonText)
    {
        try {
            JSONArray jsonArray = new JSONArray(jsonText);
            return jsonArray.length();
        } catch (JSONException e) {
            Log.d(TAG,e.getMessage());
        }
        return 0;
    }

    private int stringToTime(String jsonText)
    {
        int sum = 0;
        try{
            JSONArray jsonArray = new JSONArray(jsonText);
            for(int i = 0; i < jsonArray.length(); i++)
            {
                String tmp = jsonArray.getString(i);
                for(int j = 0; j < Constants.TELEOP_DEFENSE_TIMES.length; j++)
                {
                    if(tmp.equals(Constants.TELEOP_DEFENSE_TIMES[j]))
                    {
                        sum += Constants.TELEOP_DEFENSE_TIMES_VALUE[j];
                        break;
                    }
                }
            }
            if(jsonArray.length() > 0) {
                sum /= jsonArray.length();
            }
        } catch (JSONException e){
            Log.d(TAG,e.getMessage());
        }
        return sum;
    }

    private void writeToast(final String message)
    {
        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AggregateService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
