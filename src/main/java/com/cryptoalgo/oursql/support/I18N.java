package com.cryptoalgo.oursql.support;

import com.cryptoalgo.oursql.model.SettingsViewModel;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Utility class for all things localisation
 */
public final class I18N {
    /**
     * Name of the localisation bundle
     */
    public static final String BUNDLE_NAME = "locales/strings";
    /**
     * The current resource bundle
     */
    private static ResourceBundle RESOURCE_BUNDLE;

    /**
     * Supported languages and their locale codes
     */
    private static final HashMap<String, String> languages = new HashMap<>();
    static {
        // The human-readable names are not localised for a reason
        languages.put("en", "English");
        languages.put("zh", "中文");
        SettingsViewModel.langProperty().addListener((c, ov, nv) -> {
            if (languages.containsKey(nv))
                RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale(nv));
        });
        RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale(SettingsViewModel.langProperty().get()));
    }

    /**
     * Get the locale codes names of supported languages
     * @return A string array containing locale codes of the supported languages
     */
    public static String[] getLocales() {
        return languages.keySet().toArray(new String[0]);
    }

    /**
     * Lookup the human-readable name of a language given its locale code
     * @param localeCode Locale code to lookup
     * @return The human-readable name of the language, null if not found
     */
    @Nullable
    public static String getHumanReadableNameFor(String localeCode) {
        return languages.getOrDefault(localeCode, null);
    }

    /**
     * Get the locale code of a human-readable language name
     * @param humanReadableName Human-readable language name to lookup
     * @return The locale code of the language, null if not found
     */
    @Nullable
    public static String getLocaleCodeFor(String humanReadableName) {
        for (Map.Entry<String, String> entry : languages.entrySet())
            if (entry.getValue().equals(humanReadableName))
                return entry.getKey();
        return null;
    }

    /**
     * Get a localised string from the bundle, optionally substituting values into the template.
     * @param key Localisation key, as present in the bundle
     * @param values Values to substitute
     * @return Localised string
     */
    public static String getString(String key, Object... values) {
        return MessageFormat.format(RESOURCE_BUNDLE.getString(key), values);
    }
}
