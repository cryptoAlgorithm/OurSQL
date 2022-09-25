package com.cryptoalgo.codable;

/**
 * Abstract class which allows classes that inherit it to be
 * deserialized by {@link Decoder}s
 * <p>
 *     <b>Note:</b>
 *     For reasons best known to Java's genius developers,
 *     interfaces cannot specify mandatory constructors. It
 *     is possible in an actual compiled language known as
 *     Swift, so why not Java?
 * </p>
 * @param <T> Type of enum of codingKeys to be used during decoding
 * @see Encodable
 */
public abstract class Decodable<T extends Enum<T>> {
    /**
     * Decoding constructor that must be implemented by subclasses to
     * allow decoding of data to populate fields.
     * @param decoder Instance of a decoder to be used for decoding
     * @throws DecodingException If decoding failed for whatever reason
     */
    protected Decodable(Decoder<T> decoder) {}

    /**
     * Default no-arg constructor
     */
    private Decodable() {}
}
