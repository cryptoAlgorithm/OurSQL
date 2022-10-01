package com.cryptoalgo.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {
    SpecificDBUtils utils;

    public DatabaseUtils(SpecificDBUtils utils) {
        this.utils = utils;
    }

    /**
     * Convenience method to get an instance of {@link DatabaseUtils}.
     * It's recommended to use this method instead of calling the
     * constructor as further optimisation might be added in the
     * future to cache instances.
     * @implNote Currently equivalent to simply calling
     *           {@link DatabaseUtils#DatabaseUtils(SpecificDBUtils)},
     *           might change in the future.
     * @return An instance of {@link DatabaseUtils}
     */
    public static DatabaseUtils db(SpecificDBUtils utils) {
        return new DatabaseUtils(utils);
    }

    /**
     * Generates a database connection URI given a {@link Cluster} and a password.
     * Calls {@link #getConnectionURI(Integer port, String host, String path, boolean includeJDBC)}
     * internally.
     * @param cluster Cluster to get connection URI from
     * @return A connection URI for the provided {@link Cluster}, not valid for
     *         connection if <code>password</code> is <code>null</code> and
     * @throws URISyntaxException If non-permitted characters were present in constructed URI
     * @see #getConnectionURI(Integer port, String host, String path, boolean includeJDBC)
     */
    public URI getConnectionURI(
        @NotNull Cluster cluster
    ) throws URISyntaxException {
        return getConnectionURI(
            cluster.getPort(),
            cluster.getHost(),
            cluster.getDatabase(),
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
     * @return A connection URI for the provided connection params, not valid for
     * connection if <code>password</code> is <code>null</code> and
     * {@link Cluster#getUsername()} is not null.
     */
    public URI getConnectionURI(
        @NotNull Integer port,
        @NotNull String host,
        @NotNull String path,
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

    public Connection getConnection(
        @NotNull Cluster cluster,
        @Nullable String password
    ) throws URISyntaxException, SQLException {
        DriverManager.getConnection(
            getConnectionURI(cluster).toString(),
            cluster.getUsername(),
            password
        );
        return null;
    }
}
