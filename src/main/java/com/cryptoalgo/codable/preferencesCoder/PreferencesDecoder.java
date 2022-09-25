package com.cryptoalgo.codable.preferencesCoder;

import com.cryptoalgo.codable.Decoder;
import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.KeyedDecodingContainer;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * An implementation of a {@link Decoder} that decodes data
 * stored in Java's {@link Preferences}
 * @param <T> Type of enum of codingKeys to be used during decoding
 * @see PreferencesEncoder
 */
public final class PreferencesDecoder<T extends Enum<T>> implements Decoder<T>, KeyedDecodingContainer<T> {
    private final Preferences prefsNode;

    PreferencesDecoder(String node) {
        prefsNode = Preferences.userRoot().node(node);
    }

    public Optional<Boolean> decodeBooleanIfPresent(T forKey) throws DecodingException {
        String k = forKey.name();
        try {
            String v = prefsNode.get(k, null);
            if (v == null) return Optional.empty();
            return Optional.ofNullable(
                v.equalsIgnoreCase("false")
                    ? Boolean.FALSE
                    : v.equalsIgnoreCase("true") ? Boolean.TRUE : null
            );
        } catch (Exception e) {
            throw new DecodingException(k, "Boolean");
        }
    }
    public Boolean decodeBoolean(T forKey) throws DecodingException, NoSuchElementException {
        return decodeBooleanIfPresent(forKey).orElseThrow();
    }

    public Optional<Integer> decodeIntegerIfPresent(T forKey) throws DecodingException {
        String k = forKey.name();
        try {
            return prefsNode.nodeExists(k) ? Optional.of(prefsNode.getInt(k, 0)) : Optional.empty();
        } catch (BackingStoreException e) {
            throw new DecodingException(k, "Integer");
        }
    }
    public Integer decodeInteger(T forKey) throws DecodingException, NoSuchElementException {
        return decodeIntegerIfPresent(forKey).orElseThrow();
    }

    public Optional<String> decodeStringIfPresent(T forKey) throws DecodingException {
        return Optional.empty();
    }
    public String decodeString(T forKey) throws DecodingException, NoSuchElementException {
        return decodeStringIfPresent(forKey).orElseThrow();
    }

    public KeyedDecodingContainer<T> container() {
        return null;
    }
}
