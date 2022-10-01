package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.db.Cluster;
import com.cryptoalgo.db.DatabaseUtils;
import com.cryptoalgo.db.DBMSUtils;
import com.cryptoalgo.db.impl.BuiltInDBs;
import com.cryptoalgo.oursql.OurSQL;
import com.cryptoalgo.oursql.support.I18N;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.DatabaseMetaData;
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
    private TextField name, host, port, authUser, authPW, database, uri;
    @FXML
    private Button addButton, testButton;
    @FXML
    private ListView<HBox> dbTypeList;

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
        stage.setMinWidth(630);
        stage.setMinHeight(250);
        stage.show();
    }

    private DBMSUtils getDBUtils() {
        return BuiltInDBs.impls[dbTypeList.getSelectionModel().getSelectedIndex()];
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
            && (authType.getSelectionModel().getSelectedIndex() == 1 || (
                !authUser.getText().isBlank() && !authPW.getText().isEmpty()
            )); // Note the use of isEmpty for the password to allow leading/trailing spaces
    }

    /**
     * Updates various elements when a field's content changes
     */
    private void fieldsUpdated() {
        try {
            uri.setText(DatabaseUtils.db(getDBUtils()).getConnectionURI(
                port.getText().isEmpty() ? -1 : Integer.parseInt(port.getText()),
                host.getText().trim(),
                database.getText(),
                false
            ).toString());
            setStatus(null, verifyFields(false));
        } catch (URISyntaxException e) {
            setStatus(I18N.getString("prompt.invalidURI"), false);
            log.warning("Invalid chars in connection URI: " + e.getMessage());
            uri.setText("");
        } catch (NumberFormatException ignored) {} // Will never happen, but just to be 100% sure
    }

    private void loadDrivers() {
        ObservableList<HBox> drivers = FXCollections.observableArrayList();
        // Loop through built in DBMS implementations
        for (DBMSUtils d : BuiltInDBs.impls) {
            HBox dbOpt = new HBox(6);
            if (d.iconRes() != null) {
                URL icnSrc = OurSQL.class.getResource("img/db/" + d.iconRes());
                if (icnSrc != null) {
                    ImageView icn = new ImageView(icnSrc.toExternalForm());
                    icn.setFitHeight(20);
                    icn.setFitWidth(20);
                    icn.setSmooth(true);
                    dbOpt.getChildren().add(icn);
                } else {
                    log.warning(
                        "Could not get image resource for DBMS "
                        + d.name()
                        + " although an icon name was specified"
                    );
                }
            }
            dbOpt.getChildren().add(new Label(d.name()));
            drivers.add(dbOpt);
        }
        dbTypeList.setItems(drivers);
        dbTypeList.getSelectionModel().select(0);

        Runnable handleNewDBMS = () -> {
            // Update port hint
            port.setPromptText(getDBUtils().defaultPort().toString());
            // Update path to default database if it exists
            database.setText(getDBUtils().defaultDB() != null
                ? getDBUtils().defaultDB()
                : ""
            );
            fieldsUpdated();
        };
        dbTypeList.getSelectionModel().selectedIndexProperty().addListener((b, o, n) -> handleNewDBMS.run());
        handleNewDBMS.run(); // Run for the first time to populate fields
    }

    @FXML
    private void initialize() {
        loadDrivers();

        authType.setItems(FXCollections.observableArrayList(
            I18N.getString("opt.auth.user"),
            I18N.getString("opt.auth.none")
        ));
        authType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            boolean useAuth = newValue.intValue() == 0;
            // Make auth fields invisible and remove them from the layout
            authUser.setVisible(useAuth);
            authPW.setVisible(useAuth);
            userLabel.setVisible(useAuth);
            pwLabel.setVisible(useAuth);
            authUser.setManaged(useAuth);
            authPW.setManaged(useAuth);
            userLabel.setManaged(useAuth);
            pwLabel.setManaged(useAuth);
        });
        authType.getSelectionModel().select(0); // Select user & PW auth by default

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
        database.textProperty().addListener((b, o, n) -> fieldsUpdated());
        host.textProperty().addListener((b, o, n) -> fieldsUpdated());
        port.textProperty().addListener((b, o, n) -> fieldsUpdated());
        name.textProperty().addListener((b, o, n) -> fieldsUpdated());
        authType.getSelectionModel().selectedIndexProperty().addListener((b, o, n) -> fieldsUpdated());
    }

    @FXML
    private void add(ActionEvent evt) {
        // Just look at the amount of explicit casts it takes to get the current stage!
        ((Stage) ((Button) evt.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void testConn() {
        String dbName, dbVer, driverName, driverVer, driverSpec;
        try {
            DatabaseMetaData dbMeta = DatabaseUtils
                .db(getDBUtils())
                .getConnection(
                    new Cluster(
                        host.getText(),
                        database.getText(),
                        authUser.getText().isBlank() ? null : authUser.getText(),
                        port.getText().isEmpty()
                            ? getDBUtils().defaultPort()
                            : Integer.parseInt(port.getText()),
                        name.getText()
                    ),
                    authPW.getText().isBlank() ? null : authPW.getText()
                )
                .getMetaData();
            dbName = dbMeta.getDatabaseProductName();
            dbVer = dbMeta.getDatabaseProductVersion();
            driverName = dbMeta.getDriverName();
            driverVer = dbMeta.getDriverVersion();
            driverSpec = dbMeta.getJDBCMajorVersion()
                + "."
                + dbMeta.getJDBCMinorVersion();
        } catch (URISyntaxException | SQLException ex) {
            Alert e = new Alert(Alert.AlertType.ERROR);
            e.setTitle(I18N.getString("dialog.dbTestFail.title"));
            e.setHeaderText(I18N.getString("dialog.dbTestFail.header"));
            e.setContentText(ex.getLocalizedMessage());
            e.show();
            return;
        }
        Alert i = new Alert(Alert.AlertType.INFORMATION);
        i.setTitle(I18N.getString("dialog.dbTestOk.title"));
        i.setHeaderText(I18N.getString("dialog.dbTestOk.header"));
        i.setContentText(I18N.getString(
            "dialog.dbTestOk.body",
            dbName,
            dbVer,
            driverName,
            driverVer,
            driverSpec
        ));
        i.show();
    }
}
