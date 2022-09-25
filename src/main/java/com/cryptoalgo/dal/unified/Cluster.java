package com.cryptoalgo.dal.unified;

import com.cryptoalgo.codable.*;
import com.cryptoalgo.codable.preferencesCoder.PreferencesDecoder;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.oursql.model.Context;

import java.util.Arrays;
import java.util.NoSuchElementException;

public abstract class Cluster extends Codable<Cluster.CodingKeys> {
    protected final String path, id;
    protected String name;
    protected final int port;
    public abstract String getConnectionURI();

    public Cluster(Decoder<CodingKeys> decoder) throws DecodingException, NoSuchElementException {
        super(decoder);
        KeyedDecodingContainer<CodingKeys> container = decoder.container();
        port = container.decodeInteger(CodingKeys.port);
        path = container.decodeString(CodingKeys.path);
        name = container.decodeString(CodingKeys.name);
        id = computeID(path);
    }

    public Cluster(String path, int port, String name) {
        super();
        this.path = path;
        this.port = port;
        this.name = name;
        id = computeID(path); // Better to calculate value once during initialization
    }

    private static String computeID(String connURI) {
        return Arrays.toString(
            Context
                .getInstance()
                .hashInstance
                .digest(connURI.getBytes())
        );
    }

    /**
     * Get an ID unique to this database cluster. Not guaranteed
     * to be unique across clusters with the same URL.
     * @return A unique hash of the cluster's connection URL
     */
    public String getID() { return id; }

    /**
     * Get the user-facing name of this cluster. This name can be
     * modified by the user after the creation of this Cluster.
     * @return The user-facing name of this cluster
     */
    public String getName() { return name; }

    // Codable conformance
    public enum CodingKeys {
        path, port, name
    }

    /**
     * Convenience method that wraps {@link Decoder}, allows
     * deserializing the cluster with the requested ID from {@link java.util.prefs.Preferences Preferences}
     */
    public static Cluster decode(String ofID) throws DecodingException, NoSuchElementException {
        return new PreferencesDecoder<>("clusters/" + ofID).decode(Cluster.class);
    }

    /**
     * Persist the current instance to non-volatile storage
     * with Java {@link java.util.prefs.Preferences Preferences}.
     * <p>
     *     <b>Note:</b>
     *     Uses {@link Encodable} under the hood to serialize and
     *     store the object in {@link java.util.prefs.Preferences Preferences}.
     * </p>
     */
    public void persist() throws EncodingException {
        new PreferencesEncoder<>("clusters/" + id).encode(this);
    }

    public void encode(Encoder<CodingKeys> encoder) throws EncodingException {
        KeyedEncodingContainer<CodingKeys> container = encoder.container();
        container.encode(path, CodingKeys.path);
        container.encode(port, CodingKeys.port);
    }
}
