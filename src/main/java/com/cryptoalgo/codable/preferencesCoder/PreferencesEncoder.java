package com.cryptoalgo.codable.preferencesCoder;

import com.cryptoalgo.codable.Encodable;
import com.cryptoalgo.codable.Encoder;
import com.cryptoalgo.codable.EncodingException;
import com.cryptoalgo.codable.KeyedEncodingContainer;
import org.jetbrains.annotations.NotNull;

import java.util.prefs.Preferences;

/**
 * Built-in encoder implementation which writes data to non-volatile
 * storage through Java's Preferences module.
 * <p>
 *     <b>Implications:</b>
 *     Limitations to Java's Preferences module also apply here, most notably
 *     that large amounts of data shouldn't be written to nodes. Doing so
 *     leads to significant performance penalties and might cause OOM
 *     errors when attempting to retrieve those keys.
 * </p>
 * @param <T> Type of enum of codingKeys to be used during encoding
 */
public final class PreferencesEncoder<T extends Enum<T>> implements Encoder<T>, KeyedEncodingContainer<T> {
    private final Preferences prefsNode;

    /**
     * Creates an instance of an encoder which can be used to serialize
     * {@link com.cryptoalgo.codable.Encodable Encodable} classes to Java's {@link Preferences}
     * @param node Path of {@link Preferences} node to write values to
     */
    PreferencesEncoder(String node) {
        prefsNode = Preferences.userRoot().node(node);
    }

    <E extends Encodable> void encode(E encoding) throws EncodingException {
        encoding.encode(this);
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
