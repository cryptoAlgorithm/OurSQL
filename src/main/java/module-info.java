module com.cryptoalgo.oursql.oursql {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires java.logging;
    requires java.sql;
    requires org.jetbrains.annotations;
    requires java.desktop;

    opens com.cryptoalgo.oursql to javafx.fxml;
    exports com.cryptoalgo.oursql;
    exports com.cryptoalgo.oursql.controller;
    opens com.cryptoalgo.oursql.controller to javafx.fxml;
}