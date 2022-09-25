package com.cryptoalgo.codable;

/**
 * Convenience abstract class that can be inherited
 * <p>
 *     <b>Important:</b>
 *     Decoding constructor must be implemented for decoding
 *     to populate fields, see {@link Decodable#Decodable(Decoder)}
 * </p>
 */
public abstract class Codable<T extends Enum<T>> extends Decodable<T> implements Encodable<T> {
    protected Codable(Decoder<T> decoder) {
        super(decoder);
    }
}
