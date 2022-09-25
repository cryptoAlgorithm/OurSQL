package com.cryptoalgo.codable;

import org.jetbrains.annotations.NotNull;

public interface KeyedEncodingContainer<T extends Enum<T>> {
    // Look mom, I'm using pOlYmOrPhIsM!
    void encode(@NotNull Integer value, T forKey) throws EncodingException;
    void encode(@NotNull Boolean value, T forKey) throws EncodingException;
    void encode(@NotNull String value, T forKey) throws EncodingException;
}