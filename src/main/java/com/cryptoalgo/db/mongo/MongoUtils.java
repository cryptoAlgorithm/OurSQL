package com.cryptoalgo.db.mongo;

import com.cryptoalgo.db.SpecificDBUtils;

public class MongoUtils implements SpecificDBUtils {
    @Override
    public String scheme() {
        return "mongodb";
    }

    @Override
    public Integer defaultPort() {
        return 27017;
    }
}
