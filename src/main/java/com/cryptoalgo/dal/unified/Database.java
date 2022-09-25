package com.cryptoalgo.dal.unified;

import javafx.beans.property.ReadOnlyBooleanProperty;

import java.util.List;

public abstract class Database {
    public ReadOnlyBooleanProperty isConnected;
    public Database(Cluster cluster) throws ConnectionException {
        connect(cluster.getConnectionURI());
    }

    abstract List<QueryResult> query(String query);
    abstract void connect(String url) throws ConnectionException;
    abstract void disconnect();
}
