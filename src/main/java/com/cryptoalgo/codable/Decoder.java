package com.cryptoalgo.codable;

/**
 * An interface that will be implemented by a
 * decoder to allow decoding {@link Decodable} classes.
 * @see Encoder
 * @param <T> Enum of keys to key data with
 */
public interface Decoder<T extends Enum<T>> {
    /**
     * Retrieve an instance of a {@link KeyedDecodingContainer} to
     * decode keyed data
     * @return An instance of a {@link KeyedDecodingContainer}
     */
    KeyedDecodingContainer<T> container();
}
