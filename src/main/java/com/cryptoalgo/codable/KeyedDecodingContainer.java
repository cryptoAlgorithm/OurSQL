package com.cryptoalgo.codable;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A keyed decoding container for use by {@link Decodable}
 * classes during decoding
 * <p>
 *     {@link Decodable} classes can retrieve an instance of
 *     a keyed decoding container by calling {@link Decoder#container()}
 *     in their decoding constructor.
 * </p>
 * @see Decoder#container()
 * @param <T>
 */
public interface KeyedDecodingContainer<T extends Enum<T>> {
    // Java be insisting on "type erasure" to not break legacy code,
    // breaking new code do be better :)
    // If Java was properly designed from the get-go (like Swift), there wouldn't
    // be so many things to complain about

    /**
     * Decodes a single {@link Boolean} at a given codingKey.
     * @param forKey codingKey to attempt to decode value from
     * @return {@link Optional} decoded {@link Boolean}
     * @throws DecodingException If decoding fails for whatever reason
     */
    Optional<Boolean> decodeBooleanIfPresent(T forKey) throws DecodingException;
    /**
     * Decodes a single {@link Boolean} at a given codingKey.
     * @param forKey codingKey to attempt to decode value from
     * @return Decoded {@link Boolean}
     * @throws DecodingException If decoding fails for whatever reason
     * @throws NoSuchElementException If the value at the requested key isn't present
     */
    Boolean decodeBoolean(T forKey) throws DecodingException, NoSuchElementException;

    /**
     * Decodes a single {@link Integer} at a given codingKey.
     * @param forKey codingKey to attempt to decode value from
     * @return {@link Optional<Integer>} decoded {@link Integer}
     * @throws DecodingException If decoding fails for whatever reason
     */
    Optional<Integer> decodeIntegerIfPresent(T forKey) throws DecodingException, NoSuchElementException;
    /**
     * Decodes a single {@link Integer} at a given codingKey.
     * @param forKey codingKey to attempt to decode value from
     * @return Decoded {@link Integer}
     * @throws DecodingException If decoding fails for whatever reason
     * @throws NoSuchElementException If the value at the requested key isn't present
     */
    Integer decodeInteger(T forKey) throws DecodingException, NoSuchElementException;

    /**
     * Decodes a single {@link String} at a given codingKey.
     * @param forKey codingKey to attempt to decode value from
     * @return Decoded {@link String}
     * @throws DecodingException If decoding fails for whatever reason
     */
    Optional<String> decodeStringIfPresent(T forKey) throws DecodingException;
    /**
     * Decodes a single {@link String} at a given codingKey.
     * @param forKey codingKey to attempt to decode value from
     * @return {@link Optional<String>} decoded {@link String}
     * @throws DecodingException If decoding fails for whatever reason
     * @throws NoSuchElementException If the value at the requested key isn't present
     */
    String decodeString(T forKey) throws DecodingException, NoSuchElementException;
}
