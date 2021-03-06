package com.team3824.akmessing1.scoutingapp.fragments.PickLists;

import android.database.Cursor;

import com.team3824.akmessing1.scoutingapp.list_items.Team;
import com.team3824.akmessing1.scoutingapp.utilities.Constants;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutValue;

/**
 * @author Andrew Messing
 * @version 1
 *
 * Fragment that is the Breacher Pick List. Currently sorting is based on average points based on
 * crossing defenses with additional weights given for speed.
 */
public class BreacherPick extends ScoutPick {

    public BreacherPick() {
        setPickType("breacher");
    }

    /**
     * Fills an individual team with all the data needed to display and sort it for this particular
     * pick type.
     *
     * @param team The team object to fill with data
     * @param statsCursor The response from the database with the data to use for the team
     * @return The filled team
     */
    @Override
    protected Team setupTeam(Team team, Cursor statsCursor) {

        float avgDefensePoints = 0.0f;
        String defensesFast = "";
        if (statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_MATCHES) > -1 && statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_MATCHES)) > 0) {
            for (int i = 0; i < 9; i++) {
                avgDefensePoints += statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_DEFENSES_AUTO_REACHED[i])) * 2;
                avgDefensePoints += statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_DEFENSES_AUTO_CROSSED[i])) * 10;
                avgDefensePoints += statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_DEFENSES_TELEOP_CROSSED_POINTS[i])) * 5;
            }

            avgDefensePoints /= (float) statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_MATCHES));

            int numDefenseFast = 0;
            for (int i = 0; i < 9; i++) {
                float time = statsCursor.getFloat(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_DEFENSES_TELEOP_TIME[i]));
                float seen = statsCursor.getFloat(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_DEFENSES_SEEN[i]));
                float notCrossed = statsCursor.getFloat(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_DEFENSES_TELEOP_NOT_CROSSED[i]));

                if (seen > 0 && seen != notCrossed) {
                    float avgTime = time / (seen - notCrossed);
                    if (avgTime <= 5 && avgTime > 0) {
                        numDefenseFast++;
                        defensesFast += Constants.Defense_Arrays.DEFENSES_ABREV[i] + ", ";
                    }
                }
            }
            if (defensesFast.length() > 2) {
                defensesFast = defensesFast.substring(0, defensesFast.length() - 2);
            }
            team.setMapElement(Constants.Pick_List.BREACHER_PICKABILITY, new ScoutValue(((int) avgDefensePoints) + numDefenseFast * 5));

            String bottomText = String.format("Breacher Pickability: %d Avg Defense Points: %.2f\nFast Defenses: ", team.getMapElement(Constants.Pick_List.BREACHER_PICKABILITY).getInt(), avgDefensePoints);
            if (defensesFast.equals("")) {
                bottomText += "None";
            } else {
                bottomText += defensesFast;
            }
            team.setMapElement(Constants.Pick_List.BOTTOM_TEXT, new ScoutValue(bottomText));
        } else {
            team.setMapElement(Constants.Pick_List.BREACHER_PICKABILITY, new ScoutValue(0));
            team.setMapElement(Constants.Pick_List.BOTTOM_TEXT, new ScoutValue("No matches yet"));
        }

        return team;
    }
}
