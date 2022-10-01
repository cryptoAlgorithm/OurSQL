package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.db.Cluster;
import com.cryptoalgo.db.DatabaseUtils;
import com.cryptoalgo.db.SpecificDBUtils;
import com.cryptoalgo.db.mongo.MongoUtils;
import com.cryptoalgo.oursql.OurSQL;
import com.cryptoalgo.oursql.support.I18N;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class AddClusterDialog {
    @FXML
    private Label uriConstructError;
    @FXML
    private Text userLabel, pwLabel;
    @FXML
    private ComboBox<String> authType; // Using private visibility and FXML modifier - best practices
    @FXML
    private TextField name, host, port, authUser, authPW, path, uri;
    @FXML
    private Button addButton, testButton;

    private static final Logger log = Logger.getLogger(AddClusterDialog.class.getName());

    public static void show() throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(I18N.getString("action.addCluster"));
        stage.setScene(new Scene(FXMLLoader.load(
            Objects.requireNonNull(OurSQL.class.getResource("view/add-cluster.fxml")),
            ResourceBundle.getBundle("locales/strings", new Locale("en"))
        )));
        stage.setMinWidth(430);
        stage.setMinHeight(250);
        stage.show();
    }

    private SpecificDBUtils getDBUtils() {
        return new MongoUtils();
    }

    private void setStatus(@Nullable String URIError, boolean ok) {
        if (URIError != null) {
            uriConstructError.setText(URIError);
            uriConstructError.setManaged(true);
            uriConstructError.setVisible(true);
        } else {
            uriConstructError.setText("");
            uriConstructError.setVisible(false);
            uriConstructError.setManaged(false);
        }
        addButton.setDisable(!ok);
        testButton.setDisable(!verifyFields(true));
    }

    /**
     * Checks if all fields are present and valid
     * @param connectionOnly Only validate connection params
     */
    private boolean verifyFields(boolean connectionOnly) {
        return (connectionOnly || !name.getText().isBlank())
            && !host.getText().isBlank()
            && !port.getText().isBlank()
            && (authType.getSelectionModel().getSelectedIndex() == 1 || (
                !authUser.getText().isBlank() && !authPW.getText().isEmpty()
            )); // Note the use of isEmpty for the password to allow leading/trailing spaces
    }

    /**
     * Updates various elements when a field's content changes
     */
    private void fieldsUpdated() {
        try {
            uri.setText(DatabaseUtils.db(new MongoUtils()).getConnectionURI(
                port.getText().isEmpty() ? -1 : Integer.parseInt(port.getText()),
                host.getText().trim(),
                path.getText(),
                false
            ).toString());
            setStatus(null, verifyFields(false));
        } catch (URISyntaxException e) {
            setStatus(I18N.getString("prompt.invalidURI"), false);
            log.warning("Invalid chars in connection URI: " + e.getMessage());
            uri.setText("");
        } catch (NumberFormatException ignored) {} // Will never happen, but just to be 100% sure
    }

    @FXML
    private void initialize() {
        /*try {
            Class.forName("mongodb.jdbc.MongoDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: Unable to load SQLServer JDBC Driver");
            e.printStackTrace();
        }*/

        authType.setItems(FXCollections.observableArrayList(
            I18N.getString("opt.auth.user"),
            I18N.getString("opt.auth.none")
        ));
        authType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            boolean useAuth = newValue.intValue() == 0;
            authUser.setVisible(useAuth);
            authPW.setVisible(useAuth);
            userLabel.setVisible(useAuth);
            pwLabel.setVisible(useAuth);
            authUser.setManaged(useAuth);
            authPW.setManaged(useAuth);
            userLabel.setManaged(useAuth);
            pwLabel.setManaged(useAuth);
        });
        authType.getSelectionModel().select(0);

        // Prepopulate with some default values
        port.setText(getDBUtils().defaultPort().toString());
        // Restrict port to +ve numeric input only
        port.setTextFormatter(new TextFormatter<>(c -> {
            String t = c.getControlNewText();
            try {
                if (t.isEmpty() ||
                    (t.matches("^[1-9]\\d{0,4}$") && Integer.parseInt(t) <= 65535)) return c;
                else return null;
            } catch (NumberFormatException e) { // Just in case some funny input slipped past the regex
                return null;
            }
        }));

        // Connection param update listeners
        authUser.textProperty().addListener((b, o, n) -> fieldsUpdated());
        authPW.textProperty().addListener((b, o, n) -> fieldsUpdated());
        path.textProperty().addListener((b, o, n) -> fieldsUpdated());
        host.textProperty().addListener((b, o, n) -> fieldsUpdated());
        port.textProperty().addListener((b, o, n) -> fieldsUpdated());
        name.textProperty().addListener((b, o, n) -> fieldsUpdated());
        authType.getSelectionModel().selectedIndexProperty().addListener((b, o, n) -> fieldsUpdated());
        // Populate URL field initially
        fieldsUpdated();
    }

    @FXML
    private void add(ActionEvent evt) {
        // Just look at the amount of explicit casts it takes to get the current stage!
        ((Stage) ((Button) evt.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void testConn() throws SQLException, URISyntaxException {
        DatabaseUtils.db(getDBUtils()).getConnection(
            new Cluster(
                host.getText(),
                path.getText(),
                authUser.getText().isBlank() ? null : authUser.getText(),
                Integer.parseInt(port.getText()),
                name.getText()
            ),
            authPW.getText().isBlank() ? null : authPW.getText()
        ).createStatement().execute("""
SELECT
    db.name AS DBName,
    type_desc AS FileType,
    Physical_Name AS Location
FROM
    sys.master_files mf
INNER JOIN
    sys.databases db ON db.database_id = mf.database_id
""");
    }
}
