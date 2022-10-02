package com.cryptoalgo.oursql.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.HashMap;

/**
 * Over-engineered database connection, loading and updating class
 */
public class DBManager {
    public static final ObservableMap<String, String> tables =
        FXCollections.observableMap(new HashMap<>());

}
