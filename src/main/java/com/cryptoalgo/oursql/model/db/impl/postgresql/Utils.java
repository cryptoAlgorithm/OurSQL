package com.cryptoalgo.oursql.model.db.impl.postgresql;

import com.cryptoalgo.oursql.model.db.DBMSUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utils specific to the PostgreSQL DBMS.
 */
public class Utils implements DBMSUtils {
    public @NotNull String scheme() { return "postgresql"; }

    public @NotNull Integer defaultPort() { return 5432; }

    public @NotNull String name() { return "PostgreSQL"; }

    public @Nullable String iconRes() { return "postgresql.png"; }

    public @Nullable String defaultDB() { return "postgres"; }
}
