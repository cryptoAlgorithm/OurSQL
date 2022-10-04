package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.oursql.model.db.Cluster;
import com.cryptoalgo.oursql.model.HomeViewModel;
import com.cryptoalgo.oursql.model.db.data.Container;
import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.SecretsStore;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.prefs.*;

/**
 * Controller for the homepage of OurSQL. Corresponding
 * view is <code>home.fxml</code>
 */
public class Home {
    private static final Logger log = Logger.getLogger(Home.class.getName());

    private static final HomeViewModel viewModel = new HomeViewModel();

    @FXML
    private Accordion categoryList;
    @FXML
    private VBox addClusterTip, tableTipContainer, mainContentContainer;
    @FXML
    private TableView<ObservableList<Container<?>>> dbTable;
    @FXML
    private Label statusLabel;

    @FXML
    private void addCluster() {
        try { AddClusterDialog.show(); } catch (IOException e) {
            log.severe("IOException when showing add cluster dialog: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /*==================================================
     *
     * Initialization
     *
     ===================================================*/

    private void showHideAddClusterTip() {
        addClusterTip.setVisible(!viewModel.hasClusters());
        addClusterTip.setManaged(!viewModel.hasClusters());
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
                    t.setPrefHeight(tables.size() * 28);
                    p.setContent(t);
                    t.getSelectionModel().selectedIndexProperty().addListener((o, ov, nv) -> {
                        if (nv.intValue() < 0) return;
                        log.info("navigating to cluster ID "
                            + c.getID()
                            + " and table "
                            + tables.get(nv.intValue()));
                        viewModel.newTableSelection(c, tables.get(nv.intValue()));
                    });
                    t.getSelectionModel().select(0);
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
            p.setContent(new Label("Loading tables..."));

            // Tables are lazy-loaded the first time the titled pane is opened
            p.expandedProperty().addListener((ch, o, newVal) -> {
                // If titledPane is collapsed, clear table selection
                if (!newVal) {
                    if (categoryList.getExpandedPane() != null && p.getContent() instanceof ListView<?>)
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
                del = new MenuItem(I18N.getString("action.delCluster"));
            header.getStyleClass().add("accentedHeader");
            header.setDisable(true);
            del.setOnAction((ActionEvent ev) -> {
                try {
                    c.remove();
                    SecretsStore.remove(c.getID());
                } catch (BackingStoreException e) {
                    log.severe("Failed to remove cluster");
                }
            });

            m.getItems().addAll(header, del);
            p.setContextMenu(m);
            categoryList.getPanes().add(idx, p);
            showHideAddClusterTip();
        });

        viewModel.setOnRemoveCluster(i -> Platform.runLater(() -> {
            categoryList.getPanes().remove(i.intValue());
            showHideAddClusterTip();
        }));

        // Populate clusters map
        viewModel.loadClusters();
    }

    private void initTableView() {
        dbTable.setItems(viewModel.rows);
        viewModel.tableColumns.addListener((ListChangeListener<String>) c -> {
            while (c.next()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    if (c.wasPermutated())
                        dbTable.getColumns().get(i).setText(c.getList().get(i));
                    else if (c.wasAdded()) {
                        final int curCol = i;
                        final TableColumn<ObservableList<Container<?>>, String> col
                            = new TableColumn<>(c.getList().get(i));
                        col.setCellValueFactory(
                            param -> new ReadOnlyObjectWrapper<>(param.getValue().get(curCol).toString())
                        );
                        dbTable.getColumns().add(col);
                    }
                }
                if (c.wasRemoved()) dbTable.getColumns().remove(c.getFrom());
            }
        });
    }

    @FXML
    private void initialize() {
        initClusterList();
        initTableView();

        // Bind visible and managed properties of main content and tip
        tableTipContainer.visibleProperty().bind(viewModel.selectedTableProperty().isEmpty());
        tableTipContainer.managedProperty().bind(viewModel.selectedTableProperty().isEmpty());
        mainContentContainer.visibleProperty().bind(viewModel.selectedTableProperty().isEmpty().not());
        mainContentContainer.managedProperty().bind(viewModel.selectedTableProperty().isEmpty().not());

        statusLabel.textProperty().bind(viewModel.statusProperty());
    }
}