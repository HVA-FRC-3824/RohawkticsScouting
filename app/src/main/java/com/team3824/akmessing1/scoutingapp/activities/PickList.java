package com.team3824.akmessing1.scoutingapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.team3824.akmessing1.scoutingapp.R;
import com.team3824.akmessing1.scoutingapp.adapters.PickListFragmentPagerAdapter;
import com.team3824.akmessing1.scoutingapp.views.CustomHeader;

public class PickList extends AppCompatActivity {
    final private String TAG = "PickList";

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PickListFragmentPagerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_list);

        Bundle extras = getIntent().getExtras();

        CustomHeader header = (CustomHeader)findViewById(R.id.pick_list_header);
        header.removeHome();

        findViewById(android.R.id.content).setKeepScreenOn(true);
        viewPager = (ViewPager) findViewById(R.id.pick_list_view_pager);
        adapter = new PickListFragmentPagerAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout = (TabLayout)findViewById(R.id.pick_list_tab_layout);
        tabLayout.setTabTextColors(Color.WHITE, Color.GREEN);
        tabLayout.setSelectedTabIndicatorColor(Color.GREEN);
        tabLayout.setupWithViewPager(viewPager);
    }
}