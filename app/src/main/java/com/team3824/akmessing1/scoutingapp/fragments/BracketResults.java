package com.team3824.akmessing1.scoutingapp.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.team3824.akmessing1.scoutingapp.R;
import com.team3824.akmessing1.scoutingapp.adapters.BracketResultsAdapter;
import com.team3824.akmessing1.scoutingapp.database_helpers.PitScoutDB;
import com.team3824.akmessing1.scoutingapp.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */
public class BracketResults extends Fragment implements AdapterView.OnItemSelectedListener {

    private final String TAG = "BracketResults";
    private final int SEMI_1_INDEX = 0;
    private final int SEMI_2_INDEX = 1;
    private final int SEMI_3_INDEX = 2;
    private final int SEMI_4_INDEX = 3;
    private final int FINAL_1_INDEX = 4;
    private final int FINAL_2_INDEX = 5;
    private final String SELECT_ALLIANCE = "Select Alliance";
    File saveFile;
    FileInputStream saveFIS;
    FileOutputStream saveFOS;
    JSONArray json = null;
    ArrayList<Integer> teamList;
    String alliances[];
    Spinner spinners[];
    BracketResultsAdapter adapters[];
    TextView textViews[];
    String previous[];
    Button buttons[];

    public BracketResults() {
    }

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bracket_results, container, false);

        Context context = getContext();

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.APP_DATA, Context.MODE_PRIVATE);
        String eventId = sharedPreferences.getString(Constants.EVENT_ID, "");
        PitScoutDB pitScoutDB = new PitScoutDB(context, eventId);
        teamList = pitScoutDB.getTeamNumbers();

        alliances = new String[8];

        spinners = new Spinner[6];
        spinners[SEMI_1_INDEX] = (Spinner) view.findViewById(R.id.s_alliance1);
        spinners[SEMI_2_INDEX] = (Spinner) view.findViewById(R.id.s_alliance2);
        spinners[SEMI_3_INDEX] = (Spinner) view.findViewById(R.id.s_alliance3);
        spinners[SEMI_4_INDEX] = (Spinner) view.findViewById(R.id.s_alliance4);
        spinners[FINAL_1_INDEX] = (Spinner) view.findViewById(R.id.f_alliance1);
        spinners[FINAL_2_INDEX] = (Spinner) view.findViewById(R.id.f_alliance2);

        textViews = new TextView[6];
        textViews[SEMI_1_INDEX] = (TextView) view.findViewById(R.id.s_label1);
        textViews[SEMI_2_INDEX] = (TextView) view.findViewById(R.id.s_label2);
        textViews[SEMI_3_INDEX] = (TextView) view.findViewById(R.id.s_label3);
        textViews[SEMI_4_INDEX] = (TextView) view.findViewById(R.id.s_label4);
        textViews[FINAL_1_INDEX] = (TextView) view.findViewById(R.id.f_label1);
        textViews[FINAL_2_INDEX] = (TextView) view.findViewById(R.id.f_label2);

        buttons = new Button[3];
        buttons[0] = (Button) view.findViewById(R.id.s_1v2);
        buttons[1] = (Button) view.findViewById(R.id.s_3v4);
        buttons[2] = (Button) view.findViewById(R.id.f_1v2);


        previous = new String[6];

        saveFile = new File(getContext().getFilesDir(), String.format("%s_alliance_selection.txt", eventId));
        if (saveFile.exists()) {
            try {
                saveFIS = new FileInputStream(saveFile);
                String jsonText = "";
                char current;
                while (saveFIS.available() > 0) {
                    current = (char) saveFIS.read();
                    jsonText += String.valueOf(current);
                }
                Log.d(TAG, jsonText);
                json = new JSONArray(jsonText);
                saveFIS.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        if (json != null) {
            boolean allianceSelectionComplete = true;
            for (int i = 0; i < json.length(); i++) {
                try {
                    String team = json.getString(i);
                    if (team.equals(AllianceSelection.SELECT_TEAM)) {
                        allianceSelectionComplete = false;
                        break;
                    } else if (i < Constants.FIRST_PICK_OFFSET) {
                        alliances[i] = team;
                    } else {
                        alliances[i % 8] += String.format(" - %s", team);
                    }
                } catch (JSONException e) {
                    allianceSelectionComplete = false;
                    break;
                }
            }
            if (allianceSelectionComplete) {
                ((TextView) view.findViewById(R.id.q_alliance1)).setText(alliances[Constants.ALLIANCE_1_INDEX]);
                ((TextView) view.findViewById(R.id.q_alliance2)).setText(alliances[Constants.ALLIANCE_2_INDEX]);
                ((TextView) view.findViewById(R.id.q_alliance3)).setText(alliances[Constants.ALLIANCE_3_INDEX]);
                ((TextView) view.findViewById(R.id.q_alliance4)).setText(alliances[Constants.ALLIANCE_4_INDEX]);
                ((TextView) view.findViewById(R.id.q_alliance5)).setText(alliances[Constants.ALLIANCE_5_INDEX]);
                ((TextView) view.findViewById(R.id.q_alliance6)).setText(alliances[Constants.ALLIANCE_6_INDEX]);
                ((TextView) view.findViewById(R.id.q_alliance7)).setText(alliances[Constants.ALLIANCE_7_INDEX]);
                ((TextView) view.findViewById(R.id.q_alliance8)).setText(alliances[Constants.ALLIANCE_8_INDEX]);

                String semi1Array[] = {SELECT_ALLIANCE, alliances[Constants.ALLIANCE_1_INDEX], alliances[Constants.ALLIANCE_8_INDEX]};
                ArrayList<String> semi1 = new ArrayList<>(Arrays.asList(semi1Array));

                String semi2Array[] = {SELECT_ALLIANCE, alliances[Constants.ALLIANCE_4_INDEX], alliances[Constants.ALLIANCE_5_INDEX]};
                ArrayList<String> semi2 = new ArrayList<>(Arrays.asList(semi2Array));

                String semi3Array[] = {SELECT_ALLIANCE, alliances[Constants.ALLIANCE_2_INDEX], alliances[Constants.ALLIANCE_7_INDEX]};
                ArrayList<String> semi3 = new ArrayList<>(Arrays.asList(semi3Array));

                String semi4Array[] = {SELECT_ALLIANCE, alliances[Constants.ALLIANCE_3_INDEX], alliances[Constants.ALLIANCE_6_INDEX]};
                ArrayList<String> semi4 = new ArrayList<>(Arrays.asList(semi4Array));

                String final1Array[] = {SELECT_ALLIANCE};
                ArrayList<String> final1 = new ArrayList<>(Arrays.asList(final1Array));

                String final2Array[] = {SELECT_ALLIANCE};
                ArrayList<String> final2 = new ArrayList<>(Arrays.asList(final2Array));

                adapters = new BracketResultsAdapter[6];

                adapters[SEMI_1_INDEX] = new BracketResultsAdapter(context, android.R.layout.simple_spinner_dropdown_item, semi1);
                spinners[SEMI_1_INDEX].setAdapter(adapters[SEMI_1_INDEX]);
                spinners[SEMI_1_INDEX].setOnItemSelectedListener(this);

                adapters[SEMI_2_INDEX] = new BracketResultsAdapter(context, android.R.layout.simple_spinner_dropdown_item, semi2);
                spinners[SEMI_2_INDEX].setAdapter(adapters[SEMI_2_INDEX]);
                spinners[SEMI_2_INDEX].setOnItemSelectedListener(this);

                adapters[SEMI_3_INDEX] = new BracketResultsAdapter(context, android.R.layout.simple_spinner_dropdown_item, semi3);
                spinners[SEMI_3_INDEX].setAdapter(adapters[SEMI_3_INDEX]);
                spinners[SEMI_3_INDEX].setOnItemSelectedListener(this);

                adapters[SEMI_4_INDEX] = new BracketResultsAdapter(context, android.R.layout.simple_spinner_dropdown_item, semi4);
                spinners[SEMI_4_INDEX].setAdapter(adapters[SEMI_4_INDEX]);
                spinners[SEMI_4_INDEX].setOnItemSelectedListener(this);

                adapters[FINAL_1_INDEX] = new BracketResultsAdapter(context, android.R.layout.simple_spinner_dropdown_item, final1);
                spinners[FINAL_1_INDEX].setAdapter(adapters[FINAL_1_INDEX]);
                spinners[FINAL_1_INDEX].setOnItemSelectedListener(this);

                adapters[FINAL_2_INDEX] = new BracketResultsAdapter(context, android.R.layout.simple_spinner_dropdown_item, final2);
                spinners[FINAL_2_INDEX].setAdapter(adapters[FINAL_2_INDEX]);
                spinners[FINAL_2_INDEX].setOnItemSelectedListener(this);

                for (int i = 0; i < 6; i++) {
                    previous[i] = SELECT_ALLIANCE;
                }
            }
        }

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String spinnerValue = "";
        switch (parent.getId()) {
            case R.id.s_alliance1:
                if (!previous[SEMI_1_INDEX].equals(SELECT_ALLIANCE)) {
                    adapters[FINAL_1_INDEX].remove(previous[SEMI_1_INDEX]);
                }
                spinnerValue = String.valueOf(spinners[SEMI_1_INDEX].getSelectedItem());
                if (spinnerValue.equals(SELECT_ALLIANCE)) {
                    textViews[SEMI_1_INDEX].setText("Alliance ?");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_1_INDEX])) {
                    textViews[SEMI_1_INDEX].setText("Alliance 1");
                    adapters[FINAL_1_INDEX].add(alliances[Constants.ALLIANCE_1_INDEX]);
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_8_INDEX])) {
                    textViews[SEMI_1_INDEX].setText("Alliance 8");
                    adapters[FINAL_1_INDEX].add(alliances[Constants.ALLIANCE_8_INDEX]);
                } else {
                    assert false;
                }
                previous[SEMI_1_INDEX] = spinnerValue;

                if (!String.valueOf(spinners[SEMI_1_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE) && !String.valueOf(spinners[SEMI_2_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE)) {
                    buttons[0].setVisibility(View.VISIBLE);
                } else {
                    buttons[0].setVisibility(View.GONE);
                }
                
                break;
            case R.id.s_alliance2:
                if (!previous[SEMI_2_INDEX].equals(SELECT_ALLIANCE)) {
                    adapters[FINAL_1_INDEX].remove(previous[SEMI_2_INDEX]);
                }
                spinnerValue = String.valueOf(spinners[SEMI_2_INDEX].getSelectedItem());
                if (spinnerValue.equals(SELECT_ALLIANCE)) {
                    textViews[SEMI_2_INDEX].setText("Alliance ?");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_4_INDEX])) {
                    textViews[SEMI_2_INDEX].setText("Alliance 4");
                    adapters[FINAL_1_INDEX].add(alliances[Constants.ALLIANCE_4_INDEX]);
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_5_INDEX])) {
                    textViews[SEMI_2_INDEX].setText("Alliance 5");
                    adapters[FINAL_1_INDEX].add(alliances[Constants.ALLIANCE_5_INDEX]);
                } else {
                    assert false;
                }
                previous[SEMI_2_INDEX] = spinnerValue;

                if (!String.valueOf(spinners[SEMI_1_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE) && !String.valueOf(spinners[SEMI_2_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE)) {
                    buttons[0].setVisibility(View.VISIBLE);
                } else {
                    buttons[0].setVisibility(View.GONE);
                }

                break;
            case R.id.s_alliance3:
                if (!previous[SEMI_3_INDEX].equals(SELECT_ALLIANCE)) {
                    adapters[FINAL_2_INDEX].remove(previous[SEMI_3_INDEX]);
                }
                spinnerValue = String.valueOf(spinners[SEMI_3_INDEX].getSelectedItem());
                if (spinnerValue.equals(SELECT_ALLIANCE)) {
                    textViews[SEMI_3_INDEX].setText("Alliance ?");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_2_INDEX])) {
                    textViews[SEMI_3_INDEX].setText("Alliance 2");
                    adapters[FINAL_2_INDEX].add(alliances[Constants.ALLIANCE_2_INDEX]);
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_7_INDEX])) {
                    textViews[SEMI_3_INDEX].setText("Alliance 7");
                    adapters[FINAL_2_INDEX].add(alliances[Constants.ALLIANCE_7_INDEX]);
                } else {
                    assert false;
                }
                previous[SEMI_3_INDEX] = spinnerValue;

                if (!String.valueOf(spinners[SEMI_3_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE) && !String.valueOf(spinners[SEMI_4_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE)) {
                    buttons[1].setVisibility(View.VISIBLE);
                } else {
                    buttons[1].setVisibility(View.GONE);
                }

                break;
            case R.id.s_alliance4:
                if (!previous[SEMI_4_INDEX].equals(SELECT_ALLIANCE)) {
                    adapters[FINAL_2_INDEX].remove(previous[SEMI_4_INDEX]);
                }
                spinnerValue = String.valueOf(spinners[SEMI_4_INDEX].getSelectedItem());
                if (spinnerValue.equals(SELECT_ALLIANCE)) {
                    textViews[SEMI_4_INDEX].setText("Alliance ?");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_3_INDEX])) {
                    textViews[SEMI_4_INDEX].setText("Alliance 3");
                    adapters[FINAL_2_INDEX].add(alliances[Constants.ALLIANCE_3_INDEX]);
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_6_INDEX])) {
                    textViews[SEMI_4_INDEX].setText("Alliance 6");
                    adapters[FINAL_2_INDEX].add(alliances[Constants.ALLIANCE_6_INDEX]);
                } else {
                    assert false;
                }
                previous[SEMI_4_INDEX] = spinnerValue;

                if (!String.valueOf(spinners[SEMI_3_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE) && !String.valueOf(spinners[SEMI_4_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE)) {
                    buttons[1].setVisibility(View.VISIBLE);
                } else {
                    buttons[1].setVisibility(View.GONE);
                }

                break;
            case R.id.f_alliance1:
                spinnerValue = String.valueOf(spinners[FINAL_1_INDEX].getSelectedItem());
                if (spinnerValue.equals(SELECT_ALLIANCE)) {
                    textViews[FINAL_1_INDEX].setText("Alliance ?");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_1_INDEX])) {
                    textViews[FINAL_1_INDEX].setText("Alliance 1");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_8_INDEX])) {
                    textViews[FINAL_1_INDEX].setText("Alliance 8");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_4_INDEX])) {
                    textViews[FINAL_1_INDEX].setText("Alliance 4");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_5_INDEX])) {
                    textViews[FINAL_1_INDEX].setText("Alliance 5");
                } else {
                    assert false;
                }

                if (!String.valueOf(spinners[FINAL_1_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE) && !String.valueOf(spinners[FINAL_2_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE)) {
                    buttons[2].setVisibility(View.VISIBLE);
                } else {
                    buttons[2].setVisibility(View.GONE);
                }

                break;
            case R.id.f_alliance2:
                spinnerValue = String.valueOf(spinners[FINAL_2_INDEX].getSelectedItem());
                if (spinnerValue.equals(SELECT_ALLIANCE)) {
                    textViews[FINAL_2_INDEX].setText("Alliance ?");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_2_INDEX])) {
                    textViews[FINAL_2_INDEX].setText("Alliance 2");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_7_INDEX])) {
                    textViews[FINAL_2_INDEX].setText("Alliance 7");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_3_INDEX])) {
                    textViews[FINAL_2_INDEX].setText("Alliance 3");
                } else if (spinnerValue.equals(alliances[Constants.ALLIANCE_6_INDEX])) {
                    textViews[FINAL_2_INDEX].setText("Alliance 6");
                } else {
                    assert false;
                }

                if (!String.valueOf(spinners[FINAL_1_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE) && !String.valueOf(spinners[FINAL_2_INDEX].getSelectedItem()).equals(SELECT_ALLIANCE)) {
                    buttons[2].setVisibility(View.VISIBLE);
                } else {
                    buttons[2].setVisibility(View.GONE);
                }

                break;
            default:
                assert false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
