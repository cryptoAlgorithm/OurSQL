package com.cryptoalgo.oursql.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.Preferences;

/**
 * A pseudo-singleton for storing global application state.
 */
public class Context {
    private static final Context singleton = new Context();

    /**
     * Get a shared context instance. This method is present to conform
     * to the singleton pattern.
     * @return An instance of {@link Context}
     */
    public static Context getInstance() { return singleton; }

    /**
     * A shared preferences node for general application pref storage
     */
    public Preferences prefs = Preferences.userRoot().node(Context.class.getPackageName());
    /**
     * A shared SHA-256 hashing instance to be used throughout the application
     */
    public MessageDigest hashInstance;
    { // To catch exception in creating
        try {
            hashInstance = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not supported on this platform!");
        }
    }
}
