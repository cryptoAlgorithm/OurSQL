package com.cryptoalgo.oursql.model;

import com.cryptoalgo.db.Cluster;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A pseudo-singleton for storing global application state.
 */
public class Context {
    private static final Context singleton = new Context();
    public static Context getInstance() { return singleton; } // To conform to the singleton pattern

    public Preferences prefs = Preferences.userRoot().node(Context.class.getPackageName());
    public MessageDigest hashInstance;
    { // To catch exception in creating
        try {
            hashInstance = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not supported on this platform!");
        }
    }

    public void storeCluster(Cluster cluster) throws IOException, BackingStoreException {

    }
    public Cluster[] getStoredClusters() throws BackingStoreException {
        return null;
    }
}
