package com.cryptoalgo.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

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
     * Calls {@link #getConnectionURI(String user, String password, Integer port, String host, String path)}
     * internally.
     * @param cluster Cluster to get connection URI from
     * @param password Connection password, falls back to [password] if null
     *                 and username is not null (e.g. for displaying in the UI)
     * @return A connection URI for the provided {@link Cluster}, not valid for
     *         connection if <code>password</code> is <code>null</code> and
     * @throws URISyntaxException If non-permitted characters were present in constructed URI
     * @see #getConnectionURI(String user, String password, Integer port, String host, String path)
     */
    public URI getConnectionURI(
        @NotNull Cluster cluster,
        @Nullable String password
    ) throws URISyntaxException {
        return getConnectionURI(
            cluster.getUsername(),
            password,
            cluster.getPort(),
            cluster.getHost(),
            cluster.getDatabase()
        );
    }

    /**
     * Constructs a database connection URI from connection parameters and a password.
     * <p>
     *     <b>Tip:</b>
     *     If the password argument is <code>null</code>, a placeholder is used.
     *     Useful for display in uncensored UI.
     * </p>
     * @param user User to authenticate with (null if no authentication)
     * @param password Connection password, falls back to [password] if null
     *                 and username is not null (e.g. for displaying in the UI)
     * @param port Database connection port
     * @param host Database host
     * @param path Database path (relative to <code>host</code>)
     * @return A connection URI for the provided connection params, not valid for
     * connection if <code>password</code> is <code>null</code> and
     * {@link Cluster#getUsername()} is not null.
     */
    public URI getConnectionURI(
        @Nullable String user,
        @Nullable String password,
        @NotNull Integer port,
        @NotNull String host,
        @NotNull String path
    ) throws URISyntaxException {
        return new URI(
            "jdbc:" + utils.scheme(),
            user == null
                ? "" // Cannot be null, else // after scheme will be missing
                : user + ":" + (password == null ? "<password>" : password),
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
        @NotNull String password
    ) throws URISyntaxException, SQLException {
        Properties connProps = new Properties();
        connProps.put("username", cluster);
        DriverManager.getConnection(getConnectionURI(cluster, password).toString(), connProps);
        return null;
    }
}
