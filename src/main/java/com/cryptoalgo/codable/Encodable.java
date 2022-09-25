package com.cryptoalgo.codable;

public interface Encodable<T extends Enum<T>> {
    void encode(Encoder<T> encoder) throws EncodingException;
}
