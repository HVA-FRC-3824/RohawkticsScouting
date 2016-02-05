package com.team3824.akmessing1.scoutingapp.database_helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DriveTeamFeedbackDB extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "RohawkticsDB";
    private String TAG = "DriveTeamFeedbackDB";

    // Initial Table Columns names
    public static final String KEY_ID = "_id";
    public static final String KEY_TEAM_NUMBER = "_id";
    public static final String KEY_LAST_UPDATED = "last_updated";
    public static final String KEY_COMMENTS = "comments";

    private String tableName;
    private static SimpleDateFormat dateFormat;


    public DriveTeamFeedbackDB(Context context, String eventID)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        tableName = "driveteamFeedback_"+eventID;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String queryString = "CREATE TABLE IF NOT EXISTS "+tableName +
                "( "+KEY_ID+" INT PRIMARY KEY UNIQUE NOT NULL,"+
                " "+KEY_COMMENTS+" TEXT NOT NULL,"+
                " "+KEY_LAST_UPDATED+" DATETIME NOT NULL);";
        db.execSQL(queryString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+tableName);
        this.onCreate(db);
    }

    // Add column to table
    public void addColumn(String columnName, String columnType)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
    }

    public void updateComments(int teamNumber, String comment)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        // Make sure the last updated time gets updated
        ContentValues cvs = new ContentValues();
        cvs.put(KEY_TEAM_NUMBER,teamNumber);
        cvs.put(KEY_LAST_UPDATED, dateFormat.format(new Date()));
        cvs.put(KEY_COMMENTS,comment);
        db.replace(tableName, null, cvs);
    }

    public String getComments(int teamNumber)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, // a. table
                null, // b. column names
                KEY_TEAM_NUMBER + " = ?", // c. selections
                new String[]{String.valueOf(teamNumber)}, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                "1"); // h. limit

        if(cursor == null || cursor.getCount() == 0)
            return "";

        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(KEY_COMMENTS));
    }
}

