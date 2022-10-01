package com.cryptoalgo.db;

import org.junit.jupiter.api.Test;

class DatabaseUtilsTest {
    @Test
    void testPostgreSQL() throws Exception {
        DatabaseUtils
            .db(new com.cryptoalgo.db.impl.postgresql.Utils())
            .getConnection(
                new Cluster(
                    "localhost",
                    "postgres",
                    null,
                    5432,
                    ""
                ),
                null
            );
    }
}