package com.cryptoalgo.codable;
/**
 * An encoder to be called during encoding
 */
public interface Encoder<T extends Enum<T>> {
    KeyedEncodingContainer<T> container();
}