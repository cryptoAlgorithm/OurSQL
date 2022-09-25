package com.cryptoalgo.codable;

import java.util.Optional;

/**
 * A keyed decoding container for use by {@link Decodable}
 * classes during decoding
 * <p>
 *     {@link Decodable} classes can retrieve an instance of
 *     a keyed decoding container by calling {@link Decoder#container()}
 *     in their decoding constructor.
 * </p>
 * @param <T>
 */
public interface KeyedDecodingContainer<T extends Enum<T>> {
    // Java be insisting on "type erasure" to not break legacy code,
    // breaking new code do be better :)
    // If Java was properly designed from the get-go (like Swift), there wouldn't
    // be so many things to complain about

    /**
     * Decodes a single {@link Boolean} given a codingKey
     * @param forKey codingKey to attempt to decode value from
     * @return Optional decoded  {@link Boolean}
     * @throws DecodingException If decoding fails for whatever reason
     */
    Optional<Boolean> decodeBoolean(T forKey) throws DecodingException;

    /**
     * Decodes a single {@link Integer} given a codingKey
     * @param forKey codingKey to attempt to decode value from
     * @return Optional decoded {@link Integer}
     * @throws DecodingException If decoding fails for whatever reason
     */
    Optional<Integer> decodeInteger(T forKey) throws DecodingException;

    /**
     * Decodes a single {@link Integer} given a codingKey
     * @param forKey codingKey to attempt to decode value from
     * @return Optional decoded {@link Integer}
     * @throws DecodingException If decoding fails for whatever reason
     */
    Optional<String> decodeString(T forKey) throws DecodingException;
}
