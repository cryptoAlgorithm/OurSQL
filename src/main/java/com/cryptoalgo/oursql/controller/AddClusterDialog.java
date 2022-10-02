package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.codable.EncodingException;
import com.cryptoalgo.db.Cluster;
import com.cryptoalgo.db.DatabaseUtils;
import com.cryptoalgo.db.DBMSUtils;
import com.cryptoalgo.db.impl.BuiltInDBs;
import com.cryptoalgo.oursql.OurSQL;
import com.cryptoalgo.oursql.component.StyledAlert;
import com.cryptoalgo.oursql.component.PasswordDialog;
import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.SecretsStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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

/**
 * Controller for the add cluster flow
 */
public class AddClusterDialog {
    // Using private visibility and FXML modifier - best practices
    @FXML
    private Label uriConstructError;
    @FXML
    private Text userLabel, pwLabel, storeLabel;
    @FXML
    private ComboBox<String> authType, storeType;
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
        stage.setMinWidth(640);
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

    /**
     * Construct a cluster from the values currently populated in the fields.
     * Doesn't do any sanity checking!
     * @return an instance database cluster object
     */
    private Cluster constructCluster() {
        return new Cluster(
            host.getText(),
            database.getText(),
            authUser.getText().isBlank() ? null : authUser.getText(),
            port.getText().isEmpty()
                ? getDBUtils().defaultPort()
                : Integer.parseInt(port.getText()),
            name.getText()
        );
    }

    private void loadDrivers() {
        ObservableList<HBox> drivers = FXCollections.observableArrayList();
        // Loop through built in DBMS implementations
        for (DBMSUtils d : BuiltInDBs.impls) {
            HBox dbOpt = new HBox(6);
            dbOpt.setAlignment(Pos.CENTER_LEFT);
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
            // Set default db as prompt text to remind the user
            database.setPromptText(getDBUtils().defaultDB());
            fieldsUpdated();
        };
        dbTypeList.getSelectionModel().selectedIndexProperty().addListener((b, o, n) -> handleNewDBMS.run());
        handleNewDBMS.run(); // Run for the first time to populate fields
    }

    @FXML
    private void initialize() {
        loadDrivers();

        // Add comboBox options
        authType.setItems(FXCollections.observableArrayList(
            I18N.getString("opt.auth.user"),
            I18N.getString("opt.auth.none")
        ));
        storeType.setItems(FXCollections.observableArrayList(
            I18N.getString("opt.store.enc"),
            I18N.getString("opt.store.plain"),
            I18N.getString("opt.store.none")
        ));
        authType.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            boolean useAuth = newValue.intValue() == 0;
            // Make auth fields invisible and remove them from the layout
            authUser.setVisible(useAuth); authUser.setManaged(useAuth);
            authPW.setVisible(useAuth); authPW.setManaged(useAuth);
            userLabel.setVisible(useAuth); userLabel.setManaged(useAuth);
            pwLabel.setVisible(useAuth); pwLabel.setManaged(useAuth);
            storeLabel.setVisible(useAuth); storeLabel.setManaged(useAuth);
            storeType.setVisible(useAuth); storeType.setManaged(useAuth);
        });
        // Ensure combobox has an initial value
        storeType.getSelectionModel().select(0);
        authType.getSelectionModel().select(0);

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
        Cluster c = constructCluster();
        // Ensure cluster with same id doesn't already exist, otherwise bad things will happen
        if (c.alreadyExists()) {
            new StyledAlert(
                Alert.AlertType.ERROR,
                I18N.getString("dialog.dbDupe.title"),
                I18N.getString("dialog.dbDupe.title"),
                I18N.getString("dialog.dbDupe.body")
            ).show();
            return;
        }
        try {
            if (authType.getSelectionModel().isSelected(0)) {
                if (storeType.getSelectionModel().isSelected(0)) {
                    String pw = new PasswordDialog(
                        I18N.getString("dialog.encPw.title"),
                        I18N.getString("dialog.encPw.header"),
                        I18N.getString("dialog.encPw.body"),
                        I18N.getString("dialog.encPw.caption")
                    ).showAndWait().orElse(null);
                    if (pw == null) return; // User clicked cancel
                    SecretsStore.encrypt(authPW.getText(), pw, c.getID());
                } else if (storeType.getSelectionModel().isSelected(1))
                    SecretsStore.encrypt(authPW.getText(), c.getID());
            }
            c.persist();
        } catch (SecretsStore.StoreException e) {
            log.severe("Failed to add password to secrets store!");
            return;
        } catch (EncodingException e) {
            // This shouldn't happen during normal operation, so it's
            // fairly safe to just log a warning without showing the user anything
            log.severe("Failed to persist cluster!");
            return;
        }
        // If all is good, close the model's window
        // Just look at the amount of explicit casts it takes to get the current stage!
        ((Stage) ((Button) evt.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void testConn() {
        testButton.setDisable(true);
        String dbName, dbVer, driverName, driverVer, driverSpec;
        try {
            DatabaseMetaData dbMeta = DatabaseUtils
                .db(getDBUtils())
                .getConnection(
                    constructCluster(),
                    authPW.getText().isBlank() ? null : authPW.getText()
                )
                .getMetaData();
            // Populate hoisted vars
            dbName = dbMeta.getDatabaseProductName();
            dbVer = dbMeta.getDatabaseProductVersion();
            driverName = dbMeta.getDriverName();
            driverVer = dbMeta.getDriverVersion();
            driverSpec = dbMeta.getJDBCMajorVersion() + "." + dbMeta.getJDBCMinorVersion();
        } catch (URISyntaxException | SQLException ex) {
            new StyledAlert(
                Alert.AlertType.ERROR,
                I18N.getString("dialog.dbTestFail.title"),
                I18N.getString("dialog.dbTestFail.header"),
                ex.getLocalizedMessage()
            ).show();
            return;
        } finally {
            // Always re-enable button
            testButton.setDisable(false);
        }
        // Show db test succeeded alert
        new StyledAlert(
            Alert.AlertType.INFORMATION,
            I18N.getString("dialog.dbTestOk.title"),
            I18N.getString("dialog.dbTestOk.header"),
            I18N.getString(
                "dialog.dbTestOk.body",
                dbName,
                dbVer,
                driverName,
                driverVer,
                driverSpec
            )
        ).show();
    }
}
