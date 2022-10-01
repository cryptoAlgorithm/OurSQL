package com.cryptoalgo.db.impl.mysql;

import com.cryptoalgo.db.DBMSUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils implements DBMSUtils {
    public @NotNull String scheme() { return "mysql"; }

    public @NotNull Integer defaultPort() { return 3306; }

    public @NotNull String name() { return "MySQL"; }

    public @Nullable String iconRes() { return "mysql.png"; }
}
