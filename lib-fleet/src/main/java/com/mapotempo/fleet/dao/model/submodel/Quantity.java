package com.mapotempo.fleet.dao.model.submodel;

import com.couchbase.lite.Dictionary;
import com.mapotempo.fleet.api.FleetException;
import com.mapotempo.fleet.core.IDatabaseHandler;
import com.mapotempo.fleet.core.model.SubModelBase;

public class Quantity extends SubModelBase
{

    public static final String DELIVERABLE_UNIT_ID = "deliverable_unit_id";
    public static final String QUANTITY = "quantity";
    public static final String LABEL = "label";
    public static final String UNIT_ICON = "unit_icon";
    public static final String QUANTITY_FORMATTED = "quantity_formatted";


    public Quantity(IDatabaseHandler iDatabaseHandler, Dictionary dictionary) throws FleetException
    {
        super(iDatabaseHandler, dictionary);
    }

    public int getDeliverableUnitId()
    {
        return mDictionary.getInt(DELIVERABLE_UNIT_ID);
    }

    public float getQuantity()
    {
        return mDictionary.getFloat(QUANTITY);
    }

    public String getLabel()
    {
        String label = mDictionary.getString(LABEL);
        if (label == null || "".equals(label))
        {
            label = "N/A";
        }
        return label;
    }

    public String getUnitIcon()
    {
        return mDictionary.getString(UNIT_ICON);
    }

    public String getQuantityFormatted()
    {
        return mDictionary.getString(QUANTITY_FORMATTED);
    }

    @Override
    public boolean isValid()
    {
        return getDeliverableUnitId() > 0 && getUnitIcon() != null && !getUnitIcon().equals("");
    }
}
