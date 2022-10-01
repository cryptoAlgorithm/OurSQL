package com.cryptoalgo.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities specific to a DBMS.
 */
public interface DBMSUtils {
    /**
     * @return Connection scheme
     */
    @NotNull String scheme();

    /**
     * @return Default connection port
     */
    @NotNull Integer defaultPort();

    /**
     * @return Human-readable name for this DBMS
     */
    @NotNull String name();

    /**
     * @return Database icon name, relative to <code>img/db</code>
     */
    @Nullable String iconRes();

    /**
     * @return Default database
     */
    @Nullable default String defaultDB() { return null; }
}
