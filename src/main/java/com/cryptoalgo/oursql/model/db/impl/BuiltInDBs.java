package com.cryptoalgo.oursql.model.db.impl;

import com.cryptoalgo.oursql.model.db.DBMSUtils;
import com.cryptoalgo.oursql.model.db.impl.postgresql.Utils;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Metadata for built-in database implementations
 */
public class BuiltInDBs {
    /**
     * Array of instances of built-in DBMS utils.
     */
    static public final DBMSUtils[] impls = {
        new Utils(),
        new com.cryptoalgo.oursql.model.db.impl.mysql.Utils()
    };

    /**
     * IDs used in cluster storage that correspond to the required DBMS
     */
    static private final String[] dbmsIDs = {
        "postgresql",
        "mysql"
    };

    /**
     * Get an instance of implementations of {@link DBMSUtils} for a certain DBMS.
     * @param id DBMS ID to lookup
     * @return Instance of {@link DBMSUtils} specific to the requested DBMS
     * @throws NoSuchElementException If an implementation for the DBMS of the provided ID
     *                                was not found
     */
    static public DBMSUtils lookupImpl(String id) throws NoSuchElementException {
        // To serve as a reminder
        // assert impls.length == dbmsIDs.length;
        int idx = List.of(dbmsIDs).indexOf(id);
        if (idx < 0) throw new NoSuchElementException("DBMS with ID " + id + " not found");
        return impls[idx];
    }
}
