package com.cryptoalgo.codable;

public class EncodingException extends Exception {
    public EncodingException(String codingKey) {
        super("Exception encoding at keyPath " + codingKey);
    }
}
