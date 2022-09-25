package com.cryptoalgo.codable;

/**
 * Abstract class that classes that would like to be
 * deserialized can extend.<br><br>
 * <b>Note:</b><br>
 * For reasons best known to Java's genius developers,
 * interfaces cannot specify mandatory constructors. It
 * is possible in an actual compiled language known as
 * Swift, so why not Java?
 * @param <T>
 */
public abstract class Decodable<T extends Enum<T>> {
    protected Decodable(Decoder<T> decoder) throws DecodingException {}

    protected Decodable() {}
}
