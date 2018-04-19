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

package mapotempo.com.fleet.core;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Endpoint;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorChangeListener;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import mapotempo.com.fleet.api.FleetException;
import mapotempo.com.fleet.utils.HashHelper;

/**
 * {@inheritDoc}
 */
public class DatabaseHandler {

    private static String TAG = DatabaseHandler.class.getName();

    final int RELEASE_TIMEOUT = 10;

    private Context mContext;

    // Database
    private String mDbname;

    public Database mDatabase;

    private DatabaseConfiguration mDatabaseConfiguration;

    // Connexion params
    private Endpoint mTargetEndpoint;

    final private String mUser;

    final private String mPassword;

    final private OnCatchLoginError mOnCatchLoginError;

    public interface OnCatchLoginError {
        void CatchLoginError();
    }

    // Replicators
    private Replicator mUserReplicator = null;
    private Replicator mCompanyReplicator = null;
    //    private Replicator mReplicator = null;
    private List<Replicator> mReplicatorList = new ArrayList<>();

    public DatabaseHandler(Context context,
                           String user, String password, String syncGatewayUrl, OnCatchLoginError onCatchLoginError) throws FleetException {
        mContext = context;
        mUser = user;
        mPassword = password;
        mDbname = databaseNameGenerator(mUser, syncGatewayUrl, password);
        mDatabaseConfiguration = new DatabaseConfiguration(mContext);
        mOnCatchLoginError = onCatchLoginError;

        try {
            mDatabase = new Database(mDbname, mDatabaseConfiguration);
        } catch (CouchbaseLiteException e) {
            throw new FleetException("Error : Can't open database");
        }

        try {
            mTargetEndpoint = new URLEndpoint(new URI(syncGatewayUrl));
        } catch (URISyntaxException e) {
            throw new FleetException("Error : Invalid url endpoint", e);
        }
    }

    private String databaseNameGenerator(String userName, String url, String mPassword) throws FleetException {
        // Database name must be unique for a username, password and specific url.
        return "database_" + HashHelper.sha256(userName).substring(0, 5) + HashHelper.sha256(url).substring(0, 5) + HashHelper.sha256(mPassword).substring(0, 5);
    }

    public void configureUserReplication() throws FleetException {
        ReplicatorConfiguration replConfig = new ReplicatorConfiguration(mDatabase, mTargetEndpoint);
        replConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
        replConfig.setContinuous(true);
        replConfig.setAuthenticator(new BasicAuthenticator(mUser, mPassword));
        List<String> channels = new ArrayList<>();
        channels.add("!");
        channels.add("user:" + mUser);
        replConfig.setChannels(channels);

        mUserReplicator = new Replicator(replConfig);
        ListenerToken tk = mUserReplicator.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(ReplicatorChange change) {
                if (change.getStatus().getError() != null) {
                    Log.i(TAG, "Error code ::  " + change.getStatus().getError().getCode());
                    switch (change.getStatus().getError().getCode()) {
                        case 10401:
                            mOnCatchLoginError.CatchLoginError();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        mUserReplicator.start();

        mReplicatorList.add(mUserReplicator);
    }

    public void configureCompanyReplication(String companyId) throws FleetException {
        ReplicatorConfiguration replConfig = new ReplicatorConfiguration(mDatabase, mTargetEndpoint);
        replConfig.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
        replConfig.setContinuous(true);
        replConfig.setAuthenticator(new BasicAuthenticator(mUser, mPassword));
        List<String> channels = new ArrayList<>();
        channels.add("company:" + companyId);
        channels.add("mission_status_type:" + companyId);
        channels.add("mission_action_type:" + companyId);
        replConfig.setChannels(channels);
        mCompanyReplicator = new Replicator(replConfig);
        mCompanyReplicator.start();
        mReplicatorList.add(mCompanyReplicator);
    }

    /**
     * Close database and delete if required.
     * This function is blocking.
     *
     * @param deleteDB database deleting option
     * @throws FleetException
     */
    public void release(final boolean deleteDB) throws FleetException {
        for (Replicator r : mReplicatorList) {
            try {
                stopContinuousReplicator(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            if (deleteDB)
                mDatabase.delete();
            else
                mDatabase.close();
        } catch (CouchbaseLiteException e) {
            // TODO Comment exception propagation
            throw new FleetException("Error during database closing", e);
        }
    }

    private void stopContinuousReplicator(final Replicator repl) throws InterruptedException {
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> try to stop repl : " + System.identityHashCode(repl));

        final CountDownLatch latch = new CountDownLatch(1);
        ListenerToken token = repl.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(ReplicatorChange change) {
                Replicator.Status status = change.getStatus();
                if (status.getActivityLevel() == Replicator.ActivityLevel.STOPPED) {
                    Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> repl stopped : " + System.identityHashCode(repl));
                    latch.countDown();
                }
            }
        });
        boolean res = false;
        try {
            repl.stop();
            res = latch.await(15, TimeUnit.SECONDS);
        } finally {
            repl.removeChangeListener(token);
        }
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> repl stoping result : " + res);
    }
}
