package com.team3824.akmessing1.scoutingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.team3824.akmessing1.scoutingapp.R;
import com.team3824.akmessing1.scoutingapp.Team;

import java.util.ArrayList;

/**
 * Created by akmessing1 on 11/22/15.
 */
public class CardinalRankListAdapter extends ArrayAdapter<Integer> {

    ArrayList<Integer> teams;
    Context context;

    public CardinalRankListAdapter(Context context, int textViewResourceId, ArrayList<Integer> teams)
    {
        super(context, textViewResourceId, teams);
        this.context = context;
        this.teams = teams;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView)
    {
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_cardinal_rank, null);
        }

        TextView textView = (TextView)convertView.findViewById(R.id.textView);
        textView.setText(String.valueOf(position+1)+") "+String.valueOf(teams.get(position)));
        return  convertView;
    }

    public void add(int pos, int number)
    {
        teams.add(pos,number);
        notifyDataSetChanged();
    }

    public int get(int index)
    {
        return teams.get(index);
    }

    public int size()
    {
        return teams.size();
    }

}
