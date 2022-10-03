package com.cryptoalgo.db;

import com.cryptoalgo.codable.*;
import com.cryptoalgo.codable.preferencesCoder.PreferencesDecoder;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.oursql.model.Context;
import com.cryptoalgo.oursql.support.HexEncoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.prefs.BackingStoreException;
import java.util.regex.Pattern;

public final class Cluster extends Codable<Cluster.CodingKeys> {
    @NotNull
    private final String dbms, host, database, id;
    @Nullable
    private final String username;
    @NotNull
    private String name;
    @NotNull
    private final Integer port;

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
    private static String computeID(String host, String path, int port) {
        return HexEncoder.bytesToHex(
            Context
                .getInstance()
                .hashInstance
                .digest((host + path + port).getBytes())
        );
    }

    public Cluster(Decoder<CodingKeys> decoder) throws DecodingException, NoSuchElementException {
        super(decoder);
        KeyedDecodingContainer<CodingKeys> container = decoder.container();
        port = container.decodeInteger(CodingKeys.port);
        host = purifyHost(container.decodeString(CodingKeys.host));
        name = container.decodeString(CodingKeys.name);
        dbms = container.decodeString(CodingKeys.dbms);
        database = container.decodeString(CodingKeys.database);
        username = container.decodeStringIfPresent(CodingKeys.username).orElse(null);
        id = computeID(host, database, port);
    }

    public Cluster(
        @NotNull String dbms,
        @NotNull String host,
        @NotNull String database,
        @Nullable String username,
        @NotNull Integer port,
        @NotNull String name
    ) {
        super();
        this.dbms = dbms;
        this.username = username;
        this.database = database;
        this.host = purifyHost(host);
        this.port = port;
        this.name = name;
        id = computeID(host, database, port); // Better to calculate value once during initialization
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

    /**
     * @return A string identifying the DBMS of this cluster
     */
    public @NotNull String getDBMS() { return dbms; }

    /**
     * Update the name of the current cluster. Call {@link #persist()}
     * to persist the updated cluster info
     * @param newName New name to update cluster with
     */
    public void setName(@NotNull String newName) { name = newName; }

    // Codable conformance
    public enum CodingKeys {
        host, database, port, name, username, dbms
    }

    /**
     * Convenience method that wraps {@link Decoder}, allows
     * deserializing the cluster with the requested ID from
     * {@link java.util.prefs.Preferences Preferences}
     */
    public static Cluster decode(String ofID) throws DecodingException, InvocationTargetException {
        return new PreferencesDecoder<CodingKeys>("clusters/" + ofID).decode(Cluster.class);
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
        new PreferencesEncoder<CodingKeys>("clusters/" + id).encode(this);
    }

    /**
     * Removes a cluster from non-volatile storage.
     */
    public void remove() throws BackingStoreException {
        PreferencesEncoder.rootNode.node("clusters/" + id).removeNode();
    }

    /**
     * Check if a cluster with the same ID was already persisted
     * @return <code>true</code> if the node with this cluster's ID already exists
     * @implNote Does not validate if persisted cluster is actually valid
     */
    public boolean alreadyExists() {
        try {
            return PreferencesEncoder.rootNode.nodeExists("clusters/" + id);
        } catch (Exception e) { return false; }
    }

    public void encode(Encoder<CodingKeys> encoder) throws EncodingException {
        KeyedEncodingContainer<CodingKeys> container = encoder.container();
        container.encode(host, CodingKeys.host);
        container.encode(port, CodingKeys.port);
        container.encode(name, CodingKeys.name);
        container.encode(database, CodingKeys.database);
        container.encode(username, CodingKeys.username);
        container.encode(dbms, CodingKeys.dbms);
    }

    @Override
    public boolean equals(Object c) {
        return c instanceof Cluster ? ((Cluster) c).id.equals(id) : c.equals(this);
    }
}
