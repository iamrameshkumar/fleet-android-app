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

package mapotempo.com.fleet;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.LogDomain;
import com.couchbase.lite.LogLevel;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import mapotempo.com.fleet.core.DatabaseHandler;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(Parameterized.class)
public class DatabaseHandlerInstrumentedTest {

    @Parameterized.Parameters
    public static Object[][] data() {
        return new Object[1][0];
    }

    private static String TAG = DatabaseHandlerInstrumentedTest.class.getName();
    DatabaseHandler.OnCatchLoginError onCatchLoginError = new DatabaseHandler.OnCatchLoginError() {
        @Override
        public void CatchLoginError() {
        }
    };

    @Test
    public void simpleDatabaseHandlerTest() throws Exception {
        Database.setLogLevel(LogDomain.ALL, LogLevel.DEBUG);

        Log.i(TAG, "_____________________________________________________________");

        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        DatabaseHandler dbHandler = new DatabaseHandler(appContext,
                "5ffbb992f9c44a4e7a50897f785c5f63d38e587130f7cf86a07359d609dc50dd",
                "123456",
                "ws://192.168.1.252:4984/db",
                onCatchLoginError);

        dbHandler.configureUserReplication();
        dbHandler.configureCompanyReplication("company-dzC-REbyrC");

        // Wait for sync replication
        Thread.sleep(2000);

        // Inspect database replication
        Query query = QueryBuilder
                .select(SelectResult.expression(Meta.id),
                        SelectResult.property("type"))
                .from(DataSource.database(dbHandler.mDatabase));
        try {
            ResultSet rs = query.execute();
            for (Result result : rs) {
                Log.i("Sample", String.format("hotel id -> %s", result.getString("id")));
            }
        } catch (CouchbaseLiteException e) {
            Log.e("Sample", e.getLocalizedMessage());
        }

        Log.i(TAG, "_____________________________________________________________");

        // Release database
        dbHandler.release(true);

        Log.i(TAG, "______________________END______________________");
    }
}
