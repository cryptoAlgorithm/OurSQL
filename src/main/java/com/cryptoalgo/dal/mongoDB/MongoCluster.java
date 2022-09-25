package com.cryptoalgo.dal.mongoDB;

import com.cryptoalgo.dal.unified.Cluster;

public class MongoCluster extends Cluster {
    String username, password;

    public MongoCluster(String path, int port, String name) {
        super(path, port, name);
    }

    @Override
    public String getConnectionURI() {
        return null;
    }
}
