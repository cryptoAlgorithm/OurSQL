package com.cryptoalgo.oursql.dal.mongoDB;

import com.cryptoalgo.oursql.dal.unified.Cluster;

import java.net.URI;

public class MongoCluster extends Cluster {
    String username, password;

    public MongoCluster(String path, int port) {
        super(path, port);
    }

    @Override
    public String getConnectionURI() {
        return null;
    }
}
