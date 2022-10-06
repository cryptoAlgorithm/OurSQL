package com.cryptoalgo.codable;

/**
 * Interface which allows classes that implement it to be serialised
 * by {@link Encoder}s
 * @param <T> Type of enum of codingKeys to be used during encoding
 * @see Decodable
 */
public interface Encodable<T extends Enum<T>> {
    /**
     * Encodes the fields in this class with an encoder.
     * @param encoder Encoder to be used for encoding
     * @throws EncodingException If the encoder threw an exception
     */
    void encode(Encoder<T> encoder) throws EncodingException;
}
