package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.model.db.data.Container;
import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

/**
 * A dialog that displays a table as its primary component.
 * Primarily used for displaying the results of a query/statement. The
 * code in this dialog is of lower quality than the rest of the project
 * due to extreme time constraints.
 */
public class TableDialog extends Dialog<String> {
    /**
     * Create an instance of the dialog with a table.
     * @param data 2D {@link ObservableList} of data to display, null if no table is to be displayed
     * @param title Title of dialog
     * @param header Header of dialog
     */
    public TableDialog(
        List<String> col,
        ObservableList<ObservableList<Container<?>>> data,
        String title,
        String header,
        String body
    ) {
        if (data != null && col != null) {
            final var t = new TableView<ObservableList<Container<?>>>();
            getDialogPane().setExpanded(true);
            getDialogPane().setExpandableContent(t);
            t.setItems(data);
            for (var i = 0; i < col.size(); i++) {
                final var curIdx = i;
                final var cl = new TableColumn<ObservableList<Container<?>>, String>(col.get(i));
                cl.setCellValueFactory(r -> new ReadOnlyObjectWrapper<>(r.getValue().get(curIdx).toString()));
                cl.setCellFactory(param -> new SQLCellFactory(curIdx));
                t.getColumns().add(cl);
            }
            setResizable(true);
            final Stage s = (Stage) getDialogPane().getScene().getWindow();
            s.setMinWidth(400); s.setMinHeight(300);
            getDialogPane().setMinWidth(400);
            getDialogPane().setPrefWidth(600); getDialogPane().setPrefHeight(400);
        }
        setTitle(title);
        setHeaderText(header);
        setContentText(body);
        getDialogPane().getStylesheets().add(UIUtils.getResourcePath("css/dialog.css"));
        getDialogPane().getScene().setFill(Color.TRANSPARENT); // Prevents flash of white when opening dialog
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }
}
