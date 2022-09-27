package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.db.Cluster;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.StageStyle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

/**
 * Controller for the homepage of OurSQL. Corresponding
 * view is <code>home.fxml</code>
 */
public class Home {
    private static final Logger log = Logger.getLogger(Home.class.getName());

    @FXML
    private Accordion categoryList;

    private final LinkedHashMap<String, Cluster> linkedClusterMap = new LinkedHashMap<>();
    private final ObservableMap<String, Cluster> clusters = FXCollections.observableMap(linkedClusterMap);

    @FXML
    private void addCluster() {
        // create a text input dialog
        TextInputDialog td = new TextInputDialog("enter any text");
        td.initStyle(StageStyle.TRANSPARENT);

        td.setHeaderText("Enter Cluster URI");
        td.show();
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
        Preferences.userRoot().node("clusters").addNodeChangeListener(new NodeChangeListener() {
            public void childAdded(NodeChangeEvent evt) {
                try {
                    Cluster newCluster = Cluster.decode(evt.getChild().name());
                    clusters.put(newCluster.getID(), newCluster);
                } catch (DecodingException | NoSuchElementException ignored) {}
            }

            public void childRemoved(NodeChangeEvent evt) {
                clusters.remove(evt.getChild().name());
            }
        });

        // Listen to cluster changes for single source of truth
        clusters.addListener((MapChangeListener<String, Cluster>) evt -> {
            if (evt.wasAdded()) {
                // Add cluster to sidebar
                TitledPane p = new TitledPane();
                p.setText(evt.getValueAdded().getName());
                categoryList.getPanes().add(p);
            } else {
                categoryList
                    .getPanes()
                    .remove(List.of(evt.getMap().keySet().toArray()).indexOf(evt.getKey()));
            }
        });

        // Populate clusters map
        try {
            for (String clusterID : Preferences.userRoot().node("clusters").childrenNames()) {
                try {
                    Cluster c = Cluster.decode(clusterID);
                    clusters.put(c.getID(), c);
                } catch (DecodingException | NoSuchElementException e) {
                    log.warning(String.format(
                        "Could not decode cluster with ID %s due to exception: %s, skipping...",
                        clusterID,
                        e.getMessage()
                    ));
                }
            }
        } catch (BackingStoreException | IllegalStateException ignored) {}
    }

    @FXML
    private void initialize() {
        initClusterList();
    }
}