package com.cryptoalgo.oursql.support;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public final class I18N {
    public static final String BUNDLE_NAME = "locales/strings";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    public static String getString(String key, Object... values) {
        return MessageFormat.format(RESOURCE_BUNDLE.getString(key), values);
    }
}
