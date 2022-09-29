package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.db.Cluster;
import com.cryptoalgo.db.DatabaseUtils;
import com.cryptoalgo.db.mongo.MongoUtils;
import com.cryptoalgo.oursql.OurSQL;
import com.cryptoalgo.oursql.support.I18N;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class AddClusterDialog {
    @FXML
    private ComboBox<String> authType; // Using private visibility and FXML modifier - best practices
    @FXML
    private TextField name, host, port, authUser, authPW, path, uri;

    private static Logger log = Logger.getLogger(AddClusterDialog.class.getName());

    public static void show() throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(I18N.getString("action.addCluster"));
        stage.setScene(new Scene(FXMLLoader.load(
            Objects.requireNonNull(OurSQL.class.getResource("view/add-cluster.fxml")),
            ResourceBundle.getBundle("locales/strings", new Locale("en"))
        )));
        stage.show();
    }

    /**
     * Updates connection URI when connection params changed
     */
    private void updateURI() {
        try {
            uri.setText(DatabaseUtils.db(new MongoUtils()).getConnectionURI(
                authUser.getText(),
                null,
                Integer.valueOf(port.getText()),
                host.getText(),
                path.getText()
            ).toString());
        } catch (URISyntaxException e) {
            log.warning("Invalid chars in connection URI: " + e.getMessage());
            e.printStackTrace();
            uri.setText("");
        } catch (NumberFormatException e) {
            log.warning("Port is not a valid integer");
            uri.setText("");
        }
    }

    @FXML
    private void initialize() {
        authType.setItems(FXCollections.observableArrayList(
            I18N.getString("opt.auth.user"),
            I18N.getString("opt.auth.none")
        ));
        authType.getSelectionModel().select(0);
        // Connection param update listeners
        authUser.textProperty().addListener((observable, oldValue, newValue) -> updateURI());
        path.textProperty().addListener((observable, oldValue, newValue) -> updateURI());
        host.textProperty().addListener((observable, oldValue, newValue) -> updateURI());
        port.textProperty().addListener((observable, oldValue, newValue) -> updateURI());
    }

    @FXML
    private void add(ActionEvent evt) {
        // Just look at the amount of explicit casts it takes to get the current stage!
        ((Stage) ((Button) evt.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void testConn(ActionEvent evt) {
    }
}
