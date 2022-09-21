package com.cryptoalgo.oursql.dal.unified;

import com.cryptoalgo.oursql.model.Context;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.prefs.Preferences;

public abstract class Cluster implements Serializable {
    protected String path;
    protected int port;

    public abstract String getConnectionURI();

    public Cluster(String path, int port) {
        this.path = path;
        this.port = port;
    }

    /**
     * Returns an ID unique to this database cluster. Not guaranteed
     * to be unique across clusters with the same URL.
     * @return A unique hash of the cluster's connection URL.
     */
    String getID() {
        return Arrays.toString(
            Context
                .getInstance()
                .hashInstance
                .digest(getConnectionURI().getBytes())
        );
    }

    /**
     * Persists this cluster to non-volatile storage.
     */
    void persist() {
        Preferences clusterStorage = Preferences
            .userRoot()
            .node(Context.class.getPackageName() + "/" + getID());
        clusterStorage.put("path", path);
        clusterStorage.putInt("port", port);
    }
}
