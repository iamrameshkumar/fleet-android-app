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

package com.mapotempo.lib.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mapotempo.fleet.dao.model.Route;
import com.mapotempo.fleet.manager.MapotempoFleetManager;
import com.mapotempo.lib.MapotempoApplication;
import com.mapotempo.lib.R;
import com.mapotempo.lib.fragments.base.MapotempoBaseDialogFragment;

import java.util.Calendar;
import java.util.List;

public class ArchiveDialogFragment extends MapotempoBaseDialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.archive_outdated)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.MILLISECOND, 0);
                    final MapotempoFleetManager mapotempoFleetManagerInterface = ((MapotempoApplication) getContext().getApplicationContext()).getManager();
                    List<Route> routes = mapotempoFleetManagerInterface.getRouteAccess().all();
                    for (Route route : routes)
                    {
                        Calendar routeCalendar = Calendar.getInstance();
                        routeCalendar.setTime(route.getDate());
                        if (today.compareTo(routeCalendar) > 0)
                        {
                            route.archived();
                            route.save();
                        }
                    }
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                }
            });
        return builder.create();
    }
}
