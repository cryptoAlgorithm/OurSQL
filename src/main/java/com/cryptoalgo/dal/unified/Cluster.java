package com.cryptoalgo.dal.unified;

import com.cryptoalgo.codable.*;
import com.cryptoalgo.oursql.model.Context;

import java.util.Arrays;

public abstract class Cluster extends Codable {
    protected String path;
    protected int port;

    public abstract String getConnectionURI();

    public Cluster(Decoder decoder) throws DecodingException {
        super(decoder);
        KeyedDecodingContainer<CodingKeys> container = decoder.container();
        port = container.decodeInteger(CodingKeys.port).orElse(0);
        path = container.decodeString(CodingKeys.path).orElse("");
    }

    public Cluster(String path, int port) {
        super();
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

    private enum CodingKeys {
        path, port
    }

    public void encode(Encoder encoder) throws EncodingException {
        KeyedEncodingContainer<CodingKeys> container = encoder.container();
        container.encode(path, CodingKeys.path);
        container.encode(port, CodingKeys.port);
    }
}
