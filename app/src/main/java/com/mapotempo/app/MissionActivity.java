package com.mapotempo.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mapotempo.fleet.api.model.MissionInterface;
import com.mapotempo.lib.mission.MissionDetailsFragment;
import com.mapotempo.lib.mission.MissionsPagerFragment;

public class MissionActivity extends AppCompatActivity implements
        MissionsPagerFragment.OnMissionFocusListener,
        MissionDetailsFragment.OnMissionDetailsFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mission_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.mission);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMissionFocus(int position) {
    }


    // ==================================================
    // ==  OnMissionDetailsFragmentListener Interface  ==
    // ==================================================
    @Override
    public void onMapImageViewClick(MissionInterface mission) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("mission_id", mission.getId());
        startActivity(intent);
    }
}
