package com.cryptoalgo.db;

import com.cryptoalgo.db.impl.BuiltInDBs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {
    /**
     * Generates a database connection URI given a {@link Cluster} and a password.
     * @implNote Calls {@link #getConnectionURI(Integer, String, String, DBMSUtils, boolean)}
     * internally.
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
     * connection if <code>password</code> is <code>null</code> and
     * {@link Cluster#getUsername()} is not null.
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
     * @return The database connection object
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
