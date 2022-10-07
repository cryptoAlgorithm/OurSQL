package com.cryptoalgo.db;

import com.cryptoalgo.oursql.model.db.Cluster;
import com.cryptoalgo.oursql.model.db.DatabaseUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseUtilsTest {
    @Ignore("Only works when localhost instance of PostgreSQL is running")
    @Test
    void testPostgreSQL() {
        assertDoesNotThrow(() -> DatabaseUtils
            .getConnection(
                new Cluster(
                    "postgresql",
                    "localhost",
                    "postgres",
                    null,
                    5432,
                    ""
                ),
                null
            )
        );
    }

    @Test
    void testGetConnectionURI() throws Exception {
        // PostgreSQL
        assertEquals(
            "jdbc:postgresql://localhost:5432/postgres",
            DatabaseUtils.getConnectionURI(
                new Cluster(
                    "postgresql",
                    "localhost",
                    "postgres",
                    null,
                    5432,
                    ""
                )
            ).toString()
        );
        // MySQL
        assertEquals(
            "jdbc:mysql://localhost:3306/",
            DatabaseUtils.getConnectionURI(
                new Cluster(
                    "mysql",
                    "localhost",
                    "",
                    null,
                    3306,
                    ""
                )
            ).toString()
        );
    }
}