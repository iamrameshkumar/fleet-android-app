/*
 * Copyright © Mapotempo, 2018
 *
 * This file is part of Mapotempo.
 *
 * Mapotempo is free software. You can redistribute it and/or
 * modify since you respect the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Mapotempo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the Licenses for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Mapotempo. If not, see:
 * <http://www.gnu.org/licenses/agpl.html>
 */

package com.mapotempo.lib.fragments.actions;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapotempo.fleet.api.model.MissionStatusActionInterface;
import com.mapotempo.fleet.api.model.MissionStatusTypeInterface;
import com.mapotempo.lib.R;
import com.mapotempo.lib.fragments.missions.MissionsListFragment;

import java.util.ArrayList;
import java.util.List;

public class ActionsListFragment extends Fragment {

    private RecyclerView mRecyclerView;

    private ActionsRecyclerViewAdapter mRecyclerAdapter;

    private List<MissionStatusActionInterface> mMissionStatusActions = new ArrayList<>();

    private OnMissionActionSelectedListener mListener = new OnMissionActionSelectedListener() {
        @Override
        public void onMissionActionSelected(MissionStatusTypeInterface newStatus) {
            // DEFAULT
        }
    };

    // ===================================
    // ==  Android Fragment Life cycle  ==
    // ===================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.MapotempoTheme);
        if (false) // TODO to finish when theme switch will be implemented
            contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.MapotempoTheme_Night);

        View view = inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.fragment_actions, container, false);
        mRecyclerView = view.findViewById(R.id.action_recycler_view);
        mRecyclerAdapter = new ActionsRecyclerViewAdapter(getContext(), new OnMissionActionSelectedListener() {
            @Override
            public void onMissionActionSelected(MissionStatusTypeInterface newStatus) {
                mListener.onMissionActionSelected(newStatus);
            }
        }, mMissionStatusActions);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        return view;
    }

    // ==============
    // ==  Public  ==
    // ==============

    public void setActions(List<MissionStatusActionInterface> missionStatusActions) {
        mMissionStatusActions = new ArrayList<>(missionStatusActions);
        mRecyclerAdapter.notifyDataSyncHasChanged(mMissionStatusActions);
    }

    /**
     * This interface must be implemented by activities that contain {@link MissionsListFragment}
     */
    public interface OnMissionActionSelectedListener {
        /**
         * A Callback triggered when an item list is created. Use it to set a onMissionFocus listener to each of them.
         *
         * @param newStatus return the next newStatus item selected
         */
        void onMissionActionSelected(MissionStatusTypeInterface newStatus);
    }

    public void setOnActionSelectedListener(OnMissionActionSelectedListener onActionSelectedListener) {
        mListener = onActionSelectedListener;
    }
}
