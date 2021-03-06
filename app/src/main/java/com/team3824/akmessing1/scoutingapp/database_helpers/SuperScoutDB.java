package com.team3824.akmessing1.scoutingapp.database_helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.team3824.akmessing1.scoutingapp.utilities.Constants;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutMap;
import com.team3824.akmessing1.scoutingapp.utilities.ScoutValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * Helper class for accessing the super scout table of the database
 *
 * @author Andrew Messing
 * @version
 */
public class SuperScoutDB extends SQLiteOpenHelper {

    // Initial Table Columns names
    private static final String KEY_ID = "_id";
    public static final String KEY_MATCH_NUMBER = "_id";
    public static final String KEY_BLUE1 = "blue1";
    public static final String KEY_BLUE2 = "blue2";
    public static final String KEY_BLUE3 = "blue3";
    public static final String KEY_RED1 = "red1";
    public static final String KEY_RED2 = "red2";
    public static final String KEY_RED3 = "red3";
    private static final String KEY_LAST_UPDATED = "last_updated";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "RohawkticsDB";
    private static SimpleDateFormat dateFormat;
    private String TAG = "SuperScoutDB";
    private String tableName;

    /**
     * @param context
     * @param eventID The ID for the event based on FIRST and The Blue Alliance
     */
    public SuperScoutDB(Context context, String eventID) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        tableName = "superScouting_" + eventID;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }

    /**
     * Creates new database table if one does not exist
     *
     * @param db The database to add the table to
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryString = "CREATE TABLE IF NOT EXISTS " + tableName +
                "( " + KEY_ID + " INTEGER PRIMARY KEY UNIQUE NOT NULL," +
                " " + KEY_BLUE1 + " INTEGER NOT NULL," +
                " " + KEY_BLUE2 + " INTEGER NOT NULL," +
                " " + KEY_BLUE3 + " INTEGER NOT NULL," +
                " " + KEY_RED1 + " INTEGER NOT NULL," +
                " " + KEY_RED2 + " INTEGER NOT NULL," +
                " " + KEY_RED3 + " INTEGER NOT NULL," +
                " " + KEY_LAST_UPDATED + " DATETIME NOT NULL);";
        db.execSQL(queryString);
    }

    /**
     * Upgrades the table by dropping it and creating a new one
     *
     * @param db         The database to update
     * @param oldVersion Old version number
     * @param newVersion New version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        this.onCreate(db);
    }

    /**
     * Adds a new column to the table
     *
     * @param columnName Name of the new column
     * @param columnType What type the new column should be
     */
    private void addColumn(String columnName, String columnType) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    // Store data in the database for a specific match and team

    /**
     *
     * @param map
     */
    public void updateMatch(ScoutMap map) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(tableName, // a. table
                null, // b. column names
                KEY_MATCH_NUMBER + " = ?", // c. selections
                new String[]{String.valueOf(map.get(KEY_MATCH_NUMBER).getInt())}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                "1"); // h. limit
        String[] columnNames = cursor.getColumnNames();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                if (!map.containsKey(cursor.getColumnName(i))) {
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_FLOAT:
                            map.put(cursor.getColumnName(i), cursor.getFloat(i));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            map.put(cursor.getColumnName(i), cursor.getInt(i));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            map.put(cursor.getColumnName(i), cursor.getString(i));
                            break;
                    }

                }
            }
        }

        // Make sure the last updated time gets updated
        map.remove(KEY_LAST_UPDATED);

        ContentValues cvs = new ContentValues();
        cvs.put(KEY_LAST_UPDATED, dateFormat.format(new Date()));
        for (Map.Entry<String, ScoutValue> entry : map.entrySet()) {
            String column = entry.getKey();
            ScoutValue sv = entry.getValue();
            if (Arrays.asList(columnNames).indexOf(column) == -1) {
                switch (sv.getType()) {
                    case FLOAT_TYPE:
                        addColumn(column, "REAL");
                        break;
                    case INT_TYPE:
                        addColumn(column, "INTEGER");
                        break;
                    case STRING_TYPE:
                        addColumn(column, "TEXT");
                        break;
                }
            }

            switch (sv.getType()) {
                case FLOAT_TYPE:
                    Log.d(TAG, column + "->" + sv.getFloat());
                    cvs.put(column, sv.getFloat());
                    break;
                case INT_TYPE:
                    Log.d(TAG, column + "->" + sv.getInt());
                    cvs.put(column, sv.getInt());
                    break;
                case STRING_TYPE:
                    Log.d(TAG, column + "->" + sv.getString());
                    cvs.put(column, sv.getString());
                    break;
            }
        }
        db.replace(tableName, null, cvs);
        db.close();
    }

    // Get all the scouting information about a specific match

    /**
     *
     * @param matchNum
     * @return
     */
    public ScoutMap getMatchInfo(int matchNum) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, // a. table
                null, // b. column names
                KEY_MATCH_NUMBER + " = ?", // c. selections
                new String[]{String.valueOf(matchNum)}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
        if (cursor == null || cursor.getCount() == 0)
            return null;

        cursor.moveToFirst();
        // Setup map
        ScoutMap map = new ScoutMap();
        for (int i = 1; i < cursor.getColumnCount(); i++) {
            switch (cursor.getType(i)) {
                case Cursor.FIELD_TYPE_FLOAT:
                    Log.d(TAG, cursor.getColumnName(i) + "<-" + cursor.getFloat(i));
                    map.put(cursor.getColumnName(i), cursor.getFloat(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    Log.d(TAG, cursor.getColumnName(i) + "<-" + cursor.getInt(i));
                    map.put(cursor.getColumnName(i), cursor.getInt(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    if (cursor.getString(i) == null) {
                        Log.d(TAG, cursor.getColumnName(i) + "<-\"\"");
                        map.put(cursor.getColumnName(i), "");
                    } else {
                        Log.d(TAG, cursor.getColumnName(i) + "<-" + cursor.getString(i));
                        map.put(cursor.getColumnName(i), cursor.getString(i));
                    }
                    break;
            }
        }
        db.close();
        return map;
    }

    /**
     *
     * @param lastUpdated
     * @return
     */
    public ArrayList<Integer> getMatchesUpdatedSince(String lastUpdated) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (lastUpdated == null || lastUpdated.equals("")) {
            cursor = db.query(true, // distinct
                    tableName, // a. table
                    new String[]{KEY_MATCH_NUMBER}, // b. column names
                    null, // c. selections
                    null, // d. selections args
                    null, // e. group by
                    null, // f. having
                    null, // g. order by
                    null); // h. limit
        } else {
            cursor = db.query(true, // distinct
                    tableName, // a. table
                    new String[]{KEY_MATCH_NUMBER}, // b. column names
                    KEY_LAST_UPDATED + " > ?", // c. selections
                    new String[]{lastUpdated}, // d. selections args
                    null, // e. group by
                    null, // f. having
                    null, // g. order by
                    null); // h. limit
        }
        if (cursor == null) {
            return null;
        }

        if (cursor.getCount() == 0) {
            return new ArrayList<Integer>();
        }

        cursor.moveToFirst();
        ArrayList<Integer> matchNumbers = new ArrayList<>();
        do {
            matchNumbers.add(cursor.getInt(0));
            cursor.moveToNext();
        } while (!cursor.isAfterLast());
        db.close();
        return matchNumbers;
    }

    /**
     *
     * @return
     */
    public Cursor getAllMatches() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, // a. table
                null, // b. column names
                null, // c. selections
                null, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    /**
     *
     * @param since
     * @return
     */
    public Cursor getAllMatchesSince(String since) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, // a. table
                null, // b. column names
                KEY_LAST_UPDATED + " > ?", // c. selections
                new String[]{since}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    /**
     *
     * @param teamNumber
     * @return
     */
    public Cursor getTeamNotes(int teamNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, // a. table
                null, // b. column names
                null, // c. selections
                null, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                "1"); // h. limit

        if (cursor.getColumnIndex(Constants.Super_Inputs.SUPER_NOTES) != -1) {
            cursor = db.query(tableName, // a. table
                    new String[]{KEY_MATCH_NUMBER, Constants.Super_Inputs.SUPER_NOTES}, // b. column names
                    KEY_BLUE1 + " = ? or " + KEY_BLUE2 + " = ? or " + KEY_BLUE3 + " = ? or " + KEY_RED1 + " = ? or " +
                            KEY_RED2 + " = ? or " + KEY_RED3 + " = ?", // c. selections
                    new String[]{String.valueOf(teamNumber), String.valueOf(teamNumber),
                            String.valueOf(teamNumber), String.valueOf(teamNumber),
                            String.valueOf(teamNumber), String.valueOf(teamNumber)}, // d. selection args
                    null, // e. group by
                    null, // f. having
                    null, // g. order by
                    null); // h. limit
        } else {
            return null;
        }
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    /**
     *
     * @return
     */
    public ArrayList<Integer> getMatchNumbers() {
        ArrayList<Integer> teamNumbers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, // a. table
                new String[]{KEY_MATCH_NUMBER}, // b. column names
                null, // c. selections
                null, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
        if (cursor == null || cursor.getCount() == 0) {
            Log.d(TAG, "No rows came back");
            return null;
        }
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            teamNumbers.add(cursor.getInt(cursor.getColumnIndex(KEY_MATCH_NUMBER)));
            cursor.moveToNext();
        }

        return teamNumbers;
    }

    /**
     * Resets the table
     */
    //TODO: use onUpgrade?
    public void reset() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DROP TABLE " + tableName;
        db.execSQL(query);
        query = "CREATE TABLE IF NOT EXISTS " + tableName +
                "( " + KEY_ID + " INTEGER PRIMARY KEY UNIQUE NOT NULL," +
                " " + KEY_BLUE1 + " INTEGER NOT NULL," +
                " " + KEY_BLUE2 + " INTEGER NOT NULL," +
                " " + KEY_BLUE3 + " INTEGER NOT NULL," +
                " " + KEY_RED1 + " INTEGER NOT NULL," +
                " " + KEY_RED2 + " INTEGER NOT NULL," +
                " " + KEY_RED3 + " INTEGER NOT NULL," +
                " " + KEY_LAST_UPDATED + " DATETIME NOT NULL);";
        db.execSQL(query);
    }

    /**
     * Drops the table
     */
    public void remove() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DROP TABLE " + tableName;
        db.execSQL(query);
    }

}