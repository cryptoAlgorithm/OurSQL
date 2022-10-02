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
        prefsNode = PreferencesEncoder.rootNode.node(node);
    }

    /**
     * Create an instance of a {@link com.cryptoalgo.codable.Decodable Decodable}
     * class by decoding values from Preferences
     */
    public <D extends Decodable<T>> D decode(Class<D> decoding) throws DecodingException, InvocationTargetException {
        try {
            return decoding.getDeclaredConstructor(Decoder.class).newInstance(this);
        } catch (
            NoSuchMethodException
            | SecurityException
            | InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
             e
        ) {
            e.printStackTrace();
            throw new DecodingException("/", "root");
        }
    }

    // KeyedDecodingContainer conformance
    public Optional<Boolean> decodeBooleanIfPresent(T forKey) {
        String v = decodeStringIfPresent(forKey).orElse(null);
        if (v == null) return Optional.empty();
        return Optional.ofNullable(
            v.equalsIgnoreCase("false")
                ? Boolean.FALSE
                : v.equalsIgnoreCase("true") ? Boolean.TRUE : null
        );
    }
    public Boolean decodeBoolean(T forKey) throws DecodingException, NoSuchElementException {
        try {
            return decodeBooleanIfPresent(forKey).orElseThrow();
        } catch (NoSuchElementException e) {
            throw new DecodingException(forKey.name(), "Boolean");
        }
    }

    public Optional<Integer> decodeIntegerIfPresent(T forKey) {
        String v = decodeStringIfPresent(forKey).orElse(null);
        if (v == null){
            System.out.println("port empty!");
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.valueOf(v));
        } catch (NumberFormatException e) {
            System.out.println(v);
            return Optional.empty();
        }
    }
    public Integer decodeInteger(T forKey) throws DecodingException, NoSuchElementException {
        try {
            return decodeIntegerIfPresent(forKey).orElseThrow();
        } catch (NoSuchElementException e) {
            throw new DecodingException(forKey.name(), "Integer");
        }
    }

    public Optional<String> decodeStringIfPresent(T forKey) {
        return Optional.ofNullable(prefsNode.get(forKey.name(), null));
    }
    public String decodeString(T forKey) throws DecodingException, NoSuchElementException {
        try {
            return decodeStringIfPresent(forKey).orElseThrow();
        } catch (NoSuchElementException e) {
            throw new DecodingException(forKey.name(), "String");
        }
    }

    // Decoder conformance
    public KeyedDecodingContainer<T> container() {
        return this;
    }
}
