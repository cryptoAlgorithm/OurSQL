package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;

/**
 * A dialog that displays a table as its primary component.
 */
public class TableDialog extends Dialog<String> {
    /**
     * Create an instance of the dialog with a table.
     * @param data 2D {@link ObservableList} of data to display, null if no table is to be displayed
     * @param title Title of dialog
     * @param header Header of dialog
     */
    public TableDialog(
        ObservableList<ObservableList<String>> data,
        String title,
        String header
    ) {
        if (data != null) {
            final var t = new TableView<ObservableList<String>>();
            getDialogPane().setExpanded(true);
            getDialogPane().setExpandableContent(t);
        }
        setTitle(title);
        setHeaderText(header);
        getDialogPane().getStylesheets().add(UIUtils.getResourcePath("css/dialog.css"));
        getDialogPane().getScene().setFill(Color.TRANSPARENT); // Prevents flash of white when opening dialog
    }
}
