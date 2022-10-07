package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.model.db.data.Container;
import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

/**
 * A specialised dialog just for adding a column to a table.
 */
public final class AddColumnDialog extends Dialog<Pair<String, String>> {
    /**
     * Create an instance of an <code>AddColumnDialog</code>.
     * Result is a pair containing the name and type of column to add.
     */
    public AddColumnDialog() {
        getDialogPane().getStylesheets().add(UIUtils.getResourcePath("css/dialog.css"));
        getDialogPane().getScene().setFill(Color.TRANSPARENT); // Prevents flash of white when opening dialog

        setTitle(I18N.getString("dialog.addCol.title"));
        setHeaderText(I18N.getString("dialog.addCol.title"));
        final var container = new VBox(4);
        final var nameTf = new TextField();
        final var nameLabel = new Label(I18N.getString("dialog.addCol.name.label"));
        final var type = new ComboBox<String>();
        type.setItems(FXCollections.observableArrayList(Container.getTypes()));
        type.getSelectionModel().select(0);
        type.setStyle("-fx-font-family: 'IBM Plex Mono'");

        container.getChildren().addAll(nameLabel, nameTf, type);

        getDialogPane().setContent(container);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().lookupButton(ButtonType.OK)
            .disableProperty()
            .bind(Bindings.createBooleanBinding(() -> nameTf.getText().isBlank(), nameTf.textProperty()));

        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) return new Pair<>(nameTf.getText(), type.getValue());
            return null;
        });
    }
}
