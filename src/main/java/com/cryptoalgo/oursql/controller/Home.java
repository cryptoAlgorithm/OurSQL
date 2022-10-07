package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.oursql.component.*;
import com.cryptoalgo.oursql.model.db.Cluster;
import com.cryptoalgo.oursql.model.HomeViewModel;
import com.cryptoalgo.oursql.model.db.data.Container;
import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.SecretsStore;
import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.*;

/**
 * Controller for the homepage of OurSQL. Corresponding
 * view is <code>home.fxml</code>. Why is this file so huge?
 * I have no idea. It shouldn't be, but I guess the lack of
 * extensions in Java makes it hard to further split up the
 * file into smaller bits.
 */
public class Home {
    private static final Logger log = Logger.getLogger(Home.class.getName());

    private static final HomeViewModel viewModel = new HomeViewModel();

    @FXML
    private TextField statementField;
    @FXML
    private Button execStatementButton, delRowButton;
    @FXML
    private Accordion clusterList;
    @FXML
    private VBox addClusterTip, tableTipContainer, mainContentContainer;
    @FXML
    private HBox statusContainer;
    @FXML
    private Rectangle statusBgRect;
    @FXML
    private TableView<ObservableList<Container<?>>> dbTable;
    @FXML
    private Label statusLabel;
    @FXML
    private Text curTableLabel;

    private Stage statusPopup = null;

    @FXML
    private void addCluster() {
        try { AddClusterDialog.show(); } catch (IOException e) {
            log.severe("IOException when showing add cluster dialog: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void execStatement(String statement) {
        execStatementButton.setDisable(true);
        statementField.setDisable(true);
        new Thread(() -> {
            final Pair<Pair<List<String>, ObservableList<ObservableList<Container<?>>>>, Integer> res;
            try {
                res = viewModel.runQuery(statement);
            } catch (SQLException e) {
                Platform.runLater(() -> new StyledAlert(
                    Alert.AlertType.ERROR,
                    I18N.getString("dialog.execFail.title"),
                    I18N.getString("dialog.execFail.header"),
                    e.getLocalizedMessage()
                ).show());
                return;
            } finally {
                Platform.runLater(() -> {
                    execStatementButton.setDisable(false);
                    statementField.setDisable(false);
                });
            }
            final var hasRes = res.getKey() != null;
            Platform.runLater(() -> new TableDialog(
                hasRes ? res.getKey().getKey() : null,
                hasRes ? res.getKey().getValue() : null,
                I18N.getString("dialog.execResult.title"),
                I18N.getString("dialog.execResult.title"),
                I18N.getString(hasRes
                    ? "dialog.execResult.query.body"
                    : "dialog.execResult.update.body", res.getValue()
                )
            ).show());
        }).start();
    }

    /*==================================================
     *
     * Initialization
     *
     ===================================================*/

    /**
     * Bind visible and managed properties to show tip when cluster list is empty
     */
    private void initClusterTip() {
        addClusterTip.visibleProperty().bind(Bindings.size(clusterList.getPanes()).isEqualTo(0));
        addClusterTip.managedProperty().bind(Bindings.size(clusterList.getPanes()).isEqualTo(0));
    }

    private void loadTables(Cluster c, TitledPane p) {
        p.setDisable(true);
        Thread fetchThread = new Thread(() -> {
            ObservableList<String> tables;
            try {
                tables = viewModel.requestTables(c);
            } catch (SQLException e) {
                Hyperlink retry = new Hyperlink("Retry");
                retry.setOnAction((evt) -> loadTables(c, p));
                Platform.runLater(() -> p.setContent(new TextFlow(
                    new Label("Fetch failed"),
                    retry
                )));
                log.warning("Failed to fetch tables");
                e.printStackTrace();
                return;
            } finally { p.setDisable(false); }
            Platform.runLater(() -> {
                if (tables != null) {
                    ListView<String> t = new ListView<>();
                    t.setItems(tables);

                    // Listen for changes to tables
                    tables.addListener((ListChangeListener<String>) ch -> {
                        if (ch.getList().size() == 0) t.setPlaceholder(new Label("No tables"));
                        while (ch.next()) if (ch.wasRemoved()) t.getSelectionModel().clearSelection();
                    });
                    // Listen for changes to selection
                    t.getSelectionModel().selectedIndexProperty().addListener((o, ov, nv) -> {
                        if (nv.intValue() < 0) return;
                        log.info("navigating to cluster ID "
                            + c.getID()
                            + " and table "
                            + tables.get(nv.intValue()));
                        new Thread(() -> {
                            Platform.runLater(() -> t.setDisable(true));
                            viewModel.newTableSelection(c, tables.get(nv.intValue()));
                            Platform.runLater(() -> t.setDisable(false));
                        }).start();
                    });
                    t.getSelectionModel().select(0);

                    // hack to fix height of list
                    t.prefHeightProperty().bind(Bindings.multiply(Bindings.size(tables), 30));

                    p.setContent(t);
                } else p.setExpanded(false);
            });
        });
        fetchThread.start();
    }

    /**
     * Populates the cluster list on the left in the <code>BorderPane</code> and
     * updates it should there be any changes. To be called in the
     * {@link #initialize()} method.
     */
    private void initClusterList() {
        PreferencesEncoder.rootNode.node("clusters").addNodeChangeListener(new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) {
                // A hack to allow serialization to complete first - race condition
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                try {
                    Cluster newCluster = Cluster.decode(evt.getChild().name());
                    Platform.runLater(() -> viewModel.addCluster(newCluster));
                } catch (DecodingException | InvocationTargetException | IllegalStateException e) {
                    log.warning(
                        "Failed to deserialize added cluster with id "
                        + evt.getChild().name() + ", removing node"
                    );
                    try {
                        evt.getChild().removeNode();
                    } catch (BackingStoreException ignored) {}
                    e.printStackTrace();
                }
            }

            public void childRemoved(NodeChangeEvent evt) { viewModel.removeCluster(evt.getChild().name()); }
        });

        // Listen to cluster changes for single source of truth
        viewModel.setOnNewCluster((Cluster c, Integer idx) -> {
            // Add cluster to sidebar
            var p = new TitledPane();
            p.setAnimated(false); // Having no animation is better than a buggy mess
            p.setText(c.getName());
            p.setContent(new Label(I18N.getString("label.loadTables")));

            // Tables are lazy-loaded the first time the titled pane is opened
            p.expandedProperty().addListener((ch, o, newVal) -> {
                // If titledPane is collapsed, clear table selection
                if (!newVal) {
                    if (clusterList.getExpandedPane() != null && p.getContent() instanceof ListView<?>)
                        ((ListView<?>) p.getContent()).getSelectionModel().clearSelection();
                    return;
                }
                // Return if tables are already loaded into cache
                if (viewModel.tablesCached(c.getID())) {
                    if (p.getContent() instanceof ListView<?>) {
                        var ls = ((ListView<?>) p.getContent()).getSelectionModel();
                        if (ls.isEmpty()) ls.select(0);
                    }
                    return;
                }

                loadTables(c, p);
            });

            // Allow deleting cluster on right click
            var m = new ContextMenu();
            MenuItem
                header = new MenuItem(I18N.getString("actions")),
                newTable = new MenuItem(I18N.getString("action.newTable")),
                del = new MenuItem(I18N.getString("action.delCluster"));
            header.getStyleClass().add("accentedHeader");
            header.setDisable(true);
            newTable.setOnAction(e -> {
                String table = new StyledInputDialog(
                    I18N.getString("dialog.newTable.title"),
                    I18N.getString("dialog.newTable.title"),
                    I18N.getString("dialog.newTable.body")
                ).showAndWait().orElse(null);
                if (table == null) return;
                new Thread(() -> viewModel.createTable(c, table)).start();
            });
            del.setOnAction(ev -> {
                // Show confirmation before removing
                if (new StyledAlert(
                    Alert.AlertType.CONFIRMATION,
                    I18N.getString("dialog.removeCluster.title"),
                    I18N.getString("dialog.removeCluster.title"),
                    I18N.getString("dialog.removeCluster.body", c.getName())
                ).showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) return;
                try {
                    c.remove();
                    SecretsStore.remove(c.getID());
                } catch (BackingStoreException e) {
                    log.severe("Failed to remove cluster");
                }
            });

            m.getItems().addAll(header, newTable, del);
            p.setContextMenu(m);
            clusterList.getPanes().add(idx, p);
        });

        viewModel.setOnRemoveCluster(i -> Platform.runLater(() -> clusterList.getPanes().remove(i.intValue())));

        // Populate clusters map
        viewModel.loadClusters();
    }

    /**
     * Inits the tableView and binds the necessary values to those in the ViewModel.
     */
    private void initTableView() {
        UIUtils.clipToRadius(dbTable, 8);
        dbTable.setItems(viewModel.rows);
        dbTable.setEditable(true);

        // Listen to the selected row
        dbTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) ->
            delRowButton.setDisable(nv == null)
        );

        // Listen and update table when columns change
        viewModel.tableColumns.addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    if (c.wasPermutated())
                        dbTable.getColumns().get(i).setText(c.getList().get(i));
                    else if (c.wasAdded()) {
                        final var colTitle = c.getList().get(i);
                        final var curCol = i;
                        final var col = new TableColumn<ObservableList<Container<?>>, String>(colTitle);
                        col.setCellValueFactory(r -> new ReadOnlyObjectWrapper<>(r.getValue().get(curCol).toString()));
                        if (colTitle.equals("ctid")) col.setVisible(false); // Hide ctid col

                        // Handle editing
                        col.setOnEditCommit(evt -> {
                            final var row = evt.getRowValue();
                            try {
                                viewModel.attemptEdit(
                                    evt.getTableColumn().getText(),
                                    row.get(row.size()-1).toString(),
                                    evt.getNewValue()
                                );
                            } catch (SQLException e) {
                                e.printStackTrace();
                                throw new RuntimeException();
                            }
                        });
                        col.setCellFactory(param -> new SQLCellFactory(curCol));

                        // Set resize bounds
                        col.setPrefWidth(200);
                        col.setMinWidth(100);

                        // Add
                        Platform.runLater(() -> dbTable.getColumns().add(col));
                    }
                }
                if (c.wasRemoved()) Platform.runLater(() ->
                    dbTable.getColumns().remove(c.getFrom(), c.getFrom() + c.getRemovedSize())
                );
            }
        });
    }

    /**
     * Init status area of home page. Responsible for binding values and animating
     * the status background, among others.
     */
    void initStatus() {
        statusContainer.setOnMouseClicked(e -> {
            if (statusPopup != null) statusPopup.close();
            final var status = new Label();
            status.setWrapText(true);
            status.textProperty().bind(viewModel.displayedStatusProperty());
            status.setStyle("-fx-font-size: 1.2em");
            statusPopup = UIUtils.createUtilityStage(
                I18N.getString("popup.viewStatus.title"),
                status
            );
            statusPopup.show();
        });

        // Bind the size of the background rect to the status container
        statusBgRect.heightProperty().bind(statusContainer.heightProperty());
        statusBgRect.widthProperty().bind(statusContainer.widthProperty());
        statusBgRect.setManaged(false);
        statusBgRect.setArcWidth(14); statusBgRect.setArcHeight(14);

        statusLabel.textProperty().bind(viewModel.displayedStatusProperty());
        curTableLabel.textProperty().bind(
            Bindings.when(viewModel.selectedTableProperty().isNotNull())
                .then(Bindings.concat(
                    I18N.getString("label.curTable") + " ",
                    viewModel.selectedTableProperty())
                )
                .otherwise(I18N.getString("label.noCurTable"))
        );

        // Animate background changes
        viewModel.statusBgProperty().addListener((ob, ov, nv) -> {
            var t = new FillTransition(Duration.millis(250), statusBgRect, ov, nv);
            t.setCycleCount(1);
            t.play();
        });
    }

    /**
     * Inits the query field and handles button and field actions
     */
    private void initQuery() {
        statementField.setOnAction(ev -> execStatement(statementField.getText()));
        execStatementButton.setOnAction(ev -> execStatement(statementField.getText()));
    }

    @FXML
    private void initialize() {
        initClusterList();
        initClusterTip();
        initTableView();
        initQuery();
        initStatus();

        // Bind visible and managed properties of main content and tip
        tableTipContainer.visibleProperty().bind(viewModel.selectedTableProperty().isEmpty());
        tableTipContainer.managedProperty().bind(viewModel.selectedTableProperty().isEmpty());
        mainContentContainer.visibleProperty().bind(viewModel.selectedTableProperty().isEmpty().not());
        mainContentContainer.managedProperty().bind(viewModel.selectedTableProperty().isEmpty().not());
    }

    // Button action handlers
    @FXML
    private void handleRefreshTable(ActionEvent evt) {
        ((Button) evt.getSource()).setDisable(true);
        new Thread(() -> {
            viewModel.updateTable();
            ((Button) evt.getSource()).setDisable(false);
        }).start();
    }

    @FXML
    private void handleDropTable() {
        if (new StyledAlert(
            Alert.AlertType.CONFIRMATION,
            I18N.getString("dialog.dropTable.title"),
            I18N.getString("dialog.dropTable.title"),
            I18N.getString("dialog.dropTable.body", viewModel.selectedTableProperty().get())
        ).showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) return;
        viewModel.dropTable();
    }

    @FXML
    private void handleAddColumn(ActionEvent evt) {
        final var res = new AddColumnDialog().showAndWait().orElse(null);
        if (res == null) return;
        ((Button) evt.getSource()).setDisable(true);
        new Thread(() -> {
            viewModel.insertColumn(res.getKey(), res.getValue());
            ((Button) evt.getSource()).setDisable(false);
        }).start();
    }

    @FXML
    private void handleAddRow(ActionEvent evt) {
        ((Button) evt.getSource()).setDisable(true);
        new Thread(() -> {
            viewModel.insertRow();
            ((Button) evt.getSource()).setDisable(false);
        }).start();
    }

    @FXML
    private void handleDeleteRow() {
        if (dbTable.getSelectionModel().isEmpty()) return;
        if (new StyledAlert(
            Alert.AlertType.CONFIRMATION,
            I18N.getString("dialog.deleteRow.title"),
            I18N.getString("dialog.deleteRow.title"),
            I18N.getString("dialog.deleteRow.body")
        ).showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) return;
        new Thread(() -> {
            delRowButton.setDisable(true);
            final var row = dbTable.getSelectionModel().getSelectedItem();
            viewModel.deleteRow(row.get(row.size()-1).toString());
        }).start();
    }

    @FXML
    private void handleOpenSettings() {
        try { SettingsDialog.show(); } catch (IOException e) {
            log.severe("IOException when showing settings dialog: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}