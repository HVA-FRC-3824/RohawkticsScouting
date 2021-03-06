package com.team3824.akmessing1.scoutingapp.fragments.PickLists;

import android.database.Cursor;

import com.team3824.akmessing1.scoutingapp.utilities.Constants;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutValue;
import com.team3824.akmessing1.scoutingapp.list_items.Team;

/**
 * @author Andrew Messing
 * @version 1
 *
 * Fragment that is the Shooter Pick List. Currently sorting is based on average points based on
 * shooting into the high and low goals.
 */
public class ShooterPick extends ScoutPick{
    public ShooterPick()
    {
        setPickType("shooter");
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
    protected Team setupTeam(Team team, Cursor statsCursor)
    {
        if(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_MATCHES) > -1 && statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_MATCHES)) > 0)
        {
            float avgShootingPoints = 0.0f;
            int highPoints = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_TELEOP_HIGH_HIT)) * 5 +
                    statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_AUTO_HIGH_HIT)) * 10;

            int lowPoints = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_TELEOP_LOW_HIT)) * 2 +
                    statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_AUTO_LOW_HIT)) * 5;

            int totalMatches = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_MATCHES));
            if (totalMatches != 0)
            {
                avgShootingPoints = ((float)(highPoints + lowPoints)) / ((float) totalMatches);
            }

            int autoLowMade = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_AUTO_LOW_HIT));
            int autoLowTaken = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_AUTO_LOW_MISS)) + autoLowMade;
            float autoLowPercentage = (autoLowTaken == 0)?0:(float)autoLowMade/(float)autoLowTaken * 100.0f;

            int autoHighMade = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_AUTO_HIGH_HIT));
            int autoHighTaken = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_AUTO_HIGH_MISS)) + autoHighMade;
            float autoHighPercentage = (autoHighTaken == 0)?0:(float)autoHighMade/(float)autoHighTaken * 100.0f;

            int teleopLowMade = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_TELEOP_LOW_HIT));
            int teleopLowTaken = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_TELEOP_LOW_MISS)) + teleopLowMade;
            float teleopLowPercentage = (teleopLowTaken == 0)?0:(float)teleopLowMade/(float)teleopLowTaken * 100.0f;

            int teleopHighMade = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_TELEOP_HIGH_HIT));
            int teleopHighTaken = statsCursor.getInt(statsCursor.getColumnIndex(Constants.Calculated_Totals.TOTAL_TELEOP_HIGH_MISS)) + teleopHighMade;
            float teleopHighPercentage = (teleopHighTaken == 0)?0:(float)teleopHighMade/(float)teleopHighTaken * 100.0f;

            team.setMapElement(Constants.Pick_List.SHOOTER_PICKABILITY, new ScoutValue((int)avgShootingPoints));
            String bottomText = String.format("Auto - High: %d / %d (%.2f%%) Low: %d / %d (%.2f%%)\nTeleop - High: %d / %d (%.2f%%) Low: %d / %d (%.2f%%)",
                    autoHighMade,autoHighTaken,autoHighPercentage,autoLowMade,autoLowTaken,autoLowPercentage,
                    teleopHighMade,teleopHighTaken,teleopHighPercentage,teleopLowMade,teleopLowTaken,teleopLowPercentage);
            team.setMapElement(Constants.Pick_List.BOTTOM_TEXT,new ScoutValue(bottomText));
        }
        else
        {
            team.setMapElement(Constants.Pick_List.SHOOTER_PICKABILITY, new ScoutValue(0));
            team.setMapElement(Constants.Pick_List.BOTTOM_TEXT,new ScoutValue("No matches yet"));
        }
        return team;
    }
}
