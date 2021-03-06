package com.team3824.akmessing1.scoutingapp.utilities;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.team3824.akmessing1.scoutingapp.views.CustomOrdinalRank;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  Utilities for combining a set of small subset rankings into a full ranking. Used with the Custom
 *  Ordinal Rank inputs (Qualitative stuff).
 *
 *  Uses the Schulze Method
 *  @link https://en.wikipedia.org/wiki/Schulze_method
 *
 *  @author Andrew Messing
 *  @version
 */
class SchulzeMethod {

    private static String TAG = "SchulzeMethod";

    /**
     * This function is designed to combine qualitative rankings of subsets of teams into a ranking
     * of all the teams. It uses Schulze Method for voting.
     *
     * @param teamNumbers
     * @param matchCursor
     * @param eventID
     * @param key
     * @param context
     * @return
     */
    public static String[] CardinalRankCalc(final ArrayList<Integer> teamNumbers, Cursor matchCursor, String eventID, String key, Context context) {
        int numTeams = teamNumbers.size();
        Set<Integer> ranking = new HashSet<Integer>();
        String[] output;

        // Create empty matrix
        Integer[][] matrix = new Integer[numTeams][numTeams];
        for (Integer[] line : matrix) {
            Arrays.fill(line, 0);
        }

        matrix_to_file(String.format("%s_%s_zero.csv", eventID, key), matrix, context);

        /*
            Go through the cursor and get all the subset rankings
            Each team that is ranked higher in a subset ranking gets an increase in the direct
            path between it and those ranked lower than it
        */
        JSONArray jsonArray = null;
        for (matchCursor.moveToFirst(); !matchCursor.isAfterLast(); matchCursor.moveToNext()) {
            String line = matchCursor.getString(matchCursor.getColumnIndex(key));
            if(line == null || line.length() == 0)
                continue;
            try {
                jsonArray = new JSONArray(line);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    int teamNumber2 = jsonObject2.getInt(Constants.Intent_Extras.TEAM_NUMBER);
                    int index2 = teamNumbers.indexOf(teamNumber2);
                    int rank2 = jsonObject2.getInt(CustomOrdinalRank.RANK);
                    for (int j = 0; j < i; j++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        int teamNumber1 = jsonObject1.getInt(Constants.Intent_Extras.TEAM_NUMBER);
                        int index1 = teamNumbers.indexOf(teamNumber1);
                        int rank1 = jsonObject1.getInt(CustomOrdinalRank.RANK);
                        if(rank1 != rank2) {
                            matrix[index1][index2]++;
                        }
                    }
                    ranking.add(teamNumber2);
                }
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }

        matrix_to_file(String.format("%s_%s_matrix.csv", eventID, key), matrix, context);

        /*
         If one team has more higher rankings over another team then that gets put in the
         strongest path matrix otherwise it is left at 0
        */
        final Integer[][] strongestPathMatrix = new Integer[numTeams][numTeams];
        for (Integer[] line : strongestPathMatrix) {
            Arrays.fill(line, 0);
        }

        matrix_to_file(String.format("%s_%s_spm_zero.csv", eventID, key), strongestPathMatrix, context);


        for (int i = 0; i < numTeams; i++) {
            for (int j = 0; j < numTeams; j++) {
                if (matrix[i][j] > matrix[j][i]) {
                    strongestPathMatrix[i][j] = matrix[i][j];
                }
            }
        }

        matrix_to_file(String.format("%s_%s_spm_1.csv", eventID, key), strongestPathMatrix, context);

        // find the strongest path from each team to each other team
        for (int i = 0; i < numTeams; i++) {
            for (int j = 0; j < numTeams; j++) {
                if (i == j) {
                    continue;
                }
                for (int k = 0; k < numTeams; k++) {
                    if (i != k && j != k) {
                        strongestPathMatrix[j][k] = Math.max(strongestPathMatrix[j][k], Math.min(strongestPathMatrix[j][i], strongestPathMatrix[i][k]));
                    }
                }
            }
        }

        matrix_to_file(String.format("%s_%s_spm_2.csv", eventID, key), strongestPathMatrix, context);

        List<Integer> sortedRanking = new ArrayList<>();
        sortedRanking.addAll(ranking);
        // sort the teams based on their paths to another team
        Collections.sort(sortedRanking, new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                int indexA = teamNumbers.indexOf(a);
                int indexB = teamNumbers.indexOf(b);
                if (strongestPathMatrix[indexA][indexB] > strongestPathMatrix[indexB][indexA]) {
                    return -1;
                } else if (strongestPathMatrix[indexA][indexB] == strongestPathMatrix[indexB][indexA]) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        // Create output that includes ties
        output = new String[teamNumbers.size()];
        int rank = 1;
        for (int i = 0; i < sortedRanking.size(); i++) {
            int currentTeamNumber = sortedRanking.get(i);
            int currentTeamNumberIndex = teamNumbers.indexOf(currentTeamNumber);

            Log.d(TAG, String.format("CTN: %d", currentTeamNumber));
            if (i > 0) {
                int previousTeamNumber = sortedRanking.get(i - 1);
                int previousTeamNumberIndex = teamNumbers.indexOf(previousTeamNumber);

                if (strongestPathMatrix[currentTeamNumberIndex][previousTeamNumberIndex] == strongestPathMatrix[previousTeamNumberIndex][currentTeamNumberIndex]) {
                    output[currentTeamNumberIndex] = "T" + String.valueOf(rank);
                    Log.d(TAG, teamNumbers.get(currentTeamNumberIndex) + ": " + output[currentTeamNumberIndex]);
                    continue;
                }
            } else if (i == 0) {
                int nextTeamNumber = sortedRanking.get(i + 1);
                int nextTeamNumberIndex = teamNumbers.indexOf(nextTeamNumber);

                if (strongestPathMatrix[currentTeamNumberIndex][nextTeamNumberIndex] == strongestPathMatrix[nextTeamNumberIndex][currentTeamNumberIndex]) {
                    output[currentTeamNumberIndex] = "T" + String.valueOf(rank);
                    Log.d(TAG, teamNumbers.get(currentTeamNumberIndex) + ": " + output[currentTeamNumberIndex]);
                    continue;
                }
            }

            if (i < sortedRanking.size() - 1) {
                int nextTeamNumber = sortedRanking.get(i + 1);
                int nextTeamNumberIndex = teamNumbers.indexOf(nextTeamNumber);

                if (strongestPathMatrix[currentTeamNumberIndex][nextTeamNumberIndex] == strongestPathMatrix[nextTeamNumberIndex][currentTeamNumberIndex]) {
                    rank = i + 1;
                    output[currentTeamNumberIndex] = "T" + String.valueOf(rank);
                    Log.d(TAG, teamNumbers.get(currentTeamNumberIndex) + ": " + output[currentTeamNumberIndex]);
                    continue;
                }
            }

            rank = i + 1;
            output[currentTeamNumberIndex] = String.valueOf(rank);

            Log.d(TAG, teamNumbers.get(currentTeamNumberIndex) + ": " + output[currentTeamNumberIndex]);
        }
        rank = sortedRanking.size() + 1;
        for (int i = 0; i < teamNumbers.size(); i++) {
            if (sortedRanking.indexOf(teamNumbers.get(i)) == -1) {
                output[i] = "T" + String.valueOf(rank);
                Log.d(TAG, teamNumbers.get(i) + ": " + output[i]);
            }
        }

        rankings_to_file(String.format("%s_%s_rankings.csv", eventID, key),output,context);

        return output;
    }

    /**
     * @param filename
     * @param matrix
     * @param context
     */
    private static void matrix_to_file(String filename, Integer[][] matrix, Context context) {
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            String string = "";
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    string += matrix[i][j] + ",";
                }
                string = string.substring(0, string.length() - 1);
                string += "\n";
            }
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.d(TAG, "Exception");
        }
    }

    private static void rankings_to_file(String filename, String[] rankings, Context context)
    {
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            String string = "";
            for(int i = 0; i < rankings.length;i++)
            {
                    string += rankings[i] +";\n";
            }
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.d(TAG,"Exception");
        }
    }


}