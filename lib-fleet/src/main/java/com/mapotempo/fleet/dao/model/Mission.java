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

package com.mapotempo.fleet.dao.model;

import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableArray;
import com.mapotempo.fleet.api.FleetException;
import com.mapotempo.fleet.core.IDatabaseHandler;
import com.mapotempo.fleet.core.model.ModelBase;
import com.mapotempo.fleet.core.model.ModelType;
import com.mapotempo.fleet.dao.access.MissionActionAccess;
import com.mapotempo.fleet.dao.access.MissionStatusTypeAccess;
import com.mapotempo.fleet.dao.model.submodel.Address;
import com.mapotempo.fleet.dao.model.submodel.Location;
import com.mapotempo.fleet.dao.model.submodel.Quantity;
import com.mapotempo.fleet.dao.model.submodel.TimeWindow;
import com.mapotempo.fleet.utils.DateUtils;
import com.mapotempo.fleet.utils.ModelUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ModelType(type = "mission")
public class Mission extends ModelBase
{
    final static private String TAG = Mission.class.toString();

    // MAPOTEMPO KEY
    public static final String ROUTE = "route_id";
    public static final String NAME = "name";
    public static final String DATE = "date";
    public static final String ETA = "eta";
    public static final String LOCATION = "location";
    public static final String ADDRESS = "address";
    public static final String QUANTITIES = "quantities";
    // public static final String SYNC_USER = "sync_user";
    public static final String MISSION_STATUS_TYPE_ID = "mission_status_type_id";
    public static final String REFERENCE = "reference";
    public static final String COMMENT = "comment";
    public static final String PHONE = "phone";
    public static final String DURATION = "duration";
    public static final String TIME_WINDOWS = "time_windows";
    public static final String CUSTOM_DATA = "custom_data";
    public static final String SURVEY_LOCATION = "survey_location";
    public static final String SURVEY_ADDRESS = "survey_address";
    public static final String ARCHIVED_AT = "archived_at";

    public Mission(IDatabaseHandler databaseHandler, Document document) throws FleetException
    {
        super(databaseHandler, document);
    }

    public String getName()
    {
        String name = mDocument.getString(NAME);
        return name != null ? name : "Nameless mission";
    }

    public Date getDate()
    {
        String dateString = mDocument.getString(DATE);
        return DateUtils.fromStringISO8601(dateString);
    }

    public Date getETAOrDefault()
    {
        String eta = mDocument.getString(ETA);
        if (eta != null)
            return DateUtils.fromStringISO8601(eta);
        return getDate();
    }

    public String getReference()
    {
        String res = mDocument.getString(REFERENCE);
        return res != null ? res : "";
    }

    public String getComment()
    {
        String res = mDocument.getString(COMMENT);
        return res != null ? res : "";
    }

    public String getPhone()
    {
        String res = mDocument.getString(PHONE);
        return res != null ? res : "";
    }

    public int getDuration()
    {
        int res = mDocument.getInt(DURATION);
        return 0;
    }

    public MissionStatusType getStatusType()
    {
        String status_id = mDocument.getString(MISSION_STATUS_TYPE_ID);
        try
        {
            MissionStatusTypeAccess missionStatusTypeAccess = new MissionStatusTypeAccess(mDatabaseHandler);
            return missionStatusTypeAccess.get(status_id);
        } catch (FleetException e)
        {
            return null;
        }
    }

    public void setStatus(MissionStatusType missionStatus)
    {
        mDocument.setString(MISSION_STATUS_TYPE_ID, missionStatus.getId());
    }

    public List<TimeWindow> getTimeWindows()
    {
        MutableArray array = mDocument.getArray(TIME_WINDOWS);
        return ModelUtils.arrayToSubmodelList(array, TimeWindow.class);
    }

    public Location getLocation()
    {
        try
        {
            Location res = new Location(mDatabaseHandler, mDocument.getDictionary(LOCATION));
            return res;
        } catch (FleetException e)
        {
            return null;
        }
    }

    public Address getAddress()
    {
        try
        {
            Address res = new Address(mDatabaseHandler, mDocument.getDictionary(ADDRESS));
            return res;
        } catch (FleetException e)
        {
            return null;
        }
    }

    public List<Quantity> getQuantities()
    {
        try {
            MutableArray quantities = mDocument.getArray(QUANTITIES);
            List<Quantity> quantityList = new ArrayList<>();

            if (quantities == null) return quantityList;

            for (int i = 0; i < quantities.count(); i++) {
                quantityList.add(new Quantity(mDatabaseHandler, quantities.getDictionary(i)));
            }

            return quantityList;
        } catch (FleetException e) {
            return null;
        }
    }

    public Map<String, Object> getCustomData()
    {
        Dictionary dico = mDocument.getDictionary(CUSTOM_DATA);
        if (dico != null)
            return dico.toMap();
        return new HashMap<>();
    }

    public void setCustomData(Map<String, String> data)
    {
        mDocument.setValue(CUSTOM_DATA, data);
    }

    // ##########################
    // ##    Survey Section    ##
    // ##########################

    public Location getSurveyLocation()
    {
        try
        {
            Location res = new Location(mDatabaseHandler, mDocument.getDictionary(SURVEY_LOCATION));
            return res;
        } catch (FleetException e)
        {
            return null;
        }
    }

    public void setSurveyLocation(Location location)
    {
        if (location.isValid())
            mDocument = mDocument.setDictionary(SURVEY_LOCATION, location.getDictionary());
    }

    public void deleteSurveyLocation()
    {
        mDocument = mDocument.remove(SURVEY_LOCATION);
    }

    public Address getSurveyAddress()
    {
        try
        {
            Address res = new Address(mDatabaseHandler, mDocument.getDictionary(SURVEY_ADDRESS));
            return res;
        } catch (FleetException e)
        {
            return null;
        }
    }

    public void setSurveyAddress(Address address)
    {
        if (address.isValid())
            mDocument = mDocument.setDictionary(SURVEY_ADDRESS, address.getDictionary());
    }

    public void deleteSurveyAddress()
    {
        mDocument = mDocument.remove(SURVEY_ADDRESS);
    }

    public List<MissionAction> getMissionActions()
    {
        try
        {
            MissionActionAccess missionActionAccess = new MissionActionAccess(mDatabaseHandler);
            return missionActionAccess.byMission(this);
        } catch (FleetException e)
        {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void archived()
    {
        Date date = new Date();
        mDocument.setString(ARCHIVED_AT, DateUtils.toStringISO8601(date));
        save();
    }

    public void unArchived()
    {
        mDocument.remove(ARCHIVED_AT);
        save();
    }
}
