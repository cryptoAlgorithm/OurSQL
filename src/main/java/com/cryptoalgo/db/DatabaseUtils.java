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
     * Constructs a database connection URI given a {@link Cluster} and a password.
     * <p>
     *     <b>Tip:</b>
     *     If the password argument is <code>null</code>, a placeholder is used.
     *     Useful for display in uncensored UI.
     * </p>
     * @param cluster Cluster to get connection URI from
     * @param password Connection password, falls back to [password] if null
     *                 and username is not null (e.g. for displaying in the UI)
     * @return A connection URI for the provided {@link Cluster}, not valid for
     * connection if <code>password</code> is <code>null</code> and
     * {@link Cluster#getUsername()} is not null.
     */
    public URI getConnectionURI(@NotNull Cluster cluster, @Nullable String password) throws URISyntaxException {
        return new URI(
            "jdbc:" + utils.getScheme(),
            null,
            cluster.getUsername() == null
                ? "" // Cannot be null, else // after scheme will be missing
                : cluster.getUsername() + ":" + (password == null ? "[password]" : password),
            cluster.getPort(),
            // Combine host and database into a path without double slashes and similar issues
            // that naive string concatenation could cause
            Paths.get(cluster.getHost(), cluster.getDatabase()).toString(),
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
