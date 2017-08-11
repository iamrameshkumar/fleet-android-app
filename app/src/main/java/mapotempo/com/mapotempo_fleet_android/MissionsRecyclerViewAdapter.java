package mapotempo.com.mapotempo_fleet_android;

import android.content.res.Configuration;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RelativeLayout;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.TextView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.View;

import com.mapotempo.fleet.core.model.Mission;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import mapotempo.com.mapotempo_fleet_android.MissionsFragment.OnMissionsInteractionListener;
import mapotempo.com.mapotempo_fleet_android.dummy.MissionModel;
import mapotempo.com.mapotempo_fleet_android.dummy.MissionsManager;

/**
 * {@link RecyclerView.Adapter} that can display a {@link MissionModel} and makes a call to the
 * specified {@link MissionsFragment.OnMissionsInteractionListener}.
 */
public class MissionsRecyclerViewAdapter extends RecyclerView.Adapter<MissionsRecyclerViewAdapter.ViewHolder> {

    private final OnMissionsInteractionListener mListener;
    private final Context mContext;
    private List<View> mListViews = new ArrayList<>();
    private boolean orientLandscape;

    private int missionsCount = 0;
    private List<Mission> mMissions;


    private int mCurrentPositionInView = 0;

    public MissionsRecyclerViewAdapter(Context context, OnMissionsInteractionListener listener, List<Mission> missions) {
        mMissions = missions;
        missionsCount = missions.size();
        mContext = context;
        mListener = listener;
        orientLandscape = (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_missions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Mission mission = mMissions.get(position);
        final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String missionDate = dateFormat.format(mission.getDeliveryDate());

        holder.mItem = mission;
        holder.mName.setText(mission.getName());
        holder.mCompany.setText(mission.getCompanyId());
        holder.mDelivery_date.setText(missionDate);
        holder.mStatus.setBackgroundColor(MissionsManager.fakeStatusColor());

        holder.mView.setOnClickListener(mListener.onListMissionsInteraction(position));

        mListViews.add(holder.mView);

        if (position == 0 && mCurrentPositionInView == 0)
            setCurrentMission(0);

        checkMissionStatus(holder);
    }

    private void checkMissionStatus(final ViewHolder holder) {
        AppCompatImageButton checkBtn = holder.mView.findViewById(R.id.check_button);
        if (checkBtn == null) return;

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("NOT IMPLEMENTED", "CHANGE MISSION STATUS IS NOT YET IMPLEMENTED");
            }
        });
    }

    public void setCurrentMission(int position) {
        if (mListViews.size() > 0 && orientLandscape) {
            View newView = mListViews.get(position);
            View oldView = mListViews.get(mCurrentPositionInView);

            newView.setBackgroundColor(Color.parseColor("#e2ecfd"));
            if (position != mCurrentPositionInView)
                oldView.setBackgroundColor(Color.WHITE);

            mCurrentPositionInView = position;
        }
    }

    @Override
    public int getItemCount() {
        return missionsCount;
    }

    public void notifyDataSyncHasChanged(List<Mission> missions) {
        mMissions = missions;
        missionsCount = missions.size();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mName;
        public final TextView mCompany;
        public final TextView mDelivery_date;
        public final RelativeLayout mStatus;
        public Mission mItem;

        public ViewHolder(View view) {
            super(view);

            mView = view;
            mName = view.findViewById(R.id.name);
            mCompany = view.findViewById(R.id.company);
            mStatus = view.findViewById(R.id.mission_status);
            mDelivery_date = view.findViewById(R.id.delivery_date);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mName.getText() + "'";
        }
    }
}
