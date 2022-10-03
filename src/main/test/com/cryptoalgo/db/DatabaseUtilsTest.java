package com.cryptoalgo.db;

import org.junit.jupiter.api.Test;

class DatabaseUtilsTest {
    @Test
    void testPostgreSQL() throws Exception {
        DatabaseUtils
            .getConnection(
                new Cluster(
                    "postgresql",
                    "postgres",
                    "postgres",
                    null,
                    5432,
                    ""
                ),
                null
            );
    }
}