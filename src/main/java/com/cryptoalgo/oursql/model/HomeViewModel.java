package com.cryptoalgo.oursql.model;

import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.oursql.component.StyledAlert;
import com.cryptoalgo.oursql.model.db.Cluster;
import com.cryptoalgo.oursql.model.db.DatabaseUtils;
import com.cryptoalgo.oursql.component.PasswordDialog;
import com.cryptoalgo.oursql.model.db.data.Container;
import com.cryptoalgo.oursql.model.db.data.PlaceholderContainer;
import com.cryptoalgo.oursql.support.AsyncUtils;
import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.SecretsStore;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.*;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Over-engineered database connection, loading and updating class.
 * Serves as a view model for {@link com.cryptoalgo.oursql.controller.Home}
 */
public class HomeViewModel {
    private static final Logger log = Logger.getLogger(HomeViewModel.class.getName());

    private final ObservableList<Cluster> clusters = FXCollections.observableArrayList();
    private final ObservableMap<String, ObservableList<String>> tables =
        FXCollections.observableMap(new HashMap<>());

    private static final ObservableMap<String, String> cachedPasswords =
        FXCollections.observableMap(new HashMap<>());

    public final ObservableList<String> tableColumns = FXCollections.observableArrayList();
    public final ObservableList<ObservableList<Container<?>>> rows = FXCollections.observableArrayList();

    private final ReadOnlyStringWrapper
        status = new ReadOnlyStringWrapper(I18N.getString("status.ready")),
        selectedTable = new ReadOnlyStringWrapper(null);

    private Connection currConn = null;

    // Public getters
    public ReadOnlyStringProperty selectedTableProperty() { return selectedTable.getReadOnlyProperty(); }
    public ReadOnlyStringProperty statusProperty() { return status.getReadOnlyProperty(); }

    /**
     * Request the password of a specific ID from the user
     * @param cluster Cluster to request password for
     * @return True if sufficient credentials for connection is available
     */
    private boolean requestPassword(Cluster cluster) {
        // Return immediately if cluster doesn't have a password
        if (cluster.getUsername() == null) return true;

        String id = cluster.getID();
        if (cachedPasswords.getOrDefault(id, null) == null) {
            final boolean exists = SecretsStore.exists(id);
            if (exists && !SecretsStore.isEncrypted(id, false)) try {
                cachedPasswords.put(id, SecretsStore.decrypt(id));
            } catch (SecretsStore.StoreException ignored) {
                assert false; // Cause immediate crash, should never happen
            }
            AsyncUtils.runLaterAndWait(() -> {
                while (cachedPasswords.getOrDefault(id, null) == null) {
                    String pw = new PasswordDialog(
                        I18N.getString("dialog.connPw.title"),
                        I18N.getString(exists ? "dialog.connPw.enc.header" : "dialog.connPw.pw.header"),
                        I18N.getString(exists ? "dialog.connPw.enc.body" : "dialog.connPw.pw.body"),
                        exists ? I18N.getString("dialog.connPw.enc.caption") : null
                    ).showAndWait().orElse(null);
                    if (pw == null) break;
                    if (exists) try {
                        cachedPasswords.put(id, SecretsStore.decrypt(pw, id));
                    } catch (SecretsStore.StoreException ex) {
                        new StyledAlert(
                            Alert.AlertType.ERROR,
                            I18N.getString("dialog.wrongConnPw.title"),
                            I18N.getString("dialog.wrongConnPw.header"),
                            I18N.getString("dialog.wrongConnPw.body")
                        ).showAndWait();
                    }
                    else cachedPasswords.put(id, pw);
                }
            });
        }

        return cachedPasswords.getOrDefault(id, null) != null;
    }

    /**
     * Request to load the tables of a particular database cluster.
     * Call again with the same cluster ID to refresh clusters
     * @param cluster Cluster to fetch tables of
     * @throws NoSuchElementException If the cluster ID doesn't exist
     */
    public ObservableList<String> requestTables(Cluster cluster) throws NoSuchElementException, SQLException {
        String id = cluster.getID(); // Shorter code since id is used in many places
        int idx = clusters.indexOf(cluster);
        if (idx < 0) throw new NoSuchElementException("Requested cluster doesn't exist in clusters array");

        // If we can't get the password, return and don't go further
        if (!requestPassword(cluster)) return null;

        setStatus(I18N.getString("status.fetchTables", cluster.getName()));
        try (var conn = DatabaseUtils.getConnection(
            cluster,
            cluster.getUsername() != null ? cachedPasswords.get(id) : null
        )) {
            ResultSet r = conn
                .getMetaData()
                .getTables(null, null, "%", new String[]{"TABLE"});
            if (!tables.containsKey(id)) tables.put(id, FXCollections.observableArrayList());
            else tables.get(id).clear();
            while (r.next()) tables.get(id).add(r.getString(3));
            r.close();
        }
        catch (URISyntaxException ignored) {}
        finally { resetStatus(); }

        return tables.get(id);
    }

    public boolean tablesCached(String forCluster) {
        return tables.containsKey(forCluster);
    }

    public void newTableSelection(Cluster cluster, String table) {
        selectedTable.set(table);
        tableColumns.clear();
        rows.clear();

        // Credentials for cluster should already be populated
        assert requestPassword(cluster);

        setStatus(I18N.getString("status.fetchRows", table));
        try (var conn = DatabaseUtils.getConnection(
            cluster,
            cluster.getUsername() != null ? cachedPasswords.get(cluster.getID()) : null
        )) {
            currConn = conn;
            final ResultSet r = conn.createStatement().executeQuery("select * from " + table);
            final ResultSetMetaData meta = r.getMetaData();

            // Firstly add column names as first row
            for (int i = 1; i <= meta.getColumnCount(); i++) tableColumns.add(meta.getColumnName(i));
            // Then add all
            while (r.next()) {
                final ObservableList<Container<?>> row = FXCollections.observableArrayList();
                //meta.getColumnTypeName();
                for (int c = 1; c <= meta.getColumnCount(); c++) {
                    // TIL var exists
                    final var cont = Container.getInstance(meta.getColumnTypeName(c));
                    if (cont == null) {
                        row.add(new PlaceholderContainer());
                        continue;
                    }
                    try {
                        row.add(
                            Objects.requireNonNull(cont.getConstructor(String.class))
                                .newInstance(r.getString(c))
                        );
                    } catch (Exception ignored) { row.add(new PlaceholderContainer()); }
                }

                rows.add(row);
            }
            r.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
            log.warning("Failed to fetch rows " + e.getMessage());
        } finally { resetStatus(); }
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
                Cluster c = Cluster.decode(clusterID);
                if (!c.getID().equals(clusterID))
                    throw new IllegalStateException("Deserialized cluster has different ID as node");
                addCluster(c);
            } catch (DecodingException | InvocationTargetException | IllegalStateException e) {
                log.warning(String.format(
                    "Could not decode cluster with ID %s due to exception: %s, removing node",
                    clusterID,
                    e.getMessage()
                ));
                e.printStackTrace();
                try {
                    Preferences problem = PreferencesEncoder.rootNode.node("clusters/" + clusterID);
                    problem.removeNode();
                    problem.flush(); // Make sure that corrupted node is really gone
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
        Cluster removed = clusters.remove(idx.intValue());
        tables.remove(removed.getID());
    }

    public boolean hasClusters() {
        return !clusters.isEmpty();
    }

    // Status methods
    public void resetStatus() { setStatus(I18N.getString("status.ready")); }
    public void setStatus(String newStatus) {
        Platform.runLater(() -> status.set(newStatus));
    }

    public static void addCachedPassword(String clusterID, String pw) {
        cachedPasswords.put(clusterID, pw);
    }
}
