package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.model.db.data.Container;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;

/**
 * A {@link javafx.scene.control.TableView TableView} cell factory for SQL tables.
 */
public class SQLCellFactory extends TableCell<ObservableList<Container<?>>, String> {
    private TextField textField;
    private boolean escapePressed = false;
    private final int col;

    /**
     * Create an instance of a <code>SQLCellFactory</code>, providing
     * the column the factory is in.
     * @param col Column this cell factory is in
     */
    public SQLCellFactory(int col) { this.col = col; }

    private Container<?> getContainer() { return getTableRow().getItem().get(col); }

    @Override
    public void startEdit() {
        if (!isEditable()
            || !getTableView().isEditable()
            || !getTableColumn().isEditable()
            || isEmpty()
            || !getContainer().isEditable()) return;
        super.startEdit();
        createTextField();
        setText(null);
        setGraphic(textField);
        textField.selectAll();
        textField.requestFocus();
        getStyleClass().add("editing");
        escapePressed = false;
    }

    @Override
    public void cancelEdit() {
        if (escapePressed) {
            super.cancelEdit();
            setText(getItem()); // restore the original text in the view
            getStyleClass().remove("editing");
        } else {
            // Interpret this as a commit instead
            commitEdit(textField.getText());
        }
        setGraphic(null);
    }

    @Override
    public void commitEdit(String newVal) {
        String commitText;
        if (textField.getText() == null || textField.getText().isEmpty()) commitText = null;
        else {
            commitText = getContainer().getFinalValue(textField.getText());
            // Invalid input or no change
            if (commitText == null || commitText.equals(getString())) {
                escapePressed = true;
                cancelEdit();
                return;
            }
        }
        super.commitEdit(commitText);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) textField.setText(getString());
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(null);
                getStyleClass().remove("editing");
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
        textField.setOnAction(e -> this.commitEdit(textField.getText()));
        textField.setOnKeyPressed(t -> {
            escapePressed = t.getCode() == KeyCode.ESCAPE;
            if (escapePressed) cancelEdit();
        });
        textField.setTextFormatter(new TextFormatter<>(f -> getContainer().isValid(f.getText()) ? f: null));
    }

    private String getString() {
        return getItem();
    }
}
