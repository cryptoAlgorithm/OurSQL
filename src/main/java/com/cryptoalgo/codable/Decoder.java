package com.cryptoalgo.codable;

/**
 * Decoder interface that will be implemented by a
 * decoder to allow decoding {@link Decodable} classes.
 * @see Encoder
 * @param <T> Enum of keys to key data with
 */
public interface Decoder<T extends Enum<T>> {
    KeyedDecodingContainer<T> container();
}
