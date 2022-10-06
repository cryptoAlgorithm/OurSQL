/**
 * Main module of the OurSQL application.
 */
module com.cryptoalgo.oursql.oursql {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires java.logging;
    requires java.sql;
    requires org.jetbrains.annotations;
    requires java.desktop;

    opens com.cryptoalgo.oursql to javafx.fxml;
    // Literally export everything to appease JavaDoc
    exports com.cryptoalgo.codable;
    exports com.cryptoalgo.codable.preferencesCoder;
    exports com.cryptoalgo.oursql;
    exports com.cryptoalgo.oursql.component;
    exports com.cryptoalgo.oursql.controller;
    exports com.cryptoalgo.oursql.model;
    exports com.cryptoalgo.oursql.model.db;
    exports com.cryptoalgo.oursql.model.db.data;
    exports com.cryptoalgo.oursql.model.db.impl;
    exports com.cryptoalgo.oursql.model.db.impl.mysql;
    exports com.cryptoalgo.oursql.model.db.impl.postgresql;
    exports com.cryptoalgo.oursql.support;
    exports com.cryptoalgo.oursql.support.ui;
    opens com.cryptoalgo.oursql.controller to javafx.fxml;
    opens com.cryptoalgo.oursql.component to javafx.fxml;
}