package com.cryptoalgo.codable;

/**
 * An interface that will be implemented by a
 * encoder to allow encoding {@link Encodable} classes.
 * @see Decoder
 * @param <T> Enum of keys to key data with
 */
public interface Encoder<T extends Enum<T>> {
    /**
     * Retrieve an instance of a {@link KeyedEncodingContainer} to
     * be used to encode keyed data
     * @return An instance of a {@link KeyedEncodingContainer}
     */
    KeyedEncodingContainer<T> container();
}