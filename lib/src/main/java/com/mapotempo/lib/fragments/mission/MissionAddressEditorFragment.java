package com.mapotempo.lib.fragments.mission;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapotempo.fleet.api.FleetException;
import com.mapotempo.fleet.dao.model.Mission;
import com.mapotempo.fleet.dao.model.submodel.Address;
import com.mapotempo.fleet.manager.MapotempoFleetManager;
import com.mapotempo.lib.MapotempoApplicationInterface;
import com.mapotempo.lib.R;

public class MissionAddressEditorFragment extends Fragment
{

    private MapotempoFleetManager mapotempoFleetManager;
    private Mission mMission;
    private TextInputEditText mStreet;
    private TextInputEditText mPostalCode;
    private TextInputEditText mCity;
    private TextInputEditText mState;
    private TextInputEditText mCountry;
    private TextInputEditText mDetail;

    public MissionAddressEditorFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final String mission_id = getActivity().getIntent().getStringExtra("mission_id");
        mapotempoFleetManager = ((MapotempoApplicationInterface) getContext().getApplicationContext()).getManager();
        mMission = mapotempoFleetManager.getMissionAccess().get(mission_id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_mission_adress_editor, container, false);
        Address missionAddress;

        if (mMission.getSurveyAddress().isValid())
            missionAddress = mMission.getSurveyAddress();
        else
            missionAddress = mMission.getAddress();

        mStreet = view.findViewById(R.id.street);
        mPostalCode = view.findViewById(R.id.postal_code);
        mCity = view.findViewById(R.id.city);
        mState = view.findViewById(R.id.state);
        mCountry = view.findViewById(R.id.country);
        mDetail = view.findViewById(R.id.detail);

        mStreet.setText(missionAddress.getStreet());
        mPostalCode.setText(missionAddress.getPostalcode());
        mCity.setText(missionAddress.getCity());
        mState.setText(missionAddress.getState());
        mCountry.setText(missionAddress.getCountry());
        mDetail.setText(missionAddress.getDetail());

        return view;
    }

    /**
     * Save the current modifications as Survey Address, then close the current activity
     */
    public void saveAddress()
    {
        try
        {
            Address surveyAddress = new Address(mapotempoFleetManager, mStreet.getText().toString(),
                mPostalCode.getText().toString(),
                mCity.getText().toString(),
                mState.getText().toString(),
                mCountry.getText().toString(),
                mDetail.getText().toString()
            );
            if (!surveyAddress.equals(mMission.getAddress()) && surveyAddress.isValid())
            {
                mMission.setSurveyAddress(surveyAddress);
                mMission.save();
            }
        } catch (FleetException e)
        {
            //            TODO CL2.0
            return;
        }
    }

    /**
     * Reset the current address by removing it from the survey model.
     */
    public void resetAddress()
    {
        mMission.deleteSurveyAddress();
        mMission.save();
    }
}
