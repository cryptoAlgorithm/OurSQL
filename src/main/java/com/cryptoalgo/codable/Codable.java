package com.cryptoalgo.codable;

/**
 * Convenience abstract class that can be inherited by classes
 * which conform to both {@link Encodable} and {@link Decodable}
 * <p>
 *     <b>Important:</b>
 *     Decoding constructor must be implemented for decoding
 *     to populate fields, see {@link Decodable#Decodable(Decoder)}
 * </p>
 */
public abstract class Codable<T extends Enum<T>> extends Decodable<T> implements Encodable<T> {
    /**
     * Decoding constructor, may be called via reflection by {@link Decoder}s.
     * @param decoder Instance of {@link Decoder} to be used for decoding
     * @throws DecodingException If decoding fails for whatever reason
     */
    protected Codable(Decoder<T> decoder) throws DecodingException {
        super(decoder);
    }

    /**
     * Protected no-arg constructor to force inheriting classes to implement their
     * own constructor.
     */
    protected Codable() {}
}
