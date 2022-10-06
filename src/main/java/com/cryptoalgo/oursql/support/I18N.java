package com.cryptoalgo.oursql.support;

import java.text.MessageFormat;
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
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

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
