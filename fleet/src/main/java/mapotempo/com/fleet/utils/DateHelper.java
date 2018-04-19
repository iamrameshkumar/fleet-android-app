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

package mapotempo.com.fleet.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * DateHelper.
 */
public class DateHelper {

    // Date formats need to be synchronized.
    // TODO It is recommended to create separate format instances for each thread.
    // If multiple threads access a format concurrently, it must be synchronized externally."

    private static DateHelper ourInstance = new DateHelper();

    public static DateHelper getInstance() {
        return ourInstance;
    }

    // ####################################
    // ISO8601 date String
    // ####################################
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static String toStringISO8601(Date value) {
        long timeZoneMs = sdf.getTimeZone().getOffset(value.getTime());
        return sdf.format(value) + "+" + String.format("%02d:%02d", timeZoneMs / 3600000, timeZoneMs % 3600000);
    }

    public static Date fromStringISO8601(String value) {
        synchronized (sdf) {
            try {
                return sdf.parse(value);

            } catch (ParseException e) {
                return new Date(0);
            }
        }
    }

    // ###################################
    // Date String for SyncGateway channel
    // ###################################
    private static SimpleDateFormat sdf_for_channel = new SimpleDateFormat("yyyyMMdd");

    public static String dateForChannel(int dayOffset) {
        synchronized (sdf_for_channel) {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(calendar.DATE, dayOffset);
            return sdf_for_channel.format(calendar.getTime());
        }
    }

    public static String dateForChannel(Date date) {
        return sdf_for_channel.format(date.getTime());
    }

    // #########################
    // Date String for the debug
    // #########################
    private static SimpleDateFormat sdf_for_display = new SimpleDateFormat("dd MMMMMMM yyyy ':' hh'H' mm'M' ss's' SSS'ms'");

    public static String displayDate(Date value) {
        synchronized (sdf_for_display) {
            if (value != null)
                return sdf_for_display.format(value);
            else
                return null;
        }
    }
}
