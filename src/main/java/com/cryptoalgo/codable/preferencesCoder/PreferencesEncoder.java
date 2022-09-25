package com.cryptoalgo.codable.preferencesCoder;

import com.cryptoalgo.codable.Encoder;
import com.cryptoalgo.codable.KeyedEncodingContainer;
import org.jetbrains.annotations.NotNull;

import java.util.prefs.Preferences;

/**
 * Built-in encoder implementation which writes data to non-volatile
 * storage through Java's Preferences module. <br><br>
 * <b>Implications:</b><br>
 * Limitations to Java's Preferences module also apply here, most notably
 * that large amounts of data shouldn't be written with it.
 * @param <T> - Enum of codingKeys to be used during encoding
 */
public class PreferencesEncoder<T extends Enum<T>> implements Encoder<T>, KeyedEncodingContainer<T> {
    private final Preferences prefsNode;

    PreferencesEncoder(String node) {
        prefsNode = Preferences.userRoot().node(node);
    }

    // Encoder conformance
    public KeyedEncodingContainer<T> container() {
        return this;
    }

    // EncodingContainer conformance
    public void encode(@NotNull Integer value, T forKey) {
        prefsNode.putInt(forKey.name(), value);
    }

    public void encode(@NotNull Boolean value, T forKey) {
        prefsNode.putBoolean(forKey.name(), value);
    }

    public void encode(@NotNull String value, T forKey) {
        prefsNode.put(forKey.name(), value);
    }
}
