package com.cryptoalgo.codable;

import java.util.NoSuchElementException;

/**
 * Convenience abstract class that can be inherited
 */
public abstract class Codable extends Decodable implements Encodable {
    protected Codable(Decoder decoder) throws DecodingException, NoSuchElementException {
        super(decoder);
    }
    protected Codable() {}
}
