package com.cryptoalgo.db;

import com.cryptoalgo.codable.*;
import com.cryptoalgo.codable.preferencesCoder.PreferencesDecoder;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.oursql.model.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public final class Cluster extends Codable<Cluster.CodingKeys> {
    private final String host, database, username, id;
    private String name;
    private final int port;

    /**
     * Ensure host isn't invalid to prevent causing problems down the line
     * @param host Raw host
     * @return Purified host
     */
    private static String purifyHost(String host) {
        return Pattern.compile("/+$")
            .matcher(host)
            .replaceAll("");
    }

    /**
     * Generate a unique ID for this cluster. Used internally as an
     * identifier for cluster storage and lookup.
     * @return A unique ID for this cluster
     */
    private static String computeID(String host, String path, String user, int port) {
        return new String(Base64.getEncoder().encode(
            Context
                .getInstance()
                .hashInstance
                .digest((host + path + user + port).getBytes())
        ));
    }

    public Cluster(Decoder<CodingKeys> decoder) throws DecodingException, NoSuchElementException {
        super(decoder);
        KeyedDecodingContainer<CodingKeys> container = decoder.container();
        port = container.decodeInteger(CodingKeys.port);
        host = purifyHost(container.decodeString(CodingKeys.host));
        name = container.decodeString(CodingKeys.name);
        database = container.decodeString(CodingKeys.database);
        username = container.decodeString(CodingKeys.username);
        id = computeID(host, database, username, port);
    }

    public Cluster(
        @NotNull String host,
        @NotNull String database,
        @Nullable String username,
        @NotNull Integer port,
        @NotNull String name
    ) {
        super();
        this.username = username;
        this.database = database;
        this.host = purifyHost(host);
        this.port = port;
        this.name = name;
        id = computeID(host, database, username, port); // Better to calculate value once during initialization
    }

    /**
     * @return Database connection port
     */
    public @NotNull Integer getPort() { return port; }

    /**
     * @return Database host
     */
    public @NotNull String getHost() { return host; }

    /**
     * @return The database path
     */
    public @NotNull String getDatabase() { return database; }

    /**
     * Get an ID unique to this database cluster. Not guaranteed
     * to be unique across clusters with the same URL.
     * @return A unique hash of the cluster's connection URL
     */
    public @NotNull String getID() { return id; }

    /**
     * Get the user-facing name of this cluster. This name can be
     * modified by the user after the creation of this Cluster.
     * @return The user-facing name of this cluster
     */
    public @NotNull String getName() { return name; }

    /**
     * @return Username for authenticating with database
     */
    public @Nullable String getUsername() { return username; }

    // Codable conformance
    public enum CodingKeys {
        host, database, port, name, username
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
        container.encode(host, CodingKeys.host);
        container.encode(port, CodingKeys.port);
    }
}
