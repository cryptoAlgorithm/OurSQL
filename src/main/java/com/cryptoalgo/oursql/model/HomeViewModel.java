package com.cryptoalgo.oursql.model;

import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.db.Cluster;
import com.cryptoalgo.db.DatabaseUtils;
import com.cryptoalgo.oursql.component.PasswordDialog;
import com.cryptoalgo.oursql.support.AsyncUtils;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

/**
 * Over-engineered database connection, loading and updating class.
 * Serves as a view model for {@link com.cryptoalgo.oursql.controller.Home}
 */
public class HomeViewModel {
    private static final Logger log = Logger.getLogger(HomeViewModel.class.getName());

    private final ObservableList<Cluster> clusters = FXCollections.observableArrayList(new ArrayList<>());
    public final ObservableMap<String, ObservableList<String>> tables =
        FXCollections.observableMap(new HashMap<>());

    public final ObservableMap<String, String> cachedPasswords =
        FXCollections.observableMap(new HashMap<>());

    /**
     * Request to load the tables of a particular database cluster.
     * Call again with the same cluster ID to refresh clusters
     * @param cluster Cluster to fetch tables of
     * @throws NoSuchElementException If the cluster ID doesn't exist
     */
    public ObservableList<String> requestTable(Cluster cluster) throws NoSuchElementException, SQLException {
        String id = cluster.getID(); // Shorter code since id is used in many places
        int idx = clusters.indexOf(cluster);
        if (idx < 0) throw new NoSuchElementException("Requested cluster doesn't exist in clusters array");

        if (cluster.getUsername() != null) {
            if (cachedPasswords.getOrDefault(id, null) == null) {
                AsyncUtils.runLaterAndWait(() -> cachedPasswords.put(id, new PasswordDialog(
                    "Cluster Authentication",
                    "Enter the password for the cluster",
                    "",
                    null
                ).showAndWait().orElse(null)));
            }
            // If it still isn't populated, the user clicked cancel
            if (cachedPasswords.getOrDefault(id, null) == null) return null;
        }

        try {
            Connection conn = DatabaseUtils.getConnection(
                cluster,
                cluster.getUsername() != null ? cachedPasswords.get(id) : null
            );
            ResultSet r = conn
                .getMetaData()
                .getTables(null, null, "%", new String[]{"TABLE"});
            if (!tables.containsKey(id)) tables.put(id, FXCollections.observableArrayList());
            else tables.get(id).clear();
            while (r.next()) tables.get(id).add(r.getString(3));
        } catch (URISyntaxException ignored) {}

        return tables.get(id);
    }

    public boolean tablesCached(String forCluster) {
        return tables.containsKey(forCluster);
    }

    /**
     * Load persisted clusters. Listeners registered through
     * {@link #setOnNewCluster(BiConsumer callback)} will be notified if any new
     * clusters were loaded.
     */
    public void loadClusters() {
        if (hasClusters()) return; // Clusters were already loaded
        String[] clusterIDs;
        try {
            clusterIDs = PreferencesEncoder.rootNode.node("clusters").childrenNames();
        } catch (BackingStoreException | IllegalStateException e) {
            // This should never ever happen
            log.severe("Failed to retrieve stored cluster IDs!");
            return;
        }
        for (String clusterID : clusterIDs) {
            try {
                addCluster(Cluster.decode(clusterID));
            } catch (DecodingException | InvocationTargetException e) {
                log.warning(String.format(
                    "Could not decode cluster with ID %s due to exception: %s, removing node",
                    clusterID,
                    e.getMessage()
                ));
                e.printStackTrace();
                try {
                    PreferencesEncoder.rootNode.node("clusters/" + clusterID).removeNode();
                } catch (BackingStoreException ex) {
                    log.severe("Failed to remove corrupted cluster!");
                }
            }
        }
    }

    public void setOnNewCluster(BiConsumer<Cluster, Integer> cb) {
        clusters.addListener((ListChangeListener<Cluster>) ch -> {
            while (ch.next()) if (ch.wasAdded())
                for (int i = ch.getFrom(); i < ch.getTo(); i++) cb.accept(ch.getList().get(i), i);
        });
    }

    public void setOnRemoveCluster(Consumer<Integer> cb) {
        clusters.addListener((ListChangeListener<Cluster>) ch -> {
            while (ch.next()) if (ch.wasRemoved()) cb.accept(ch.getFrom());
        });
    }

    /**
     * Get the index of the cluster with the specified ID.
     * @param clusterID ID to get index of
     * @return Index of cluster with specified ID, -1 if not found
     */
    public int indexOf(@NotNull String clusterID) {
        for (int i = 0; i < clusters.size(); i++)
            if (clusters.get(i).getID().equals(clusterID)) return i;
        return -1;
    }

    public void addCluster(@NotNull Cluster c) {
        clusters.add(c);
    }

    /**
     * Remove the cluster with the specified ID
     * @param clusterID ID of cluster to remove
     * @throws NoSuchElementException If no cluster with specified ID exists
     */
    public void removeCluster(@NotNull String clusterID) throws NoSuchElementException {
        int idx = indexOf(clusterID);
        if (idx < 0) throw new NoSuchElementException("No cluster with requested ID exists");
        removeCluster(idx);
    }
    public void removeCluster(@NotNull Integer idx) {
        clusters.remove(idx.intValue());
    }

    public boolean hasClusters() {
        return !clusters.isEmpty();
    }
}
