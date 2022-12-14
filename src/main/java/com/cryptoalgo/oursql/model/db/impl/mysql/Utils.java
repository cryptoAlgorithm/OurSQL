package com.cryptoalgo.oursql.model.db.impl.mysql;

import com.cryptoalgo.oursql.model.db.DBMSUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utils specific to the MySQL DBMS.
 */
public class Utils implements DBMSUtils {
    public @NotNull String scheme() { return "mysql"; }

    public @NotNull Integer defaultPort() { return 3306; }

    public @NotNull String name() { return "MySQL"; }

    public @Nullable String iconRes() { return "mysql.png"; }
}
