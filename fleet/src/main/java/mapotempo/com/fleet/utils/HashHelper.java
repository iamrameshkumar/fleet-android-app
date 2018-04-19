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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import mapotempo.com.fleet.api.FleetException;

public class HashHelper {
    private static String algorithm = "SHA-256";

    public static String sha256(String string) throws FleetException {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            String test = printHexBinary(md.digest(string.getBytes()));
            return test.toLowerCase();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new FleetException("Error with : " + algorithm + " algorithm", e);
        }
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    private static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}
