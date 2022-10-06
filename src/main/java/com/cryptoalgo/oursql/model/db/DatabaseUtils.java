package com.cryptoalgo.oursql.model.db;

import com.cryptoalgo.oursql.model.db.impl.BuiltInDBs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Various utilities for working with JDBC databases.
 */
public class DatabaseUtils {
    /**
     * Generates a database connection URI given a {@link Cluster} and a password.
     * <p>
     *     <b>Implementation Note:</b>
     *     Calls {@link #getConnectionURI(Integer, String, String, DBMSUtils, boolean)}
     *     internally.
     * </p>
     * @param cluster Cluster to get connection URI from
     * @return A connection URI for the provided {@link Cluster}, not valid for
     *         connection if <code>password</code> is <code>null</code> and
     * @throws URISyntaxException If non-permitted characters were present in constructed URI
     * @see #getConnectionURI(Integer, String, String, DBMSUtils, boolean)
     */
    public static URI getConnectionURI(
        @NotNull Cluster cluster
    ) throws URISyntaxException {
        return getConnectionURI(
            cluster.getPort(),
            cluster.getHost(),
            cluster.getDatabase(),
            BuiltInDBs.lookupImpl(cluster.getDBMS()),
            true
        );
    }

    /**
     * Constructs a database connection URI from connection parameters and a password.
     * <p>
     *     <b>Tip:</b>
     *     If the password argument is <code>null</code>, a placeholder is used.
     *     Useful for display in uncensored UI.
     * </p>
     * @param port Database connection port
     * @param host Database host
     * @param path Database path (relative to <code>host</code>)
     * @param utils An instance of the database utils for the cluster's DBMS
     * @param includeJDBC If jdbc: should be appended before the scheme.
     *                    Set to false for user-facing UIs
     * @return A connection URI for the provided connection params, not valid for
     *         connection if <code>password</code> is <code>null</code> and
     *         {@link Cluster#getUsername()} is not null.
     * @throws URISyntaxException If construction of the connection URI failed due to
     *                            invalid characters/syntax
     */
    public static URI getConnectionURI(
        @NotNull Integer port,
        @NotNull String host,
        @NotNull String path,
        @NotNull DBMSUtils utils,
        boolean includeJDBC
    ) throws URISyntaxException {
        return new URI(
            (includeJDBC ? "jdbc:" : "") + utils.scheme(),
            null,
            host,
            port,
            // Combine host and database into a path without double slashes and similar issues
            // that naive string concatenation could cause
            Paths.get("/", path).toString(),
            null,
            null
        );
    }

    /**
     * Convenience method to get a JDBC connection from a cluster and password.
     * @param cluster Cluster to get connection of
     * @param password Connection password, if applicable
     * @return The database connection object
     * @throws URISyntaxException If construction of the connection URI failed due to
     *                            invalid characters/syntax
     * @throws SQLException If the underlying JDBC driver threw an exception while
     *                      a connection was attempted
     * @see #getConnection(URI connURI, Cluster cluster, String password)
     */
    public static Connection getConnection(
        @NotNull Cluster cluster,
        @Nullable String password
    ) throws URISyntaxException, SQLException {
        return getConnection(
            getConnectionURI(cluster),
            cluster,
            password
        );
    }

    /**
     * Get a database connection
     * @param connURI Connection URI
     * @param cluster Cluster to get connection of
     * @param password Connection password, if applicable
     * @return The database connection object
     * @throws SQLException If the underlying JDBC driver threw an exception while
     *                      a connection was attempted
     */
    public static Connection getConnection(
        @NotNull URI connURI,
        @NotNull Cluster cluster,
        @Nullable String password
    ) throws SQLException {
        return DriverManager.getConnection(
            connURI.toString(),
            cluster.getUsername(),
            password
        );
    }
}
