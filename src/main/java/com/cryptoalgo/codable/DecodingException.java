package com.cryptoalgo.codable;

/**
 * Exception that might be thrown by {@link Decoder} implementations
 * should decode of a requested key fail.
 */
public class DecodingException extends Throwable {
    /**
     * Constructs an instance of the exception, including
     * the codingKey which failed to decode and type requested.
     * @param codingKey key of data which failed to be deserialized
     * @param type Requested data type
     */
    public DecodingException(String codingKey, String type) {
        super("Exception decoding at keyPath " + codingKey + " of type " + type);
    }
}
