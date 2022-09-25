package com.cryptoalgo.codable;

/**
 * Exception that might be thrown by {@link Encoder} implementations
 * should encode of a requested key fail.
 */
public class EncodingException extends Exception {
    /**
     * Constructs an instance of the exception, including
     * the codingKey which failed to encode to aid in debugging.
     * @param codingKey key of data which failed to be serialized
     */
    public EncodingException(String codingKey) {
        super("Exception encoding at keyPath " + codingKey);
    }
}
