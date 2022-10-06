package com.cryptoalgo.db;

import com.cryptoalgo.oursql.model.db.Cluster;
import com.cryptoalgo.oursql.model.db.DatabaseUtils;
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

    @Test
    void numberTests() {
        System.out.println(Integer.parseInt("23234"));
    }
}