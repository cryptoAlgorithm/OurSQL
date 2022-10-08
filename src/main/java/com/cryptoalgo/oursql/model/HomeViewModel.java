package com.cryptoalgo.oursql.model;

import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.preferencesCoder.PreferencesEncoder;
import com.cryptoalgo.oursql.component.StyledAlert;
import com.cryptoalgo.oursql.model.db.Cluster;
import com.cryptoalgo.oursql.model.db.DatabaseUtils;
import com.cryptoalgo.oursql.component.PasswordDialog;
import com.cryptoalgo.oursql.model.db.data.Container;
import com.cryptoalgo.oursql.model.db.data.PlaceholderContainer;
import com.cryptoalgo.oursql.model.db.data.TIDContainer;
import com.cryptoalgo.oursql.support.AsyncUtils;
import com.cryptoalgo.oursql.support.ui.Colors;
import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.SecretsStore;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
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
    private final HashMap<String, ObservableList<String>> tables = new HashMap<>();

    private static final ObservableMap<String, String> cachedPasswords =
        FXCollections.observableMap(new HashMap<>());

    /**
     * Columns of the current selected table
     */
    public final ObservableList<String>
        tableColumns = FXCollections.observableArrayList(),
        columnTypes = FXCollections.observableArrayList();
    /**
     * Rows of the current selected table
     */
    public final ObservableList<ObservableList<Container<?>>> rows = FXCollections.observableArrayList();

    /**
     * Current status to be displayed in the status area/dialogs
     */
    private final ReadOnlyStringWrapper
        displayedStatus = new ReadOnlyStringWrapper(I18N.getString("status.ready")),
    /**
     * The currently-selected table
     */
        selectedTable = new ReadOnlyStringWrapper(null),
    /**
     * The ID of the currently-selected cluster
     */
        selectedCluster = new ReadOnlyStringWrapper(null);

    /**
     * The background of the status area.
     */
    private final ReadOnlyObjectWrapper<Color> statusBg = new ReadOnlyObjectWrapper<>(Color.TRANSPARENT);

    private String status = null, statusID = null;
    private long statusStart = 0;

    private Connection currConn = null;

    // Public getters
    /**
     * Get an observable <code>ReadOnlyStringProperty</code> for the selected table.
     * @return Read only observable property for the selected table.
     *         Might be <code>null</code> if no table is selected.
     */
    public ReadOnlyStringProperty selectedTableProperty() { return selectedTable.getReadOnlyProperty(); }

    /**
     * Get an observable <code>ReadOnlyStringProperty</code> for the current status.
     * @return Read only observable property for the current status.
     */
    public ReadOnlyStringProperty displayedStatusProperty() { return displayedStatus.getReadOnlyProperty(); }

    /**
     * Get an observable <code>ReadOnlyStringProperty</code> for the current status background.
     * Implementations might choose to transition changes in this property.
     * @return Read only observable property for the requested status background.
     */
    public ReadOnlyObjectProperty<Color> statusBgProperty() { return statusBg.getReadOnlyProperty(); }

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
     * Call again with the same cluster ID to refresh clusters.
     * @param cluster Cluster to fetch tables of
     * @throws NoSuchElementException If the cluster ID doesn't exist
     * @throws SQLException If the underlying JDBC driver threw an exception during the
     *                      creation of a connection or during a query.
     * @return An ObservableList of tables for this cluster. Implementations can bind to this
     *         ObservableList to be notified of modifications to tables in this cluster (currently
     *         only occurs when this method is called again to refresh tables).
     */
    @Nullable
    public ObservableList<String> requestTables(Cluster cluster) throws NoSuchElementException, SQLException {
        String id = cluster.getID(); // Shorter code since id is used in many places
        int idx = clusters.indexOf(cluster);
        if (idx < 0) throw new NoSuchElementException("Requested cluster doesn't exist in clusters array");

        // If we can't get the password, return and don't go further
        if (!requestPassword(cluster)) return null;

        var st = setStatusJob(I18N.getString("status.fetchTables", cluster.getName()));
        try (var conn = DatabaseUtils.getConnection(
            cluster,
            cluster.getUsername() != null ? cachedPasswords.get(id) : null
        )) {
            ResultSet r = conn
                .getMetaData()
                .getTables(null, null, "%", new String[]{"TABLE"});
            if (!tables.containsKey(id)) // If a cache entry for this cluster ID doesn't exist, create it
                Platform.runLater(() -> tables.put(id, FXCollections.observableArrayList()));
            else Platform.runLater(() -> tables.get(id).clear());
            while (r.next()) {
                final var table = r.getString(3);
                Platform.runLater(() -> tables.get(id).add(table));
            }
            r.close();
            finishStatusJob(st);
        }
        catch (URISyntaxException ignored) {}
        catch (NoSuchElementException | SQLException e) {
            finishStatusJob(st, e.getLocalizedMessage());
            throw e;
        }

        return tables.get(id);
    }

    /**
     * Check if the tables for a particular cluster are present in the table cache.
     * @param forCluster Cluster ID to check for cached tables
     * @return true if tables of the cluster are already present in the cache
     */
    public boolean tablesCached(String forCluster) {
        return tables.containsKey(forCluster);
    }

    /**
     * Get a container to store the specified SQL value. Creates
     * the required instance of {@link Container} via reflection.
     */
    @NotNull
    private Container<?> getSQLContainer(String type, String data) {
        final var cont = Container.getInstance(type);
        if (cont == null) {
            log.warning("Unsupported SQL type: " + type);
            return new PlaceholderContainer();
        }
        try {
            return Objects.requireNonNull(cont.getConstructor(String.class)).newInstance(data);
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("Failed to init an instance of container for type " + type);
            return new PlaceholderContainer();
        }
    }

    /**
     * Select and load a new table
     * @param cluster Cluster to load table from
     * @param table Table name to load
     */
    public void newTableSelection(Cluster cluster, String table) {
        selectedTable.set(table);
        selectedCluster.set(cluster.getID());

        // Credentials for cluster should already be populated
        assert requestPassword(cluster);

        final var stID = setStatusJob(I18N.getString("status.getConn", cluster.getName()));
        try {
            // Prevent a build-up of stale connections
            if (currConn != null) currConn.close();
            currConn = DatabaseUtils.getConnection(
                cluster,
                cluster.getUsername() != null ? cachedPasswords.get(cluster.getID()) : null
            );
        } catch (SQLException | URISyntaxException e) {
            log.warning("Failed to fetch rows " + e.getMessage());
            finishStatusJob(stID, e.getLocalizedMessage());
            selectedTable.set(null);
        }
        finishStatusJob(stID);

        updateTable();
    }

    /**
     * Add rows from a ResultSet to a 2D observable list
     * @param r Reference to the observable list of rows to add to
     * @param rs Result set to read from
     * @throws SQLException If retrieving values from the ResultSet failed
     */
    private void addRowsFromResult(
        ObservableList<ObservableList<Container<?>>> r,
        ResultSet rs
    ) throws SQLException {
        final var meta = rs.getMetaData();
        while (rs.next()) {
            final ObservableList<Container<?>> row = FXCollections.observableArrayList();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
                row.add(getSQLContainer(meta.getColumnTypeName(i), rs.getString(i)));
            r.add(row);
        }
    }

    /**
     * Fetch the columns and rows of the currently selected table
     */
    public void updateTable() {
        // Pre update sanity checks
        assert currConn != null; // If this is null something is very wrong
        if (selectedTable.get() == null || selectedCluster.get() == null) return;
        final var table = selectedTable.get();

        final var stID = setStatusJob(I18N.getString("status.fetchRows", table));
        try {
            final var r = currConn.createStatement().executeQuery(
                String.format("select *, CTID from \"%s\"", selectedTable.get())
            );
            final ResultSetMetaData meta = r.getMetaData();

            // Clear all table-related values
            tableColumns.clear();
            columnTypes.clear();
            rows.clear();

            // Add column names and types to their respective arrays
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                tableColumns.add(meta.getColumnName(i));
                columnTypes.add(meta.getColumnTypeName(i));
            }
            // Then add all the rows
            addRowsFromResult(rows, r);
            r.close();
        } catch (SQLException e) {
            finishStatusJob(stID, e.getLocalizedMessage());
            return;
        }
        finishStatusJob(stID);
    }

    /**
     * Run a SQL query and return the resultSet
     * @param query SQL query to run
     * @return A pair containing a pair of the column names and types and a list of rows,
     *         and the number of results updated if the operation was an update operation
     *         or the length of the result set if the statement returned one
     * @throws SQLException If the statement failed to execute
     */
    public Pair<Pair<List<String>, ObservableList<ObservableList<Container<?>>>>, Integer> runQuery(
        String query
    ) throws SQLException {
        assert currConn != null;
        final var tID = setStatusJob(I18N.getString("status.runQuery"));
        try (final var statement = currConn.createStatement()) {
            if (statement.execute(query)) {
                final var r = statement.getResultSet();
                final var meta = r.getMetaData();
                final var cols = new ArrayList<String>();
                final ObservableList<ObservableList<Container<?>>> resultRows
                    = FXCollections.observableArrayList();
                // Add column names to an array
                for (int i = 1; i <= meta.getColumnCount(); i++) cols.add(meta.getColumnName(i));
                // Then add the rows
                addRowsFromResult(resultRows, r);
                r.close();
                finishStatusJob(tID);
                return new Pair<>(new Pair<>(cols, resultRows), resultRows.size());
            } else {
                updateTable(); // The statement might have updated something
                return new Pair<>(null, statement.getUpdateCount());
            }
        } catch (SQLException e) {
            finishStatusJob(tID, e.getLocalizedMessage());
            throw e;
        }
    }

    private String buildWhere(ObservableList<Container<?>> r, boolean includeCTID) {
        final var sb = new StringBuilder();
        for (var i = 0; i < r.size(); i++) {
            final var c = r.get(i);
            if (c instanceof PlaceholderContainer) continue;
            if (!includeCTID && tableColumns.get(i).equals("ctid")) continue;
            sb
                .append("\"")
                .append(tableColumns.get(i))
                .append("\"")
                .append(c.toString() == null ? " is " : " = ")
                .append(c.toSQLString());
            if (i != r.size() - (includeCTID ? 1 : 2)) sb.append(" AND ");
        }
        return sb.toString();
    }

    /**
     * Attempt to edit a field in a table
     * @param col Column name of field to edit
     * @param ctid TID of row to edit
     * @param nv New value to update field with (could be null)
     * @throws SQLException If editing failed (JDBC driver threw an exception
     *                      while executing update statement)
     */
    public void attemptEdit(
        @NotNull String col,
        @NotNull String ctid,
        @Nullable String nv
    ) throws SQLException {
        assert currConn != null;

        // Find the row that was updated by CTID
        final var colIdx = tableColumns.indexOf(col);
        // Get a new container for the new value and update the db with the
        // string representation of the new value. We do this to ensure the value
        // in the table after updating is actually what was added to the database.
        final var newContainer = getSQLContainer(columnTypes.get(colIdx), nv);
        nv = newContainer.toSQLString();

        // Find the row by ctid
        ObservableList<Container<?>> updateRow = null;
        for (final var row : rows) if (row.get(row.size()-1).toString().equals(ctid)) {
            // That's the row we want!
            updateRow = row;
            break;
        }
        if (updateRow == null) throw new SQLException("Row not found");

        log.info("Attempting edit: col=" + col + ", nv=" + nv + ", ctid=" + ctid);
        final var tID = setStatusJob(I18N.getString("status.commitUpdate"));
        try {
            final var statement = currConn.createStatement();
            statement.execute(
                String.format("""
                UPDATE "%s"
                SET "%s" = %s
                WHERE %s;
                """,
                    selectedTable.get(),
                    col,
                    nv,
                    buildWhere(updateRow, true)
                )
            );
            if (statement.getUpdateCount() != 1) throw new SQLException("Update failed, no rows updated");

            // Then (attempt to) update the corresponding row that was modified
            updateRow.set(colIdx, newContainer);
            // Ensure the CTID is up-to-date (it has a habit of changing across updates)
            final var rs = currConn.createStatement().executeQuery(
                String.format(
                    "SELECT CTID FROM \"%s\" WHERE %s",
                    selectedTable.get(),
                    buildWhere(updateRow, false)
                )
            );
            rs.next();
            updateRow.set(updateRow.size()-1, new TIDContainer(rs.getString(1)));
            rs.close();
        } catch (SQLException e) {
            finishStatusJob(tID, e.getLocalizedMessage());
            throw e;
        }
        finishStatusJob(tID);
    }

    /**
     * Inserts a new row into the current table with null values for each column
     */
    public void insertRow() {
        assert currConn != null;
        final var tID = setStatusJob(I18N.getString("status.insertRow", selectedTable.get()));
        try {
            currConn.createStatement().execute(
                String.format("insert into \"%s\" default values", selectedTable.get())
            );
        } catch (SQLException e) {
            finishStatusJob(tID, e.getLocalizedMessage());
            return;
        }
        finishStatusJob(tID);
        updateTable();
    }

    /**
     * Remove a row in the current table given a ctid
     * @param ctid CTID of row to remove
     */
    public void deleteRow(String ctid) {
        assert currConn != null;
        final var tID = setStatusJob(I18N.getString("status.deleteRow", selectedTable.get()));
        try {
            currConn.createStatement().execute(
                String.format("delete from \"%s\" where ctid = '%s'", selectedTable.get(), ctid)
            );
        } catch (SQLException e) {
            finishStatusJob(tID, e.getLocalizedMessage());
            return;
        }
        finishStatusJob(tID);
        // Remove the removed row from the table without refreshing the whole table
        rows.removeIf(r -> r.get(r.size() - 1).toString().equals(ctid));
    }

    /**
     * Inserts a column into the currently selected table.
     */
    public void insertColumn(@NotNull String name, @NotNull String type) {
        assert currConn != null;
        final var tID = setStatusJob(I18N.getString("status.insertColumn", name, selectedTable.get()));
        try {
            currConn.createStatement().execute(
                String.format("""
                    ALTER TABLE "%s"
                    ADD COLUMN "%s" %s;
                    """,
                    selectedTable.get(),
                    name,
                    type
                )
            );
        } catch (SQLException e) {
            finishStatusJob(tID, e.getLocalizedMessage());
            return;
        }
        finishStatusJob(tID);
        updateTable(); // There's probably a better way to do this
    }

    /**
     * Create a table in a cluster
     * @param cluster Cluster to create table in
     * @param table Name of new table to create
     */
    public void createTable(@NotNull Cluster cluster, @NotNull String table) {
        if (!requestPassword(cluster)) return;
        final var tID = setStatusJob(I18N.getString("status.createTable", table));
        try (final var conn = DatabaseUtils.getConnection(
            cluster,
            cluster.getUsername() != null ? cachedPasswords.get(cluster.getID()) : null
        )) {
            conn.createStatement().execute("create table \"" + table + "\" ()");
        } catch (URISyntaxException | SQLException ex) {
            finishStatusJob(tID, ex.getLocalizedMessage());
            return;
        }
        finishStatusJob(tID);
        try {
            requestTables(cluster);
        } catch (SQLException ignored) {}
    }

    /**
     * Delete a table from a cluster
     */
    public void dropTable() {
        assert currConn != null;
        final var tID = setStatusJob(I18N.getString("status.dropTable", selectedTable.get()));
        try {
            currConn.createStatement().execute(String.format("drop table \"%s\"", selectedTable.get()));
        } catch (SQLException ex) {
            ex.printStackTrace();
            finishStatusJob(tID, ex.getLocalizedMessage());
        }
        finishStatusJob(tID);
        tables.get(selectedCluster.get()).remove(selectedTable.get());
        selectedTable.set(null);
    }

    /**
     * Load persisted clusters. Listeners registered through
     * {@link #setOnNewCluster(BiConsumer callback)} will be notified if any new
     * clusters were loaded.
     */
    public void loadClusters() {
        if (!clusters.isEmpty()) return; // Clusters were already loaded
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

    /**
     * Register a new listener to be called when cluster(s) are added
     * @param cb Consumer callback to be call when a new cluster is added
     */
    public void setOnNewCluster(BiConsumer<Cluster, Integer> cb) {
        clusters.addListener((ListChangeListener<Cluster>) ch -> {
            while (ch.next()) if (ch.wasAdded())
                for (int i = ch.getFrom(); i < ch.getTo(); i++) cb.accept(ch.getList().get(i), i);
        });
    }

    /**
     * Register a new listener to be called when cluster(s) are removed
     * @param cb Consumer callback to be called when a cluster is removed
     */
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

    /**
     * Add a specified cluster, will fire listeners if any are registered
     * @param c Cluster to add
     */
    public void addCluster(@NotNull Cluster c) {
        clusters.add(c);
    }

    /**
     * Remove the cluster with the specified ID
     * @param clusterID ID of cluster to remove
     * @throws NoSuchElementException If no cluster with specified ID exists
     * @see #removeCluster(Integer idx)
     */
    public void removeCluster(@NotNull String clusterID) throws NoSuchElementException {
        int idx = indexOf(clusterID);
        if (idx < 0) throw new NoSuchElementException("No cluster with requested ID exists");
        removeCluster(idx);
    }
    /**
     * Remove a cluster at the specified index
     * @param idx Index to remove cluster at
     */
    public void removeCluster(@NotNull Integer idx) {
        Cluster removed = clusters.remove(idx.intValue());
        tables.remove(removed.getID());
    }

    // Status methods

    /**
     * Create a job to display its status
     * @param newStatus Name or description of new job
     * @return ID of job, can be passed to {@link #finishStatusJob(String id)} or
     *         {@link #finishStatusJob(String id, String err)} upon job completion
     */
    public String setStatusJob(String newStatus) {
        status = newStatus;
        Platform.runLater(() -> displayedStatus.set(I18N.getString("status.progress", status)));
        statusID = UUID.randomUUID().toString();
        statusStart = System.currentTimeMillis();
        statusBg.set(Colors.LOADING);
        return statusID;
    }

    /**
     * Convenience method to mark a job started by calling {@link #setStatusJob(String status)}
     * as successfully completed
     * @param id ID of job, returned by {@link #setStatusJob(String status)}
     * @see #finishStatusJob(String id, String err)
     */
    public void finishStatusJob(String id) {
        finishStatusJob(id, null);
    }

    /**
     * Mark a job started by calling {@link #setStatusJob(String status)}
     * as completed.
     * @param id ID of job, returned by {@link #setStatusJob(String status)}
     * @param err If the job failed, the exception string to display (null if no exception)
     * @see #finishStatusJob(String id)
     */
    public void finishStatusJob(String id, String err) {
        final var success = err == null;
        if (!Objects.equals(statusID, id)) return;
        final var curStatus = status; // Mitigate race condition issues
        Platform.runLater(() -> displayedStatus.set(I18N.getString(
            success ? "status.done" : "status.error",
            curStatus,
            System.currentTimeMillis() - statusStart,
            success ? null : err.replaceFirst("\n.*", "")
        )));
        status = null;

        // Set the status background color and clear it after 3s if there are no further statuses
        statusBg.set(success ? Colors.SUCCESS : Colors.ERROR);
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            if (!Objects.equals(statusID, id)) return;
            statusBg.set(Color.TRANSPARENT);
            statusID = null;
        }).start();
    }

    /**
     * Add a password to the cache manually, for example right after a cluster is created.
     * <p>
     *     <b>Note:</b>
     *     No validation is done to ensure the password is valid etc.
     * </p>
     * @param clusterID ID of cluster this password is for
     * @param pw Password to add to cache
     */
    public static void addCachedPassword(String clusterID, String pw) {
        cachedPasswords.put(clusterID, pw);
    }
}
