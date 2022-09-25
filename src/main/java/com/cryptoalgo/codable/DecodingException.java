package com.cryptoalgo.codable;

/**
 * Exception that might be thrown by {@link Decoder} implementations
 * should decode of a requested key fail.
 *
 */
public class DecodingException extends Throwable {
    public DecodingException(String codingKey, String type) {
        super("Exception decoding at keyPath " + codingKey + " of type " + type);
    }
}
