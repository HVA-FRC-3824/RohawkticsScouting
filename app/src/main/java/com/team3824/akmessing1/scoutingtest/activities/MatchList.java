package com.team3824.akmessing1.scoutingtest.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.team3824.akmessing1.scoutingtest.JsonUTF8Request;
import com.team3824.akmessing1.scoutingtest.R;
import com.team3824.akmessing1.scoutingtest.ScheduleDB;

import org.json.JSONArray;

// Activity which displays each match and corresponding team based on the selected alliance color
// and number as button which lead to match scouting
public class MatchList extends AppCompatActivity {

    private static final String TAG = "MatchList";
    private SimpleCursorAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_list);

        final SharedPreferences sharedPreferences = getSharedPreferences("appData", Context.MODE_PRIVATE);
        final String eventID = sharedPreferences.getString("event_id", "");


        final ScheduleDB scheduleDB = new ScheduleDB(this, eventID);
        displayListView(scheduleDB, sharedPreferences);
    }

    // Setup list view with the schedule
    private void displayListView(ScheduleDB scheduleDB, SharedPreferences sharedPreferences)
    {
        Cursor cursor = scheduleDB.getSchedule();
        if(cursor != null)
        {
            LinearLayout linearLayout = (LinearLayout)findViewById(R.id.match_list);
            int alliance_number = sharedPreferences.getInt("alliance_number",0);
            String alliance_color = sharedPreferences.getString("alliance_color","");

            cursor.moveToFirst();
            TableLayout.LayoutParams lp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(4,4,4,4);

            // Add buttons
            do{
                Button button = new Button(this);
                button.setLayoutParams(lp);
                final int matchNumber = cursor.getInt(0);
                final int teamNumber = cursor.getInt(cursor.getColumnIndex(alliance_color.toLowerCase() + alliance_number));
                button.setText("Match " + matchNumber + ": " + teamNumber);
                switch (alliance_color.toLowerCase())
                {
                    case "blue":
                        button.setBackgroundColor(Color.BLUE);
                        break;
                    case "red":
                        button.setBackgroundColor(Color.RED);
                        break;
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MatchList.this, MatchScouting.class);
                        intent.putExtra("team_number",teamNumber);
                        intent.putExtra("match_number",matchNumber);
                        startActivity(intent);
                    }
                });
                linearLayout.addView(button);
                cursor.moveToNext();
            }while(!cursor.isAfterLast());
        }
    }
}
