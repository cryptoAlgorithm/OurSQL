package com.cryptoalgo.db.impl;

import com.cryptoalgo.db.DBMSUtils;

/**
 * Metadata for built-in database implementations
 */
public class BuiltInDBs {
    static public DBMSUtils[] impls = {
        new com.cryptoalgo.db.impl.postgresql.Utils(),
        new com.cryptoalgo.db.impl.mysql.Utils()
    };
}
