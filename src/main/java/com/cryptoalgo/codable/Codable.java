package com.cryptoalgo.codable;

/**
 * Utility interface for implementing both
 * encodable and decodable interfaces
 */
public abstract class Codable extends Decodable implements Encodable {
    protected Codable(Decoder decoder) throws DecodingException {
        super(decoder);
    }
    protected Codable() {}
}
