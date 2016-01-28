package com.team3824.akmessing1.scoutingapp.event_list_items;

public class EventListItemShots {

    public int mRank, mTeamNumber, mAutoMade, mAutoTaken, mTeleopMade, mTeleopTaken;
    public float mAutoPercentage, mTeleopPercentage;

    public EventListItemShots(int teamNumber)
    {
        mTeamNumber = teamNumber;
        mAutoMade = 0;
        mAutoTaken = 0;
        mAutoPercentage = 0.0f;
        mTeleopMade = 0;
        mTeleopTaken = 0;
        mTeleopPercentage = 0.0f;
    }
}