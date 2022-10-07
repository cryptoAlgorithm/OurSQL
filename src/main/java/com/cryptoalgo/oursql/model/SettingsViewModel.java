package com.cryptoalgo.oursql.model;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.prefs.Preferences;

/**
 * ViewModel for app settings. Can be used outside the Settings
 * view controller. Acts as a bridge between JavaFX observables and
 * preference nodes.
 */
public class SettingsViewModel {
    /**
     * The preferences node for storing settings
     */
    private static final Preferences prefs = Context.getInstance().prefs.node("settings");

    private static final String
        SPLASH_ENABLED_KEY = "showSplash",
        LANG_KEY = "lang";

    private static final SimpleBooleanProperty splashEnabled = new ReadOnlyBooleanWrapper();
    private static final SimpleStringProperty lang = new SimpleStringProperty();

    static {
        // Manually update all properties first
        splashEnabled.set(prefs.getBoolean(SPLASH_ENABLED_KEY, true));
        lang.set(prefs.get(LANG_KEY, "en"));
        // Listen for preference changes
        // Properties will be updated twice but that doesn't exactly hurt
        prefs.addPreferenceChangeListener(evt -> {
            switch(evt.getKey()) {
                case SPLASH_ENABLED_KEY -> splashEnabled.set(prefs.getBoolean(SPLASH_ENABLED_KEY, true));
                case LANG_KEY -> lang.set(prefs.get(LANG_KEY, "en"));
            }
        });
        // Handle observable property updates
        splashEnabled.addListener((observable, oldValue, newValue) ->
            prefs.putBoolean(SPLASH_ENABLED_KEY, newValue)
        );
        lang.addListener((observable, oldValue, newValue) -> prefs.put(LANG_KEY, newValue));
    }

    /**
     * Get a read-write observable boolean property for the splash screen enabled pref.
     * @return A read-write observable boolean property
     */
    public static SimpleBooleanProperty splashEnabledProperty() { return splashEnabled; }

    public static SimpleStringProperty langProperty() { return lang; }
}
