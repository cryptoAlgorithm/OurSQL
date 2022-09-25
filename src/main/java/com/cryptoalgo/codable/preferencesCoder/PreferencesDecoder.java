package com.cryptoalgo.codable.preferencesCoder;

import com.cryptoalgo.codable.Decodable;
import com.cryptoalgo.codable.Decoder;
import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.KeyedDecodingContainer;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * An implementation of a {@link Decoder} that decodes data
 * stored in Java's {@link Preferences}
 * <p>
 *     <b>Note:</b>
 *     Decoding methods returns {@link Optional#empty()} if the value in the provided
 *     key exists but cannot be cased to the requested type. This behaviour might be
 *     modified in the future to throw {@link DecodingException} instead.
 * </p>
 * @param <T> Type of enum of codingKeys to be used during decoding
 * @see PreferencesEncoder
 */
public final class PreferencesDecoder<T extends Enum<T>> implements Decoder<T>, KeyedDecodingContainer<T> {
    private final Preferences prefsNode;

    /**
     * Creates an instance of a decoder which can be used to deserialize
     * {@link com.cryptoalgo.codable.Decodable Decodable} classes from
     * Java's {@link Preferences}
     * @param node Path of {@link Preferences} node to read values from
     */
    public PreferencesDecoder(String node) {
        prefsNode = Preferences.userRoot().node(node);
    }

    /**
     * Create an instance of a {@link com.cryptoalgo.codable.Decodable Decodable}
     * class by decoding values from Preferences
     */
    public <D extends Decodable> D decode(Class<D> decoding) throws DecodingException, NoSuchElementException {
        try {
            return decoding.getDeclaredConstructor(Decoder.class).newInstance(this);
        } catch (Exception e) {
            throw new DecodingException("/", "root");
        }
    }

    // KeyedDecodingContainer conformance
    public Optional<Boolean> decodeBooleanIfPresent(T forKey) throws DecodingException {
        String v = decodeStringIfPresent(forKey).orElse(null);
        if (v == null) return Optional.empty();
        return Optional.ofNullable(
            v.equalsIgnoreCase("false")
                ? Boolean.FALSE
                : v.equalsIgnoreCase("true") ? Boolean.TRUE : null
        );
    }
    public Boolean decodeBoolean(T forKey) throws DecodingException, NoSuchElementException {
        return decodeBooleanIfPresent(forKey).orElseThrow();
    }

    public Optional<Integer> decodeIntegerIfPresent(T forKey) throws DecodingException {
        String v = decodeStringIfPresent(forKey).orElse(null);
        if (v == null) return Optional.empty();
        try {
            return Optional.of(Integer.valueOf(v));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    public Integer decodeInteger(T forKey) throws DecodingException, NoSuchElementException {
        return decodeIntegerIfPresent(forKey).orElseThrow();
    }

    public Optional<String> decodeStringIfPresent(T forKey) throws DecodingException {
        return Optional.ofNullable(prefsNode.get(forKey.name(), null));
    }
    public String decodeString(T forKey) throws DecodingException, NoSuchElementException {
        return decodeStringIfPresent(forKey).orElseThrow();
    }

    // Decoder conformance
    public KeyedDecodingContainer<T> container() {
        return this;
    }
}
