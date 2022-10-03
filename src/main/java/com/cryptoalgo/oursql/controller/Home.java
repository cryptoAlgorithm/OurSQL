package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.db.Cluster;
import com.cryptoalgo.oursql.model.HomeViewModel;
import com.cryptoalgo.oursql.support.I18N;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
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
    private VBox addClusterTip;

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
                } catch (DecodingException | InvocationTargetException e) {
                    e.printStackTrace();
                    log.warning(
                        "Failed to deserialize added cluster with id "
                        + evt.getChild().name()
                    );
                }
            }

            public void childRemoved(NodeChangeEvent evt) { viewModel.removeCluster(evt.getChild().name()); }
        });

        // Listen to cluster changes for single source of truth
        viewModel.setOnNewCluster((Cluster c, Integer idx) -> {
            // Add cluster to sidebar
            TitledPane p = new TitledPane();
            p.setText(c.getName());
            p.setContent(new Label("Loading tables..."));

            // Tables are lazy-loaded the first time the titled pane is opened
            p.setOnMouseClicked(ev -> {
                // Only respond to right clicks
                // Return immediately if tables are already loaded into cache
                if (ev.getButton() != MouseButton.PRIMARY || viewModel.tablesCached(c.getID())) return;

                p.setDisable(true);
                Thread fetchThread = new Thread(() -> {
                    ObservableList<String> tables;
                    try {
                        tables = viewModel.requestTable(c);
                    } catch (SQLException e) {
                        Hyperlink retry = new Hyperlink("Retry");
                        //retry.setOnAction();
                        Platform.runLater(() -> p.setContent(new TextFlow(
                            new Label("Fetch failed"),
                            retry
                        )));
                        return;
                    } finally { p.setDisable(false); }
                    Platform.runLater(() -> {
                        if (tables != null) {
                            ListView<String> t = new ListView<>();
                            t.setItems(tables);
                            t.setPrefHeight(tables.size() * 28);
                            p.setContent(t);
                        } else p.setExpanded(false);
                    });
                });
                fetchThread.start();
            });

            // Allow deleting cluster on right click
            ContextMenu m = new ContextMenu();
            MenuItem
                header = new MenuItem(I18N.getString("actions")),
                del = new MenuItem(I18N.getString("action.delCluster"));
            header.getStyleClass().add("accentedHeader");
            header.setDisable(true);
            del.setOnAction((ActionEvent ev) -> {
                try { c.remove(); } catch (BackingStoreException e) {
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

    @FXML
    private void initialize() {
        initClusterList();
    }
}