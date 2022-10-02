package com.cryptoalgo.codable;

import org.jetbrains.annotations.Nullable;

public interface KeyedEncodingContainer<T extends Enum<T>> {
    // Look mom, I'm using pOlYmOrPhIsM!
    void encode(@Nullable Integer value, T forKey) throws EncodingException;
    void encode(@Nullable Boolean value, T forKey) throws EncodingException;
    void encode(@Nullable String value, T forKey) throws EncodingException;
}