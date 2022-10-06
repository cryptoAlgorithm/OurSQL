package com.cryptoalgo.codable;

import org.jetbrains.annotations.Nullable;

/**
 * A container for encoding keyed values.
 * @param <T> Type of CodingKeys used for encoding
 */
public interface KeyedEncodingContainer<T extends Enum<T>> {
    // Look mom, I'm using pOlYmOrPhIsM!

    /**
     * Encode an {@link Integer} value into a specified key
     * @param value Value to encode
     * @param forKey Key to encode value into
     * @throws EncodingException If encoding of this key failed
     */
    void encode(@Nullable Integer value, T forKey) throws EncodingException;

    /**
     * Encode a {@link Boolean} value into a specified key
     * @param value Value to encode
     * @param forKey Key to encode value into
     * @throws EncodingException If encoding of this key failed
     */
    void encode(@Nullable Boolean value, T forKey) throws EncodingException;

    /**
     * Encode a {@link String} value into a specified key
     * @param value Value to encode
     * @param forKey Key to encode value into
     * @throws EncodingException If encoding of this key failed
     */
    void encode(@Nullable String value, T forKey) throws EncodingException;
}