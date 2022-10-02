package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.db.Cluster;
import com.cryptoalgo.oursql.support.I18N;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.prefs.*;

/**
 * Controller for the homepage of OurSQL. Corresponding
 * view is <code>home.fxml</code>
 */
public class Home {
    private static final Logger log = Logger.getLogger(Home.class.getName());

    @FXML
    private Accordion categoryList;

    private final ObservableMap<String, Cluster> clusters = FXCollections.observableMap(new HashMap<>());

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

    /**
     * Populates the cluster list on the left in the <code>BorderPane</code> and
     * updates it should there be any changes. To be called in the
     * {@link #initialize()} method.
     */
    private void initClusterList() {
        PreferencesEncoder.rootNode.node("clusters").addNodeChangeListener(new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) {
                // A hack to allow serialization to complete first - race condition
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                try {
                    Cluster newCluster = Cluster.decode(evt.getChild().name());
                    Platform.runLater(() -> clusters.put(newCluster.getID(), newCluster));
                } catch (DecodingException | InvocationTargetException e) {
                    e.printStackTrace();
                    log.warning(
                        "Failed to deserialize added cluster with id "
                        + evt.getChild().name()
                    );
                }
            }

            public void childRemoved(NodeChangeEvent evt) { clusters.remove(evt.getChild().name()); }
        });

        // Listen to cluster changes for single source of truth
        clusters.addListener((MapChangeListener<String, Cluster>) evt -> {
            if (evt.wasAdded()) {
                Cluster c = evt.getValueAdded();
                // Add cluster to sidebar
                TitledPane p = new TitledPane();
                p.setText(c.getName());

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
                        return;
                    }
                    categoryList.getPanes().remove(p);
                });

                m.getItems().addAll(header, del);
                p.setContextMenu(m);
                categoryList.getPanes().add(p);
            }
        });

        // Populate clusters map
        try {
            for (String clusterID : PreferencesEncoder.rootNode.node("clusters").childrenNames()) {
                try {
                    Cluster c = Cluster.decode(clusterID);
                    clusters.put(c.getID(), c);
                } catch (DecodingException | InvocationTargetException e) {
                    log.warning(String.format(
                        "Could not decode cluster with ID %s due to exception: %s, removing node",
                        clusterID,
                        e.getMessage()
                    ));
                    e.printStackTrace();
                    PreferencesEncoder.rootNode.node("clusters/" + clusterID).removeNode();
                }
            }
        } catch (BackingStoreException | IllegalStateException ignored) {}
    }

    @FXML
    private void initialize() {
        initClusterList();
    }
}